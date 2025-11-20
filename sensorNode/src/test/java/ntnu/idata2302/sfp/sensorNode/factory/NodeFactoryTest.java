package ntnu.idata2302.sfp.sensorNode.factory;

import ntnu.idata2302.sfp.sensorNode.core.Actuator;
import ntnu.idata2302.sfp.sensorNode.core.Sensor;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.entity.ActuatorType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link NodeFactory}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>defaultNode() creates a non-null SensorNode.</li>
 *   <li>defaultNode() creates exactly 5 sensors.</li>
 *   <li>defaultNode() creates exactly 3 actuators.</li>
 *   <li>All expected sensor types are present with correct configuration.</li>
 *   <li>All expected actuator types are present with correct configuration.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>defaultNode() does not return null lists of sensors or actuators.</li>
 *   <li>defaultNode() creates new lists on each call (no shared references).</li>
 * </ul>
 */
public class NodeFactoryTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that defaultNode() returns a non-null SensorNode.
   */
  @Test
  void defaultNode_notNull_positive() {
    // Arrange / Act
    SensorNode node = NodeFactory.defaultNode();

    // Assert
    assertNotNull(node);
  }

  /**
   * Verifies that defaultNode() constructs exactly 5 sensors.
   */
  @Test
  void defaultNode_sensorCount_positive() {
    // Arrange / Act
    SensorNode node = NodeFactory.defaultNode();
    List<Sensor> sensors = node.getSensors();

    // Assert
    assertEquals(5, sensors.size());
  }

  /**
   * Verifies that defaultNode() constructs exactly 3 actuators.
   */
  @Test
  void defaultNode_actuatorCount_positive() {
    // Arrange / Act
    SensorNode node = NodeFactory.defaultNode();
    List<Actuator> actuators = node.getActuators();

    // Assert
    assertEquals(3, actuators.size());
  }

  /**
   * Verifies that all required sensors are present with correct configuration.
   */
  @Test
  void defaultNode_expectedSensors_positive() {
    // Arrange / Act
    SensorNode node = NodeFactory.defaultNode();
    List<Sensor> sensors = node.getSensors();

    // Assert
    assertTrue(sensors.stream().anyMatch(s -> s.getName().equals("Temperature")
      && s.getMinValue() == 10.0 && s.getMaxValue() == 40.0));

    assertTrue(sensors.stream().anyMatch(s -> s.getName().equals("Humidity")
      && s.getMinValue() == 20.0 && s.getMaxValue() == 100.0));

    assertTrue(sensors.stream().anyMatch(s -> s.getName().equals("CO2")
      && s.getMinValue() == 300.0 && s.getMaxValue() == 3000.0));

    assertTrue(sensors.stream().anyMatch(s -> s.getName().equals("SoilMoisture")
      && s.getMinValue() == 0.0 && s.getMaxValue() == 100.0));
  }

  /**
   * Verifies that all expected actuator types are present with correct configuration.
   */
  @Test
  void defaultNode_expectedActuators_positive() {
    // Arrange / Act
    SensorNode node = NodeFactory.defaultNode();
    List<Actuator> actuators = node.getActuators();

    // Assert
    assertTrue(actuators.stream().anyMatch(a -> a.getType() == ActuatorType.FAN));
    assertTrue(actuators.stream().anyMatch(a -> a.getType() == ActuatorType.HEATER));
    assertTrue(actuators.stream().anyMatch(a -> a.getType() == ActuatorType.LIGHT));
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that defaultNode() does not return null sensor or actuator lists.
   */
  @Test
  void defaultNode_listsNotNull_negative() {
    // Arrange / Act
    SensorNode node = NodeFactory.defaultNode();

    // Assert
    assertNotNull(node.getSensors());
    assertNotNull(node.getActuators());
  }

  /**
   * Verifies that each call to defaultNode() produces new list instances
   * (no shared collections between nodes).
   */
  @Test
  void defaultNode_newInstancesEachCall_negative() {
    // Arrange / Act
    SensorNode node1 = NodeFactory.defaultNode();
    SensorNode node2 = NodeFactory.defaultNode();

    // Assert
    assertTrue(node1.getSensors() != node2.getSensors());
    assertTrue(node1.getActuators() != node2.getActuators());
  }
}
