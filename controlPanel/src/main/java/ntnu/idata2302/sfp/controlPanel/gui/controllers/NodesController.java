package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ntnu.idata2302.sfp.controlPanel.backendlogic.BackendEventBus;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.controlPanel.gui.model.NodeItem;
import org.json.JSONObject;


/**
 * Controller for the Nodes view.
 * Responsible for populating the node table, showing node details,
 * reacting to backend sensor messages and handling actuator UI controls.
 */
public class NodesController {


  /**
   * Switch to the home scene.
   */

  @FXML
  public void openHome() {
    SceneManager.switchScene("home");
  }

  /**
   * Switch to the data log scene.
   */

  @FXML
  public void openDataLog() {
    SceneManager.switchScene("dataLog");
  }


  @FXML
  private TableView<NodeItem> nodesTable;
  @FXML
  private TableColumn<NodeItem, String> colId;
  @FXML
  private TableColumn<NodeItem, String> colIp;
  @FXML
  private TableColumn<NodeItem, String> colStatus;
  @FXML
  private TableColumn<NodeItem, String> colSensor;
  @FXML
  private TableColumn<NodeItem, String> colInfo;

  @FXML
  private Label detailTitle;
  @FXML
  private Label detailTemp;
  @FXML
  private Label detailHumidity;
  @FXML
  private ToggleButton heaterToggle;
  @FXML
  private ToggleButton fanToggle;
  @FXML
  private Button refreshBtn;

  private final ObservableList<NodeItem> sampleData = FXCollections.observableArrayList();


  @FXML
  private void initialize() {
    // Bind columns
    colId.setCellValueFactory(cell -> cell.getValue().idProperty());
    colIp.setCellValueFactory(cell -> cell.getValue().ipProperty());
    colStatus.setCellValueFactory(cell -> cell.getValue().statusProperty());
    colSensor.setCellValueFactory(cell -> cell.getValue().sensorTypeProperty());
    colInfo.setCellValueFactory(cell -> cell.getValue().infoProperty());

    // Temporary sample nodes
    sampleData.addAll(
        new NodeItem("Node 001", "192.168.0.10", "Online", "Temperature", "Last seen 2s"),
        new NodeItem("Node 002", "192.168.0.11", "Online", "Humidity", "Last seen 5s"),
        new NodeItem("Node 003", "192.168.0.12", "Offline", "Soil", "Last seen 12m")
    );

    nodesTable.setItems(sampleData);

    // Update details when selected
    nodesTable.getSelectionModel().selectedItemProperty()
        .addListener((obs, oldSel, newSel) -> showDetails(newSel));

    // Select first by default
    if (!sampleData.isEmpty()) {
      nodesTable.getSelectionModel().select(0);
      showDetails(sampleData.getFirst());
    }

    BackendEventBus.onSensorMessage(json ->
        Platform.runLater(() -> updateNode(json))
    );
  }


  private void showDetails(NodeItem node) {
    if (node == null) {
      detailTitle.setText("No node selected");
      detailTemp.setText("");
      detailHumidity.setText("");
      heaterToggle.setSelected(false);
      fanToggle.setSelected(false);
      return;
    }

    detailTitle.setText(node.getId() + " (" + node.getIp() + ")");

    // TEMP/HUM example placeholders
    detailTemp.setText("🌡 Temperature: --");
    detailHumidity.setText("💧 Humidity: --");

    heaterToggle.setSelected(false);
    heaterToggle.setText("OFF");

    fanToggle.setSelected(true);
    fanToggle.setText("ON");
  }


  private void updateNode(String json) {
    try {
      JSONObject obj = new JSONObject(json);

      String id = obj.optString("temp_id", null);
      if (id == null) {
        return;
      }

      String temp = obj.optString("temperature", "--");
      String hum = obj.optString("humidity", "--");

      for (NodeItem item : sampleData) {
        if (item.getId().equals(id)) {
          item.setStatus("Online");
          item.setSensorType("Temperature/Humidity");
          item.setInfo("T: " + temp + "°C, H: " + hum + "%");

          // Update visible details
          NodeItem selected = nodesTable.getSelectionModel().getSelectedItem();
          if (selected != null && selected.getId().equals(id)) {
            detailTemp.setText("🌡 Temperature: " + temp + "°C");
            detailHumidity.setText("💧 Humidity: " + hum + "%");
          }
        }
      }

    } catch (Exception e) {
      System.err.println("❌ Failed to update node: " + json);
    }
  }


  @FXML
  private void onToggleHeater() {
    boolean on = heaterToggle.isSelected();
    heaterToggle.setText(on ? "ON" : "OFF");

    heaterToggle.getStyleClass().removeIf(s -> s.equals("toggle-on") || s.equals("toggle-off"));
    heaterToggle.getStyleClass().add(on ? "toggle-on" : "toggle-off");
  }

  @FXML
  private void onToggleFan() {
    boolean on = fanToggle.isSelected();
    fanToggle.setText(on ? "ON" : "OFF");

    fanToggle.getStyleClass().removeIf(s -> s.equals("toggle-on") || s.equals("toggle-off"));
    fanToggle.getStyleClass().add(on ? "toggle-on" : "toggle-off");
  }


  @FXML
  private void onRefresh() {
    NodeItem sel = nodesTable.getSelectionModel().getSelectedItem();
    if (sel != null) {
      detailTemp.setText(detailTemp.getText() + " · refreshed");
      refreshBtn.setDisable(true);

      new Thread(() -> {
        try {
          Thread.sleep(500);
        } catch (InterruptedException ignored) {
          System.out.println("❌ Refresh interrupted");
        }
        Platform.runLater(() -> refreshBtn.setDisable(false));
      }).start();
    }
  }

}
