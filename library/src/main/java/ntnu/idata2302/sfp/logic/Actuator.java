package ntnu.idata2302.sfp.logic;

import ntnu.idata2302.sfp.entity.ActuatorType;



/**
 * This class represents a controllable actuator
 * that can adjust a physical or simulated value
 * within a defined range. An actuator has a current value,
 * a target value, and moves
 * gradually toward its target when updated.
 *
 * <p>This class supports three key operations:</p>
 * <ul>
 *   <li>{@link #act(double)} — sets a new target value (e.g. "set fan to 80%").</li>
 *   <li>{@link #update()} — simulates the actuator gradually moving toward its target.</li>
 *   <li>{@link #setStepFraction(double)} — adjusts how quickly the actuator reacts to changes.</li>
 * </ul>
 *
 * @author Nathaniel Don Ilarde
 * @version 30.10.2025
 *
 */


public class Actuator {
  private final ActuatorType type; // the kind of actuator
  private final double minValue; // the minimum allowed value
  private final double maxValue; // the maximum allowed value
  private final String unit; // the unit of measurement

  private double currentValue; // the current value of the actuator
  private double targetValue; // the desired target value
  private double stepFraction = 0.05; // 5% of range per update()

  /**
   * Constructs a new {@code Actuator} instance of the specified type with a defined
   * operating range.
   *
   * <p>The actuator starts at the midpoint between the minimum and maximum values,
   * and its initial target value is set to this same midpoint. The {@link ActuatorType}
   * determines the actuator’s purpose and default unit of measurement.</p>
   *
   * @param minValue  the minimum allowed value that this actuator can reach
   * @param maxValue  the maximum allowed value that this actuator can reach
   * @throws IllegalArgumentException if {@code maxValue} is less than or equal to {@code minValue}
   *
   */

  public Actuator(ActuatorType type, double minValue, double maxValue) {
    if (maxValue <= minValue) {
      throw new IllegalArgumentException("maxValue must be > minValue");
    }
    this.type = type;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.unit = type.unit();
    this.currentValue = minValue + (maxValue - minValue) / 2.0; // start in middle
    this.targetValue = currentValue;
  }

  // --------------- Setters ---------------

  /**
   * Sets a new target value for this actuator.
   *
   * <p>The value is automatically clamped
   * so it stays within the minimum and maximum range.</p>
   *
   * @param value the desired target value
   */

  public void act(double value) {
    this.targetValue = clamp(value, minValue, maxValue);
  }

  /**
   * Sets how quickly the actuator moves toward its target value.
   *
   * <p>A higher fraction means faster response.
   * Must be greater than 0 and less than or equal to 1.
   * </p>
   *
   * @param fraction the step fraction per update (0 < fraction ≤ 1)
   * @throws IllegalArgumentException if the fraction is outside the valid range
   */
  public void setStepFraction(double fraction) {
    if (fraction <= 0 || fraction > 1) {
      throw new IllegalArgumentException("stepFraction in (0,1]");
    }
    this.stepFraction = fraction;
  }

  // --------------- Getters ---------------

  /**
   * Returns the type of this actuator.
   *
   * @return the actuator type
   */

  public ActuatorType getType() {
    return type;
  }


  /**
   * Returns the unit of measurement used by this actuator.
   *
   * @return the unit as a string
   */

  public String getUnit() {
    return unit;
  }


  /**
   * Returns the minimum value this actuator can reach.
   *
   * @return the minimum value
   */

  public double getMinValue() {
    return minValue;
  }


  /**
   * Returns the maximum value this actuator can reach.
   *
   * @return the maximum value
   */

  public double getMaxValue() {
    return maxValue;
  }


  /**
   * Returns the current value of this actuator.
   *
   * @return the current value
   */

  public double getCurrentValue() {
    return currentValue;
  }


  /**
   * Returns the target value the actuator is moving toward.
   *
   * @return the target value
   */

  public double getTargetValue() {
    return targetValue;
  }


  /**
   * Returns the fraction that controls how quickly the actuator moves toward its target.
   *
   * @return the step fraction
   */

  public double getStepFraction() {
    return stepFraction;
  }


  /**
   * Clamps a value so that it stays within the given range.
   *
   * @param v   the value to clamp
   * @param min the minimum allowed value
   * @param max the maximum allowed value
   * @return the clamped value
   */

  private static double clamp(double v, double min, double max) {
    return (v < min) ? min : (v > max) ? max : v;
  }



  // ---------- Behavior ----------

  /**
   * Updates the actuator’s current value, moving it closer to the target value.
   *
   * <p>Each call moves the value by a fraction of the total range defined by
   * {@link #setStepFraction(double)}. If the actuator is close enough to its target,
   * it snaps directly to the target value.
   * </p>
   */

  public void update() {
    double diff = targetValue - currentValue;
    if (Math.abs(diff) < 1e-9) {
      return;
    }
    double step = (maxValue - minValue) * stepFraction;
    currentValue = (Math.abs(diff) <= step)
      ? targetValue : currentValue + Math.copySign(step, diff);
  }
}


