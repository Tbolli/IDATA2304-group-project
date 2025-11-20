package ntnu.idata2302.sfp.controlpanel.gui.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;

/**
 * Mutable, observable node entry used by the ListView. The data property
 * is an {@link ObjectProperty} so cells can listen for updates and refresh
 * themselves without replacing the item object.
 */
public class NodeEntry {

  private final int nodeId;
  private final ObjectProperty<DataReportBody> data = new SimpleObjectProperty<>();

  /**
   * Creates a new NodeEntry with a given node ID and initial data.
   *
   * @param nodeId the identifier of the sensor node
   * @param data   the initial data report associated with this node
   */

  public NodeEntry(int nodeId, DataReportBody data) {
    this.nodeId = nodeId;
    this.data.set(data);
  }

  /**
   * Returns the node ID associated with this entry.
   *
   * @return the sensor node identifier
   */

  public int nodeId() {
    return nodeId;
  }

  /**
   * Returns the current {@link DataReportBody} stored in this entry.
   *
   * @return the latest data report, or {@code null} if none is set
   */

  public DataReportBody getData() {
    return data.get();
  }

  /**
   * Updates the data report for this node.
   *
   * @param d the new {@link DataReportBody} to set
   */

  public void setData(DataReportBody d) {
    this.data.set(d);
  }

  /**
   * Returns the observable JavaFX property for the node's data.
   *
   * @return the data property
   */

  public ObjectProperty<DataReportBody> dataProperty() {
    return data;
  }

  @Override
  public String toString() {
    return "NodeEntry{" + "nodeId=" + nodeId + ", data=" + data.get() + '}';
  }
}
