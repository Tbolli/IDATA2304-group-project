package ntnu.idata2302.sfp.server.net.handlers;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.factory.HeaderFactory;
import ntnu.idata2302.sfp.server.net.ServerContext;

/**
 * Handles UNSUBSCRIBE messages sent by control-panel nodes.
 *
 * <p>When a control-panel requests to stop receiving updates from a specific
 * sensor node, this handler:
 * <ul>
 *   <li>Extracts the unsubscribe request</li>
 *   <li>Removes the corresponding subscription from the server context</li>
 *   <li>Sends an UNSUBSCRIBE_ACK response back to the requester</li>
 * </ul>
 * This enables clients to dynamically manage which sensor nodes they receive reports from.
 */

public class UnSubscribeHandler implements MessageHandler {
  private final AtomicInteger counter = new AtomicInteger(1);

  /**
   * Processes an UNSUBSCRIBE request from a control-panel node.
   *
   * <p>The method removes the subscription linking the requesting control-panel
   * node with the specified sensor node, and replies with an
   * UNSUBSCRIBE_ACK message confirming successful removal.
   *
   * @param message the protocol packet containing the UNSUBSCRIBE request
   * @param client  the socket of the requesting control-panel node
   * @param context the shared server context used to modify subscription records
   * @throws IOException if sending the acknowledgement back to the client fails
   */

  @Override
  public void handle(SmartFarmingProtocol message,
                     Socket client, ServerContext context) throws IOException {
    Header reqHeader = message.getHeader();
    UnsubscribeBody reqBody = (UnsubscribeBody) message.getBody();

    // Remove subscriptions
    context.removeSubscription(reqHeader.getSourceId(), reqBody.sensorNodeId());

    // Response - Header
    Header resHeader = HeaderFactory
          .serverHeader(MessageTypes.UNSUBSCRIBE_ACK, reqHeader.getSourceId());

    // Response - Body
    SubscribeAckBody resBody = new SubscribeAckBody(
          reqBody.requestId(),
          1
    );

    // Send to client
    context.sendTo(
          client, new SmartFarmingProtocol(resHeader, resBody)
    );

  }
}
