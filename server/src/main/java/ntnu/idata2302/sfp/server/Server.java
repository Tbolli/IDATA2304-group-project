package ntnu.idata2302.sfp.server;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.handlers.DataReportHandler;
import ntnu.idata2302.sfp.server.handlers.DataRequestHandler;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

  private static final int PORT = 5050;

  private static final MessageDispatcher dispatcher = new MessageDispatcher();
  private static final ServerContext context = new ServerContext();

  static {
    dispatcher.registerHandler(MessageTypes.DATA_REPORT, new DataReportHandler());
    dispatcher.registerHandler(MessageTypes.DATA_REQUEST, new DataRequestHandler());
  }

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

    try (DataInputStream dis = new DataInputStream(socket.getInputStream())) {

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
}
