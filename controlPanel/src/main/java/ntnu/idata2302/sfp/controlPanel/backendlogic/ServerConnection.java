
package ntnu.idata2302.sfp.controlPanel.backendlogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Manages the outgoing TCP connection between the Control Panel and the main
 * Smart Farming Server.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Establish a TCP connection to the server at construction time.</li>
 *   <li>Create and manage input and output streams for the socket.</li>
 *   <li>Start a dedicated background thread that listens for server messages.</li>
 *   <li>Provide a {@link #send(String)} method to transmit text messages (for
 *       example, JSON) to the server.</li>
 *   <li>Send a short identify/protocol message immediately after connecting.</li>
 * </ul>
 *
 * <p>Threading and lifecycle:
 * <ul>
 *   <li>The constructor starts a background thread named {@code ServerListener}
 *       that runs {@link #listenToServer()} and reads incoming lines until the
 *       socket is closed or an I/O error occurs.</li>
 *   <li>Callers are responsible for handling the lifecycle of this object; closing
 *       the underlying socket will terminate the listener thread.</li>
 * </ul>
 *
 * <p>Usage notes:
 * <ul>
 *   <li>Messages sent via {@link #send(String)} are written as lines using
 *       {@link PrintWriter#println}; callers should send complete JSON strings
 *       or protocol lines expected by the server.</li>
 *   <li>Errors are currently written to standard error; consider replacing
 *       with a logging framework for production use.</li>
 * </ul>
 */
public class ServerConnection {
  /**
   * The underlying TCP socket connected to the server.
   */
  private final Socket socket;

  /**
   * The Writer used to send textual protocol messages to the server. The writer
   * is created with auto-flush enabled on newline.
   */
  private final PrintWriter out;

  /**
   * Reader used to receive textual lines from the server.
   */
  private final BufferedReader in;

  /**
   * Creates a new {@code ServerConnection}, opens a socket to the specified
   * host/port, starts a background listener thread, and sends an initial
   * identify/protocol message.
   *
   * <p>This constructor will attempt to connect synchronously and will throw
   * {@link IOException} on failure.
   *
   * @param host the server host name or IP address to connect to
   * @param port the server TCP port to connect to
   * @throws IOException if establishing the socket or its streams fails
   */
  public ServerConnection(String host, int port) throws IOException {
    socket = new Socket(host, port);
    out = new PrintWriter(socket.getOutputStream(), true);
    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

    System.out.println(
        "connected to server " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());

    Thread serverListener = new Thread(this::listenToServer);
    serverListener.setName("ServerListener");
    serverListener.start();

    sendIdentifyMessage();
  }

  /**
   * Sends a short identify/protocol message to the server identifying this
   * connection as the Control Panel bridge.
   *
   * <p>The format is a plain text line: {@code IDENTIFY CONTROL PANEL}. This
   * method writes the line and flushes the output. The exact protocol and
   * message format can be changed to match the server's expectations.
   */
  private void sendIdentifyMessage() {
    String identifyMsg = "IDENTIFY CONTROL PANEL";
    out.println(identifyMsg);
    System.out.println("Sent identify message: " + identifyMsg);
  }

  /**
   * Listens for incoming lines from the server on the {@code in} reader.
   *
   * <p>This method runs on the dedicated {@code ServerListener} thread. It
   * continuously reads lines until the socket is closed or {@code readLine()}
   * returns {@code null}. Received lines are printed to standard output and
   * may be handled or parsed here in the future.
   *
   * <p>If an {@link IOException} occurs while reading, the exception is logged
   * to standard error and the socket is closed in the {@code finally} block.
   */
  private void listenToServer() {
    try {
      String line;
      while (!socket.isClosed() && (line = in.readLine()) != null) {

        System.out.println("Server -> " + line);
        BackendEventBus.publishServerMessage(line);

        MessageRouter.routeFromServer(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }


  /**
   * Sends a message string to the main Smart Farming Server.
   *
   * <p>The message is written as a single line using {@link PrintWriter#println}
   * and flushed immediately so the server receives it without delay.
   *
   * <p>Note: this method currently prints errors to standard error. If this
   * class is used concurrently by multiple threads, consider synchronizing
   * calls to this method or using a thread-safe writer wrapper.
   *
   * @param message The textual message to send (for example, a JSON string).
   */
  public void send(String message) {
    try {
      if (out != null) {
        out.println(message);
        out.flush();
        System.out.println(" Sent to Server: " + message);
      } else {
        System.out.println(" Output stream is null. Message not sent: " + message);
      }
    } catch (Exception e) {
      System.err.println(" Failed to send message to server: " + e.getMessage());
    }
  }


}