package ntnu.idata2302.sfp.controlPanel.gui.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class NodeItem {
    private final StringProperty id = new SimpleStringProperty();
    private final StringProperty ip = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();
    private final StringProperty sensorType = new SimpleStringProperty();
    private final StringProperty info = new SimpleStringProperty();

    public NodeItem(String id, String ip, String status, String sensorType, String info) {
        this.id.set(id);
        this.ip.set(ip);
        this.status.set(status);
        this.sensorType.set(sensorType);
        this.info.set(info);
    }

    public StringProperty idProperty() { return id; }
    public StringProperty ipProperty() { return ip; }
    public StringProperty statusProperty() { return status; }
    public StringProperty sensorTypeProperty() { return sensorType; }
    public StringProperty infoProperty() { return info; }

    public String getId() { return id.get(); }
    public String getIp() { return ip.get(); }
    public String getStatus() { return status.get(); }
    public String getSensorType() { return sensorType.get(); }
    public String getInfo() { return info.get(); }

    public void setStatus(String status) { this.status.set(status); }
    public void setSensorType(String sensorType) { this.sensorType.set(sensorType); }
    public void setInfo(String info) { this.info.set(info); }

}