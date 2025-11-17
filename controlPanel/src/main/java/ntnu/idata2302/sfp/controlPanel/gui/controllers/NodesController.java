package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;

import java.util.List;

public class NodesController {

  @FXML private ListView<NodeDescriptor> nodesList;
  @FXML private Label connectionLabel;

  @FXML
  public void initialize() {
    // TODO: Replace with backend provided nodes dynamically
    List<NodeDescriptor> sample = createSampleNodes();
    nodesList.getItems().setAll(sample);
    nodesList.setCellFactory(list -> new NodeCell());

    // Display active server
    connectionLabel.setText("Connected to 127.0.0.1:5050");
  }

  /* NAVIGATION */
  @FXML public void openHome()    { SceneManager.switchScene("home"); }
  @FXML public void openDataLog() { SceneManager.switchScene("dataLog"); }

  /* =============================== CUSTOM CELL =============================== */
  private static class NodeCell extends ListCell<NodeDescriptor> {

    @Override
    protected void updateItem(NodeDescriptor node, boolean empty) {
      super.updateItem(node, empty);

      if (empty || node == null) {
        setGraphic(null);
        return;
      }

      /* Card container */
      VBox card = new VBox(10);
      card.getStyleClass().add("node-card");

      /* Title/Header */
      Label header = new Label("Sensor-node #" + (node.nodeId() != null ? node.nodeId() : "?"));
      header.getStyleClass().add("node-header");

      /* Two-column grid layout */
      GridPane contentGrid = new GridPane();
      contentGrid.setHgap(50);
      contentGrid.setVgap(6);

      /* ---------------- SENSORS COLUMN ---------------- */
      if (node.sensors() != null && !node.sensors().isEmpty()) {
        VBox sensorColumn = new VBox(2);

        Label sensorTitle = new Label("Sensors:");
        sensorTitle.getStyleClass().add("node-subtitle");
        sensorColumn.getChildren().add(sensorTitle);

        node.sensors().forEach(sensor -> {
          Label lbl = new Label("• " + sensor.id() +
            (sensor.unit() != null ? " (" + sensor.unit() + ")" : ""));
          sensorColumn.getChildren().add(lbl);
        });

        contentGrid.add(sensorColumn, 0, 0);
      }

      /* ---------------- ACTUATORS COLUMN ---------------- */
      if (node.actuators() != null && !node.actuators().isEmpty()) {
        VBox actuatorColumn = new VBox(6);

        Label actuatorTitle = new Label("Actuators:");
        actuatorTitle.getStyleClass().add("node-subtitle");
        actuatorColumn.getChildren().add(actuatorTitle);

        node.actuators().forEach(act -> {

          HBox row = new HBox(6);
          Label lbl = new Label("• " + act.id() + " :");
          row.getChildren().add(lbl);

          boolean isBinary = "state".equalsIgnoreCase(act.unit())
            && act.minValue() != null && act.maxValue() != null
            && act.minValue() == 0.0 && act.maxValue() == 1.0;

          if (isBinary) {
            ToggleButton toggle = new ToggleButton(act.value() > 0.5 ? "ON" : "OFF");
            toggle.setSelected(act.value() > 0.5);
            toggle.selectedProperty().addListener((obs, oldVal, newVal) ->
              toggle.setText(newVal ? "ON" : "OFF"));
            row.getChildren().add(toggle);
          } else {
            Slider slider = new Slider(
              act.minValue() != null ? act.minValue() : 0,
              act.maxValue() != null ? act.maxValue() : 100,
              act.value()
            );
            slider.setPrefWidth(150);
            row.getChildren().add(slider);
          }

          actuatorColumn.getChildren().add(row);
        });

        contentGrid.add(actuatorColumn, 1, 0);
      }

      /* Assemble card */
      card.getChildren().addAll(header, contentGrid);
      setGraphic(card);
    }
  }

  /* ============================ TEMP SAMPLE DATA ============================ */
  private List<NodeDescriptor> createSampleNodes() {
    return List.of(
      new NodeDescriptor(
        1,
        1,
        List.of(
          new NodeDescriptor.SensorDescriptor("temperature", "°C", 0.0, 50.0),
          new NodeDescriptor.SensorDescriptor("humidity", "%", 0.0, 100.0)
        ),
        List.of(
          new NodeDescriptor.ActuatorDescriptor("heater", 0, 0.0, 1.0, "state"),
          new NodeDescriptor.ActuatorDescriptor("fan speed", 35, 0.0, 100.0, "%")
        ),
        true,
        false
      ),
      new NodeDescriptor(
        2,
        2,
        List.of(
          new NodeDescriptor.SensorDescriptor("soil", "%", 0.0, 100.0)
        ),
        List.of(
          new NodeDescriptor.ActuatorDescriptor("water pump", 0, 0.0, 1.0, "state")
        ),
        false,
        true
      )
    );
  }
}
