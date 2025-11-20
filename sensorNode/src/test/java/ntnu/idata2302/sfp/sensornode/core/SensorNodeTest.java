package ntnu.idata2302.sfp.sensornode.core;

import ntnu.idata2302.sfp.sensornode.entity.ActuatorType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link SensorNode}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Constructor copies sensor and actuator lists defensively.</li>
 *   <li>tick() updates sensor values and moves actuators toward targets.</li>
 *   <li>findActuator() locates actuators by display name (case-insensitive).</li>
 *   <li>supportsImage() and supportsAggregate() reflect constructor flags.</li>
 *   <li>setId() and getId() correctly store and return the node identifier.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Constructor is unaffected by later modifications to the original lists.</li>
 *   <li>findActuator() returns null when no matching actuator is found.</li>
 *   <li>tick() works correctly when there are no sensors or actuators.</li>
 * </ul>
 */
public class SensorNodeTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that the constructor defensively copies the provided sensor and actuator lists.
   */
  @Test
  void constructor_defensiveCopy_positive() {
    // Arrange
    Sensor sensor = new Sensor("Temp", 0.0, 100.0, "°C");
    Actuator actuator = new Actuator(ActuatorType.FAN, 0.0, 100.0);

    List<Sensor> sensorList = new ArrayList<>();
    sensorList.add(sensor);

    List<Actuator> actuatorList = new ArrayList<>();
    actuatorList.add(actuator);

    // Act
    SensorNode node = new SensorNode(sensorList, actuatorList, true, false);

    // Mutate original lists after construction
    sensorList.add(new Sensor("Humidity", 0.0, 100.0, "%"));
    actuatorList.add(new Actuator(ActuatorType.HEATER, 0.0, 100.0));

    // Assert
    assertNotSame(sensorList, node.getSensors());
    assertNotSame(actuatorList, node.getActuators());
    assertEquals(1, node.getSensors().size());
    assertEquals(1, node.getActuators().size());
    assertSame(sensor, node.getSensors().get(0));
    assertSame(actuator, node.getActuators().get(0));
  }

  /**
   * Verifies that tick() updates sensors and actuators toward their new states.
   */
  @Test
  void tick_updatesSensorsAndActuators_positive() {
    // Arrange
    Sensor sensor = new Sensor("Temp", 0.0, 100.0, "°C");
    Actuator actuator = new Actuator(ActuatorType.HEATER, 0.0, 100.0);

    List<Sensor> sensors = new ArrayList<>();
    sensors.add(sensor);

    List<Actuator> actuators = new ArrayList<>();
    actuators.add(actuator);

    SensorNode node = new SensorNode(sensors, actuators, false, false);

    double initialSensorValue = sensor.getValue();
    double initialActuatorValue = actuator.getCurrentValue();

    // Move actuator target higher than current value
    actuator.act(80.0);

    // Act
    node.tick();

    // Assert
    // Sensor should have changed its reading
    assertNotEquals(initialSensorValue, sensor.getValue());

    // Actuator should have moved towards target, but not yet reached it
    double updatedActuatorValue = actuator.getCurrentValue();
    assertTrue(updatedActuatorValue > initialActuatorValue);
    assertTrue(updatedActuatorValue < actuator.getTargetValue());
  }

  /**
   * Verifies that findActuator() locates an actuator by its display name, ignoring case.
   */
  @Test
  void findActuator_byDisplayName_positive() {
    // Arrange
    Actuator fan = new Actuator(ActuatorType.FAN, 0.0, 100.0);
    Actuator heater = new Actuator(ActuatorType.HEATER, 0.0, 100.0);

    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    actuators.add(fan);
    actuators.add(heater);

    SensorNode node = new SensorNode(sensors, actuators, false, false);

    // Act
    Actuator resultLowerCase = node.findActuator("fan");
    Actuator resultExact = node.findActuator("Fan");

    // Assert
    assertNotNull(resultLowerCase);
    assertSame(fan, resultLowerCase);
    assertNotNull(resultExact);
    assertSame(fan, resultExact);
  }

  /**
   * Verifies that supportsImage() and supportsAggregate() reflect the constructor parameters.
   */
  @Test
  void supportsFlags_positive() {
    // Arrange
    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();

    // Act
    SensorNode node = new SensorNode(sensors, actuators, true, false);

    // Assert
    assertTrue(node.supportsImage());
    assertEquals(false, node.supportsAggregate());
  }

  /**
   * Verifies that setId() and getId() correctly store and return the node identifier.
   */
  @Test
  void idSetAndGet_positive() {
    // Arrange
    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    SensorNode node = new SensorNode(sensors, actuators, false, false);
    int expectedId = 42;

    // Act
    node.setId(expectedId);

    // Assert
    assertEquals(expectedId, node.getId());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that later changes to the original lists do not affect the node's internal lists.
   */
  @Test
  void constructor_originalListMutation_negative() {
    // Arrange
    Sensor sensor = new Sensor("Temp", 0.0, 100.0, "°C");
    Actuator actuator = new Actuator(ActuatorType.FAN, 0.0, 100.0);

    List<Sensor> sensorList = new ArrayList<>();
    sensorList.add(sensor);

    List<Actuator> actuatorList = new ArrayList<>();
    actuatorList.add(actuator);

    SensorNode node = new SensorNode(sensorList, actuatorList, false, false);

    // Act
    sensorList.clear();
    actuatorList.clear();

    // Assert
    assertEquals(1, node.getSensors().size());
    assertEquals(1, node.getActuators().size());
  }

  /**
   * Verifies that findActuator() returns null when an actuator with the given name does not exist.
   */
  @Test
  void findActuator_unknownName_negative() {
    // Arrange
    Actuator fan = new Actuator(ActuatorType.FAN, 0.0, 100.0);
    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    actuators.add(fan);

    SensorNode node = new SensorNode(sensors, actuators, false, false);

    // Act
    Actuator result = node.findActuator("UnknownActuator");

    // Assert
    assertNull(result);
  }

  /**
   * Verifies that tick() works correctly when there are no sensors or actuators.
   */
  @Test
  void tick_emptyCollections_negative() {
    // Arrange
    List<Sensor> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    SensorNode node = new SensorNode(sensors, actuators, false, false);

    // Act
    node.tick();

    // Assert
    assertEquals(0, node.getSensors().size());
    assertEquals(0, node.getActuators().size());
  }
}
