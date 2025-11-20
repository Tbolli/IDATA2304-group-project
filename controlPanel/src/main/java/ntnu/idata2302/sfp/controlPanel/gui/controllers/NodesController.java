package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.controlPanel.gui.components.ActuatorControlUI;
import ntnu.idata2302.sfp.controlPanel.gui.components.NodeCell;
import ntnu.idata2302.sfp.controlPanel.gui.model.NodeEntry;
import ntnu.idata2302.sfp.controlPanel.net.AppContext;
import ntnu.idata2302.sfp.controlPanel.net.EventBus;
import ntnu.idata2302.sfp.controlPanel.net.SfpClient;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesListBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Controller for the Nodes view. In addition to existing responsibilities this
 * class now auto-refreshes capabilities whenever the view becomes visible again.
 *
 * It uses multiple listeners (scene, parent, visible) and a small debounce
 * to ensure a single refresh is triggered when the user switches back to this view.
 */
public class NodesController implements Unloadable {

  private SfpClient client;

  @FXML private ListView<NodeEntry> nodesList;
  @FXML private Label connectionLabel;

  private final Map<Integer, NodeEntry> nodes = new ConcurrentHashMap<>();
  private final ObservableList<NodeEntry> observableNodes = FXCollections.observableArrayList();

  final Map<String, Double> actuatorPendingValues = new ConcurrentHashMap<>();
  volatile boolean uiFreeze = false;
  SmartFarmingProtocol bufferedPacket = null;

  final Map<Integer, Map<String, ActuatorControlUI>> uiControlsPerNode = new ConcurrentHashMap<>();

  private final Consumer<SmartFarmingProtocol> packetListener = this::handlePacket;

  /** debounce: last time we triggered an auto-refresh */
  private volatile long lastAutoRefresh = 0L;
  /** minimum milliseconds between auto-refresh calls to avoid duplicates */
  private static final long AUTO_REFRESH_COOLDOWN_MS = 400;

  @FXML
  public void initialize() {
    client = AppContext.getClient();
    if (client == null) {
      System.err.println("No SFP client in AppContext");
      return;
    }

    connectionLabel.setText("Connected to " + client.getHost() + ":" + client.getPort());

    nodesList.setItems(observableNodes);

    nodesList.setCellFactory(list -> new NodeCell(
      this,
      nodes,
      uiControlsPerNode,
      actuatorPendingValues,
      nodesList,
      client
    ));

    EventBus.subscribe(packetListener);

    // Ensure capabilities refresh every time scene is opened
    Platform.runLater(() -> {
      if (AppContext.getControllerId() != null) {
        refreshCapabilities();
      }
    });
  }

  /**
   * Request an auto-refresh but debounce to avoid multiple calls during one switch.
   * This runs on JavaFX thread when actually performing refresh operations.
   */
  private void scheduleAutoRefresh() {
    long now = System.currentTimeMillis();
    if (now - lastAutoRefresh < AUTO_REFRESH_COOLDOWN_MS) {
      // too soon - skip duplicate
      return;
    }
    lastAutoRefresh = now;

    // run on FX thread (safe) with small delay to allow layout to settle
    Platform.runLater(() -> {
      try {
        // small pause gives SceneManager time to finish swap if needed
        Thread.sleep(30);
      } catch (InterruptedException ignored) {}
      // Only refresh if we still have a client and the nodes list is visible/attached
      if (client != null && nodesList.getScene() != null && nodesList.isVisible()) {
        refreshCapabilities();
      }
    });
  }

  @FXML
  public void refreshCapabilities() {
    nodes.clear();
    observableNodes.clear();
    uiControlsPerNode.clear();
    actuatorPendingValues.clear();

    if (AppContext.getControllerId() != null)
      client.sendCapabilitiesQuery();
  }

  @FXML
  private void goToPopulate() {
    SceneManager.switchScene("populate");
  }

  public void unsubscribeNode(int nodeId) {
    nodes.remove(nodeId);
    observableNodes.removeIf(n -> n.nodeId() == nodeId);
    uiControlsPerNode.remove(nodeId);
    actuatorPendingValues.keySet().removeIf(k -> k.startsWith(nodeId + ":"));
    client.sendUnsubscribe(nodeId);
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
        NodeEntry entry = nodes.get(id);
        if (!observableNodes.contains(entry))
          observableNodes.add(entry);
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

    Platform.runLater(() -> {
      NodeEntry entry = nodes.get(nodeId);

      if (entry == null) {
        entry = new NodeEntry(nodeId, report);
        nodes.put(nodeId, entry);
        observableNodes.add(entry);
      } else {
        entry.setData(report);
      }

      updateControlsWithReport(nodeId, report);
    });
  }

  public void updateControlsWithReport(int nodeId, DataReportBody report) {
    Map<String, ActuatorControlUI> controls = uiControlsPerNode.get(nodeId);
    if (controls == null) return;

    Map<String, DataReportBody.ActuatorState> reported = new HashMap<>();
    if (report.actuators() != null)
      report.actuators().forEach(a -> reported.put(a.id(), a));

    controls.forEach((id, ui) -> {
      DataReportBody.ActuatorState r = reported.get(id);
      if (r == null) return;

      ui.updateValueLabel(r);

      String key = nodeId + ":" + id;
      if (!actuatorPendingValues.containsKey(key) && !ui.isEditing())
        ui.applyReportedValue(r);
    });

    nodesList.refresh();
  }

  public boolean getUiFreeze() { return uiFreeze; }
  public SmartFarmingProtocol getBufferedPacket() { return bufferedPacket; }
  public void setUiFreeze(boolean f) { uiFreeze = f; }
  public void setBufferedPacket(SmartFarmingProtocol p) { bufferedPacket = p; }

  @Override
  public void onUnload() {
    EventBus.unsubscribe(packetListener);
  }
}
