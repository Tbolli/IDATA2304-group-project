package ntnu.idata2302;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeIds;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ClientExample {

  private static final String SERVER_HOST = "localhost";
  private static final int SERVER_PORT = 5050;
  private static final AtomicInteger counter = new AtomicInteger(0);

  private Socket socket;
  private InputStream in;
  private OutputStream out;

  private volatile boolean listening = false;

  public static void main(String[] args) {
    new ClientExample().start();
  }

  public void start() {
    try {
      connect();
      menuLoop();
    } catch (Exception e) {
      System.err.println("Fatal error: " + e.getMessage());
    } finally {
      close();
    }
  }

  private void connect() throws IOException {
    socket = new Socket(SERVER_HOST, SERVER_PORT);
    in = socket.getInputStream();
    out = socket.getOutputStream();
    System.out.println("Connected to server.");
  }

  private void menuLoop() {
    Scanner scanner = new Scanner(System.in);

    while (true) {
      System.out.println("\n=== CLIENT MENU ===");
      System.out.println("1      | Announce");
      System.out.println("2      | Capabilities Query");
      System.out.println("listen | Listen for packets");
      System.out.println("exit   | Close client");
      System.out.print("Select option: ");

      String choice = scanner.nextLine().trim();

      switch (choice) {
        case "1":
          sendAnnounce();
          waitForSingleResponse();
          break;
        case "2":
          sendCapQuery();
          waitForSingleResponse();
          break;
        case "listen":
          startListening(scanner);
          break;

        case "exit":
          return;

        default:
          System.out.println("Unknown command.");
      }
    }
  }

  private void sendAnnounce() {
    try {
      Header header = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.ANNOUNCE,
        NodeIds.BROADCAST,
        NodeIds.SERVER,
        0,
        UUID.randomUUID()
      );

      NodeDescriptor nodeDescriptor = new NodeDescriptor(
        null, 0, null, null, null, null
      );

      AnnounceBody body = new AnnounceBody(
        counter.incrementAndGet(),
        nodeDescriptor
      );

      SmartFarmingProtocol packet = new SmartFarmingProtocol(header, body);

      out.write(packet.toBytes());
      out.flush();

      System.out.println("Packet sent.");
    } catch (Exception e) {
      System.err.println("Failed to send packet: " + e.getMessage());
    }
  }

  private void sendCapQuery(){
    try {
      Header header = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.CAPABILITIES_QUERY,
        NodeIds.BROADCAST,
        NodeIds.SERVER,
        0,
        UUID.randomUUID()
      );

      CapabilitiesQueryBody body = new CapabilitiesQueryBody(counter.incrementAndGet());
      SmartFarmingProtocol packet = new SmartFarmingProtocol(header, body);

      out.write(packet.toBytes());
      out.flush();

      System.out.println("Packet sent.");
    } catch (Exception e) {
      System.err.println("Failed to send packet: " + e.getMessage());
    }
  }

  private void waitForSingleResponse() {
    try {
      System.out.println("Waiting for server response...");

      byte[] buffer = new byte[2048];
      int bytesRead = in.read(buffer);

      if (bytesRead == -1) {
        System.out.println("Server closed connection.");
        return;
      }

      byte[] msg = new byte[bytesRead];
      System.arraycopy(buffer, 0, msg, 0, bytesRead);

      try {
        SmartFarmingProtocol parsed = SmartFarmingProtocol.fromBytes(msg);
        PacketDecoder.printPacket(parsed);
      }
      catch (Exception decodeErr) {
        System.err.println("Failed to decode packet:");
        decodeErr.printStackTrace();
        System.out.println("Raw bytes: " + toHex(msg));
      }

    } catch (IOException e) {
      System.err.println("Error reading response: " + e.getMessage());
    }
  }


  private void startListening(Scanner scanner) {
    listening = true;

    Thread listenerThread = new Thread(() -> {
      System.out.println("Listening... (type 'quit' to stop)");
      listenLoop();
    });

    listenerThread.start();

    // Wait for user to type "quit"
    while (true) {
      String command = scanner.nextLine().trim();
      if (command.equals("quit")) {
        listening = false;
        System.out.println("Stopping listener...");
        break;
      } else {
        System.out.println("Type 'quit' to exit listening.");
      }
    }

    try {
      listenerThread.join();
    } catch (InterruptedException ignored) {}
  }

  private void listenLoop() {
    try {
      byte[] buffer = new byte[2048];

      while (listening) {
        if (in.available() > 0) {

          int bytesRead = in.read(buffer);
          if (bytesRead == -1) {
            System.out.println("Server disconnected.");
            break;
          }

          byte[] msg = new byte[bytesRead];
          System.arraycopy(buffer, 0, msg, 0, bytesRead);

          try {
            SmartFarmingProtocol parsed = SmartFarmingProtocol.fromBytes(msg);
            PacketDecoder.printPacket(parsed);
          }
          catch (Exception e) {
            System.out.println("Failed to parse packet: " + e.getMessage());
          }
        }

        Thread.sleep(20);
      }

    } catch (Exception e) {
      System.out.println("Listener stopped: " + e.getMessage());
    }
  }

  private void close() {
    try { if (socket != null) socket.close(); } catch (Exception ignored) {}
    System.out.println("Client closed.");
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) sb.append(String.format("%02X ", b));
    return sb.toString();
  }
}
