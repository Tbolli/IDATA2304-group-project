package ntnu.idata2302.sfp.server.net.handlers;

import java.io.IOException;
import java.net.Socket;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.entity.Subscription;
import ntnu.idata2302.sfp.server.factory.HeaderFactory;
import ntnu.idata2302.sfp.server.net.ServerContext;





/**
 * Handles SUBSCRIBE messages sent by control-panel nodes.
 *
 * <p>
 * When a control-panel requests to subscribe to updates from a specific
 * sensor node, this handler:
 * <ul>
 *   <li>Extracts the subscription request</li>
 *   <li>Registers the subscription in the server context</li>
 *   <li>Sends a SUBSCRIBE_ACK response back to the requester</li>
 * </ul>
 * This enables the publish-subscribe mechanism used for forwarding sensor reports
 * to the appropriate control-panel nodes.
 */

public class SubscribeHandler implements MessageHandler {

  /**
   * Processes an incoming SUBSCRIBE request from a control-panel node.
   *
   * <p>
   * The method creates a new Subscription linking the requesting control-panel
   * with the target sensor node, stores it in the server context, and replies
   * with a SUBSCRIBE_ACK message indicating success.
   *
   * @param message the protocol packet containing the SUBSCRIBE request
   * @param client  the socket representing the requesting control-panel node
   * @param context the shared server context used to store subscriptions
   * @throws IOException if an error occurs while sending the acknowledgement
   */

  @Override
  public void handle(SmartFarmingProtocol message,
                     Socket client, ServerContext context) throws IOException {
    Header reqHeader = message.getHeader();
    SubscribeBody reqBody = (SubscribeBody) message.getBody();
    int cpId = reqHeader.getSourceId();

    Subscription subscription = new Subscription(
          cpId,
         reqBody.sensorNodeId()
    );

    // Set subscriptions
    context.setSubscription(subscription);

    // Response - Header
    Header resHeader = HeaderFactory
         .serverHeader(MessageTypes.SUBSCRIBE_ACK, reqHeader.getSourceId());

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
