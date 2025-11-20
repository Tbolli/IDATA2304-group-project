package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.server.net.ServerContext;
import ntnu.idata2302.sfp.server.factory.HeaderFactory;
import ntnu.idata2302.sfp.server.util.IdAllocator;

import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class AnnounceHandler implements MessageHandler {

  @Override
  public void handle(SmartFarmingProtocol message,
                     Socket client,
                     ServerContext context) throws IOException {

    // Extract announce request
    AnnounceBody reqBody = (AnnounceBody) message.getBody();
    NodeDescriptor announcedNode = reqBody.descriptor();

    // Allocate ID from allocator
    int givenId = IdAllocator.allocate();

    // Create new descriptor same as incoming, only ID replaced
    NodeDescriptor registeredNode = new NodeDescriptor(
      givenId,
      announcedNode.nodeType(),
      announcedNode.sensors(),
      announcedNode.actuators(),
      announcedNode.supportsImages(),
      announcedNode.supportsAggregates()
    );

    // Register node & its socket
    context.registerNode(givenId, registeredNode, client);

    // Respond with ACK -------------------------
    Header resHeader = HeaderFactory.serverHeader(
      MessageTypes.ANNOUNCE_ACK,
      givenId
    );

    AnnounceAckBody resBody = new AnnounceAckBody(
      reqBody.requestId(),
      1  // success status
    );

    context.sendTo(client, new SmartFarmingProtocol(resHeader, resBody));
  }
}
