package ntnu.idata2302.sfp.server.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Server-side context that tracks connected nodes, their sockets and
 * subscription relationships.
 *
 * <p>This class holds thread-safe registries used by server handlers:
 * socketRegistry maps logical node IDs to their connected {@link Socket},
 * nodeRegistry stores {@link NodeDescriptor} metadata and subscriptions maps
 * control-panel IDs to lists of {@link SubscribeBody.NodeSubscription} entries.</p>
 *
 * <p>All collections are concurrent implementations to allow safe access from
 * multiple handler threads. Methods perform simple registry operations and I/O
 * actions (sending packets) and therefore may throw {@link IOException} when
 * network operations fail.</p>
 */
public class ServerContext {

  // Each connected node (Sensor or Control Panel | LogicalId, Socket)
  private final Map<Integer, Socket> socketRegistry = new ConcurrentHashMap<>();
  private final Map<Integer, NodeDescriptor> nodeRegistry = new ConcurrentHashMap<>();

  // For each control-panel nodeId → list of per-SN subscriptions
  private final Map<Integer, List<SubscribeBody.NodeSubscription>> subscriptions =
      new ConcurrentHashMap<>();

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
   * @param nodeId the logical node id to remove
   */
  public void unregisterNode(int nodeId) {
    socketRegistry.remove(nodeId);
    nodeRegistry.remove(nodeId);
    System.out.println("Node " + nodeId + " disconnected");
  }

  /**
   * Retrieve the socket for a node.
   *
   * @param nodeId logical id of the node
   * @return the {@link Socket} associated with {@code nodeId}, or {@code null} if not connected
   */
  public Socket getSocket(int nodeId) {
    return socketRegistry.get(nodeId);
  }

  /**
   * Check whether a node is currently registered/connected.
   *
   * @param nodeId logical id of the node
   * @return {@code true} if a socket is registered for {@code nodeId}
   */
  public boolean hasNode(int nodeId) {
    return socketRegistry.containsKey(nodeId);
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
   * @param subId control-panel node id (subscriber id)
   * @param subs  list of {@link SubscribeBody.NodeSubscription} entries associated with {@code subId}
   */
  public void setSubscriptions(int subId, List<SubscribeBody.NodeSubscription> subs) {
    subscriptions.put(subId, subs);
  }

  /**
   * Remove all subscriptions for a control-panel id.
   *
   * @param subId control-panel node id whose subscriptions should be removed
   */
  public void removeSubscriptions(int subId) {
    subscriptions.remove(subId);
  }

  /**
   * Get the list of control-panel ids that subscribe to a specific sensor node.
   *
   * @param sensorNodeId the sensor's logical node id
   * @return a list of control-panel ids (may be empty)
   */
  public List<Integer> getSubscribersForSensorNode(int sensorNodeId) {
    return subscriptions.entrySet()
        .stream()
        .filter(e -> e.getValue().stream()
            .anyMatch(sub -> sub.sensorNodeId() == sensorNodeId))
        .map(Map.Entry::getKey) // control panel IDs
        .toList();
  }

  /**
   * Forward a sensor report packet to all subscribed control-panels.
   *
   * <p>Note: this method mutates the packet header's target id before sending
   * to each subscriber so callers should be aware the provided packet object
   * will be reused/modified.</p>
   *
   * @param packet sensor report {@link SmartFarmingProtocol} whose {@link Header#getSourceId source id}
   *               is used to determine subscribers
   */
  public void sendToSubscribers(SmartFarmingProtocol packet) {
    int sensorId = packet.getHeader().getSourceId();
    for (int cpId : getSubscribersForSensorNode(sensorId)) {
      try {
        packet.getHeader().setTargetId(cpId);
        System.out.println(packet.getHeader().getMessageType());
        System.out.println(packet.getHeader().getTargetId());
        sendTo(packet);
      } catch (IOException e) {
        System.out.println("Failed to send report to CP " + cpId);
      }
    }
  }

  /**
   * Return a snapshot list of registered node descriptors that represent server-side nodes.
   *
   * @return list of {@link NodeDescriptor} instances where {@link NodeDescriptor#nodeType()}
   * equals 1; may be empty
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