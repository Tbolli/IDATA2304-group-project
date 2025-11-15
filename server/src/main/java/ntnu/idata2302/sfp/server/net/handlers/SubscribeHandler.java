package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.net.ServerContext;
import ntnu.idata2302.sfp.server.factory.HeaderFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class SubscribeHandler implements MessageHandler{
  private final AtomicInteger counter = new AtomicInteger(1);

  @Override
  public void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws IOException {
    Header reqHeader = message.getHeader();
    SubscribeBody reqBody = (SubscribeBody) message.getBody();
    int subId = counter.getAndIncrement();

    // Set subscriptions
    context.setSubscriptions(subId, reqBody.nodes());

    // Response - Header
    Header resHeader = HeaderFactory.serverHeader(MessageTypes.SUBSCRIBE_ACK,reqHeader.getSourceId());

    // Response - Body
    SubscribeAckBody resBody = new SubscribeAckBody(
      reqBody.requestId(),
      subId,
      1
    );

    // Send to client
    context.sendTo(
      client, new SmartFarmingProtocol(resHeader, resBody)
    );

  }
}
