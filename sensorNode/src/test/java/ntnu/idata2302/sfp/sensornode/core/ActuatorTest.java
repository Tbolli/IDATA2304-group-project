package ntnu.idata2302.sfp.sensornode.core;

import ntnu.idata2302.sfp.sensornode.entity.ActuatorType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link Actuator}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Correctly initializes a continuous actuator in the middle of its range.</li>
 *   <li>Correctly initializes a binary actuator with allowed values.</li>
 *   <li>Correctly clamps target values within the configured range.</li>
 *   <li>Correctly updates current value gradually toward the target.</li>
 *   <li>Correctly applies step fraction configuration.</li>
 *   <li>Immediately applies state changes for binary actuators.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Rejects invalid range where maxValue is less than or equal to minValue.</li>
 *   <li>Rejects invalid initial value for binary actuators (not 0.0 or 1.0).</li>
 *   <li>Rejects invalid step fraction values outside the (0, 1] interval.</li>
 * </ul>
 */
public class ActuatorTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a continuous actuator is initialized with correct min, max,
   * unit and midpoint current/target values.
   */
  @Test
  void constructor_continuous_positive() {
    // Arrange
    ActuatorType type = ActuatorType.HEATER;
    double min = 0.0;
    double max = 100.0;

    // Act
    Actuator actuator = new Actuator(type, min, max);

    // Assert
    assertEquals(type, actuator.getType());
    assertEquals(min, actuator.getMinValue());
    assertEquals(max, actuator.getMaxValue());
    assertEquals(type.unit(), actuator.getUnit());

    double expectedMid = min + (max - min) / 2.0;
    assertEquals(expectedMid, actuator.getCurrentValue());
  }

  /**
   * Verifies that a binary actuator is initialized correctly with allowed states 0.0 or 1.0.
   */
  @Test
  void constructor_binary_positive() {
    // Arrange
    ActuatorType type = ActuatorType.DOOR_LOCK;
    double initialState = 1.0;

    // Act
    Actuator actuator = new Actuator(type, initialState);

    // Assert
    assertEquals(type, actuator.getType());
    assertEquals(0.0, actuator.getMinValue());
    assertEquals(1.0, actuator.getMaxValue());
    assertEquals("state", actuator.getUnit());
    assertEquals(initialState, actuator.getCurrentValue());
    assertEquals(initialState, actuator.getTargetValue());
  }

  /**
   * Verifies that act() clamps a continuous target value into the configured range.
   */
  @Test
  void act_clampsTargetWithinRange_positive() {
    // Arrange
    Actuator actuator = new Actuator(ActuatorType.HEATER, 0.0, 100.0);

    // Act
    actuator.act(200.0); // above max

    // Assert
    assertEquals(100.0, actuator.getTargetValue());
  }

  /**
   * Verifies that update() moves the current value gradually towards the target value.
   */
  @Test
  void update_movesTowardsTarget_positive() {
    // Arrange
    Actuator actuator = new Actuator(ActuatorType.HEATER, 0.0, 100.0);
    double initial = actuator.getCurrentValue(); // should be 50.0
    actuator.act(80.0);

    // Act
    actuator.update();

    // Assert
    double updated = actuator.getCurrentValue();
    assertTrue(updated > initial);
    assertTrue(updated < actuator.getTargetValue());
  }

  /**
   * Verifies that setting a valid step fraction updates the stored fraction value.
   */
  @Test
  void setStepFraction_valid_positive() {
    // Arrange
    Actuator actuator = new Actuator(ActuatorType.HEATER, 0.0, 100.0);
    double fraction = 0.5;

    // Act
    actuator.setStepFraction(fraction);

    // Assert
    assertEquals(fraction, actuator.getStepFraction());
  }

  /**
   * Verifies that update() does nothing when current value is already at the target value.
   */
  @Test
  void update_noChangeWhenAtTarget_positive() {
    // Arrange
    Actuator actuator = new Actuator(ActuatorType.HEATER, 0.0, 100.0);
    double current = actuator.getCurrentValue();
    actuator.act(current);

    // Act
    actuator.update();

    // Assert
    assertEquals(current, actuator.getCurrentValue());
  }

  /**
   * Verifies that binary actuators update current and target values immediately in act().
   */
  @Test
  void act_binaryImmediateApply_positive() {
    // Arrange
    Actuator actuator = new Actuator(ActuatorType.DOOR_LOCK, 0.0);

    // Act
    actuator.act(1.0);

    // Assert
    assertEquals(1.0, actuator.getCurrentValue());
    assertEquals(1.0, actuator.getTargetValue());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that constructor rejects invalid range where maxValue <= minValue.
   */
  @Test
  void constructor_invalidRange_negative() {
    // Arrange
    final ActuatorType type = ActuatorType.HEATER;
    final double min = 10.0;
    final double max = 5.0;

    // Act
    Executable executable = new Executable() {
      @Override
      public void execute() {
        new Actuator(type, min, max);
      }
    };

    // Assert
    assertThrows(IllegalArgumentException.class, executable);
  }

  /**
   * Verifies that binary constructor rejects initial value that is not 0.0 or 1.0.
   */
  @Test
  void constructor_invalidBinaryValue_negative() {
    // Arrange
    final ActuatorType type = ActuatorType.DOOR_LOCK;
    final double invalidState = 0.5;

    // Act
    Executable executable = new Executable() {
      @Override
      public void execute() {
        new Actuator(type, invalidState);
      }
    };

    // Assert
    assertThrows(IllegalArgumentException.class, executable);
  }

  /**
   * Verifies that setStepFraction rejects values less than or equal to 0.
   */
  @Test
  void setStepFraction_nonPositive_negative() {
    // Arrange
    final Actuator actuator = new Actuator(ActuatorType.HEATER, 0.0, 100.0);
    final double invalidFraction = 0.0;

    // Act
    Executable executable = new Executable() {
      @Override
      public void execute() {
        actuator.setStepFraction(invalidFraction);
      }
    };

    // Assert
    assertThrows(IllegalArgumentException.class, executable);
  }

  /**
   * Verifies that setStepFraction rejects values greater than 1.
   */
  @Test
  void setStepFraction_aboveOne_negative() {
    // Arrange
    final Actuator actuator = new Actuator(ActuatorType.HEATER, 0.0, 100.0);
    final double invalidFraction = 1.1;

    // Act
    Executable executable = new Executable() {
      @Override
      public void execute() {
        actuator.setStepFraction(invalidFraction);
      }
    };

    // Assert
    assertThrows(IllegalArgumentException.class, executable);
  }
}
