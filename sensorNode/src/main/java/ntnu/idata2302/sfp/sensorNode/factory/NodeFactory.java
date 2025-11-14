package ntnu.idata2302.sfp.sensorNode.factory;

import ntnu.idata2302.sfp.sensorNode.core.Actuator;
import ntnu.idata2302.sfp.sensorNode.core.Sensor;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.entity.ActuatorType;

import java.util.List;

/**
 * Factory class for constructing preconfigured {@link SensorNode} instances.
 * <p>
 * A sensor node typically represents one physical device in the Smart Farming
 * Protocol ecosystem. This factory provides default loadouts of sensors and
 * actuators that mimic real greenhouse hardware.
 * </p>
 *
 * Usage:
 * <pre>
 *     SensorNode node = NodeFactory.defaultNode();
 * </pre>
 */
public final class NodeFactory {

  private NodeFactory() {} // prevent instantiation

  /**
   * Creates a fully configured default {@link SensorNode} including:
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
   *
   * @return a complete sensor node ready for simulation + networking
   */
  public static SensorNode defaultNode() {
    List<Sensor> sensors = List.of(
      new Sensor("Temperature", 10, 40, "°C"),
      new Sensor("Humidity", 20, 100, "%"),
      new Sensor("CO2", 300, 3000, "ppm"),
      new Sensor("Light", 0, 100_000, "lux"),
      new Sensor("SoilMoisture", 0, 100, "%")
    );

    List<Actuator> actuators = List.of(
      new Actuator(ActuatorType.FAN, 0, 100),       // % power
      new Actuator(ActuatorType.HEATER, 0, 100),    // % power
      new Actuator(ActuatorType.LIGHT, 0, 100)      // % brightness
    );

    return new SensorNode(sensors, actuators, false, false);
  }
}
