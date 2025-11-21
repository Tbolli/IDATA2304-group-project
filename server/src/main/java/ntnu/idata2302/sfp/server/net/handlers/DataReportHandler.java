package ntnu.idata2302.sfp.server.net.handlers;

import java.io.IOException;
import java.net.Socket;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.server.net.ServerContext;

/**
 * Handles incoming DATA_REPORT messages from sensor nodes.
 *
 * <p>When a sensor node reports new sensor or actuator state data,
 * this handler forwards that report to all control-panel nodes
 * subscribed to the reporting sensor node.
 * </p>
 */

public class DataReportHandler implements MessageHandler {

  /**
   * Forwards an incoming DATA_REPORT message to all subscribers
   * of the reporting sensor node.
   *
   * @param message the protocol packet containing sensor/actuator data
   * @param client  the socket of the reporting sensor node
   * @param context the server context used to look up subscriptions
   * @throws IOException if forwarding the report to any subscriber fails
   */

  @Override
  public void handle(SmartFarmingProtocol message,
                     Socket client, ServerContext context) throws IOException {
    // Direct message to all subscribers of the node
    context.sendToSubscribers(message);
  }
}
