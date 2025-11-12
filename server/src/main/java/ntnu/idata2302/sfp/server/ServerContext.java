package ntnu.idata2302.sfp.server;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerContext {
  // Each connected node (Sensor or Control Panel | LogicalId, Socket)
  private final Map<Integer, Socket> nodeRegistry = new ConcurrentHashMap<>();

  public void registerNode(int nodeId, Socket socket) {
    nodeRegistry.put(nodeId, socket);
    System.out.println("Registered node " + nodeId + " (" + socket.getInetAddress() + ")");
  }

  public void unregisterNode(int nodeId) {
    nodeRegistry.remove(nodeId);
    System.out.println("Node " + nodeId + " disconnected");
  }

  public Socket getSocket(int nodeId) {
    return nodeRegistry.get(nodeId);
  }

  public boolean hasNode(int nodeId) {
    return nodeRegistry.containsKey(nodeId);
  }

  // Send to specific node
  public void sendTo(int targetId, SmartFarmingProtocol packet) throws IOException {
    Socket targetSocket = nodeRegistry.get(targetId);
    if (targetSocket == null || targetSocket.isClosed()) {
      System.out.println("Cannot send to " + targetId + " — not connected.");
      return;
    }
    targetSocket.getOutputStream().write(packet.toBytes());
    targetSocket.getOutputStream().flush();
  }

  // Reply to the same client
  public void replyToSender(SmartFarmingProtocol original, SmartFarmingProtocol reply) throws IOException {
    int sourceId = original.getHeader().getSourceId();
    sendTo(sourceId, reply);
  }

  // Broadcast to all nodes
  public void broadcast(SmartFarmingProtocol packet) {
    for (Socket s : nodeRegistry.values()) {
      try {
        s.getOutputStream().write(packet.toBytes());
        s.getOutputStream().flush();
      } catch (IOException e) {
        System.out.println("Failed to send broadcast: " + e.getMessage());
      }
    }
  }
}
