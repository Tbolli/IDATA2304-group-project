package ntnu.idata2302;


import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeIds;



/**
 * Simple example TCP client that builds and exchanges Smart Farming Protocol
 * packets with a server.
 *
 * <p>This class demonstrates creating headers and bodies for two message
 * types (announce and capabilities query), sending packets to a server,
 * and optionally listening for incoming packets. It is a small interactive
 * CLI used for manual testing and debugging.</p>
 *
 * <p>Usage:
 * start the client, choose menu entries to send packets, or enter {@code listen}
 * to continuously print incoming packets until typing {@code quit}.</p>
 */
public class ClientExample {

  /**
   * Server host name to connect to (default {@code "localhost"}).
   */
  private static final String SERVER_HOST = "localhost";

  /**
   * Server TCP port to connect to (default {@code 5050}).
   */
  private static final int SERVER_PORT = 5050;

  /**
   * Local counter used to provide unique ids inside message bodies.
   */
  private static final AtomicInteger counter = new AtomicInteger(0);

  private Socket socket;
  private InputStream in;
  private OutputStream out;

  /**
   * Flag controlling the background listen to loop.
   */
  private volatile boolean listening = false;

  /**
   * Program entry point. Creates a {@code ClientExample} and starts it.
   *
   * @param args ignored
   */
  public static void main(String[] args) {
    new ClientExample().start();
  }

  /**
   * Start the client: connect to the server, run the interactive menu loop,
   * and ensure resources are closed on exit.
   *
   * <p>Any exception thrown during connecting or menu processing is logged to
   * {@code System.err} and the client cleans up resources in a {@code finally}
   * block.</p>
   */
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

  /**
   * Establish a TCP connection to the configured server and initialize
   * input/output streams.
   *
   * @throws IOException if the socket cannot be opened or streams cannot be obtained
   */
  private void connect() throws IOException {
    socket = new Socket(SERVER_HOST, SERVER_PORT);
    in = socket.getInputStream();
    out = socket.getOutputStream();
    System.out.println("Connected to server.");
  }

  /**
   * Interactive command loop reading user choices from {@link Scanner}
   * and dispatching to the appropriate actions.
   *
   * <p>Supported commands: send announce, send capabilities query, start
   * listening mode, or exit the client.</p>
   */
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

  /**
   * Build and send an ANNOUNCE packet to the server.
   *
   * <p>The method constructs a header with the {@code ANNOUNCE} message type,
   * builds a minimal {@link NodeDescriptor} and {@link AnnounceBody}, wraps
   * them in a {@link SmartFarmingProtocol} packet and writes the serialized
   * bytes to the socket output stream.</p>
   * Any exceptions during construction or send are reported to {@code System.err}.
   */
  private void sendAnnounce() {
    try {
      Header header = new Header(
          new byte[] {'S', 'F', 'P'},
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

  /**
   * Build and send a CAPABILITIES_QUERY packet to the server.
   *
   * <p>Constructs the header and a {@link CapabilitiesQueryBody}, serializes
   * into a {@link SmartFarmingProtocol} and writes it to the output stream.</p>
   * Any exceptions during construction or send are reported to {@code System.err}.
   */
  private void sendCapQuery() {
    try {
      Header header = new Header(
          new byte[] {'S', 'F', 'P'},
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

  /**
   * Read a single response packet from the server, attempt to parse it and
   * print the decoded packet via {@code PacketDecoder.printPacket(...) }.
   *
   * <p>If EOF is encountered, the method reports server closure. Parsing errors
   * are logged including a hex dump of the raw bytes.</p>
   */
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
      } catch (Exception decodeErr) {
        System.err.println("Failed to decode packet:");
        decodeErr.printStackTrace();
        System.out.println("Raw bytes: " + toHex(msg));
      }

    } catch (IOException e) {
      System.err.println("Error reading response: " + e.getMessage());
    }
  }


  /**
   * Enter a listening mode that spawns a background thread which continuously
   * reads and decodes incoming packets until the user types {@code quit}.
   *
   * @param scanner the {@link Scanner} used to read the user's {@code quit} command
   */
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
    } catch (InterruptedException ignored) {
      System.out.println("Listening interrupted.");
    }
  }

  /**
   * Background loop used by the listener thread. Polls the input stream for
   * available bytes, reads and decodes packets, and prints any errors encountered.
   *
   * <p>The loop periodically sleeps to avoid busy-waiting and terminates when
   * {@link #listening} becomes {@code false} or the server disconnects.</p>
   */
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
          } catch (Exception e) {
            System.out.println("Failed to parse packet: " + e.getMessage());
          }
        }

        Thread.sleep(20);
      }

    } catch (Exception e) {
      System.out.println("Listener stopped: " + e.getMessage());
    }
  }

  /**
   * Close socket and associated streams. Exceptions are ignored to keep the
   * shutdown path robust.
   */
  private void close() {
    try {
      if (socket != null) {
        socket.close();
      }
    } catch (Exception ignored) {

    System.out.println("Client closed.");
    }
  }

  /**
   * Convert a byte array to a space-separated hexadecimal string useful for
   * logging raw packet contents.
   *
   * @param bytes input byte array
   * @return hex representation such as {@code "DE AD BE EF "}
   */
  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
    }
    return sb.toString();
  }
}