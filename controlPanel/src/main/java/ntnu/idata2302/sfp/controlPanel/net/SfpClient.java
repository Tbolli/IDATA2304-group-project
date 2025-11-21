package ntnu.idata2302.sfp.controlPanel.net;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import ntnu.idata2302.sfp.controlPanel.factory.PacketFactory;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.header.Header;

/**
 * Client responsible for managing a TLS-secured connection to the Smart Farming
 * server from the control panel.
 *
 * <p>This class encapsulates connection setup, background listening for incoming
 * packets, and sending protocol messages such as ANNOUNCE, CAPABILITIES_QUERY,
 * SUBSCRIBE, UNSUBSCRIBE, and COMMAND on behalf of the control panel.</p>
 */

public class SfpClient {

  private final String host;
  private final int port;

  private SSLContext sslContext;

  private SSLSocket socket;
  private DataInputStream in;
  private OutputStream out;
  private volatile boolean running = false;

  private final ExecutorService listenerThread =
        Executors.newSingleThreadExecutor(r -> {
          Thread t = new Thread(r, "SFP-Listener");
          t.setDaemon(true);
          return t;
        });

  private final ExecutorService writerThread =
        Executors.newSingleThreadExecutor(r -> {
          Thread t = new Thread(r, "SFP-Writer");
          t.setDaemon(true);
          return t;
        });

  /**
   * Constructs a new SFP client for the given host and port.
   *
   * @param host the server hostname or IP address
   * @param port the TCP port the server listens on
   */

  public SfpClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /**
   * Establishes a TLS-secured TCP connection to the server and starts
   * the background listener loop.
   *
   * <p>On successful connection this method initializes input/output streams,
   * marks the client as running, submits the read loop to the listener executor,
   * and sends an initial ANNOUNCE packet.</p>
   *
   * @throws IOException if TLS initialization, socket creation, handshake,
   *                     or stream setup fails
   */

  public void connect() throws IOException {
    try {
      initializeTLS();

      SSLSocketFactory factory = sslContext.getSocketFactory();
      socket = (SSLSocket) factory.createSocket(host, port);
      socket.startHandshake();

      in = new DataInputStream(socket.getInputStream());
      out = socket.getOutputStream();
      running = true;

      listenerThread.submit(this::readLoop);
      sendAnnounce();

    } catch (Exception e) {
      throw new IOException("TLS connection failed: " + e.getMessage(), e);
    }
  }

  /**
   * Blocking packet read loop that continuously receives SFP packets from
   * the server while the client is running.
   *
   * <p>This method runs only on the listener executor thread. It reads the
   * fixed-size header first, validates it, then reads the body and constructs
   * a {@link SmartFarmingProtocol} instance which is forwarded to the UI via
   * the {@link EventBus}.</p>
   */

  private void readLoop() {
    try {
      while (running) {
        // Read header (33 bytes)
        byte[] headerBytes = in.readNBytes(Header.HEADER_SIZE);
        if (headerBytes.length < Header.HEADER_SIZE) {
          System.out.println("Client: Incomplete header, closing.");
          break;
        }

        Header header = Header.fromBytes(headerBytes);

        if (!Header.validateHeader(header)) {
          System.out.println("Client: Invalid SFP header.");
          continue; // Skip packet, keep socket open
        }

        int bodyLength = header.getPayloadLength();
        byte[] bodyBytes = in.readNBytes(bodyLength);

        SmartFarmingProtocol packet =
              SmartFarmingProtocol.fromBytes(header, bodyBytes);

        // Notify UI and other listeners
        EventBus.post(packet);
      }
    } catch (IOException e) {
      System.out.println("Client socket closed or failed: " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      close();
    }
  }

  /**
   * Sends an ANNOUNCE packet to the server to register this control panel.
   */

  public void sendAnnounce() {
    SmartFarmingProtocol packet = PacketFactory.announce(
          AppContext.getRequestId()
    );
    sendPacket(packet);
  }

  /**
   * Sends a CAPABILITIES_QUERY packet to request capabilities from sensor nodes.
   */

  public void sendCapabilitiesQuery() {
    SmartFarmingProtocol packet = PacketFactory.capabilitiesQuery(
          AppContext.getControllerId(),
          AppContext.getRequestId()
    );
    sendPacket(packet);
  }

  /**
   * Sends a SUBSCRIBE packet to subscribe to updates from a single sensor node.
   *
   * @param nodeId the identifier of the sensor node to subscribe to
   */

  public void sendSubscribe(int nodeId) {
    SmartFarmingProtocol packet = PacketFactory.subscribeNode(
          AppContext.getControllerId(),
          AppContext.getRequestId(),
          nodeId
              );
    sendPacket(packet);
  }

  /**
   * Sends an UNSUBSCRIBE packet to stop receiving updates from a single node.
   *
   * @param sensorNodeId the identifier of the sensor node to unsubscribe from
   */

  public void sendUnsubscribe(int sensorNodeId) {
    SmartFarmingProtocol packet = PacketFactory.unSubscribeNode(
          AppContext.getControllerId(),
          AppContext.getRequestId(),
          sensorNodeId
    );
    sendPacket(packet);
  }

  /**
   * Sends a COMMAND packet instructing a sensor node to perform one or more
   * actuator operations.
   *
   * @param nodeId the target sensor node identifier
   * @param parts  list of command parts describing the actuator operations
   */

  public void sendActuatorCommand(int nodeId, List<CommandBody.CommandPart> parts) {
    SmartFarmingProtocol packet = PacketFactory.command(
          AppContext.getControllerId(),
          nodeId,
          AppContext.getRequestId(),
          parts
    );
    sendPacket(packet);
  }

  /**
   * Thread-safe outgoing message dispatcher that encodes and writes the given
   * packet to the TCP socket using the writer executor.
   *
   * @param packet the {@link SmartFarmingProtocol} packet to send
   */

  private void sendPacket(SmartFarmingProtocol packet) {
    writerThread.submit(() -> {
      try {
        byte[] bytes = packet.toBytes(); // assumed available
        out.write(bytes);
        out.flush();
      } catch (Exception e) {
        System.err.println("Failed to send packet: " + e.getMessage());
      }
    });
  }


  /**
   * Initializes the {@link SSLContext} used for establishing TLS connections.
   *
   * <p>The method loads a JKS truststore named {@code server.truststore} from
   * the classpath, initializes a {@link TrustManagerFactory}, and configures
   * an {@link SSLContext} that trusts the server certificate in the store.</p>
   *
   * @throws Exception if the truststore cannot be found, loaded, or if the
   *                   TLS context initialization fails
   */

  private void initializeTLS() throws Exception {
    KeyStore trustStore = KeyStore.getInstance("JKS");


    InputStream ts = SfpClient.class.getClassLoader()
          .getResourceAsStream("server.truststore");

    if (ts == null) {
      throw new FileNotFoundException("server.truststore not found in classpath");
    }

    trustStore.load(ts, "password".toCharArray());

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(trustStore);

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, tmf.getTrustManagers(), null);
  }

  /**
   * Closes the client connection, stops the read loop, and shuts down
   * the executor services.
   *
   * <p>This method is safe to call multiple times.</p>
   */

  public void close() {
    running = false;
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (IOException ignored) {
      // Ignored on close

    }
    listenerThread.shutdownNow();
    writerThread.shutdownNow();
  }

  /**
   * Returns the remote host this client is configured to connect to.
   *
   * @return the server hostname or IP address
   */

  public String getHost() {
    return host;
  }

  /**
   * Returns the TCP port this client is configured to connect to.
   *
   * @return the server TCP port
   */
  public int getPort() {
    return port;
  }
}

