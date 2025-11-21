package ntnu.idata2302.sfp.server.net;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.server.entity.Subscription;



/**
 * Server-side context that tracks connected nodes, their sockets and
 * subscription relationships.
 *
 * <p>This class holds thread-safe registries used by server handlers:
 * socketRegistry maps logical node IDs to their connected {@link Socket},
 * nodeRegistry stores {@link NodeDescriptor} metadata, and subscriptions
 * stores a flat list of {@link Subscription} entries linking control-panel
 * IDs to sensor-node IDs.</p>
 *
 * <p>All collections are concurrent implementations to allow safe access from
 * multiple handler threads. Methods perform registry operations and I/O
 * actions (sending packets), and therefore may throw {@link IOException}
 * when network operations fail.</p>
 */
public class ServerContext {

  // Each connected node (Sensor or Control Panel | LogicalId, Socket)
  private final Map<Integer, Socket> socketRegistry = new ConcurrentHashMap<>();
  private final Map<Integer, NodeDescriptor> nodeRegistry = new ConcurrentHashMap<>();

  // Stores all subscription relationships as flat entries where each
  // Subscription links one control-panel node ID (cpId) to one sensor-node ID (snId)
  private final List<Subscription> subscriptions = new CopyOnWriteArrayList<>();

  /**
   * Register a connected node.
   *
   * @param nodeId the logical node id assigned to the connection
   * @param node   the {@link NodeDescriptor} describing the node (may be {@code null})
   * @param socket the connected {@link Socket} associated with the node
   */
  public void registerNode(int nodeId, NodeDescriptor node, Socket socket) {
    nodeRegistry.put(nodeId, node);
    socketRegistry.put(nodeId, socket);
    System.out.println("Registered node " + nodeId + " (" + socket.getInetAddress() + ")");
  }

  /**
   * Unregister a node and remove its socket association.
   *
   * @param socket the socket of the client
   */
  public void unregisterNode(Socket socket) {
    Integer nodeId = socketRegistry.entrySet().stream()
         .filter(e -> e.getValue().equals(socket))
          .map(Map.Entry::getKey)
          .findFirst()
         .orElse(null);

    if (nodeId == null) {
      return;

    }
    removeAllSubscriptions(nodeId);
    socketRegistry.remove(nodeId);
    nodeRegistry.remove(nodeId);
    System.out.println("Node: #" + nodeId + " removed from sever ");
  }

  /**
   * Send a packet to the node identified by the packet's header target id.
   *
   * <p>This method looks up the socket for the packet's target id and writes
   * the packet bytes to it. If the target is not connected or the socket is
   *  closed, the method logs and returns without throwing.</p>
   *
   * @param packet the {@link SmartFarmingProtocol} packet to send (must not be {@code null})
   * @throws IOException if writing to the socket fails
   */
  public void sendTo(SmartFarmingProtocol packet) throws IOException {
    int targetId = packet.getHeader().getTargetId();
    Socket targetSocket = socketRegistry.get(targetId);
    if (targetSocket == null || targetSocket.isClosed()) {
      System.out.println("Cannot send to " + targetId + " — not connected.");
      return;
    }
    targetSocket.getOutputStream().write(packet.toBytes());
    targetSocket.getOutputStream().flush();
  }

  /**
   * Send a packet to a specific node id.
   *
   * @param nodeId the logical node id of the destination
   * @param packet the {@link SmartFarmingProtocol} packet to send
   * @throws IOException if writing to the socket fails
   */

  public void sendTo(int nodeId, SmartFarmingProtocol packet) throws IOException {
    Socket targetSocket = socketRegistry.get(nodeId);
    if (targetSocket == null || targetSocket.isClosed()) {
      System.out.println("Cannot send to " + nodeId + " — not connected.");
      return;
    }
    targetSocket.getOutputStream().write(packet.toBytes());
    targetSocket.getOutputStream().flush();
  }

  /**
   * Send a packet to a specific socket.
   *
   * @param socket destination {@link Socket}
   * @param packet the {@link SmartFarmingProtocol} packet to send
   * @throws IOException if writing to the socket fails
   */

  public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
    socket.getOutputStream().write(packet.toBytes());
    socket.getOutputStream().flush();
  }

  /**
   * Store subscriptions for a control-panel id.
   *
   * @param subscription the subscription to store
   */

  public void setSubscription(Subscription subscription) {
    subscriptions.add(subscription);
  }

  /**
   * Remove all subscriptions for a control-panel id.
   *
   * @param cpId control-panel node id
   * @param snId sensorNode id
   */

  public void removeSubscription(int cpId, int snId) {
    subscriptions.removeIf(s -> s.getSnId() == snId && s.getCpId() == cpId);
  }

  /**
   * Remove all subscriptions related to a specific node id.
   *
   * <p>This removes any subscription where the given id is either
   * the control-panel id or the sensor-node id.</p>
   *
   * @param id the node id whose subscriptions should be removed
   */

  public void removeAllSubscriptions(int id) {
    subscriptions.removeIf(s -> s.getSnId() == id || s.getCpId() == id);
  }

  /**
   * Get the list of control-panel ids that subscribe to a specific sensor node.
   *
   * @param sensorNodeId the sensor's logical node id
   * @return a list of control-panel ids (may be empty)
   */
  public List<Integer> getSubscribersForSensorNode(int sensorNodeId) {
    return subscriptions.stream()
      .filter(s -> s.getSnId() == sensorNodeId)
      .map(Subscription::getCpId)
      .toList();
  }

  /**
   * Forward a sensor report packet to all control-panel nodes subscribed
   * to the reporting sensor node.
   *
   * @param packet the sensor report {@link SmartFarmingProtocol} packet
   *               whose source ID identifies the sensor node
   */

  public void sendToSubscribers(SmartFarmingProtocol packet) {
    int sensorId = packet.getHeader().getSourceId();
    for (int cpId : getSubscribersForSensorNode(sensorId)) {
      try {
        sendTo(cpId, packet);
      } catch (IOException e) {
        System.out.println("Failed to send report to CP " + cpId);
      }
    }
  }

  /**
   * Return a snapshot list of registered node descriptors that represent server-side nodes.
   *
   * @return list of {@link NodeDescriptor} instances where {@link NodeDescriptor#nodeType()}
   *          equals 1; may be empty
   */

  public List<NodeDescriptor> getServerNodeDescriptors() {
    return nodeRegistry.values()
        .stream()
        .filter(desc -> desc != null && desc.nodeType() == 1)
        .collect(Collectors.toList());
  }

  /**
   * Broadcast a packet to all currently connected sockets.
   *
   * <p>The method attempts to write to every socket in the registry; failures
   * for individual sockets are logged but do not interrupt the broadcast to
   * other sockets.</p>
   *
   * @param packet the {@link SmartFarmingProtocol} packet to broadcast
   */

  public void broadcast(SmartFarmingProtocol packet) {
    for (Socket s : socketRegistry.values()) {
      try {
        s.getOutputStream().write(packet.toBytes());
        s.getOutputStream().flush();
      } catch (IOException e) {
        System.out.println("Failed to send broadcast: " + e.getMessage());
      }
    }
  }
}