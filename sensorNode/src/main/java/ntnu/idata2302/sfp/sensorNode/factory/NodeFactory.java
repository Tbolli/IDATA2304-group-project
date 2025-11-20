package ntnu.idata2302.sfp.sensorNode.factory;

import java.util.List;

import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.sensorNode.core.Actuator;
import ntnu.idata2302.sfp.sensorNode.core.Sensor;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.entity.ActuatorType;


/**
 * Factory for constructing preconfigured {@link SensorNode} instances used by
 * the smart farming sensor node simulation.
 *
 * <p>The factory provides convenient methods to obtain commonly used, ready-to-run
 * {@link SensorNode} configurations composed of sensible default {@link Sensor}
 * and {@link Actuator} loadouts. Instances returned by this factory are intended
 * for simulation and testing; callers may further modify the returned {@link SensorNode}
 * (for example, setting its id) before use.</p>
 *
 * <p>Usage example:
 * <pre>
 *   SensorNode node = NodeFactory.defaultNode();
 * </pre>
 * </p>
 */
public final class NodeFactory {

  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * <p>All methods on this class are static; calling code should use the static
   * factory methods rather than creating an instance.</p>
   */
  private NodeFactory() {
  } // prevent instantiation

  /**
   * Create a fully configured default {@link SensorNode}.
   *
   * <p>The returned node contains a set of common environmental sensors and
   * actuators that emulate a simple greenhouse device. The default configuration
   * includes:
   * <ul>
   *   <li>Temperature sensor</li>
   *   <li>Humidity sensor</li>
   *   <li>CO₂ sensor</li>
   *   <li>Light sensor</li>
   *   <li>Soil moisture sensor</li>
   *   <li>Fan actuator</li>
   *   <li>Heater actuator</li>
   *   <li>Grow-light actuator</li>
   * </ul>
   * </p>
   *
   * <p>Callers receive a new {@link SensorNode} instance populated with the above
   * sensors and actuators; the node's id is not set by this method and can be
   * assigned by the caller via {@link SensorNode#setId(int)} if needed.</p>
   *
   * @return a new {@link SensorNode} ready for simulation and networking
   */
  public static SensorNode defaultNode() {
    List<Sensor> sensors = List.of(
        new Sensor("Temperature", 10, 40, "°C"),
        new Sensor("Humidity", 20, 100, "%"),
        new Sensor("CO2", 300, 3000, "ppm"),
        new Sensor("Light Sensor", 0, 100_000, "lux"),
        new Sensor("SoilMoisture", 0, 100, "%")
    );

    List<Actuator> actuators = List.of(
        new Actuator(ActuatorType.FAN, 0, 100),       // % power
        new Actuator(ActuatorType.HEATER, 0, 100),    // % power
        new Actuator(ActuatorType.LIGHT, 1.0)                // state brightness
    );

    return new SensorNode(sensors, actuators, false, false);
  }

  public static SensorNode fromDescriptor(NodeDescriptor desc) {

    // -----------------------------
    // Convert sensors
    // -----------------------------
    List<Sensor> sensors = desc.sensors().stream()
      .map(s -> new Sensor(
        s.id(),
        s.minValue() == null ? 0 : s.minValue(),
        s.maxValue() == null ? 100 : s.maxValue(),
        s.unit()
      ))
      .toList();

    // -----------------------------
    // Convert actuators
    // -----------------------------
    List<Actuator> actuators = desc.actuators().stream()
      .map(a -> {

        ActuatorType type;

        try {
          // interpret descriptor.id() as display name of the enum
          type = ActuatorType.fromDisplayName(a.id());
        } catch (IllegalArgumentException e) {
          System.err.println("Unknown actuator name '" + a.id() +
            "'. Falling back to FAN.");
          type = ActuatorType.FAN; // safe fallback
        }

        // If actuator has min + max → range actuator (Slider)
        if (a.minValue() != null && a.maxValue() != null) {
          return new Actuator(
            type,
            a.minValue(),
            a.maxValue()
          );

        } else {
          // Otherwise treat it as a simple state actuator
          return new Actuator(
            type,
            a.value()
          );
        }
      })
      .toList();

    // -----------------------------
    // Build final SensorNode
    // -----------------------------
    boolean supportsImages = desc.supportsImages() != null && desc.supportsImages();
    boolean supportsAggregates = desc.supportsAggregates() != null && desc.supportsAggregates();

    System.out.println(new SensorNode(sensors, actuators, supportsImages, supportsAggregates));

    return new SensorNode(sensors, actuators, supportsImages, supportsAggregates);
  }
}