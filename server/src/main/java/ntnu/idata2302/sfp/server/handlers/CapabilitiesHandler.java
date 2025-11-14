package ntnu.idata2302.sfp.server.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesListBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeIds;
import ntnu.idata2302.sfp.server.ServerContext;
import ntnu.idata2302.sfp.server.factory.HeaderFactory;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.UUID;


public class CapabilitiesHandler implements MessageHandler {

  @Override
  public void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws IOException {
    Header reqHeader = message.getHeader();
    CapabilitiesQueryBody reqBody = (CapabilitiesQueryBody) message.getBody();

    // Response - Header
    Header resHeader = HeaderFactory.serverHeader(MessageTypes.CAPABILITIES_LIST,reqHeader.getSourceId());

    // Response - Body
    CapabilitiesListBody resBody = new CapabilitiesListBody(
      reqBody.requestId(),
      context.getServerNodeDescriptors()
    );

    context.sendTo(
      client, new SmartFarmingProtocol(resHeader, resBody)
    );
  }
}
