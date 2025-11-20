package ntnu.idata2302.sfp.server.net.handlers;

import java.net.Socket;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeIds;
import ntnu.idata2302.sfp.server.net.ServerContext;

/**
 * Handles forwarding of packets that are intended for other nodes.
 *
 * <p>
 * This handler inspects incoming messages and:
 * <ul>
 *   <li>Logs a warning if a COMMAND or COMMAND_ACK is incorrectly addressed to the server</li>
 *   <li>Logs details of ERROR packets addressed to the server</li>
 *   <li>Forwards any other packet to its intended target node</li>
 * </ul>
 * It acts as a general router for internode communication, ensuring packets
 * reach the correct destination.
 */

public class ForwardPacketHandler implements MessageHandler {

  /**
   * Processes and routes an incoming protocol message.
   *
   * <p>
   * Depending on the message type and its target, this method:
   * <ul>
   *   <li>Warns if a COMMAND or COMMAND_ACK is mistakenly addressed to the server</li>
   *   <li>Logs ERROR packets that target the server</li>
   *   <li>Forwards all other packets to the appropriate node using the server context</li>
   * </ul>
   *
   * @param message the protocol packet received from a client
   * @param client  the socket from which the message originated
   * @param context the server context used for forwarding or handling the packet
   * @throws Exception if forwarding the message fails or unexpected errors occur
   */

  @Override
  public void handle(SmartFarmingProtocol message,
                     Socket client, ServerContext context) throws Exception {

    MessageTypes type = message.getHeader().getMessageType();
    int targetId = message.getHeader().getTargetId();

    // If the server is the intended target for a COMMAND / COMMAND_ACK, notify and stop.
    if (targetId == NodeIds.SERVER
         && (type == MessageTypes.COMMAND || type == MessageTypes.COMMAND_ACK)) {

      System.out.println("[WARN] Server was the intended target for COMMAND / COMMAND_ACK.");
      return;
    }

    // If the server is the intended target for an ERROR packet, print it and stop.
    if (targetId == NodeIds.SERVER && type == MessageTypes.ERROR) {

      if (message.getBody() instanceof ErrorBody errorBody) {
        System.out.printf(
             "[ERROR] Code: %d | Text: %s%n",
              errorBody.errorCode(),
              errorBody.errorText()
        );
      } else {
        System.out.println("[ERROR] Invalid ERROR packet: missing ErrorBody.");
      }

      return;
    }

    // Default: forward the packet to its destination.
    context.sendTo(message);
  }
}
