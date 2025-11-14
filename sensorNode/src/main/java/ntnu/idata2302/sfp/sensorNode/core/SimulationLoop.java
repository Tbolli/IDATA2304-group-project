package ntnu.idata2302.sfp.sensorNode.core;

import ntnu.idata2302.sfp.sensorNode.factory.PacketFactory;
import ntnu.idata2302.sfp.sensorNode.net.SensorNodeContext;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;

public class SimulationLoop implements Runnable {

  private final SensorNode node;
  private final SensorNodeContext client;

  public SimulationLoop(SensorNode node, SensorNodeContext client) {
    this.node = node;
    this.client = client;
  }

  @Override
  public void run() {
    try {
      while (true) {
        // update internal sensor/actuator simulation
        node.tick();

        // build and send report packet
        SmartFarmingProtocol report = PacketFactory.buildReportPacket(node);
        client.sendPacket(report);

        Thread.sleep(2000); // send report every 2 seconds
      }
    } catch (InterruptedException ignored) {}
  }
}