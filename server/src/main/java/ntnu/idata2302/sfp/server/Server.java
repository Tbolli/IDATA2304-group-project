package ntnu.idata2302.sfp.server;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.net.MessageDispatcher;
import ntnu.idata2302.sfp.server.net.ServerContext;
import ntnu.idata2302.sfp.server.net.handlers.AnnounceHandler;
import ntnu.idata2302.sfp.server.net.handlers.CapabilitiesHandler;
import ntnu.idata2302.sfp.server.net.handlers.DataReportHandler;
import ntnu.idata2302.sfp.server.net.handlers.ForwardPacketHandler;
import ntnu.idata2302.sfp.server.net.handlers.SubscribeHandler;
import ntnu.idata2302.sfp.server.net.handlers.UnSubscribeHandler;



/**
 * TLS-enabled example server for the Smart Farming Protocol (SFP).
 *
 * <p>This class initializes a TLS context using a keystore stored in the
 * resources, creates an SSL server socket, accepts client connections and
 * spawns a dedicated thread to handle each client. Incoming SFP packets are
 * read from the client stream, parsed into {@link SmartFarmingProtocol}
 * objects and dispatched to registered handlers using {@link MessageDispatcher}.</p>
 *
 * <p>The server is intended for example/demo use: it logs status and errors
 * to standard output and performs minimal validation of client input.</p>
 */
public class Server {

  private static final int PORT = 5050;
  private static SSLContext sslContext;
  private static final MessageDispatcher dispatcher = new MessageDispatcher();
  private static final ServerContext context = new ServerContext();

  static {
    // Register handlers for known message types at class load time.
    dispatcher.registerHandler(MessageTypes.DATA_REPORT, new DataReportHandler());
    dispatcher.registerHandler(MessageTypes.ANNOUNCE, new AnnounceHandler());
    dispatcher.registerHandler(MessageTypes.CAPABILITIES_QUERY, new CapabilitiesHandler());
    dispatcher.registerHandler(MessageTypes.SUBSCRIBE, new SubscribeHandler());
    dispatcher.registerHandler(MessageTypes.UNSUBSCRIBE, new UnSubscribeHandler());
    dispatcher.registerHandler(MessageTypes.COMMAND, new ForwardPacketHandler());
    dispatcher.registerHandler(MessageTypes.COMMAND_ACK, new ForwardPacketHandler());
    dispatcher.registerHandler(MessageTypes.ERROR, new ForwardPacketHandler());
  }

  /**
   * Application entry point.
   *
   * <p>This method initializes the TLS context, creates an {@link SSLServerSocket}
   * bound to the configured {@link #PORT}, and enters an acceptance loop. For each
   * accepted client socket a new thread is started which invokes {@link #handleClient(Socket)}.</p>
   *
   * @param args command line arguments (ignored)
   */
  public static void main(String[] args) {

    try {
      initializeTLS();

      SSLServerSocketFactory factory = sslContext.getServerSocketFactory();
      SSLServerSocket serverSocket = (SSLServerSocket) factory.createServerSocket(PORT);

      System.out.println("TLS Server running on port " + PORT);

      while (true) {

        SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
        clientSocket.startHandshake();
        new Thread(() -> handleClient(clientSocket)).start();
      }

    } catch (IOException e) {
      e.printStackTrace();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Handle a connected client socket.
   *
   * <p>This method runs on a dedicated thread for a single client. It reads
   * a full SFP header (fixed size {@link Header#HEADER_SIZE}), validates the
   * protocol prefix, reads the body according to the header payload length,
   * constructs a {@link SmartFarmingProtocol} message and dispatches it using
   * {@link MessageDispatcher} together with the provided {@link ServerContext}.</p>
   *
   * <p>The method logs and handles {@link EOFException} (a client closed the
   * connection) and other I/O or parsing errors; it ensures the socket is
   * closed on exit.</p>
   *
   * @param socket the client socket to read from; must not be {@code null}
   */
  private static void handleClient(Socket socket) {
    System.out.println(
        "New connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

    try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {
      while (true) {
        // Read full header
        byte[] headerBytes = dis.readNBytes(Header.HEADER_SIZE);

        if (headerBytes.length < Header.HEADER_SIZE) {
          System.out.println("Incomplete header.");
          break;
        }

        // Decode header
        Header header = Header.fromBytes(headerBytes);

        // Validate protocol prefix (0x53 0x46 0x50)
        if (!Header.validateHeader(header)) {
          System.out.println("Invalid protocol prefix from client.");
          continue;
        }

        // Read body based on header payload length
        int bodyLength = header.getPayloadLength();
        byte[] bodyBytes = dis.readNBytes(bodyLength);

        // Parse full SFP message
        SmartFarmingProtocol packet = SmartFarmingProtocol.fromBytes(header, bodyBytes);

        // dispatch the packet
        dispatcher.dispatch(packet, socket, context);
      }

    } catch (EOFException e) {
      System.out.println(
          "Client disconnected normally: " + socket.getInetAddress().getHostAddress());
    } catch (Exception e) {
      System.out.println("Unexpected error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException ignored) {
        // Socket already closed or closing; safe to ignore.

      }
      System.out.println("Connection closed: "
            + socket.getInetAddress().getHostAddress() + ", removing node.");
      context.unregisterNode(socket);
    }
  }

  /**
   * Initialize TLS context used by the server.
   *
   * <p>The method loads a JKS keystore named `server.keystore` from the classpath
   * resources, initializes {@link KeyManagerFactory} and {@link TrustManagerFactory}
   * and sets up the {@link SSLContext} instance used to create server sockets.</p>
   *
   * @throws Exception if the keystore cannot be found, read, or if the TLS
   *                   context initialization fails for any reason
   */
  private static void initializeTLS() throws Exception {

    KeyStore keyStore = KeyStore.getInstance("JKS");

    // Since this won't be hosted, we can just keep our keystore in resources
    InputStream is = Server.class.getClassLoader()
        .getResourceAsStream("server.keystore");

    if (is == null) {
      throw new FileNotFoundException("server.keystore not found in resources");
    }

    keyStore.load(is, "password".toCharArray());

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(keyStore, "password".toCharArray());

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(keyStore);

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
  }
}