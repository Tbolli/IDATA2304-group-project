package ntnu.idata2302.sfp.server.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.net.ServerContext;
import ntnu.idata2302.sfp.server.factory.HeaderFactory;
import ntnu.idata2302.sfp.server.util.IdAllocator;

import java.io.IOException;
import java.net.Socket;

public class AnnounceHandler implements MessageHandler{

  @Override
  public void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws IOException {
    AnnounceBody reqBody = (AnnounceBody) message.getBody();

    // Register node
    int givenId = IdAllocator.allocate();
    context.registerNode(
      givenId,
      reqBody.descriptor(),
      client
    );

    // Response - Header
    Header resHeader = HeaderFactory.serverHeader(MessageTypes.ANNOUNCE_ACK,givenId);

    // Response - Body
    AnnounceAckBody resBody = new AnnounceAckBody(
      reqBody.requestId(),
      1
    );

    context.sendTo(
      client, new SmartFarmingProtocol(resHeader, resBody)
    );
  }
}
