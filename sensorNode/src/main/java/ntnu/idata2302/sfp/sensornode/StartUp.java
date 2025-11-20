package ntnu.idata2302.sfp.sensornode;

import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.sensornode.core.SensorNode;
import ntnu.idata2302.sfp.sensornode.core.SimulationLoop;
import ntnu.idata2302.sfp.sensornode.factory.NodeFactory;
import ntnu.idata2302.sfp.sensornode.factory.PacketFactory;
import ntnu.idata2302.sfp.sensornode.net.NetworkLoop;
import ntnu.idata2302.sfp.sensornode.net.SensorNodeContext;

import java.util.ArrayList;
import java.util.List;

public class StartUp {

  private static final String SERVER_HOST = "localhost";
  private static final int SERVER_PORT = 5050;

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

  // =====================================================================
  // Parse NodeDescriptor from command line arguments (OPTION 1)
  // =====================================================================
  private static NodeDescriptor parseDescriptorFromArgs(String[] args) {
    try {
      System.out.println("Parsing command-line arguments.");
      if (args.length == 0)
        return null; // no descriptor passed

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

          if (p.length != 4)
            throw new IllegalArgumentException("Invalid sensor format: " + arg);

          sensors.add(new NodeDescriptor.SensorDescriptor(
            p[0],           // id
            p[1],           // unit
            Double.valueOf(p[2]),
            Double.valueOf(p[3])
          ));

        } else if (arg.startsWith("--actuator=")) {
          // Format: id:value:min:max:unit
          String[] p = arg.substring(11).split(":");

          if (p.length != 5)
            throw new IllegalArgumentException("Invalid actuator format: " + arg);

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
