package ntnu.idata2302;

import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.entity.ActuatorType;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class ClientExample {

  public static void main(String[] args) throws IOException, URISyntaxException {
    List<NodeDescriptor> nodeList = new ArrayList<>();

    NodeDescriptor node1 = new NodeDescriptor(
      null,
      1,
      List.of(
        new NodeDescriptor.SensorDescriptor("temp", "°C", -20.0, 50.0)
      ),
      List.of(
        new NodeDescriptor.ActuatorDescriptor(
          ActuatorType.HEATER.displayName(),
          0.0,
          0.0,
          1.0,
          ActuatorType.HEATER.unit()
        ),
        new NodeDescriptor.ActuatorDescriptor(
          ActuatorType.FAN.displayName(),
          0.0,
          0.0,
          100.0,
          ActuatorType.FAN.unit()
        )
      ),
      false,
      false
    );

    NodeDescriptor node2 = new NodeDescriptor(
      null,
      1,
      List.of(
        new NodeDescriptor.SensorDescriptor("soilMoist", "%", 0.0, 100.0)
      ),
      List.of(
        new NodeDescriptor.ActuatorDescriptor(
          ActuatorType.SPRINKLER.displayName(),
          0.0,
          0.0,
          100.0,
          ActuatorType.SPRINKLER.unit()
        ),
        new NodeDescriptor.ActuatorDescriptor(
          ActuatorType.VALVE.displayName(),
          0.0,
          0.0,
          100.0,
          ActuatorType.VALVE.unit()
        )
      ),
      false,
      true
    );


    NodeDescriptor node3 = new NodeDescriptor(
      null,
      1,
      List.of(
        new NodeDescriptor.SensorDescriptor("light", "lux", 0.0, 10000.0),
        new NodeDescriptor.SensorDescriptor("humidity", "%", 0.0, 100.0)
      ),
      List.of(
        new NodeDescriptor.ActuatorDescriptor(
          ActuatorType.VENT.displayName(),
          0.0,
          0.0,
          100.0,
          ActuatorType.VENT.unit()
        ),
        new NodeDescriptor.ActuatorDescriptor(
          ActuatorType.LIGHT.displayName(),
          0.0,
          0.0,
          1.0,
          ActuatorType.LIGHT.unit()
        )
      ),
      true,
      false
    );

    nodeList.add(node1);
    nodeList.add(node2);
    nodeList.add(node3);

    Path classPath = Path.of(
      ClientExample.class.getProtectionDomain()
        .getCodeSource()
        .getLocation()
        .toURI()
    );

    if (!Files.isDirectory(classPath))
      classPath = classPath.getParent();

    Path projectRoot = classPath.getParent().getParent().getParent();
    Path jarPath = projectRoot.resolve("sensorNode/target/sfp-sensorNode-1.0-SNAPSHOT.jar");

    System.out.println("Resolved JAR: " + jarPath);

    for (NodeDescriptor node : nodeList) {

      List<String> cmd = new ArrayList<>();
      cmd.add("java");
      cmd.add("-jar");
      cmd.add(jarPath.toString());

      // Convert NodeDescriptor → Argument list
      buildArgsForNode(node, cmd);

      // Launch process
      ProcessBuilder pb = new ProcessBuilder(cmd);
      pb.inheritIO();
      pb.start();
    }
  }

  /** Converts a NodeDescriptor into command-line args */
  private static void buildArgsForNode(NodeDescriptor node, List<String> cmd) {

    cmd.add("--nodeId=" + node.nodeId());
    cmd.add("--nodeType=" + node.nodeType());
    cmd.add("--supportsImages=" + node.supportsImages());
    cmd.add("--supportsAggregates=" + node.supportsAggregates());

    // Sensors
    for (var s : node.sensors()) {
      cmd.add("--sensor=" + s.id() + ":" + s.unit() + ":" + s.minValue() + ":" + s.maxValue());
    }

    // Actuators
    for (var a : node.actuators()) {
      cmd.add("--actuator="
        + a.id() + ":" + a.value() + ":" + a.minValue() + ":" + a.maxValue() + ":" + a.unit());
    }
  }

}