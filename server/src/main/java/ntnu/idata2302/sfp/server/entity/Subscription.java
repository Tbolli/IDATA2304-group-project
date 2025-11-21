package ntnu.idata2302.sfp.server.entity;

import java.util.Objects;

/**
 * Represents a subscription between a control-panel node and a sensor node.
 * A Subscription indicates that a given control-panel (cpId) is subscribed
 * to updates from a specific sensor node (snId).
 */

public class Subscription {

  private int cpId; // Control-panel node ID
  private int snId; // Sensor node ID

  /**
   * Creates an empty Subscription
   * with no assigned control-panel ID or sensor-node ID.
   */

  public Subscription() {}

  /**
   * Creates a Subscription linking a specific control-panel node to a specific sensor node.
   *
   * @param cpId the ID of the control-panel node
   * @param snId the ID of the sensor node being subscribed to
   */

  public Subscription(int cpId, int snId) {
    this.cpId = cpId;
    this.snId = snId;
  }

  /**
   * Returns the ID of the control-panel node.
   *
   * @return the cpId value
   */

  public int getCpId() {
    return cpId;
  }

  /**
   * Sets the ID of the control-panel node.
   *
   * @param cpId the new control-panel ID
   */

  public void setCpId(int cpId) {
    this.cpId = cpId;
  }

  /**
   * Returns the ID of the sensor node.
   *
   * @return the snId value
   */

  public int getSnId() {
    return snId;
  }

  public void setSnId(int snId) {
    this.snId = snId;
  }

  /**
   * Compares this Subscription with another
   * object for equality.
   * Two subscriptions are equal if they have the
   * same control-panel ID
   * and sensor-node ID.
   *
   * @param o the object to compare with
   * @return true if both objects represent the same subscription, false otherwise
   */

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Subscription that = (Subscription) o;
    return cpId == that.cpId && snId == that.snId;
  }

  /**
   * Generates a hash code consistent with equals(),
   * based on cpId and snId.
   *
   * @return the computed hash code
   */

  @Override
  public int hashCode() {
    return Objects.hash(cpId, snId);
  }

  /**
   * Returns a string representation of this Subscription,
   * including cpId and snId.
   *
   * @return a formatted Subscription string
   */

  @Override
  public String toString() {
    return "Subscription{"
      + "cpId=" + cpId
      + ", snId=" + snId
      + '}';
  }
}
