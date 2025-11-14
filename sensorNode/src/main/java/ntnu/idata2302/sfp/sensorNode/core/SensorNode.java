package ntnu.idata2302.sfp.sensorNode.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete sensor node device in the Smart Farming Protocol.
 * A sensor node owns a collection of {@link Sensor} instances (producers of
 * environmental values) and {@link Actuator} instances (controllable outputs).
 * <p>
 * The node provides methods to update its sensors, update actuators toward
 * targets, and expose their current state to whoever requests it (e.g., server).
 * This class does not handle networking directly—only local simulation logic.
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
   * @param sensors   environmental sensors this node provides
   * @param actuators controllable actuators this node exposes
   */
  public SensorNode(List<Sensor> sensors, List<Actuator> actuators, boolean supportsImage, boolean supportsAggregate) {
    this.sensors = new ArrayList<>(sensors);
    this.actuators = new ArrayList<>(actuators);
    this.supportsImage = supportsImage;
    this.supportsAggregate = supportsAggregate;
  }

  /**
   * Updates internal simulation state.
   * - Sensors generate new dynamic readings
   * - Actuators move toward their target values
   */
  public void tick() {
    sensors.forEach(Sensor::updateValue);
    actuators.forEach(Actuator::update);
  }


  public void setId(int id){
    this.id = id;
  }

  public int getId(){
    return id;
  }

  public List<Sensor> getSensors() {
    return sensors;
  }

  public boolean supportsImage() {
    return supportsImage;
  }

  public boolean supportsAggregate() {
    return supportsAggregate;
  }

  public List<Actuator> getActuators() {
    return actuators;
  }
}
