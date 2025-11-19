package ntnu.idata2302.sfp.sensorNode.entity;


/**
 * This class defines various types of actuators used in a smart greenhouse.
 *
 * <p>Each actuator type specifies a display name and its default unit
 * of measurement for clarity when visualizing or reporting actuator states.
 * </p>
 *
 * <p>This enumeration helps standardize how actuators are identified and
 * referenced throughout the app.</p>
 *
 * <p>Examples:
 * <ul>
 *  <li>{@link #FAN} - circulates air to lower temperature or humidity</li>
 *  <li>{@link #HEATER} - increases the air temperature</li>
 *  <li>{@link #COOLER} - decreases the air temperature</li>
 *  <li>{@link #WINDOW} - opens or closes greenhouse windows for ventilation</li>
 *  <li>{@link #VENT} - controls vents to regulate airflow</li>
 *  <li>{@link #SHADE_SCREEN} - adjusts shading to control sunlight exposure</li>
 *  <li>{@link #LIGHT} - provides artificial lighting to plants</li>
 *  <li>{@link #SPRINKLER} - waters plants automatically</li>
 *  <li>{@link #VALVE} - controls water flow to irrigation systems</li>
 *  <li>{@link #HUMIDIFIER} - adds moisture to the air</li>
 *  <li>{@link #DEHUMIDIFIER} - removes excess moisture from the air</li>
 *  <li>{@link #CO2_INJECTOR} - releases CO₂ to stimulate plant growth</li>
 * </ul>
 * </p>
 *
 * @author Nathaniel Don Ilarde
 * @version 28.10.2025
 */


public enum ActuatorType {

  // Temperature
  FAN("Fan", "%"),
  HEATER("Heater", "°C"),
  COOLER("Cooler", "°C"),

  // Air and ventilation
  WINDOW("Window", "%"),
  VENT("Vent", "%"),

  // Lighting
  SHADE_SCREEN("Shade Screen", "%"),
  LIGHT("Light", "state"),

  // Watering
  SPRINKLER("Sprinkler", "%"),
  VALVE("Valve", "%"),

  // Gas and humidity
  HUMIDIFIER("Humidifier", "%"),
  DEHUMIDIFIER("Dehumidifier", "%"),
  CO2_INJECTOR("CO2 Injector", "ppm"),

  // Security
  DOOR_LOCK("Door Lock", "state"),
  ALARM("Alarm", "state");


  private final String displayName;
  private final String unit;

  ActuatorType(String displayName, String unit) {
    this.displayName = displayName;
    this.unit = unit;
  }

  /**
   * Returns the display name of the actuator type.
   *
   * @return the display name
   */

  public String displayName() {
    return displayName;
  }


  /**
   * Returns the default unit of measurement for the actuator type.
   *
   * @return the unit of measurement
   */

  public String unit() {
    return unit;
  }

  /**
   * Parse an {@link ActuatorType} from its display name.
   *
   * <p>The comparison is case-insensitive and trims surrounding whitespace.
   * Example inputs: {@code "Fan"}, {@code "Shade Screen"}, {@code "CO2 Injector"}.</p>
   *
   * @param displayName the human-readable display name to parse; must not be {@code null}
   * @return the matching {@link ActuatorType}
   * @throws IllegalArgumentException if
   *                                  {@code displayName} is {@code null}
   *                                  or does not match any actuator type
   */
  public static ActuatorType fromDisplayName(String displayName) {
    if (displayName == null) {
      throw new IllegalArgumentException("Display name cannot be null");
    }

    for (ActuatorType t : values()) {
      if (t.displayName.equalsIgnoreCase(displayName.trim())) {
        return t;
      }
    }

    throw new IllegalArgumentException("Unknown actuator display name: " + displayName);
  }
}
