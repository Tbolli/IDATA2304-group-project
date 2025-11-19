package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.controlPanel.gui.model.NodeEntry;
import ntnu.idata2302.sfp.controlPanel.net.AppContext;
import ntnu.idata2302.sfp.controlPanel.net.EventBus;
import ntnu.idata2302.sfp.controlPanel.net.SfpClient;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesListBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NodesController implements Unloadable {

  private SfpClient client;

  @FXML private ListView<NodeEntry> nodesList;
  @FXML private Label connectionLabel;

  private final Map<Integer, NodeEntry> nodes = new ConcurrentHashMap<>();
  private final ObservableList<NodeEntry> observableNodes = FXCollections.observableArrayList();

  /** Pending user edits */
  private final Map<String, Double> actuatorPendingValues = new ConcurrentHashMap<>();

  /** Freeze UI updates during slider/toggle editing */
  private volatile boolean uiFreeze = false;
  private SmartFarmingProtocol bufferedPacket = null;

  /** Stable UI per actuator */
  private final Map<Integer, Map<String, ActuatorControlUI>> uiControlsPerNode =
    new ConcurrentHashMap<>();

  private final Consumer<SmartFarmingProtocol> packetListener = this::handlePacket;

  // ------------------------------------------------------------
  // INITIALIZATION
  // ------------------------------------------------------------
  @FXML
  public void initialize() {
    client = AppContext.getClient();
    if (client == null) {
      System.err.println("No SFP client in AppContext");
      return;
    }

    connectionLabel.setText("Connected to " + client.getHost() + ":" + client.getPort());
    nodesList.setItems(observableNodes);
    nodesList.setCellFactory(list -> new NodeCell());

    EventBus.subscribe(packetListener);
  }

  // ------------------------------------------------------------
  // BUTTON ACTIONS
  // ------------------------------------------------------------
  @FXML
  public void refreshCapabilities() {
    nodes.clear();
    observableNodes.clear();
    uiControlsPerNode.clear();
    actuatorPendingValues.clear();

    if (AppContext.getControllerId() != null) {
      client.sendCapabilitiesQuery();
    }
  }

  private void unsubscribeNode(int nodeId) {
    nodes.remove(nodeId);
    observableNodes.removeIf(n -> n.nodeId() == nodeId);
    uiControlsPerNode.remove(nodeId);

    actuatorPendingValues.keySet().removeIf(k -> k.startsWith(nodeId + ":"));
    client.sendUnsubscribe(nodeId);
  }

  @FXML
  private void openDataLog() {
    SceneManager.switchScene("dataLog");
  }

  // ------------------------------------------------------------
  // PACKET HANDLING
  // ------------------------------------------------------------
  private void handlePacket(SmartFarmingProtocol packet) {
    switch (packet.getHeader().getMessageType()) {
      case MessageTypes.ANNOUNCE_ACK -> handleAnnounceAck(packet);
      case MessageTypes.CAPABILITIES_LIST -> handleCapabilities(packet);
      case MessageTypes.DATA_REPORT -> handleDataReport(packet);
    }
  }

  private void handleAnnounceAck(SmartFarmingProtocol packet) {
    Header header = packet.getHeader();
    AppContext.setControllerId(header.getTargetId());
    client.sendCapabilitiesQuery();
  }

  private void handleCapabilities(SmartFarmingProtocol packet) {
    if (!(packet.getBody() instanceof CapabilitiesListBody capBody)) return;

    capBody.nodes().forEach(nodeDesc -> {
      Integer id = nodeDesc.nodeId();
      if (id == null) return;

      nodes.putIfAbsent(id, new NodeEntry(id, null));
      Platform.runLater(() -> {
        observableNodes.removeIf(n -> n.nodeId() == id);
        observableNodes.add(nodes.get(id));
      });

      client.sendSubscribe(id);
    });
  }

  private void handleDataReport(SmartFarmingProtocol packet) {
    // FREEZE WHILE EDITING
    if (uiFreeze) {
      bufferedPacket = packet;
      return;
    }

    if (!(packet.getBody() instanceof DataReportBody report)) return;

    int nodeId = packet.getHeader().getSourceId();
    NodeEntry updated = new NodeEntry(nodeId, report);
    nodes.put(nodeId, updated);

    Platform.runLater(() -> {
      observableNodes.removeIf(n -> n.nodeId() == nodeId);
      observableNodes.add(updated);
      updateControlsWithReport(nodeId, report);
    });
  }

  // ------------------------------------------------------------
  // UPDATE UI CONTROLS WITH NEW REPORT (non-destructive)
  // ------------------------------------------------------------
  private void updateControlsWithReport(int nodeId, DataReportBody report) {
    Map<String, ActuatorControlUI> controls = uiControlsPerNode.get(nodeId);
    if (controls == null) return;

    Map<String, DataReportBody.ActuatorState> reported = new HashMap<>();
    if (report.actuators() != null) {
      report.actuators().forEach(a -> reported.put(a.id(), a));
    }

    controls.forEach((id, ui) -> {
      DataReportBody.ActuatorState r = reported.get(id);
      if (r == null) return;

      ui.updateValueLabel(r);

      String key = nodeId + ":" + id;
      if (actuatorPendingValues.containsKey(key)) return;
      if (ui.isEditing()) return;

      ui.applyReportedValue(r);
    });

    nodesList.refresh();
  }

  // ------------------------------------------------------------
  // LIST CELL
  // ------------------------------------------------------------
  private class NodeCell extends ListCell<NodeEntry> {

    @Override
    protected void updateItem(NodeEntry entry, boolean empty) {
      super.updateItem(entry, empty);

      if (empty || entry == null) {
        setGraphic(null);
        return;
      }

      VBox card = new VBox(12);
      card.getStyleClass().add("node-card");
      card.setFocusTraversable(false);

      Label header = new Label("Node #" + entry.nodeId());
      header.getStyleClass().add("node-header");

      // SENSORS ------------------------------------------------
      VBox sensorsBox = new VBox(5);
      Label sensLabel = new Label("Sensors:");
      sensLabel.getStyleClass().add("small-section-label");
      sensorsBox.getChildren().add(sensLabel);

      if (entry.data() != null && entry.data().sensors() != null) {
        entry.data().sensors().forEach(s -> {
          String v = s.value() != null ? String.format("%.2f", s.value()) : "N/A";
          Label l = new Label("• " + s.id() + " = " + v +
            (s.unit() != null ? " " + s.unit() : ""));
          l.getStyleClass().add("sensor-item");
          sensorsBox.getChildren().add(l);
        });
      }

      // ACTUATORS ---------------------------------------------
      VBox actuatorsBox = new VBox(8);
      Label actLabel = new Label("Actuators:");
      actLabel.getStyleClass().add("small-section-label");
      actuatorsBox.getChildren().add(actLabel);

      ensureUiControls(entry);

      Map<String, ActuatorControlUI> map = uiControlsPerNode.get(entry.nodeId());
      if (map != null && entry.data() != null && entry.data().actuators() != null) {
        for (var a : entry.data().actuators()) {
          ActuatorControlUI ui = map.get(a.id());
          if (ui != null) actuatorsBox.getChildren().add(ui.getRow());
        }
      }

      // BUTTONS -----------------------------------------------
      VBox buttons = new VBox(10);
      buttons.setPrefWidth(150);
      Button submitAll = new Button("Submit All");
      Button cancelAll = new Button("Cancel All");
      Button unsub = new Button("Unsubscribe");

      boolean pending =
        actuatorPendingValues.keySet().stream()
          .anyMatch(k -> k.startsWith(entry.nodeId() + ":"));

      submitAll.setDisable(!pending);
      cancelAll.setDisable(!pending);

      submitAll.setOnAction(e -> submitAll(entry.nodeId()));
      cancelAll.setOnAction(e -> cancelAll(entry.nodeId()));
      unsub.setOnAction(e -> unsubscribeNode(entry.nodeId()));

      buttons.getChildren().addAll(submitAll, cancelAll, unsub);

      // LAYOUT -------------------------------------------------
      HBox row = new HBox(20,
        sensorsBox,
        new Separator(javafx.geometry.Orientation.VERTICAL),
        actuatorsBox,
        buttons
      );

      card.getChildren().addAll(header, row);
      setGraphic(card);
    }

    private void submitAll(int nodeId) {
      String prefix = nodeId + ":";

      List<CommandBody.CommandPart> parts =
        actuatorPendingValues.entrySet().stream()
          .filter(e -> e.getKey().startsWith(prefix))
          .map(e -> new CommandBody.CommandPart(
            e.getKey().substring(prefix.length()),     // actuatorId
            e.getValue()                               // pending value
          ))
          .toList();
      client.sendActuatorCommand(nodeId, parts);

      actuatorPendingValues.keySet().removeIf(k -> k.startsWith(prefix));

      Map<String, ActuatorControlUI> map = uiControlsPerNode.get(nodeId);
      if (map != null) map.values().forEach(ActuatorControlUI::stopEditing);

      nodesList.refresh();
    }

    private void cancelAll(int nodeId) {
      String prefix = nodeId + ":";
      actuatorPendingValues.keySet().removeIf(k -> k.startsWith(prefix));

      Map<String, ActuatorControlUI> map = uiControlsPerNode.get(nodeId);
      if (map != null) map.values().forEach(ActuatorControlUI::revertPending);

      nodesList.refresh();
    }

    private void ensureUiControls(NodeEntry entry) {
      int nodeId = entry.nodeId();
      uiControlsPerNode.computeIfAbsent(nodeId, k -> new LinkedHashMap<>());

      if (entry.data() == null || entry.data().actuators() == null) return;

      Map<String, ActuatorControlUI> map = uiControlsPerNode.get(nodeId);

      for (var a : entry.data().actuators()) {
        map.computeIfAbsent(a.id(), id -> new ActuatorControlUI(nodeId, a));
      }

      // remove controls no longer in report
      Set<String> idsNow = new HashSet<>();
      entry.data().actuators().forEach(a -> idsNow.add(a.id()));
      map.keySet().removeIf(k -> !idsNow.contains(k));
    }
  }
  // ============================================================
  // ACTUATOR CONTROL UI
  // ============================================================
  private class ActuatorControlUI {
    private final int nodeId;
    private final String actuatorId;
    private final HBox row;

    private final Label nameLabel;
    private final Node control;
    private final Label valueLabel;

    private volatile boolean editing = false;

    ActuatorControlUI(int nodeId, DataReportBody.ActuatorState reported) {
      this.nodeId = nodeId;
      this.actuatorId = reported.id();

      this.row = new HBox(14);
      row.setStyle("-fx-padding:6;");

      this.nameLabel = new Label(reported.id());
      nameLabel.setPrefWidth(120);
      nameLabel.getStyleClass().add("actuator-name");

      // Value label (shows slider value + unit)
      this.valueLabel = new Label();
      valueLabel.setPrefWidth(90);
      valueLabel.getStyleClass().add("reported-label");

      // Build control
      if ("state".equalsIgnoreCase(reported.unit())) {
        ToggleButton toggle = new ToggleButton();

        String key = nodeId + ":" + actuatorId;
        Double pending = actuatorPendingValues.get(key);
        boolean on = pending != null ? pending >= 0.5 :
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

        this.control = toggle;

      } else {
        double min = reported.minValue() != null ? reported.minValue() : 0;
        double max = reported.maxValue() != null ? reported.maxValue() : 100;

        String key = nodeId + ":" + actuatorId;
        Double pending = actuatorPendingValues.get(key);
        double initial = pending != null ? pending :
          (reported.value() != null ? reported.value() : min);

        Slider slider = new Slider(min, max, initial);
        slider.setPrefWidth(260);
        slider.setShowTickMarks(false);
        slider.setShowTickLabels(false);
        slider.getStyleClass().add("slider");

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

        this.control = slider;
      }

      updateValueLabel(reported);

      row.getChildren().addAll(nameLabel, control, valueLabel);
    }

    Node getRow() {
      return row;
    }

    boolean isEditing() {
      return editing;
    }

    private void setEditing(boolean v) {
      editing = v;
      uiFreeze = v;

      if (!v && bufferedPacket != null) {
        SmartFarmingProtocol pkt = bufferedPacket;
        bufferedPacket = null;
        Platform.runLater(() -> handleDataReport(pkt));
      }
    }

    void stopEditing() {
      setEditing(false);
    }

    void revertPending() {
      actuatorPendingValues.remove(nodeId + ":" + actuatorId);
      NodeEntry entry = nodes.get(nodeId);
      if (entry != null && entry.data() != null && entry.data().actuators() != null) {
        for (var a : entry.data().actuators()) {
          if (a.id().equals(actuatorId)) {
            applyReportedValue(a);
            updateValueLabel(a);
            break;
          }
        }
      }
      stopEditing();
    }

    void applyReportedValue(DataReportBody.ActuatorState reported) {
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

    // --------------------------------------------------------------------
    // VALUE LABEL LOGIC (super important!)
    // --------------------------------------------------------------------
    void updateValueLabel(DataReportBody.ActuatorState reported) {
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

    void updateValueLabelFromSlider(Slider slider, DataReportBody.ActuatorState reported) {
      String unit = reported.unit() != null ? reported.unit() : "";
      valueLabel.setText(String.format("%.0f%s", slider.getValue(), unit));
    }
  }

  @Override
  public void onUnload() {
    EventBus.unsubscribe(packetListener);
  }
}
