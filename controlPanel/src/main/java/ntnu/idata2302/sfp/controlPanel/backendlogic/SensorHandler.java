package ntnu.idata2302.sfp.controlPanel.backendlogic;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Handles a single sensor connection on its own thread.
 *
 * <p>Responsibilities:
 * - Read incoming text lines from the sensor socket.
 * - Forward each raw message to {@link MessageRouter#routeFromSensor(String, SensorHandler)}.
 * - Publish messages to the GUI via {@link BackendEventBus#publishSensorMessage(String)}.
 * - Provide a simple method to send text back to the sensor.
 *
 * <p>Lifecycle notes:
 * - Each instance runs in its own thread (extends {@link Thread}).
 * - The input loop terminates when end-of-stream is reached or an {@link IOException} occurs.
 * - The socket is closed in a {@code finally} block to ensure resources are released.
 */
public class SensorHandler extends Thread {

  private final Socket socket;
  private final PrintWriter out;
  private final BufferedReader in;

  /**
   * Create a new handler for the given sensor socket.
   *
   * @param socket           open socket connected to a sensor;
   *                         must not be {@code null} and should be connected
   * @param serverConnection helper/manager used by routing logic; retained for use by this handler
   * @throws IOException if input or output streams cannot be created from the socket
   */
  public SensorHandler(Socket socket, ServerConnection serverConnection) throws IOException {
    this.socket = socket;
    this.out = new PrintWriter(socket.getOutputStream(), true);
    this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
  }

  /**
   * Main loop that reads messages from the sensor and processes them.
   *
   * <p>For each received line:
   * - Calls {@link MessageRouter#routeFromSensor(String, SensorHandler)} to route the message.
   * - Publishes the raw message to the GUI via
   * {@link BackendEventBus#publishSensorMessage(String)}.
   * - Logs the raw message to standard output.
   *
   * <p>If an {@link IOException} occurs the handler logs the disconnect
   * and ensures the socket is closed.
   */
  @Override
  public void run() {
    try {
      String message;

      while ((message = in.readLine()) != null) {

        MessageRouter.routeFromSensor(message, this);
        BackendEventBus.publishSensorMessage(message);

        System.out.println("Sensor -> ControlPanel: " + message);
      }
    } catch (IOException e) {
      System.err.println(" Sensor disconnected: " + socket.getInetAddress());
    } finally {
      try {
        socket.close();
      } catch (IOException ignored) {
        System.out.println("Socket closed: " + socket.getInetAddress());
      }
    }
  }

  /**
   * Send a text message to the connected sensor.
   *
   * <p>The {@link PrintWriter} was created with autoFlush enabled, so {@code println} will
   * flush the line automatically. Passing {@code null} will write the string "null".
   *
   * @param message the text to send to the sensor; may be {@code null}
   */
  public void sendToSensor(String message) {
    out.println(message);
  }
}