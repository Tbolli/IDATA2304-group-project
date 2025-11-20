package ntnu.idata2302.sfp.controlPanel.gui.components;

import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ntnu.idata2302.sfp.controlPanel.gui.controllers.NodesController;
import ntnu.idata2302.sfp.controlPanel.gui.model.NodeEntry;
import ntnu.idata2302.sfp.controlPanel.net.SfpClient;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;

import java.util.*;

public class NodeCell extends ListCell<NodeEntry> {

  private final NodesController controller;
  private final Map<Integer, NodeEntry> nodes;
  private final Map<Integer, Map<String, ActuatorControlUI>> uiControlsPerNode;
  private final Map<String, Double> actuatorPendingValues;
  private final ListView<NodeEntry> nodesList;
  private final SfpClient client;

  public NodeCell(
    NodesController controller,
    Map<Integer, NodeEntry> nodes,
    Map<Integer, Map<String, ActuatorControlUI>> uiControlsPerNode,
    Map<String, Double> actuatorPendingValues,
    ListView<NodeEntry> nodesList,
    SfpClient client
  ) {
    this.controller = controller;
    this.nodes = nodes;
    this.uiControlsPerNode = uiControlsPerNode;
    this.actuatorPendingValues = actuatorPendingValues;
    this.nodesList = nodesList;
    this.client = client;
  }

  @Override
  protected void updateItem(NodeEntry entry, boolean empty) {
    super.updateItem(entry, empty);

    if (empty || entry == null) {
      setGraphic(null);
      return;
    }

    // Ensure we listen for data changes so the cell re-renders when a report arrives
    entry.dataProperty().addListener((obs, oldV, newV) -> Platform.runLater(() -> {
      // only rebuild UI if item still associated with this cell
      if (getItem() == entry) {
        setGraphic(buildCell(entry));
      }
    }));

    setGraphic(buildCell(entry));
  }

  private VBox buildCell(NodeEntry entry) {
    VBox card = new VBox(12);
    card.getStyleClass().add("node-card");

    Label header = new Label("Node #" + entry.nodeId());
    header.getStyleClass().add("node-header");

    // SENSORS ------------------------------------------------
    VBox sensorsBox = new VBox(5);
    Label sensLabel = new Label("Sensors:");
    sensLabel.getStyleClass().add("small-section-label");
    sensorsBox.getChildren().add(sensLabel);

    if (entry.getData() != null && entry.getData().sensors() != null) {
      entry.getData().sensors().forEach(s -> {
        String v = s.value() != null ? String.format("%.2f", s.value()) : "N/A";
        Label l = new Label("• " + s.id() + " = " + v + (s.unit() != null ? " " + s.unit() : ""));
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

    // If there is a current report with actuator list: add those in report order.
    if (entry.getData() != null && entry.getData().actuators() != null) {
      for (var a : entry.getData().actuators()) {
        ActuatorControlUI ui = map != null ? map.get(a.id()) : null;
        if (ui != null) actuatorsBox.getChildren().add(ui.getRow());
      }
    } else {
      // No actuators in latest report → display existing stable controls (if any)
      if (map != null) {
        for (var ui : map.values()) {
          actuatorsBox.getChildren().add(ui.getRow());
        }
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
    unsub.setOnAction(e -> controller.unsubscribeNode(entry.nodeId()));

    buttons.getChildren().addAll(submitAll, cancelAll, unsub);

    // LAYOUT -------------------------------------------------
    HBox row = new HBox(20, sensorsBox, new Separator(Orientation.VERTICAL), actuatorsBox, buttons);

    card.getChildren().addAll(header, row);
    return card;
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

    Map<String, ActuatorControlUI> map = uiControlsPerNode.get(nodeId);

    // If the current report has actuator descriptions, ensure UI controls exist for them.
    if (entry.getData() != null && entry.getData().actuators() != null) {
      for (var a : entry.getData().actuators()) {
        map.computeIfAbsent(a.id(), id ->
          new ActuatorControlUI(
            controller,
            nodesList,
            uiControlsPerNode,
            actuatorPendingValues,
            nodes,
            nodeId,
            a
          )
        );
      }

      // remove controls no longer in report
      Set<String> idsNow = new HashSet<>();
      entry.getData().actuators().forEach(a -> idsNow.add(a.id()));
      map.keySet().removeIf(k -> !idsNow.contains(k));
    }
    // If there is no actuator list in the report, we keep existing controls intact.
  }
}
