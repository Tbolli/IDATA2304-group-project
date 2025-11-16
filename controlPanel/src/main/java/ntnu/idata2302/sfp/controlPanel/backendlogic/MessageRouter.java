package ntnu.idata2302.sfp.controlPanel.backendlogic;

import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONObject;

/**
 * MessageRouter routes JSON messages between connected sensor nodes and the
 * central Smart Farming Server.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Maintain a thread-safe registry of connected sensors (sensorId → SensorHandler).</li>
 *   <li>Receive messages from sensor nodes, optionally register sensors, and
 *       forward messages to the server connection when available.</li>
 *   <li>Receive messages from the server and forward them to the targeted sensor
 *       when the target is known.</li>
 * </ul>
 *
 * <p>Message format expectations (JSON):
 * <ul>
 *   <li>From sensors: at minimum, the messages are JSON strings. Identification
 *       messages should include {@code "type": "IDENTIFICATION_REQUEST"} and a
 *       {@code "temp_id"} field with the sensor identifier.</li>
 *   <li>From server: messages should include {@code "type"} and a {@code "target_id"}
 *       when the server intends to address a particular sensor.</li>
 * </ul>
 *
 * <p>Thread-safety: the underlying {@link ConcurrentHashMap} allows concurrent
 * access from multiple handler threads. Methods are static utility-style
 * operations intended to be called by {@link SensorHandler} instances and the
 * single {@link ServerConnection} instance registered with this router.
 */
public class MessageRouter {

  /**
   * Thread-safe map of connected sensors (sensorId → SensorHandler).
   */
  private static final ConcurrentHashMap<String, SensorHandler> sensors = new ConcurrentHashMap<>();

  /**
   * The CP's single connection to the main Smart Farming Server.
   */
  private static ServerConnection serverConnection;

  /**
   * Registers the active connection to the Smart Farming Server.
   *
   * <p>This method should be called once when the bridge establishes its outgoing
   * connection to the server. The registered {@code ServerConnection} will be used
   * by {@link #routeFromSensor(String, SensorHandler)} to forward messages.
   *
   * @param connection the connection object used to send messages to the server;
   *                   may be {@code null} to explicitly clear the registration
   *                   (future calls to routing methods will not forward messages).
   */
  public static void registerServer(ServerConnection connection) {
    serverConnection = connection;
  }

  /**
   * Handles and routes a message received from a connected sensor node.
   *
   * <p>Behavior summary:
   * <ul>
   *   <li>Parses the provided {@code message} as JSON.</li>
   *   <li>If the message has {@code "type": "IDENTIFICATION_REQUEST"} and contains
   *       a {@code "temp_id"}, the sensor is registered in the internal map.</li>
   *   <li>All messages are forwarded to the registered server connection (if any)
   *       by calling {@code serverConnection.send(message)}.</li>
   *   <li>Any parsing or routing errors are caught and logged to {@code System.err}
   *       to avoid bringing down the calling handler thread.</li>
   * </ul>
   *
   * <p>Note: this method does not block waiting for server responses. Forwarding
   * is synchronous to the {@code ServerConnection.send} call; if non-blocking
   * behavior is required, wrap this call in an executor or queue.
   *
   * @param message       the JSON-formatted message string from the sensor.
   *                      Must not be {@code null}.
   * @param sensorHandler the {@link SensorHandler} instance that received the message;
   *                      used to create a fallback sensor id when none is provided.
   */
  public static void routeFromSensor(String message, SensorHandler sensorHandler) {
    try {
      JSONObject msg = new JSONObject(message);
      String type = msg.optString("type", "UNKNOWN");

      // Register a new sensor when it identifies itself
      if (type.equalsIgnoreCase("IDENTIFICATION_REQUEST")) {
        String sensorId = msg.optString("temp_id", "sensor-" + sensorHandler.hashCode());
        sensors.put(sensorId, sensorHandler);
        System.out.println(" Registered Sensor Node: " + sensorId);
      }

      // Forward every valid message to the main server
      if (serverConnection != null) {
        serverConnection.send(message);
        System.out.println(" Forwarded from Sensor → Server: " + type);
      } else {
        System.out.println("️ No server connection available; message dropped.");
      }

    } catch (Exception e) {
      System.err.println(" Failed to route message from sensor: " + e.getMessage());
    }
  }

  /**
   * Handles and routes a message received from the main Smart Farming Server.
   *
   * <p>Behavior summary:
   * <ul>
   *   <li>Parses the incoming {@code message} as JSON.</li>
   *   <li>Attempts to locate the target sensor using the {@code "Node ID"} field.</li>
   *   <li>If a connected {@link SensorHandler} exists for the target, forwards the
   *       raw message to that sensor via {@code SensorHandler.sendToSensor}.</li>
   *   <li>If the target is missing or unknown, logs a warning to standard output.</li>
   * </ul>
   *
   * <p>Any parsing or routing errors are caught and written to {@code System.err}
   * so callers (typically a server-listener thread) are not interrupted by exceptions.
   *
   * @param message the JSON-formatted message string from the server. Must not be {@code null}.
   */
  public static void routeFromServer(String message) {
    try {
      JSONObject msg = new JSONObject(message);
      String type = msg.optString("type", "UNKNOWN");
      String targetId = msg.optString("Node ID", null);

      // If this message is meant for a specific sensor
      if (targetId != null && sensors.containsKey(targetId)) {
        SensorHandler targetSensor = sensors.get(targetId);
        targetSensor.sendToSensor(message);
        System.out.println("️ Forwarded from Server → Sensor [" + targetId + "]: " + type);
      } else {
        System.out.println(" Unknown or missing target for message: " + message);
      }

    } catch (Exception e) {
      System.err.println(" Failed to route message from server: " + e.getMessage());
    }
  }
}