package ntnu.idata2302.sfp.sensorNode.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;

/**
 * A dedicated thread that continuously reads protocol packets from a server
 * connection and delegates processing to {@link PacketHandler}.
 *
 * <p>This runnable is intended to run in its own thread, so packet I/O and
 * processing never block the sensor simulation thread. The loop repeatedly
 * reads a single {@link SmartFarmingProtocol} packet from the provided
 * {@link SensorNodeContext} and forwards it to {@link PacketHandler#handle}.</p>
 *
 * <p>The loop continues while {@link SensorNodeContext#isConnected()} returns
 * {@code true}. Any exception thrown while reading or handling a packet will
 * stop the loop; the exception is caught, and a short message is printed.</p>
 *
 * @see SensorNodeContext
 * @see PacketHandler
 */
public class NetworkLoop implements Runnable {

  private final SensorNodeContext client;

  /**
   * Create a network loop bound to the given client context.
   *
   * @param client the {@link SensorNodeContext} used to read and send packets;
   *               must not be {@code null}
   */
  public NetworkLoop(SensorNodeContext client) {
    this.client = client;
  }

  /**
   * Run the network reading loop.
   *
   * <p>The method repeatedly performs:
   * <ol>
   *   <li>Check that the client connection is active via {@link
   *   SensorNodeContext#isConnected()}</li>
   *   <li>Read a single {@link SmartFarmingProtocol} packet using
   *   {@link SensorNodeContext#readOnePacket()}</li>
   *   <li>Dispatch the packet to {@link PacketHandler#handle(SensorNodeContext,
   *   SmartFarmingProtocol)}</li>
   * </ol>
   * The loop executes in the calling thread until the connection is closed or
   * an exception occurs. Exceptions are caught; the loop stops and a message
   * is printed to standard output.</p>
   *
   * <p>Note: blocking I/O performed by {@link SensorNodeContext#readOnePacket()}
   * occurs only in this thread and will not block the sensor simulation thread.</p>
   */
  @Override
  public void run() {
    try {
      while (client.isConnected()) {
        SmartFarmingProtocol packet = client.readOnePacket();
        PacketHandler.handle(client, packet);
      }
    } catch (Exception e) {
      System.out.println("Network loop stopped: " + e.getMessage());
    }
  }
}