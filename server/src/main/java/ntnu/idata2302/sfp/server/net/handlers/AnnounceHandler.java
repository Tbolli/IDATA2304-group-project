package ntnu.idata2302.sfp.server.net.handlers;

import java.io.IOException;
import java.net.Socket;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.server.factory.HeaderFactory;
import ntnu.idata2302.sfp.server.net.ServerContext;
import ntnu.idata2302.sfp.server.util.IdAllocator;

/**
 * Handles ANNOUNCE messages sent by sensor or control-panel nodes.
 *
 * <p>When a new node connects and announces itself, this handler:
 * <ul>
 *   <li>Extracts the node's descriptor from the ANNOUNCE message</li>
 *   <li>Allocates and assigns a unique node ID</li>
 *   <li>Registers the node and its socket in the server context</li>
 *   <li>Sends an ANNOUNCE_ACK response back to the node</li>
 * </ul>
 * This allows the server to keep track of newly connected nodes and
 * establish their identity before any further communication.
 */

public class AnnounceHandler implements MessageHandler {

  /**
   * Handles an incoming ANNOUNCE message from a node.
   *
   * @param message The incoming SmartFarmingProtocol message containing the ANNOUNCE request.
   * @param client  The socket connection to the announcing node.
   * @param context The server context for managing nodes and communication.
   * @throws IOException If an I/O error occurs during message handling.
   */

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
