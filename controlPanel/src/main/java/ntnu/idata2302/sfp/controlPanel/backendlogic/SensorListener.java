
package ntnu.idata2302.sfp.controlPanel.backendlogic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listens for incoming sensor node connections on a configured TCP port and
 * dispatches each accepted connection to a dedicated {@link SensorHandler}.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Bind a {@link ServerSocket} to the supplied port.</li>
 *   <li>Accept incoming TCP connections from sensor nodes.</li>
 *   <li>Create a {@link SensorHandler} for each accepted socket and start it on
 *       its own thread so each sensor is handled concurrently.</li>
 * </ul>
 *
 * <p>Threading and lifecycle:
 * <ul>
 *   <li>This class implements {@link Runnable}; its {@link #run()} method blocks
 *       while the listener is active and therefore should be executed on a
 *       dedicated thread (for example: {@code new Thread(new SensorListener(...)).start();}).</li>
 *   <li>The listener uses a try-with-resources {@link ServerSocket} which is
 *       closed automatically when {@link #run()} exits. To stop the listener
 *       cleanly, close the listening socket from another thread or interrupt the
 *       thread and rely on additional shutdown handling added by the caller.</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 *   <li>The provided {@link ServerConnection} (may be {@code null}) is forwarded
 *       to created {@link SensorHandler} instances so they can relay messages to
 *       the central server if needed.</li>
 *   <li>Errors during acceptance or socket handling are logged to standard error
 *       in the current implementation; consider using a logging framework and
 *       more robust shutdown semantics for production use.</li>
 * </ul>
 */
public class SensorListener implements Runnable {
  /**
   * TCP port on which this listener accepts incoming sensor connections.
   */
  private final int port;

  /**
   * Optional outgoing connection to the central server; passed to each
   * {@link SensorHandler} so handlers can forward sensor messages.
   */
  private final ServerConnection serverConnection;

  /**
   * Create a new SensorListener.
   *
   * @param port             TCP port to listen on for incoming sensor connections
   * @param serverConnection optional {@link ServerConnection} instance that will be
   *                         supplied to created {@link SensorHandler} instances;
   *                         may be {@code null} if forwarding to the server is not required
   */
  public SensorListener(int port, ServerConnection serverConnection) {
    this.port = port;
    this.serverConnection = serverConnection;
  }

  /**
   * Main listener loop.
   *
   * <p>Behavior:
   * <ol>
   *   <li>Create a {@link ServerSocket} bound to {@link #port} (try-with-resources used).</li>
   *   <li>Log that the listener is active.</li>
   *   <li>Loop indefinitely accepting incoming connections. For each accepted socket:
   *     <ul>
   *       <li>Log the remote address.</li>
   *       <li>Create a {@link SensorHandler} for the socket and
   *       the shared {@link ServerConnection}.</li>
   *       <li>Start the handler on a new thread to handle communication with that sensor.</li>
   *     </ul>
   *   </li>
   *   <li>If an {@link IOException} occurs the exception is logged and {@code run()} exits,
   *       allowing the {@link ServerSocket} to be closed by the try-with-resources block.</li>
   * </ol>
   *
   * <p>Important: this method blocks while accepting connections. Callers must run it
   * on a dedicated thread to avoid blocking application startup or other tasks.
   */
  @Override
  public void run() {
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println(" SensorListener active on port " + port);

      while (true) {
        Socket sensorSocket = serverSocket.accept();
        System.out.println(" New Sensor connected: " + sensorSocket.getInetAddress());
        SensorHandler handler = new SensorHandler(sensorSocket, serverConnection);
        new Thread(handler).start();
      }
    } catch (IOException e) {
      System.err.println(" Error in SensorListener: " + e.getMessage());
    }
  }
}