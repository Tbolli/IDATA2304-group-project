package ntnu.idata2302.sfp.sensorNode;

import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.core.SimulationLoop;
import ntnu.idata2302.sfp.sensorNode.factory.NodeFactory;
import ntnu.idata2302.sfp.sensorNode.factory.PacketFactory;
import ntnu.idata2302.sfp.sensorNode.net.NetworkLoop;
import ntnu.idata2302.sfp.sensorNode.net.SensorNodeContext;

import java.util.concurrent.atomic.AtomicInteger;

public class StartUp {

  private static final String SERVER_HOST = "localhost";
  private static final int SERVER_PORT = 5050;
  private static final AtomicInteger counter = new AtomicInteger(0);


  public static void main(String[] args) {
    try {
      // Build simulated node
      SensorNode node = NodeFactory.defaultNode();

      // Create network client
      SensorNodeContext client = new SensorNodeContext(SERVER_HOST, SERVER_PORT);
      // Connect to the server
      client.connect();
      // Send initial packet
      client.sendPacket(PacketFactory.buildAnnouncePacket(node));

      // Start network listener & simulation thread
      new Thread(new NetworkLoop(client)).start();
      new Thread(new SimulationLoop(node, client)).start();

      System.out.println("Sensor Node Running.");

    } catch (Exception e) {
      System.err.println("Fatal error in startup: " + e.getMessage());
    }
  }
}
