package ntnu.idata2302.sfp.sensorNode.core;

/**
 * Represents a dynamic sensor that produces smoothly changing values.
 * The sensor can optionally define a valid numeric range. If a range is
 * provided, values are clamped; otherwise, updates scale based on magnitude.
 *
 * Updates use lightweight pseudo-noise and a slow drift component to create
 * realistic environmental fluctuations.
 */
public class Sensor {

  /**
   * Internal offset used to generate smooth pseudo-noise. Incremented slightly
   * on each update so values change gradually.
   */
  private double noiseOffset = Math.random() * 1000;

  /**
   * A slowly changing bias that adds long-term trending to the value.
   * Drift stays bounded to prevent runaway values.
   */
  private double drift = 0.0;

  private final String name;
  private final Double minValue;
  private final Double maxValue;
  private final String unit;

  private double currentValue;

  /**
   * Constructs a ranged sensor. The initial value is placed at the midpoint
   * for a realistic starting point, avoiding extremes or unnatural jumps.
   *
   * @param name     sensor identifier
   * @param minValue lower bound (inclusive)
   * @param maxValue upper bound (inclusive)
   * @param unit     unit of measurement
   */
  public Sensor(String name, double minValue, double maxValue, String unit) {
    if (maxValue <= minValue)
      throw new IllegalArgumentException("maxValue must be > minValue");

    this.name = name;
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.unit = unit;
    this.currentValue = (minValue + maxValue) / 2.0;
  }

  /**
   * Constructs an unranged sensor. The starting value is taken literally,
   * and update behavior scales with the magnitude of the current value.
   *
   * @param name         sensor identifier
   * @param currentValue initial sensor value
   * @param unit         unit of measurement
   */
  public Sensor(String name, double currentValue, String unit) {
    this.name = name;
    this.minValue = null;
    this.maxValue = null;
    this.unit = unit;
    this.currentValue = currentValue;
  }

  /**
   * Sets the sensor to a new value. If the sensor has a defined range,
   * the value is automatically clamped to remain valid.
   *
   * @param newValue desired reading
   */
  private void setValue(double newValue) {
    if (minValue == null || maxValue == null) {
      currentValue = newValue;
      return;
    }
    currentValue = Math.max(minValue, Math.min(newValue, maxValue));
  }

  /**
   * Evolves the sensor value based on smooth pseudo-noise and slow drift.
   * - Noise produces natural short-term movement.
   * - Drift adds long-term trending.
   * - Step size adapts to either the defined range or magnitude.
   */
  public void updateValue() {

    // Smooth noise progression for natural changes
    noiseOffset += 0.05;
    double smoothNoise =
      Math.sin(noiseOffset) * 0.5 +
        Math.sin(noiseOffset * 0.37) * 0.2;

    // Determine update step based on range or magnitude
    double step;
    if (minValue != null && maxValue != null) {
      step = (maxValue - minValue) * 0.02;
    } else {
      double magnitude = Math.max(1.0, Math.abs(currentValue));
      step = magnitude * 0.04;
    }

    // Slight drifting behavior over time
    drift += (Math.random() - 0.5) * 0.005;
    drift = Math.max(-0.1, Math.min(0.1, drift));

    // Compute the updated reading
    double newValue = currentValue
      + smoothNoise * step
      + drift * step * 2;

    setValue(newValue);
  }

  public double getValue() { return currentValue; }
  public String getName() { return name; }
  public String getUnit() { return unit; }
  public Double getMinValue() { return minValue; }
  public Double getMaxValue() { return maxValue; }

  @Override
  public String toString() {
    return name + ": " + currentValue + " " + unit;
  }
}
