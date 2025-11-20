package ntnu.idata2302.sfp.server.net.handlers;

import java.io.IOException;
import java.net.Socket;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesListBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.factory.HeaderFactory;
import ntnu.idata2302.sfp.server.net.ServerContext;



/**
 * Handles CAPABILITIES_QUERY messages sent by control-panel or sensor nodes.
 *
 * <p>
 * When a node requests the list of known node descriptors, this handler:
 * <ul>
 *   <li>Reads the incoming CAPABILITIES_QUERY request</li>
 *   <li>Collects all registered node descriptors from the server context</li>
 *   <li>Builds and sends a CAPABILITIES_LIST response back to the requester</li>
 * </ul>
 * This allows nodes to discover available devices and their capabilities in the system.
 */

public class CapabilitiesHandler implements MessageHandler {

  /**
   * Processes an incoming CAPABILITIES_QUERY message.
   *
   * <p>
   * This method extracts the request information, creates a response header and body
   * containing the current list of registered node descriptors, and sends a
   * CAPABILITIES_LIST packet back to the requesting client.
   *
   * @param message the parsed protocol packet containing the query
   * @param client  the TCP socket representing the requesting node
   * @param context the shared server context providing registered node information
   * @throws IOException if sending the response back to the client fails
   */

  @Override
  public void handle(SmartFarmingProtocol message,
                     Socket client, ServerContext context) throws IOException {
    Header reqHeader = message.getHeader();
    CapabilitiesQueryBody reqBody = (CapabilitiesQueryBody) message.getBody();

    // Response - Header
    Header resHeader = HeaderFactory
          .serverHeader(MessageTypes.CAPABILITIES_LIST,
            reqHeader.getSourceId());


    // Response - Body
    CapabilitiesListBody resBody = new CapabilitiesListBody(
         reqBody.requestId(),
         context.getServerNodeDescriptors()
    );
    // TODO remove
    System.out.println("Getting descriptors");
    System.out.println(context.getServerNodeDescriptors());

    context.sendTo(
         client, new SmartFarmingProtocol(resHeader, resBody)
    );
  }
}
