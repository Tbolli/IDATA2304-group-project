package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.controlPanel.net.AppContext;
import ntnu.idata2302.sfp.controlPanel.net.EventBus;
import ntnu.idata2302.sfp.controlPanel.net.SfpClient;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesListBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class NodesController implements Unloadable {

  private SfpClient client;

  @FXML private ListView<NodeEntry> nodesList;
  @FXML private Label connectionLabel;

  /** Node storage + observable list for GUI */
  private final Map<Integer, NodeEntry> nodes = new ConcurrentHashMap<>();
  private final ObservableList<NodeEntry> observableNodes = FXCollections.observableArrayList();

  private final Consumer<SmartFarmingProtocol> packetListener = this::handlePacket;

  /* -------------------------------------------------------- */
  /* INITIALIZATION */
  /* -------------------------------------------------------- */
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

  /* -------------------------------------------------------- */
  /* BUTTON ACTIONS */
  /* -------------------------------------------------------- */
  @FXML
  public void refreshCapabilities() {
    nodes.clear();
    observableNodes.clear();

    if (AppContext.getControllerId() != null) {
      sendCapabilitiesQuery();
    } else {
      System.err.println("Cannot request capabilities — no controller ID assigned yet.");
    }
  }

  private void sendCapabilitiesQuery() {
    System.out.println("➡ Requesting CAPABILITIES_LIST");
    client.sendCapabilitiesQuery();
  }

  private void subscribeNode(int nodeId, List<String> sensorNames, List<String> actuatorNames) {
    System.out.println("Subscribing node: " + nodeId);
    client.sendSubscribe(nodeId, sensorNames, actuatorNames);
  }

  private void unsubscribeNode(int nodeId) {
    System.out.println("Unsubscribing node: " + nodeId);
    nodes.remove(nodeId);
    observableNodes.removeIf(n -> n.nodeId() == nodeId);
    // client.sendUnsubscribe(nodeId); // enable when server supports it
  }

  /* -------------------------------------------------------- */
  /* PACKET DISPATCHER */
  /* -------------------------------------------------------- */
  private void handlePacket(SmartFarmingProtocol packet) {
    switch (packet.getHeader().getMessageType()) {
      case MessageTypes.ANNOUNCE_ACK -> handleAnnounceAck(packet);
      case MessageTypes.CAPABILITIES_LIST -> handleCapabilities(packet);
      case MessageTypes.DATA_REPORT -> handleDataReport(packet);
      default -> {}
    }
  }

  private void handleAnnounceAck(SmartFarmingProtocol packet) {
    Header header = packet.getHeader();
    AppContext.setControllerId(header.getTargetId());
    sendCapabilitiesQuery();
  }

  private void handleCapabilities(SmartFarmingProtocol packet) {
    Body body = packet.getBody();
    if (!(body instanceof CapabilitiesListBody capBody)) return;

    capBody.nodes().forEach(nodeDesc -> {
      int id = nodeDesc.nodeId();

      List<String> sensors = nodeDesc.sensors() == null
        ? List.of()
        : nodeDesc.sensors().stream().map(NodeDescriptor.SensorDescriptor::id).toList();

      List<String> actuators = nodeDesc.actuators() == null
        ? List.of()
        : nodeDesc.actuators().stream().map(NodeDescriptor.ActuatorDescriptor::id).toList();

      nodes.putIfAbsent(id, new NodeEntry(id, null));

      Platform.runLater(() -> {
        observableNodes.removeIf(n -> n.nodeId() == id);
        observableNodes.add(nodes.get(id));
      });

      subscribeNode(id, sensors, actuators);
    });
  }

  private void handleDataReport(SmartFarmingProtocol packet) {
    DataReportBody report = (DataReportBody) packet.getBody();
    int nodeId = packet.getHeader().getSourceId();

    NodeEntry updated = new NodeEntry(nodeId, report);
    nodes.put(nodeId, updated);

    Platform.runLater(() -> {
      observableNodes.removeIf(n -> n.nodeId() == nodeId);
      observableNodes.add(updated);
      nodesList.refresh();
    });
  }

  @Override
  public void onUnload() {
    EventBus.unsubscribe(packetListener);
  }

  /* Navigation */
  @FXML
  private void openDataLog() {
    SceneManager.switchScene("dataLog");
  }

  /* -------------------------------------------------------- */
  /* LIST CELL */
  /* -------------------------------------------------------- */
  private class NodeCell extends ListCell<NodeEntry> {
    @Override
    protected void updateItem(NodeEntry entry, boolean empty) {
      super.updateItem(entry, empty);

      if (empty || entry == null) {
        setGraphic(null);
        return;
      }

      VBox card = new VBox(10);
      card.getStyleClass().add("node-card");

      Label title = new Label("Node #" + entry.nodeId());
      title.getStyleClass().add("node-header");

      VBox sensors = new VBox(4);
      VBox actuators = new VBox(4);

      if (entry.data() != null) {
        if (entry.data().sensors() != null) {
          entry.data().sensors().forEach(s ->
            sensors.getChildren().add(new Label(
              "• " + s.id() + " = " + (s.value() != null ? s.value() : "N/A") +
                (s.unit() != null ? " " + s.unit() : "")
            )));
        }

        if (entry.data().actuators() != null) {
          entry.data().actuators().forEach(a ->
            actuators.getChildren().add(new Label(
              "• " + a.id() + " = " + (a.value() != null ? a.value() : "N/A") +
                (a.unit() != null ? " " + a.unit() : "")
            )));
        }
      }

      Button unsub = new Button("Unsubscribe");
      unsub.setOnAction(e -> unsubscribeNode(entry.nodeId()));

      HBox content = new HBox(40, sensors, actuators);
      card.getChildren().addAll(title, content, unsub);

      setGraphic(card);
    }
  }

  /* -------------------------------------------------------- */
  /* DATA MODEL */
  /* -------------------------------------------------------- */
  public record NodeEntry(int nodeId, DataReportBody data) {}
}
