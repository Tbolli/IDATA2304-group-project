package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeIds;
import ntnu.idata2302.sfp.server.net.ServerContext;

import java.net.Socket;

public class ForwardPacketHandler implements MessageHandler {

  @Override
  public void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws Exception {

    MessageTypes type = message.getHeader().getMessageType();
    int targetId = message.getHeader().getTargetId();

    // If the server is the intended target for a COMMAND / COMMAND_ACK, notify and stop.
    if (targetId == NodeIds.SERVER &&
      (type == MessageTypes.COMMAND || type == MessageTypes.COMMAND_ACK)) {

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
