package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.controlPanel.gui.components.ActuatorControlUI;
import ntnu.idata2302.sfp.controlPanel.gui.components.NodeCell;
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
  final Map<String, Double> actuatorPendingValues = new ConcurrentHashMap<>();

  /** Freeze UI updates during slider/toggle editing */
  volatile boolean uiFreeze = false;
  SmartFarmingProtocol bufferedPacket = null;

  /** Stable UI per actuator */
  final Map<Integer, Map<String, ActuatorControlUI>> uiControlsPerNode =
    new ConcurrentHashMap<>();

  private final Consumer<SmartFarmingProtocol> packetListener = this::handlePacket;

  @FXML
  public void initialize() {
    client = AppContext.getClient();
    if (client == null) {
      System.err.println("No SFP client in AppContext");
      return;
    }

    connectionLabel.setText("Connected to " + client.getHost() + ":" + client.getPort());
    nodesList.setItems(observableNodes);

    // Pass all required references to NodeCell
    nodesList.setCellFactory(list ->
      new NodeCell(
        this,
        nodes,
        uiControlsPerNode,
        actuatorPendingValues,
        nodesList,
        client
      )
    );

    EventBus.subscribe(packetListener);
  }

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

  public void unsubscribeNode(int nodeId) {
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

  public void updateControlsWithReport(int nodeId, DataReportBody report) {
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

  public boolean getUiFreeze(){
    return uiFreeze;
  }

  public SmartFarmingProtocol getBufferedPacket() {
    return bufferedPacket;
  }

  public void setUiFreeze(boolean uiFreeze) {
    this.uiFreeze = uiFreeze;
  }

  public void setBufferedPacket(SmartFarmingProtocol packet) {
    this.bufferedPacket = packet;
  }

  @Override
  public void onUnload() {
    EventBus.unsubscribe(packetListener);
  }
}
