package ntnu.idata2302.sfp.sensorNode.core;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.sensorNode.factory.PacketFactory;
import ntnu.idata2302.sfp.sensorNode.net.SensorNodeContext;

/**
 * Runnable that drives a {@link SensorNode} simulation and periodically
 * sends report packets to a {@link SensorNodeContext}.
 *
 * <p>The simulation loop repeatedly advances the node's internal state by
 * calling {@link SensorNode#tick()}, builds a report packet using
 * {@link PacketFactory#buildReportPacket(SensorNode)} and delivers it via
 * {@link SensorNodeContext#sendPacket(SmartFarmingProtocol)}. The loop sleeps
 * between iterations to control the report interval.</p>
 *
 * <p>Behavior notes:
 * <ul>
 *   <li>The loop runs until the thread is interrupted, at which point {@link #run()}
 *       exits quietly.</li>
 *   <li>Any runtime exceptions thrown while building or sending packets will
 *       escape the loop and may terminate the thread; callers should ensure
 *       appropriate error handling in the provided {@code client} if needed.</li>
 * </ul>
 * </p>
 *
 * @see SensorNode
 * @see SensorNodeContext
 * @see PacketFactory
 */
public class SimulationLoop implements Runnable {

  private final SensorNode node;
  private final SensorNodeContext client;

  /**
   * Create a simulation loop for a sensor node and a client context used to
   * send packets.
   *
   * @param node   the {@link SensorNode} whose state will be advanced (must not be {@code null})
   * @param client the {@link SensorNodeContext}
   *               used to deliver report packets (must not be {@code null})
   */
  public SimulationLoop(SensorNode node, SensorNodeContext client) {
    this.node = node;
    this.client = client;
  }

  /**
   * Run the simulation loop.
   *
   * <p>The method performs the following steps in a loop:
   * <ol>
   *   <li>Advance the node simulation via {@link SensorNode#tick()}.</li>
   *   <li>Build a report packet using {@link PacketFactory#buildReportPacket(SensorNode)}.</li>
   *   <li>Send the packet through {@link SensorNodeContext#sendPacket(SmartFarmingProtocol)}.</li>
   *   <li>Sleep for approximately 2000 milliseconds before the next iteration.</li>
   * </ol>
   * The loop continues until the thread is interrupted. If the thread is
   * interrupted, the method returns and the loop stops.</p>
   */
  @Override
  public void run() {
    try {
      while (true) {
        // update internal sensor/actuator simulation
        node.tick();

        // build and send a report packet
        SmartFarmingProtocol report = PacketFactory.buildReportPacket(node);
        client.sendPacket(report);

        Thread.sleep(2000); // send a report every 2 seconds
      }
    } catch (InterruptedException ignored) {
      System.out.println("Simulation loop interrupted");
    }
  }
}