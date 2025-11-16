package ntnu.idata2302.sfp.sensorNode;

import java.util.concurrent.atomic.AtomicInteger;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.core.SimulationLoop;
import ntnu.idata2302.sfp.sensorNode.factory.NodeFactory;
import ntnu.idata2302.sfp.sensorNode.factory.PacketFactory;
import ntnu.idata2302.sfp.sensorNode.net.NetworkLoop;
import ntnu.idata2302.sfp.sensorNode.net.SensorNodeContext;

/**
 * Application entry point for the sensor node simulation.
 *
 * <p>This class is responsible for creating a simulated {@link SensorNode},
 * establishing a TLS/TCP connection to the configured server, sending an
 * initial ANNOUNCE packet, and starting the network listener and simulation
 * threads that drive the node's behavior.</p>
 *
 * <p>Usage: run the {@link #main(String[])} method. The startup sequence logs
 * success or prints a fatal error message if an exception occurs.</p>
 */
public class StartUp {

  /**
   * Default server host used to connect the sensor node.
   */
  private static final String SERVER_HOST = "localhost";

  /**
   * Default server port used to connect the sensor node.
   */
  private static final int SERVER_PORT = 5050;

  /**
   * Simple counter used by the application (reserved for future use).
   *
   * <p>Currently unused in startup logic; kept for compatibility or later
   * extensions.</p>
   */
  private static final AtomicInteger counter = new AtomicInteger(0);


  /**
   * Program entry point.
   *
   * <p>Performs the following steps in order:
   * <ol>
   *   <li>Builds a default simulated {@link SensorNode} using {@link NodeFactory}.</li>
   *   <li>Creates a {@link SensorNodeContext} for network I/O and connects to
   *       the configured server host/port.</li>
   *   <li>Sends an ANNOUNCE packet describing the node using
   *       {@link PacketFactory#buildAnnouncePacket(SensorNode)}.</li>
   *   <li>Starts a {@link NetworkLoop} thread to receive incoming packets and
   *       a {@link SimulationLoop} thread to advance the node simulation.</li>
   *   <li>Logs that the sensor node is running or prints a fatal error if an
   *       exception occurs during startup.</li>
   * </ol>
   * </p>
   *
   * @param args command line arguments (not used)
   */
  public static void main(String[] args) {
    try {
      // Build simulated node
      SensorNode node = NodeFactory.defaultNode();

      // Create network client
      SensorNodeContext client = new SensorNodeContext(SERVER_HOST, SERVER_PORT, node);
      // Connect to the server
      client.connect();
      // Send an initial packet
      client.sendPacket(PacketFactory.buildAnnouncePacket(node));

      // Start network listener and simulation thread
      new Thread(new NetworkLoop(client)).start();
      new Thread(new SimulationLoop(node, client)).start();

      System.out.println("Sensor Node Running.");

    } catch (Exception e) {
      System.err.println("Fatal error in startup: " + e.getMessage());
    }
  }
}