package ntnu.idata2302.sfp.server.entity;

import java.util.Objects;

public class Subscription {

  private int cpId;
  private int snId;

  public Subscription() {}

  public Subscription(int cpId, int snId) {
    this.cpId = cpId;
    this.snId = snId;
  }

  public int getCpId() {
    return cpId;
  }

  public void setCpId(int cpId) {
    this.cpId = cpId;
  }

  public int getSnId() {
    return snId;
  }

  public void setSnId(int snId) {
    this.snId = snId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Subscription that = (Subscription) o;
    return cpId == that.cpId && snId == that.snId;
  }

  @Override
  public int hashCode() {
    return Objects.hash(cpId, snId);
  }

  @Override
  public String toString() {
    return "Subscription{" +
      "cpId=" + cpId +
      ", snId=" + snId +
      '}';
  }
}
