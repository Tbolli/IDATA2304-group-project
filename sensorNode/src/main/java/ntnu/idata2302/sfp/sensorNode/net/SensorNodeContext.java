package ntnu.idata2302.sfp.sensorNode.net;

import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;


/**
 * Manages a TLS/TCP connection between a sensor node and the server and
 * provides utilities for sending and receiving Smart Farming Protocol (SFP)
 * packets.
 *
 * <p>This context holds the connection socket and input/output streams and
 * delegates packet serialization/deserialization to {@link SmartFarmingProtocol}
 * and {@link Header} helpers. It is intended to be used by network threads
 * (for example {@link NetworkLoop}) and higher-level components that need to
 * send or receive SFP messages.</p>
 */
public class SensorNodeContext {
  private SSLContext sslContext;

  private final String host;
  private final int port;
  private final SensorNode sensorNode;

  private SSLSocket socket;
  private DataInputStream in;
  private OutputStream out;

  /**
   * Create a new SensorNodeContext for the given host/port and sensor node.
   *
   * @param host the server host name or IP address
   * @param port the server TCP port
   * @param node the local {@link SensorNode} associated with this context
   */
  public SensorNodeContext(String host, int port, SensorNode node) {
    this.host = host;
    this.port = port;
    this.sensorNode = node;
  }

  /**
   * Open a blocking TLS socket to the configured server and initialize I/O
   * streams used for sending and receiving SFP packets.
   *
   * <p>This method initializes TLS using the bundled truststore, creates an
   * SSL socket, performs the TLS handshake, and prepares {@link DataInputStream}
   * and {@link OutputStream} for further packet operations.</p>
   *
   * @throws Exception if TLS initialization, socket creation, or handshake fails
   */
  public void connect() throws Exception {
    initializeTLS();
    SSLSocketFactory factory = sslContext.getSocketFactory();
    socket = (SSLSocket) factory.createSocket(host, port);
    socket.startHandshake();

    in = new DataInputStream(socket.getInputStream());
    out = socket.getOutputStream();

    System.out.println("Connected to server.");
  }

  /**
   * Initialize the {@link SSLContext} used to establish TLS connections.
   *
   * <p>The method loads the trust store resource `server.truststore` from the
   * classpath and configures a {@link TrustManagerFactory} so the socket trusts
   * the server certificate contained in the store.</p>
   *
   * @throws Exception if the truststore cannot be found, loaded, or the TLS
   *                   context cannot be initialized
   */
  private void initializeTLS() throws Exception {
    KeyStore trustStore = KeyStore.getInstance("JKS");

    InputStream ts = SensorNodeContext.class.getClassLoader()
        .getResourceAsStream("server.truststore");

    if (ts == null) {
      throw new FileNotFoundException("server.truststore not found in resources");
    }

    trustStore.load(ts, "password".toCharArray());

    TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
    tmf.init(trustStore);

    sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, tmf.getTrustManagers(), null);
  }


  /**
   * Returns whether the underlying socket is currently connected and open.
   *
   * @return {@code true} if the socket is non-null, connected and not closed;
   *          {@code false} otherwise
   */
  public boolean isConnected() {
    return socket != null && socket.isConnected() && !socket.isClosed();
  }

  /**
   * Send a fully formed {@link SmartFarmingProtocol} packet over the socket.
   *
   * <p>The method is synchronized to prevent concurrent writes to the output
   * stream. Errors during writing are logged to standard error but not rethrown.</p>
   *
   * @param packet the SFP packet to send; callers are responsible for creating a valid packet
   */
  public synchronized void sendPacket(SmartFarmingProtocol packet) {
    try {
      out.write(packet.toBytes());
      out.flush();
    } catch (IOException e) {
      System.err.println("Failed to send packet: " + e.getMessage());
    }
  }

  /**
   * Blocking read for a single SFP packet.
   *
   * <p>The method reads the fixed-size header first using {@link Header#HEADER_SIZE},
   * then reads the body based on the header's payload length and constructs the
   * {@link SmartFarmingProtocol} instance.</p>
   *
   * @return the deserialized {@link SmartFarmingProtocol} packet
   * @throws IOException if the socket is closed, the header is incomplete, or an I/O error occurs
   */
  public SmartFarmingProtocol readOnePacket() throws IOException {
    if (!isConnected()) {
      throw new IOException("Socket is closed");
    }

    // Read header first
    byte[] headerBytes = in.readNBytes(Header.HEADER_SIZE);
    if (headerBytes.length < Header.HEADER_SIZE) {
      throw new IOException("Incomplete header");
    }

    Header header = Header.fromBytes(headerBytes);

    // Read body
    int len = header.getPayloadLength();
    byte[] bodyBytes = in.readNBytes(len);

    return SmartFarmingProtocol.fromBytes(header, bodyBytes);
  }

  /**
   * Set the sensor node id on the associated {@link SensorNode}.
   *
   * @param id the id assigned by the server
   */
  public void setId(int id) {
    sensorNode.setId(id);
  }

  /**
   * Get the id of the associated {@link SensorNode}.
   *
   * @return the node id assigned to this sensor node
   */
  public int getId() {
    return sensorNode.getId();
  }

  /**
   * Access the underlying {@link SensorNode} instance associated with this context.
   *
   * @return the local {@link SensorNode}
   */
  public SensorNode getSensorNode() {
    return sensorNode;
  }
}