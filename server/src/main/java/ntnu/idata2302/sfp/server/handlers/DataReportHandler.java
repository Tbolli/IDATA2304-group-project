package ntnu.idata2302.sfp.server.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.data.DataRequestBody;
import ntnu.idata2302.sfp.server.ServerContext;

import java.net.Socket;

public class DataReportHandler implements MessageHandler {

  @Override
  public void handle(SmartFarmingProtocol message, Socket client, ServerContext server) {
    DataRequestBody body = (DataRequestBody) message.getBody();
    System.out.printf(
      "DATA_REPORT | FROM: %d | UUID: %s " , message.getHeader().getSourceId(), message.getHeader().getMessageId()
    );
  }

}
