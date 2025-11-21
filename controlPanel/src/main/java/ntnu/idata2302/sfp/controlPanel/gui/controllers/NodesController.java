package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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


/**
 * Controller for the "Nodes" view in the control panel GUI.
 *
 * <p>This controller displays all known sensor nodes, listens for incoming
 * Smart Farming Protocol packets, subscribes to node data, and keeps
 * actuator controls synchronized with the latest reports.</p>
 *
 * <p>It also manages automatic capability refreshes and cleans up its
 * subscriptions when the view is unloaded.</p>
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

  /** debounce: last time we triggered an auto-refresh. */
  private volatile long lastAutoRefresh = 0L;
  /** minimum milliseconds between auto-refresh calls to avoid duplicates. */
  private static final long AUTO_REFRESH_COOLDOWN_MS = 400;

  /**
   * Initializes the controller after its FXML fields have been injected.
   *
   * <p>Sets up the client connection label, configures the ListView cell
   * factory, subscribes to the EventBus, and triggers an initial capabilities
   * refresh if the controller ID is already known.</p>
   */

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
      } catch (InterruptedException ignored) {
        // Only refresh if we still have a client and the nodes list is visible/attached
      }

      if (client != null && nodesList.getScene() != null && nodesList.isVisible()) {
        refreshCapabilities();
      }
    });
  }

  /**
   * Clears all current node state and requests a new capabilities list
   * from the server for the current controller.
   */

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

  /**
   * Switches to the "Populate" view where sensor nodes can be configured
   * and spawned as separate processes.
   */

  @FXML
  private void goToPopulate() {
    SceneManager.switchScene("populate");
  }

  /**
   * Unsubscribes from updates for the given node ID and removes it from the UI.
   *
   * @param nodeId the identifier of the node to unsubscribe and remove
   */

  public void unsubscribeNode(int nodeId) {
    nodes.remove(nodeId);
    observableNodes.removeIf(n -> n.nodeId() == nodeId);
    uiControlsPerNode.remove(nodeId);
    actuatorPendingValues.keySet().removeIf(k -> k.startsWith(nodeId + ":"));
    client.sendUnsubscribe(nodeId);
  }

  /**
   * Central handler for all incoming Smart Farming Protocol packets.
   *
   * <p>Dispatches packets based on their message type to the corresponding
   * specialized handler methods.</p>
   *
   * @param packet the received protocol packet
   */

  private void handlePacket(SmartFarmingProtocol packet) {
    switch (packet.getHeader().getMessageType()) {
      case MessageTypes.ANNOUNCE_ACK -> handleAnnounceAck(packet);
      case MessageTypes.CAPABILITIES_LIST -> handleCapabilities(packet);
      case MessageTypes.DATA_REPORT -> handleDataReport(packet);
    }
  }

  /**
   * Handles an ANNOUNCE_ACK packet from the server, storing the assigned
   * controller ID and triggering a capabilities query.
   *
   * @param packet the ANNOUNCE_ACK packet
   */

  private void handleAnnounceAck(SmartFarmingProtocol packet) {
    Header header = packet.getHeader();
    AppContext.setControllerId(header.getTargetId());
    client.sendCapabilitiesQuery();
  }

  /**
   * Handles a CAPABILITIES_LIST packet by creating NodeEntry objects for
   * each reported node and subscribing to their data reports.
   *
   * @param packet the CAPABILITIES_LIST packet
   */

  private void handleCapabilities(SmartFarmingProtocol packet) {
    if (!(packet.getBody() instanceof CapabilitiesListBody capBody)) {
      return;
    }

    capBody.nodes().forEach(nodeDesc -> {
      Integer id = nodeDesc.nodeId();
      if (id == null) {
        return;
      }

      nodes.putIfAbsent(id, new NodeEntry(id, null));

      Platform.runLater(() -> {
        NodeEntry entry = nodes.get(id);
        if (!observableNodes.contains(entry)) {

          observableNodes.add(entry);
        }
      });

      client.sendSubscribe(id);
    });
  }

  /**
   * Handles a DATA_REPORT packet by updating the corresponding NodeEntry and
   * refreshing any actuator controls associated with the node.
   *
   * @param packet the DATA_REPORT packet
   */

  private void handleDataReport(SmartFarmingProtocol packet) {
    if (uiFreeze) {
      bufferedPacket = packet;
      return;
    }

    if (!(packet.getBody() instanceof DataReportBody report)) {
      return;
    }

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

  /**
   * Updates actuator control UI components for a specific node based on
   * the latest {@link DataReportBody}.
   *
   * <p>Only applies reported values to controls that are not currently
   * being edited and do not have a pending override value.</p>
   *
   * @param nodeId the identifier of the node whose controls should be updated
   * @param report the latest data report for that node
   */

  public void updateControlsWithReport(int nodeId, DataReportBody report) {
    Map<String, ActuatorControlUI> controls = uiControlsPerNode.get(nodeId);
    if (controls == null) {
      return;
    }

    Map<String, DataReportBody.ActuatorState> reported = new HashMap<>();
    if (report.actuators() != null) {


      report.actuators().forEach(a -> reported.put(a.id(), a));
    }

    controls.forEach((id, ui) -> {
      DataReportBody.ActuatorState r = reported.get(id);
      if (r == null) {
        return;
      }

      ui.updateValueLabel(r);

      String key = nodeId + ":" + id;
      if (!actuatorPendingValues.containsKey(key) && !ui.isEditing()) {
        ui.applyReportedValue(r);
      }
    });

    nodesList.refresh();
  }

  /**
   * Returns whether the UI is currently frozen for incoming updates.
   *
   * @return {@code true} if the UI is frozen; {@code false} otherwise
   */

  public boolean getUiFreeze() {
    return uiFreeze;
  }

  /**
   * Returns the last buffered packet stored while the UI was frozen.
   *
   * @return the buffered {@link SmartFarmingProtocol} packet, or {@code null} if none
   */

  public SmartFarmingProtocol getBufferedPacket() {
    return bufferedPacket;
  }

  /**
   * Sets whether the UI should temporarily freeze and buffer incoming updates.
   *
   * @param f {@code true} to freeze the UI; {@code false} to resume updates
   */

  public void setUiFreeze(boolean f) {
    uiFreeze = f;
  }

  /**
   * Sets the buffered packet used while the UI is frozen.
   *
   * @param p the packet to buffer, or {@code null} to clear
   */

  public void setBufferedPacket(SmartFarmingProtocol p) {
    bufferedPacket = p;
  }

  /**
   * Called when this controller is unloaded so it can release resources
   * such as event subscriptions.
   *
   * <p>Unsubscribes this controller from the global {@link EventBus}.</p>
   */

  @Override
  public void onUnload() {
    EventBus.unsubscribe(packetListener);
  }
}
