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

  public NodeEntry(int nodeId, DataReportBody data) {
    this.nodeId = nodeId;
    this.data.set(data);
  }

  public int nodeId() {
    return nodeId;
  }

  public DataReportBody getData() {
    return data.get();
  }

  public void setData(DataReportBody d) {
    this.data.set(d);
  }

  public ObjectProperty<DataReportBody> dataProperty() {
    return data;
  }

  @Override
  public String toString() {
    return "NodeEntry{" + "nodeId=" + nodeId + ", data=" + data.get() + '}';
  }
}
