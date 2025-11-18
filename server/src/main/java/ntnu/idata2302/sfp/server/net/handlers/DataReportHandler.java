package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.server.net.ServerContext;

import java.io.IOException;
import java.net.Socket;

public class DataReportHandler implements MessageHandler {

  @Override
  public void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws IOException {
    // Direct message to all subscribers of the node
    // TODO -REMOVE print
    System.out.println(context.getSubscribersForSensorNode(message.getHeader().getSourceId()));
    context.sendToSubscribers(message);
  }
}
