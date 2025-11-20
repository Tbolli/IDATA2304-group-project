package ntnu.idata2302.sfp.controlPanel.gui.components;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import ntnu.idata2302.sfp.controlPanel.gui.controllers.NodesController;
import ntnu.idata2302.sfp.controlPanel.gui.model.NodeEntry;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;

import java.util.Map;

public class ActuatorControlUI {

  private final NodesController controller;
  private final Map<Integer, Map<String, ActuatorControlUI>> uiControlsPerNode;
  private final Map<String, Double> actuatorPendingValues;
  private final Map<Integer, NodeEntry> nodes;
  private final int nodeId;
  private final String actuatorId;
  private final HBox row;

  private final Label nameLabel;
  private final Node control;
  private final Label valueLabel;

  private final javafx.scene.control.ListView<NodeEntry> nodesList;
  private volatile boolean editing = false;

  public ActuatorControlUI(
    NodesController controller,
    javafx.scene.control.ListView<NodeEntry> nodesList,
    Map<Integer, Map<String, ActuatorControlUI>> uiControlsPerNode,
    Map<String, Double> actuatorPendingValues,
    Map<Integer, NodeEntry> nodes,
    int nodeId,
    DataReportBody.ActuatorState reported
  ) {
    this.controller = controller;
    this.nodesList = nodesList;
    this.uiControlsPerNode = uiControlsPerNode;
    this.actuatorPendingValues = actuatorPendingValues;
    this.nodes = nodes;

    this.nodeId = nodeId;
    this.actuatorId = reported.id();

    row = new HBox(14);
    row.setStyle("-fx-padding:6;");

    nameLabel = new Label(reported.id());
    nameLabel.setPrefWidth(120);
    nameLabel.getStyleClass().add("actuator-name");

    valueLabel = new Label();
    valueLabel.setPrefWidth(90);
    valueLabel.getStyleClass().add("reported-label");

    // CONTROL CREATION ---------------------------------------------
    if ("state".equalsIgnoreCase(reported.unit())) {
      ToggleButton toggle = new ToggleButton();

      String key = nodeId + ":" + actuatorId;
      Double pending = actuatorPendingValues.get(key);
      boolean on = pending != null ?
        pending >= 0.5 :
        (reported.value() != null && reported.value() >= 0.5);

      toggle.setSelected(on);
      toggle.setText(on ? "ON" : "OFF");
      toggle.getStyleClass().add(on ? "toggle-on" : "toggle-off");

      toggle.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> setEditing(true));
      toggle.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> setEditing(false));

      toggle.setOnAction(e -> {
        boolean newState = toggle.isSelected();
        toggle.setText(newState ? "ON" : "OFF");
        toggle.getStyleClass().removeAll("toggle-on", "toggle-off");
        toggle.getStyleClass().add(newState ? "toggle-on" : "toggle-off");

        actuatorPendingValues.put(key, newState ? 1.0 : 0.0);
        Platform.runLater(nodesList::refresh);
      });

      control = toggle;

    } else {

      double min = reported.minValue() != null ? reported.minValue() : 0;
      double max = reported.maxValue() != null ? reported.maxValue() : 100;

      String key = nodeId + ":" + actuatorId;
      Double pending = actuatorPendingValues.get(key);
      double initial = pending != null ? pending :
        (reported.value() != null ? reported.value() : min);

      Slider slider = new Slider(min, max, initial);
      slider.setPrefWidth(260);

      slider.valueChangingProperty().addListener((obs, wasChanging, isChanging) -> {
        setEditing(isChanging);
        if (!isChanging) {
          actuatorPendingValues.put(key, slider.getValue());
          updateValueLabelFromSlider(slider, reported);
          Platform.runLater(nodesList::refresh);
        }
      });

      slider.valueProperty().addListener((obs, oldV, newV) -> {
        if (slider.isValueChanging()) {
          actuatorPendingValues.put(key, newV.doubleValue());
          updateValueLabelFromSlider(slider, reported);
        }
      });

      slider.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> setEditing(true));
      slider.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> setEditing(false));

      control = slider;
    }

    updateValueLabel(reported);
    row.getChildren().addAll(nameLabel, control, valueLabel);
  }

  public Node getRow() {
    return row;
  }

  public boolean isEditing() {
    return editing;
  }

  private void setEditing(boolean v) {
    editing = v;
    controller.setUiFreeze(v);

    if (!v && controller.getBufferedPacket() != null) {
      SmartFarmingProtocol pkt = controller.getBufferedPacket();
      controller.setBufferedPacket(null);

      Platform.runLater(() ->
        controller.updateControlsWithReport(nodeId,
          (DataReportBody) pkt.getBody())
      );
    }
  }

  public void stopEditing() {
    setEditing(false);
  }

  public void revertPending() {
    actuatorPendingValues.remove(nodeId + ":" + actuatorId);

    NodeEntry entry = nodes.get(nodeId);
    if (entry != null && entry.getData() != null && entry.getData().actuators() != null) {
      for (var a : entry.getData().actuators()) {
        if (a.id().equals(actuatorId)) {
          applyReportedValue(a);
          updateValueLabel(a);
          break;
        }
      }
    }
    stopEditing();
  }

  public void applyReportedValue(DataReportBody.ActuatorState reported) {
    if (control instanceof Slider slider) {
      if (!editing && !slider.isValueChanging()) {
        double val = reported.value() != null ? reported.value() : slider.getMin();
        slider.setValue(val);
        updateValueLabel(reported);
      }
    } else if (control instanceof ToggleButton toggle) {
      if (!editing) {
        boolean on = reported.value() != null && reported.value() >= 0.5;
        toggle.setSelected(on);
        toggle.setText(on ? "ON" : "OFF");
        toggle.getStyleClass().removeAll("toggle-on", "toggle-off");
        toggle.getStyleClass().add(on ? "toggle-on" : "toggle-off");
      }
    }
  }

  public void updateValueLabel(DataReportBody.ActuatorState reported) {
    if ("state".equalsIgnoreCase(reported.unit())) {
      valueLabel.setText("");
      return;
    }

    String unit = reported.unit() != null ? reported.unit() : "";
    double val;

    String key = nodeId + ":" + actuatorId;

    if (actuatorPendingValues.containsKey(key)) {
      val = actuatorPendingValues.get(key);
    } else if (control instanceof Slider s) {
      val = s.getValue();
    } else {
      val = reported.value() != null ? reported.value() : 0;
    }

    valueLabel.setText(String.format("%.0f%s", val, unit));
  }

  public void updateValueLabelFromSlider(Slider slider, DataReportBody.ActuatorState reported) {
    String unit = reported.unit() != null ? reported.unit() : "";
    valueLabel.setText(String.format("%.0f%s", slider.getValue(), unit));
  }
}
