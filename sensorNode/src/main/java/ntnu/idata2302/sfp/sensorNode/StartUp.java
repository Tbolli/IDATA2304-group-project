package ntnu.idata2302.sfp.sensorNode;

import java.util.ArrayList;
import java.util.List;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.core.SimulationLoop;
import ntnu.idata2302.sfp.sensorNode.factory.NodeFactory;
import ntnu.idata2302.sfp.sensorNode.factory.PacketFactory;
import ntnu.idata2302.sfp.sensorNode.net.NetworkLoop;
import ntnu.idata2302.sfp.sensorNode.net.SensorNodeContext;

/**
 * Entry point for starting a simulated sensor node.
 *
 * <p>The StartUp class creates a SensorNode based on command-line arguments
 * or defaults, establishes a secure TLS connection to the server, sends the
 * ANNOUNCE packet, and launches both the network loop and simulation loop.</p>
 */

public class StartUp {

  private static final String SERVER_HOST = "localhost";
  private static final int SERVER_PORT = 5050;

  /**
   * Application entry point for launching a sensor node instance.
   *
   * <p>This method performs the following steps:</p>
   * <ol>
   *   <li>Parses optional command-line arguments into a {@link NodeDescriptor}</li>
   *   <li>Builds a {@link SensorNode} using {@link NodeFactory}</li>
   *   <li>Connects to the server using {@link SensorNodeContext}</li>
   *   <li>Sends an ANNOUNCE packet using {@link PacketFactory}</li>
   *   <li>Starts the {@link NetworkLoop} and {@link SimulationLoop} threads</li>
   * </ol>
   *
   * @param args optional command-line arguments describing the node configuration
   */

  public static void main(String[] args) {
    try {
      // ----------------------------------------
      // 1) Parse node descriptor from args
      // ----------------------------------------
      NodeDescriptor desc = parseDescriptorFromArgs(args);

      SensorNode node;

      if (desc != null) {
        node = NodeFactory.fromDescriptor(desc);
        System.out.println("Created SensorNode from command-line descriptor.");
      } else {
        node = NodeFactory.defaultNode();
        System.out.println("No arguments provided → Using default node.");
      }

      // ----------------------------------------
      // 2) Create network client
      // ----------------------------------------
      SensorNodeContext client = new SensorNodeContext(SERVER_HOST, SERVER_PORT, node);

      client.connect();
      client.sendPacket(PacketFactory.buildAnnouncePacket(node));

      // ----------------------------------------
      // 3) Start simulation + network threads
      // ----------------------------------------
      new Thread(new NetworkLoop(client)).start();
      new Thread(new SimulationLoop(node, client)).start();

      System.out.println("Sensor Node Running.");

    } catch (Exception e) {
      System.err.println("Fatal error in startup: " + e.getMessage());
      e.printStackTrace();
    }
  }

  /**
   * Parses command-line arguments into a {@link NodeDescriptor}.
   *
   * <p>This method supports sensor and actuator definitions, node ID,
   * node type, and capability flags. If arguments are missing or invalid,
   * the method logs the error and returns {@code null}, causing the caller
   * to fall back to a default node configuration.</p>
   *
   * <p>Supported argument formats:</p>
   * <ul>
   *   <li><code>--nodeId=VALUE</code></li>
   *   <li><code>--nodeType=VALUE</code></li>
   *   <li><code>--supportsImages=true|false</code></li>
   *   <li><code>--supportsAggregates=true|false</code></li>
   *   <li><code>--sensor=id:unit:min:max</code></li>
   *   <li><code>--actuator=id:value:min:max:unit</code></li>
   * </ul>
   *
   * @param args the raw command-line arguments passed to {@code main}
   * @return a populated {@link NodeDescriptor}, or {@code null} if parsing fails
   */

  // =====================================================================
  // Parse NodeDescriptor from command line arguments (OPTION 1)
  // =====================================================================
  private static NodeDescriptor parseDescriptorFromArgs(String[] args) {
    try {
      System.out.println("Parsing command-line arguments.");
      if (args.length == 0) {
        return null; // no descriptor passed

      }

      Integer nodeId = null;
      int nodeType = 0;
      Boolean supportsImages = null;
      Boolean supportsAggregates = null;

      List<NodeDescriptor.SensorDescriptor> sensors = new ArrayList<>();
      List<NodeDescriptor.ActuatorDescriptor> actuators = new ArrayList<>();

      for (String arg : args) {

        System.out.println("[ARG] " + arg); // print every argument for debugging

        if (arg.startsWith("--nodeId=")) {
          String v = arg.substring(9);
          nodeId = v.equals("null") ? null : Integer.parseInt(v);

        } else if (arg.startsWith("--nodeType=")) {
          nodeType = Integer.parseInt(arg.substring(11));

        } else if (arg.startsWith("--supportsImages=")) {
          supportsImages = Boolean.parseBoolean(arg.substring(17));

        } else if (arg.startsWith("--supportsAggregates=")) {
          supportsAggregates = Boolean.parseBoolean(arg.substring(21));

        } else if (arg.startsWith("--sensor=")) {
          // Format: id:unit:min:max
          String[] p = arg.substring(9).split(":");

          if (p.length != 4) {
            throw new IllegalArgumentException("Invalid sensor format: " + arg);

          }

          sensors.add(new NodeDescriptor.SensorDescriptor(
                p[0],           // id
                p[1],           // unit
                Double.valueOf(p[2]),
                Double.valueOf(p[3])
          ));

        } else if (arg.startsWith("--actuator=")) {
          // Format: id:value:min:max:unit
          String[] p = arg.substring(11).split(":");

          if (p.length != 5) {
            throw new IllegalArgumentException("Invalid actuator format: " + arg);
          }

          actuators.add(new NodeDescriptor.ActuatorDescriptor(
                p[0],                 // id
                Double.valueOf(p[1]), // initial value
                Double.valueOf(p[2]), // min
                Double.valueOf(p[3]), // max
                p[4]                  // unit
          ));
        }
      }

      return new NodeDescriptor(
        nodeId,
        nodeType,
        sensors,
        actuators,
        supportsImages,
        supportsAggregates
      );

    } catch (Exception e) {

      System.err.println("Error while parsing descriptor arguments!");
      System.err.println("Message: " + e.getMessage());
      System.err.println("Args passed:");

      for (String a : args) {
        System.err.println("     - " + a);
      }
      // Fail safely instead of crashing
      return null;
    }
  }

}
