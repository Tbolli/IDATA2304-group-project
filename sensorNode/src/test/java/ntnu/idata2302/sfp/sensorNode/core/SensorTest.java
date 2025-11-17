package ntnu.idata2302.sfp.sensorNode.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link Sensor}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Ranged constructor initializes midpoint and range correctly.</li>
 *   <li>Unranged constructor initializes with null bounds and literal value.</li>
 *   <li>updateValue() for ranged sensor keeps values within the configured bounds.</li>
 *   <li>updateValue() for unranged sensor eventually changes the value.</li>
 *   <li>toString() includes the sensor name and unit.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Ranged constructor rejects invalid range where maxValue is less than or equal to minValue.</li>
 * </ul>
 */
public class SensorTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that the ranged constructor initializes name, unit, bounds
   * and sets the initial value to the midpoint of the range.
   */
  @Test
  void constructor_ranged_positive() {
    // Arrange
    String name = "Temperature";
    double min = -10.0;
    double max = 30.0;
    String unit = "°C";

    // Act
    Sensor sensor = new Sensor(name, min, max, unit);

    // Assert
    assertEquals(name, sensor.getName());
    assertEquals(unit, sensor.getUnit());
    assertEquals(min, sensor.getMinValue());
    assertEquals(max, sensor.getMaxValue());

    double expectedMid = (min + max) / 2.0;
    assertEquals(expectedMid, sensor.getValue());
  }

  /**
   * Verifies that the unranged constructor stores the literal value
   * and leaves min and max bounds as null.
   */
  @Test
  void constructor_unranged_positive() {
    // Arrange
    String name = "CO2";
    double initialValue = 500.0;
    String unit = "ppm";

    // Act
    Sensor sensor = new Sensor(name, initialValue, unit);

    // Assert
    assertEquals(name, sensor.getName());
    assertEquals(unit, sensor.getUnit());
    assertNull(sensor.getMinValue());
    assertNull(sensor.getMaxValue());
    assertEquals(initialValue, sensor.getValue());
  }

  /**
   * Verifies that repeated updateValue() calls for a ranged sensor
   * always keep the value within the configured bounds.
   */
  @Test
  void updateValue_rangedWithinBounds_positive() {
    // Arrange
    double min = 0.0;
    double max = 100.0;
    Sensor sensor = new Sensor("Humidity", min, max, "%");

    // Act
    for (int i = 0; i < 100; i++) {
      sensor.updateValue();
      double value = sensor.getValue();

      // Assert
      assertTrue(value >= min);
      assertTrue(value <= max);
    }
  }

  /**
   * Verifies that updateValue() for an unranged sensor eventually changes the value.
   */
  @Test
  void updateValue_unrangedChangesValue_positive() {
    // Arrange
    Sensor sensor = new Sensor("Pressure", 1013.0, "hPa");
    double initialValue = sensor.getValue();
    boolean changed = false;

    // Act
    for (int i = 0; i < 50; i++) {
      sensor.updateValue();
      if (sensor.getValue() != initialValue) {
        changed = true;
        break;
      }
    }

    // Assert
    assertTrue(changed);
  }

  /**
   * Verifies that toString() contains the sensor name and unit.
   */
  @Test
  void toString_containsNameAndUnit_positive() {
    // Arrange
    String name = "Light";
    String unit = "lux";
    Sensor sensor = new Sensor(name, 200.0, unit);

    // Act
    String text = sensor.toString();

    // Assert
    assertNotNull(text);
    assertTrue(text.contains(name));
    assertTrue(text.contains(unit));
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that the ranged constructor rejects an invalid range
   * where maxValue is less than or equal to minValue.
   */
  @Test
  void constructor_invalidRange_negative() {
    // Arrange
    final String name = "Temperature";
    final double min = 10.0;
    final double max = 5.0;
    final String unit = "°C";

    // Act
    Executable executable = new Executable() {
      @Override
      public void execute() {
        new Sensor(name, min, max, unit);
      }
    };

    // Assert
    assertThrows(IllegalArgumentException.class, executable);
  }
}
