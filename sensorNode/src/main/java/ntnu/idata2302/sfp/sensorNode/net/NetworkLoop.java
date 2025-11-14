package ntnu.idata2302.sfp.sensorNode.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;

/**
 * A dedicated thread that continuously reads packets
 * from the server. This thread will never block
 * sensor simulation.
 */
public class NetworkLoop implements Runnable {

  private final SensorNodeContext client;

  public NetworkLoop(SensorNodeContext client) {
    this.client = client;
  }

  @Override
  public void run() {
    try {
      while (client.isConnected()) {
        SmartFarmingProtocol packet = client.readOnePacket();
        PacketHandler.handle(client,packet);
      }
    } catch (Exception e) {
      System.out.println("Network loop stopped: " + e.getMessage());
    }
  }
}
