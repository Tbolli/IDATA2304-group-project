package ntnu.idata2302.sfp.controlPanel.backendlogic;

    import org.json.JSONObject;

    /**
     * Utility for constructing and sending control commands from the Control Panel.
     *
     * <p>Responsibilities:
     * <ul>
     *   <li>Create JSON messages that represent control commands for sensor nodes or the server.</li>
     *   <li>Forward those messages to the system message routing layer via {@link MessageRouter}.</li>
     *   <li>Provide simple console feedback about sent commands.</li>
     * </ul>
     *
     * <p>Message format:
     * <ul>
     *   <li>Sensor commands are sent with {@code "type":"COMMAND"}, a {@code "target_id"} field and a {@code "body"} object containing a {@code "command"} field.</li>
     *   <li>Server commands are sent with {@code "type":"SERVER_COMMAND"} and a {@code "body"} object containing a {@code "command"} field.</li>
     * </ul>
     *
     * <p>Thread-safety and side effects:
     * <ul>
     *   <li>All methods are static and stateless; they are safe to call from multiple threads.</li>
     *   <li>Each method routes a JSON string through {@link MessageRouter#routeFromServer(String)} and prints a status line to standard output.</li>
     * </ul>
     *
     * @see MessageRouter
     * @see org.json.JSONObject
     * @since 1.0
     */
    public class CommandSender {

      /**
       * Constructs and sends a command message targeted at a specific sensor node.
       *
       * <p>The outgoing JSON has the structure:
       * <pre>
       * {
       *   "type": "COMMAND",
       *   "target_id": &lt;targetId&gt;,
       *   "body": { "command": &lt;commandType&gt; }
       * }
       * </pre>
       *
       * <p>Behavior:
       * <ul>
       *   <li>Builds the JSON object, converts it to a string and forwards it to {@link MessageRouter#routeFromServer(String)}.</li>
       *   <li>Prints a concise status message to standard output.</li>
       * </ul>
       *
       * @param targetId the identifier of the sensor node that should receive the command; may be {@code null} but will be included verbatim in the message
       * @param commandType the command type to send (for example {@code "TURN_ON_FAN"} or {@code "SET_TEMP"}); must be a non-null string representing the command
       */
      public static void sendToSensor(String targetId, String commandType) {
        JSONObject msg = new JSONObject();
        msg.put("type", "COMMAND");
        msg.put("target_id", targetId);

        JSONObject body = new JSONObject();
        body.put("command", commandType);
        msg.put("body", body);

        MessageRouter.routeFromServer(msg.toString());
        System.out.println("📡 Sent command to Sensor " + targetId + ": " + commandType);
      }

      /**
       * Constructs and sends a command message intended for the central server.
       *
       * <p>The outgoing JSON has the structure:
       * <pre>
       * {
       *   "type": "SERVER_COMMAND",
       *   "body": { "command": &lt;commandType&gt; }
       * }
       * </pre>
       *
       * <p>Behavior:
       * <ul>
       *   <li>Builds the JSON object, converts it to a string and forwards it to {@link MessageRouter#routeFromServer(String)}.</li>
       *   <li>Prints a concise status message to standard output.</li>
       * </ul>
       *
       * @param commandType the command to send to the server (for example {@code "RESTART"}); must be a non-null string representing the command
       */
      public static void sendToServer(String commandType) {
        JSONObject msg = new JSONObject();
        msg.put("type", "SERVER_COMMAND");

        JSONObject body = new JSONObject();
        body.put("command", commandType);
        msg.put("body", body);

        MessageRouter.routeFromServer(msg.toString());
        System.out.println("Sent command to Server: " + commandType);
      }
    }