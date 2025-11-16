package ntnu.idata2302.sfp.sensorNode.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete sensor node device in the Smart Farming Protocol.
 *
 * <p>A sensor node owns collections of {@link Sensor} instances (producers of
 * environmental values) and {@link Actuator} instances (controllable outputs).
 * The node provides methods to advance its internal simulation state
 * ({@link #tick()}), look up actuators by name, and expose its configuration
 * and runtime identifiers to callers.</p>
 *
 * <p>Notes:
 * <ul>
 *   <li>The constructor defensively copies the provided sensor and actuator lists
 *       to avoid external modification of the internal lists.</li>
 *   <li>The getters return references to the internal lists. Callers should
 *       treat these as live collections and avoid modifying them unless that
 *       is intentionally allowed by the application.</li>
 * </ul>
 * </p>
 */
public class SensorNode {

  private final List<Sensor> sensors;
  private final List<Actuator> actuators;
  private int id;
  private final boolean supportsImage;
  private final boolean supportsAggregate;

  /**
   * Creates a new sensor node with a set of sensors and actuators.
   * The lists are copied defensively to avoid external modification.
   *
   * @param sensors   environmental sensors this node provides; must not be {@code null}
   * @param actuators controllable actuators this node exposes; must not be {@code null}
   * @param supportsImage whether this node can capture/send images
   * @param supportsAggregate whether this node supports aggregate reporting
   */
  public SensorNode(List<Sensor> sensors, List<Actuator> actuators, boolean supportsImage,
                    boolean supportsAggregate) {
    this.sensors = new ArrayList<>(sensors);
    this.actuators = new ArrayList<>(actuators);
    this.supportsImage = supportsImage;
    this.supportsAggregate = supportsAggregate;
  }

  /**
   * Advances the internal simulation state by one step.
   *
   * <p>This updates each {@link Sensor} to produce a new reading and causes each
   * {@link Actuator} to move toward its configured target state.</p>
   */
  public void tick() {
    sensors.forEach(Sensor::updateValue);
    actuators.forEach(Actuator::update);
  }

  /**
   * Find an actuator by its display name (case-insensitive).
   *
   * <p>The method compares the provided {@code actuatorName} to each actuator's
   * {@code getType().displayName()} using a case-insensitive match and returns
   * the first match.</p>
   *
   * @param actuatorName the display name of the actuator to find; may be {@code null}
   * @return the first matching {@link Actuator}, or {@code null} if no match is found
   */
  public Actuator findActuator(String actuatorName) {
    return actuators.stream()
        .filter(a -> a.getType().displayName().equalsIgnoreCase(actuatorName))
        .findFirst()
        .orElse(null);
  }

  /**
   * Set the logical identifier for this sensor node.
   *
   * @param id the id to assign to this node
   */
  public void setId(int id) {
    this.id = id;
  }

  /**
   * Get the logical identifier assigned to this node.
   *
   * @return the node id
   */
  public int getId() {
    return id;
  }

  /**
   * Return the internal list of sensors.
   *
   * <p>Note: this is the live internal list previously created by the constructor.
   * Callers should avoid modifying the returned list unless modification of the
   * node's sensor set is intended.</p>
   *
   * @return the list of {@link Sensor} instances owned by this node
   */
  public List<Sensor> getSensors() {
    return sensors;
  }

  /**
   * Whether this node supports image capture/transfer.
   *
   * @return {@code true} if image support is available
   */
  public boolean supportsImage() {
    return supportsImage;
  }

  /**
   * Whether this node supports aggregate reporting mode.
   *
   * @return {@code true} if aggregate reporting is supported
   */
  public boolean supportsAggregate() {
    return supportsAggregate;
  }

  /**
   * Return the internal list of actuators.
   *
   * <p>As with {@link #getSensors()}, this returns the live internal collection;
   * callers should treat it as read-only unless modifying the node's actuators
   * is desired.</p>
   *
   * @return the list of {@link Actuator} instances exposed by this node
   */
  public List<Actuator> getActuators() {
    return actuators;
  }
}