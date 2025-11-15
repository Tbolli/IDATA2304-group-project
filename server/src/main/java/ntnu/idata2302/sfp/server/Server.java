package ntnu.idata2302.sfp.server;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.net.MessageDispatcher;
import ntnu.idata2302.sfp.server.net.ServerContext;
import ntnu.idata2302.sfp.server.net.handlers.*;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


public class Server {

  private static final int PORT = 5050;
  private static SSLContext sslContext;
  private static final MessageDispatcher dispatcher = new MessageDispatcher();
  private static final ServerContext context = new ServerContext();

  static {
    dispatcher.registerHandler(MessageTypes.DATA_REPORT, new DataReportHandler());
    dispatcher.registerHandler(MessageTypes.ANNOUNCE, new AnnounceHandler());
    dispatcher.registerHandler(MessageTypes.CAPABILITIES_QUERY, new CapabilitiesHandler());
    dispatcher.registerHandler(MessageTypes.SUBSCRIBE, new SubscribeHandler());
    dispatcher.registerHandler(MessageTypes.UNSUBSCRIBE, new UnSubscribeHandler());
    dispatcher.registerHandler(MessageTypes.COMMAND, new ForwardPacketHandler());
    dispatcher.registerHandler(MessageTypes.COMMAND_ACK, new ForwardPacketHandler());
    dispatcher.registerHandler(MessageTypes.ERROR, new ForwardPacketHandler());
  }

  public static void main(String[] args) {

    try{
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

  private static void handleClient(Socket socket) {
    System.out.println("New connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

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
      System.out.println("Client disconnected normally: " + socket.getInetAddress().getHostAddress());
    } catch (IOException e) {
      System.out.println("I/O error: " + e.getMessage());
    } catch (Exception e) {
      System.out.println("Unexpected error: " + e.getMessage());
      e.printStackTrace();
    } finally {
      try {
        socket.close();
      } catch (IOException ignored) {}
      System.out.println("Connection closed: " + socket.getInetAddress().getHostAddress());
    }
  }
  private static void initializeTLS() throws Exception {

    KeyStore keyStore = KeyStore.getInstance("JKS");

    // Since this won't be hosted we can just keep our keystore in resources
    InputStream is = Server.class.getClassLoader()
      .getResourceAsStream("server.keystore");

    if (is == null)
      throw new FileNotFoundException("server.keystore not found in resources");

    keyStore.load(is, "password".toCharArray());

    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
    kmf.init(keyStore, "password".toCharArray());

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(keyStore);

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
  }
}
