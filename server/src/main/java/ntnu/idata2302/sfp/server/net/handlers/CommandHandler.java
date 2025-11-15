package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.server.net.ServerContext;

import java.net.Socket;

public class CommandHandler implements MessageHandler {

  @Override
  public void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws Exception {
    // Send to sensor node
    context.sendTo(message);
  }
}
