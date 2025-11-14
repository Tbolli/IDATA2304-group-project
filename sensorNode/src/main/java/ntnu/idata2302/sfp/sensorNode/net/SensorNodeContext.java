package ntnu.idata2302.sfp.sensorNode.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Handles TCP connection to the server.
 * Provides packet send/receive utilities.
 */
public class SensorNodeContext {

  private final String host;
  private final int port;
  private int id;

  private Socket socket;
  private DataInputStream in;
  private OutputStream out;

  public SensorNodeContext(String host, int port) {
    this.host = host;
    this.port = port;
  }

  /** Opens a blocking TCP connection to the server. */
  public void connect() throws IOException {
    socket = new Socket(host, port);
    in = new DataInputStream(socket.getInputStream());
    out = socket.getOutputStream();
    System.out.println("Connected to server.");
  }

  /** Returns true if socket is alive. */
  public boolean isConnected() {
    return socket != null && socket.isConnected() && !socket.isClosed();
  }

  /** Sends a complete SFP packet. */
  public synchronized void sendPacket(SmartFarmingProtocol packet) {
    try {
      out.write(packet.toBytes());
      out.flush();
    } catch (IOException e) {
      System.err.println("Failed to send packet: " + e.getMessage());
    }
  }

  /** Blocking read for one SFP packet. */
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

  public void setId(int id) {
    this.id = id;
  }
  public int getId() {
    return id;
  }
}
