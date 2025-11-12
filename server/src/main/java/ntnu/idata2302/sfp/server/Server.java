package ntnu.idata2302.sfp.server;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Server {

  private static final int PORT = 5050;
  private static final Map<Integer, Socket> nodeRegistry = new ConcurrentHashMap<>();

  public static void main(String[] args) {

    try (ServerSocket serverSocket = new ServerSocket(PORT)) {
      System.out.println("Server running on port: " + serverSocket.getInetAddress().getHostAddress() + ":" + PORT);

      // Server up and running at all times
      while (true) {
        Socket socket = serverSocket.accept();
        new Thread(() -> handleClient(socket)).start();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  private static void handleClient(Socket socket) {
    System.out.println("New connection from " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

    try (DataInputStream dis = new DataInputStream(socket.getInputStream());
         OutputStream os = socket.getOutputStream()) {

      while (true) {
        // Read full header
        byte[] headerBytes = dis.readNBytes(Header.HEADER_SIZE);
        if (headerBytes.length < Header.HEADER_SIZE) {
          System.out.println("Client closed connection or incomplete header.");
          break;
        }

        // Decode header
        Header header = Header.fromBytes(headerBytes);

        // Validate protocol prefix (0x53 0x46 0x50)
        byte[] protocol = header.getProtocolName();
        if (protocol[0] != 0x53 || protocol[1] != 0x46 || protocol[2] != 0x50) {
          System.out.println("Invalid protocol prefix from client. Closing connection.");
          break;
        }

        // Read body based on header payload length
        int bodyLength = header.getPayloadLength();
        byte[] bodyBytes = dis.readNBytes(bodyLength);

        // Parse full SFP message
        SmartFarmingProtocol packet = SmartFarmingProtocol.fromBytes(header, bodyBytes);

        // Handle message (route, respond, log, etc.)
        handleMessage(packet, os);

        // Continue loop to handle next message in the same connection
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

  private static void handleMessage(SmartFarmingProtocol packet, OutputStream os) throws IOException {
    System.out.println("Payload length: " + packet.getHeader().getPayloadLength());

    Header header = new Header(
      new byte[] { 'S', 'F', 'P' },   // Protocol name
      (byte)1,                        // Version
      MessageTypes.ERROR,   // Message Type = ERROR
      1001,                           // Source ID
      2002,                           // Target ID
      0,                              // payloadLength placeholder
      UUID.randomUUID()               // Message ID
    );

    ErrorBody body = new ErrorBody(101, "UNKNOWN");
    SmartFarmingProtocol sendPacket = new SmartFarmingProtocol(header, body);
    os.write(sendPacket.toBytes());
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) sb.append(String.format("%02X ", b));
    return sb.toString();
  }
}
