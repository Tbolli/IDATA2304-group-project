package ntnu.idata2302.sfp.controlPanel.backendlogic;

/**
 * Entry point and main logic for the Control Panel application.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li>Establish an outgoing connection to the central Smart Farming Server.</li>
 *   <li>Start a listener that accepts incoming sensor connections on a configured port.</li>
 *   <li>Create and hold references to the {@link ServerConnection} and {@link SensorListener}
 *       used by the control panel.</li>
 *   <li>Register the {@code ServerConnection} with {@link MessageRouter} so sensor messages
 *       can be forwarded to the server.</li>
 * </ul>
 *
 * <p>Notes:
 * <ul>
 *   <li>This class uses simple console output for status messages; consider replacing
 *       with a logging framework for production use.</li>
 *   <li>Port and host constants are defined as application defaults and can be made
 *       configurable if needed.</li>
 * </ul>
 */
@SuppressWarnings("InstantiationOfUtilityClass")
public class ControlPanelApp {

  /**
   * TCP port used to listen for incoming sensor node connections.
   * Default value: 5051.
   */
  private static final int SensorPort = 5051;

  /**
   * Hostname or IP address of the Smart Farming Server to connect to.
   * Default value: "localhost".
   */
  private static final String ServerHost = "localhost";

  /**
   * TCP port of the Smart Farming Server to connect to.
   * Default value: 5050.
   */
  private static final int ServerPort = 5050;

  /**
   * Creates and starts the Control Panel application.
   *
   * <p>Behavior:
   * <ol>
   *   <li>Attempts to create a {@link ServerConnection}
   *   to {@link #ServerHost}:{@link #ServerPort}.</li>
   *   <li>Registers the created {@code ServerConnection} with {@link MessageRouter} so incoming
   *       sensor messages can be forwarded to the server.</li>
   *   <li>Starts a {@link SensorListener} that listens for sensor connections on
   *   {@link #SensorPort}.</li>
   *   <li>Prints basic status messages to the console.</li>
   * </ol>
   *
   * <p>Exceptions:
   * <ul>
   *   <li>Any exceptions during startup are printed and rethrown as a {@link RuntimeException}
   *       to indicate failure to initialize the application.</li>
   * </ul>
   */
  public ControlPanelApp() {
    try {
      System.out.println("Welcome to Smart Farming Control");

      // Connect to server
      ServerConnection serverConnection = new ServerConnection(ServerHost, ServerPort);

      // Register server with the message router so sensor messages are forwarded
      MessageRouter.registerServer(serverConnection);

      // Start listening for sensor nodes on the sensor port

      SensorListener sensorListener = new SensorListener(SensorPort, serverConnection);
      new Thread(sensorListener).start();


      System.out.println("Listening for sensors on port " + SensorPort);
      System.out.println("Connected to server " + ServerHost + ":" + ServerPort);


    } catch (Exception e) {
      System.out.println("Startup failed: " + e.getMessage());
      throw new RuntimeException(e);
    }
  }


  /**
   * Application entry point.
   *
   * <p>Constructs a {@code ControlPanelApp} instance which performs all required
   * startup actions in its constructor. The method currently does not process
   * command-line arguments.
   *
   * @param args command-line arguments (currently unused)
   */
  public static void main(String[] args) {
    new ControlPanelApp();
  }
}