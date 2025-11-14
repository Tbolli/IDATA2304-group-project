package ntnu.idata2302.sfp.server;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class ServerContext {
  // Each connected node (Sensor or Control Panel | LogicalId, Socket)
  private Map<Integer, Socket> socketRegistry = new ConcurrentHashMap<>();
  private Map<Integer, NodeDescriptor> nodeRsegistry = new ConcurrentHashMap<>();

  public void registerNode(int nodeId, NodeDescriptor node, Socket socket) {
    nodeRsegistry.put(nodeId,node);
    socketRegistry.put(nodeId,socket);
    System.out.println("Registered node " + nodeId + " (" + socket.getInetAddress() + ")");
  }

  public void unregisterNode(int nodeId) {
    socketRegistry.remove(nodeId);
    System.out.println("Node " + nodeId + " disconnected");
  }

  public Socket getSocket(int nodeId) {
    return socketRegistry.get(nodeId);
  }

  public boolean hasNode(int nodeId) {
    return socketRegistry.containsKey(nodeId);
  }

  // Send to specific node with ID
  public void sendTo(int targetId, SmartFarmingProtocol packet) throws IOException {
    Socket targetSocket = socketRegistry.get(targetId);
    if (targetSocket == null || targetSocket.isClosed()) {
      System.out.println("Cannot send to " + targetId + " — not connected.");
      return;
    }
    targetSocket.getOutputStream().write(packet.toBytes());
    targetSocket.getOutputStream().flush();
  }

  // Send to specific node with Socket
  public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
    socket.getOutputStream().write(packet.toBytes());
    socket.getOutputStream().flush();
  }

  public List<NodeDescriptor> getServerNodeDescriptors() {
    return nodeRsegistry.values()
      .stream()
      .filter(desc -> desc != null && desc.nodeType() == 1)
      .collect(Collectors.toList());
  }

  // Broadcast to all nodes
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
