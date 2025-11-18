package ntnu.idata2302.sfp.controlPanel.net;

import ntnu.idata2302.sfp.controlPanel.factory.PacketFactory;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

  public SfpClient(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /** Establish a TCP connection over TLS and start listener thread */
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

  /** Blocking packet read loop — runs only on listener thread */
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

  /** Send ANNOUNCE packet to the server */
  public void sendAnnounce(){
    SmartFarmingProtocol packet = PacketFactory.Announce(
      AppContext.getRequestId()
    );
    sendPacket(packet);
  }

  /** Send CAPABILITIES_QUERY packet to the server */
  public void sendCapabilitiesQuery(){
    SmartFarmingProtocol packet = PacketFactory.capabilitiesQuery(
      AppContext.getControllerId(),
      AppContext.getRequestId()
    );
    // TODO remove
    System.out.println("sending cap req");
    sendPacket(packet);
    System.out.println("done sending cap req");

  }

  /** Send SUBSCRIBE packet subscribing a single node to the server */
  public void sendSubscribe(int nodeId, List<String> sensorNames, List<String> actuatorNames){
    SmartFarmingProtocol packet = PacketFactory.SubscribeSingularNode(
      AppContext.getControllerId(),
      AppContext.getRequestId(),
      nodeId,
      sensorNames,
      actuatorNames
    );
    sendPacket(packet);
  }

  /** Thread-safe outgoing message dispatcher */
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


  public void close() {
    running = false;
    try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    listenerThread.shutdownNow();
    writerThread.shutdownNow();
  }

  public String getHost() {
    return host;
  }

  public int getPort() {
    return port;
  }
}

