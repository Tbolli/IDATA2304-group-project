package ntnu.idata2302.sfp.controlPanel.gui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Represents a node (sensor/actuator) entry shown in the UI.
 *
 * <p>This class exposes JavaFX {@link StringProperty} fields for use with
 * table bindings and other UI bindings. It stores identifying information
 * such as id and IP as well as runtime fields like status, sensor type, and
 * auxiliary info.</p>
 *
 * @since 1.0
 */
public class NodeItem {
  private final StringProperty id = new SimpleStringProperty();
  private final StringProperty ip = new SimpleStringProperty();
  private final StringProperty status = new SimpleStringProperty();
  private final StringProperty sensorType = new SimpleStringProperty();
  private final StringProperty info = new SimpleStringProperty();

  /**
   * Create a NodeItem with the given values.
   *
   * @param id         unique identifier for the node (displayed in the table)
   * @param ip         IP address of the node
   * @param status     textual status (e.g. "Online", "Offline")
   * @param sensorType short description of the sensor type (e.g. "Temperature")
   * @param info       auxiliary information (e.g. "Last seen 2 s")
   */
  public NodeItem(String id, String ip, String status, String sensorType, String info) {
    this.id.set(id);
    this.ip.set(ip);
    this.status.set(status);
    this.sensorType.set(sensorType);
    this.info.set(info);
  }

  /**
   * Returns the observable id property for bindings.
   *
   * @return id property
   */
  public StringProperty idProperty() {
    return id;
  }

  /**
   * Returns the observable ip property for bindings.
   *
   * @return ip property
   */
  public StringProperty ipProperty() {
    return ip;
  }

  /**
   * Returns the observable status property for bindings.
   *
   * @return status property
   */
  public StringProperty statusProperty() {
    return status;
  }

  /**
   * Returns the observable sensorType property for bindings.
   *
   * @return sensorType property
   */
  public StringProperty sensorTypeProperty() {
    return sensorType;
  }

  /**
   * Returns the observable info property for bindings.
   *
   * @return info property
   */
  public StringProperty infoProperty() {
    return info;
  }

  /**
   * Gets the node id value.
   *
   * @return id string, may be null
   */
  public String getId() {
    return id.get();
  }

  /**
   * Gets the node ip value.
   *
   * @return ip string, may be null
   */
  public String getIp() {
    return ip.get();
  }


  /**
   * Sets the node status.
   *
   * @param status new status string
   */
  public void setStatus(String status) {
    this.status.set(status);
  }

  /**
   * Sets the sensor type.
   *
   * @param sensorType new sensor type string
   */
  public void setSensorType(String sensorType) {
    this.sensorType.set(sensorType);
  }

  /**
   * Sets the auxiliary info.
   *
   * @param info new info string
   */
  public void setInfo(String info) {
    this.info.set(info);
  }

}