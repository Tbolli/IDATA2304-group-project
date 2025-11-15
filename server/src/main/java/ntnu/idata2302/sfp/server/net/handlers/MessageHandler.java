package ntnu.idata2302.sfp.server.net.handlers;


import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.server.net.ServerContext;

import java.net.Socket;

public interface MessageHandler {
  void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws Exception;
}