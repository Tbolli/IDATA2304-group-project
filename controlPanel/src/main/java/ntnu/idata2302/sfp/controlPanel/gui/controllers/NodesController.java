package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.controlPanel.gui.model.NodeItem;

import java.io.IOException;
import java.net.URL;

import ntnu.idata2302.sfp.controlPanel.backendlogic.BackendEventBus;
import org.json.JSONObject;

public class NodesController {

    /* ─────────────────────────────────────────────
                    NAVIGATION BUTTONS
       ───────────────────────────────────────────── */

  @FXML
  private void goHome(ActionEvent event) {
    SceneManager.switchScene("home");
  }

  @FXML
  public void openHome(ActionEvent event) {
    SceneManager.switchScene("home");
  }

  @FXML
  public void openDataLog(ActionEvent event) {
    SceneManager.switchScene("dataLog");
  }

    /* ─────────────────────────────────────────────
                    FXML UI REFERENCES
       ───────────────────────────────────────────── */

  @FXML private Label pageTitle;

  @FXML private TableView<NodeItem> nodesTable;
  @FXML private TableColumn<NodeItem, String> colId;
  @FXML private TableColumn<NodeItem, String> colIp;
  @FXML private TableColumn<NodeItem, String> colStatus;
  @FXML private TableColumn<NodeItem, String> colSensor;
  @FXML private TableColumn<NodeItem, String> colInfo;

  @FXML private Label detailTitle;
  @FXML private Label detailTemp;
  @FXML private Label detailHumidity;
  @FXML private ToggleButton heaterToggle;
  @FXML private ToggleButton fanToggle;
  @FXML private Button refreshBtn;

  private final ObservableList<NodeItem> sampleData = FXCollections.observableArrayList();


    /* ─────────────────────────────────────────────
                    INITIALIZE
       ───────────────────────────────────────────── */

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
    nodesTable.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
      showDetails(newSel);
    });

    // Select first by default
    if (!sampleData.isEmpty()) {
      nodesTable.getSelectionModel().select(0);
      showDetails(sampleData.get(0));
    }

        /* ─────────────────────────────────────────────
                    BACKEND LIVE UPDATE LISTENER
           ───────────────────────────────────────────── */
    BackendEventBus.onSensorMessage(json ->
        Platform.runLater(() -> updateNode(json))
    );
  }


    /* ─────────────────────────────────────────────
                    SHOW NODE DETAILS
       ───────────────────────────────────────────── */

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

    // TEMP/HUMI example placeholders
    detailTemp.setText("🌡 Temperature: --");
    detailHumidity.setText("💧 Humidity: --");

    heaterToggle.setSelected(false);
    heaterToggle.setText("OFF");

    fanToggle.setSelected(true);
    fanToggle.setText("ON");
  }


    /* ─────────────────────────────────────────────
                    BACKEND REAL-TIME UPDATE
       ───────────────────────────────────────────── */

  private void updateNode(String json) {
    try {
      JSONObject obj = new JSONObject(json);

      String id = obj.optString("temp_id", null);
      if (id == null) return;

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


    /* ─────────────────────────────────────────────
                    ACTUATOR BUTTONS
       ───────────────────────────────────────────── */

  @FXML
  private void onToggleHeater(ActionEvent event) {
    boolean on = heaterToggle.isSelected();
    heaterToggle.setText(on ? "ON" : "OFF");

    heaterToggle.getStyleClass().removeIf(s -> s.equals("toggle-on") || s.equals("toggle-off"));
    heaterToggle.getStyleClass().add(on ? "toggle-on" : "toggle-off");
  }

  @FXML
  private void onToggleFan(ActionEvent event) {
    boolean on = fanToggle.isSelected();
    fanToggle.setText(on ? "ON" : "OFF");

    fanToggle.getStyleClass().removeIf(s -> s.equals("toggle-on") || s.equals("toggle-off"));
    fanToggle.getStyleClass().add(on ? "toggle-on" : "toggle-off");
  }


    /* ─────────────────────────────────────────────
                    REFRESH BUTTON
       ───────────────────────────────────────────── */

  @FXML
  private void onRefresh(ActionEvent event) {
    NodeItem sel = nodesTable.getSelectionModel().getSelectedItem();
    if (sel != null) {
      detailTemp.setText(detailTemp.getText() + " · refreshed");
      refreshBtn.setDisable(true);

      new Thread(() -> {
        try { Thread.sleep(500); } catch (InterruptedException ignored) {}
        Platform.runLater(() -> refreshBtn.setDisable(false));
      }).start();
    }
  }


    /* ─────────────────────────────────────────────
                LOAD OTHER SCREENS INTO CENTER
       ───────────────────────────────────────────── */

  @FXML
  private void onViewSelected(ActionEvent event) {
    NodeItem sel = nodesTable.getSelectionModel().getSelectedItem();
    if (sel != null) {
      loadViewIntoCenter("/ntnu/smartFarm/gui/views/nodeDetails.fxml");
    } else {
      new Alert(Alert.AlertType.INFORMATION, "Select a node first.", ButtonType.OK).showAndWait();
    }
  }

  private void loadViewIntoCenter(String fxmlResourcePath) {
    try {
      URL resource = getClass().getResource(fxmlResourcePath);
      if (resource == null) {
        System.err.println("FXML not found: " + fxmlResourcePath);
        return;
      }
      Parent view = FXMLLoader.load(resource);
      BorderPane main = (BorderPane) pageTitle.getScene().getRoot();
      main.setCenter(view);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
