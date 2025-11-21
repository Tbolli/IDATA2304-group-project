package ntnu.idata2302.sfp.controlpanel.factory;

import java.util.List;
import java.util.UUID;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeIds;



/**
 * Factory class for constructing outgoing Smart Farming Protocol messages
 * used by the control panel application.
 *
 * <p>This class centralizes the creation of headers and bodies for the
 * controller-to-server and controller-to-node communication.</p>
 *
 * <p>All packets created here follow SFP conventions:
 * <ul>
 *   <li>Protocol prefix: {@code "SFP"}</li>
 *   <li>Automatic UUID generation for {@code messageId}</li>
 *   <li>Payload length is set to 0 (computed during encoding)</li>
 * </ul>
 * </p>
 */

public class PacketFactory {

  /**
   * Builds a CAPABILITIES_QUERY packet requesting the server to send
   * the list of known sensor nodes.
   *
   * @param sourceId  the ID of the controller sending the query
   * @param requestId unique request identifier
   * @return the constructed protocol packet
   */

  public static SmartFarmingProtocol capabilitiesQuery(int sourceId, int requestId) {
    Header header = new Header(
          new byte[]{ 'S', 'F', 'P' },
          (byte) 1,
          MessageTypes.CAPABILITIES_QUERY,
          sourceId,
          NodeIds.SERVER,
          0,
          UUID.randomUUID()
    );

    CapabilitiesQueryBody body = new CapabilitiesQueryBody(requestId);

    return new SmartFarmingProtocol(header, body);
  }

  /**
   * Builds an ANNOUNCE packet used when the controller first connects.
   *
   * <p>The ANNOUNCE packet contains a minimal {@link NodeDescriptor} with
   * placeholders, since the controller itself is not a sensor node.</p>
   *
   * @param requestId unique request identifier
   * @return the constructed ANNOUNCE protocol packet
   */

  public static SmartFarmingProtocol announce(int requestId) {
    Header header = new Header(
          new byte[]{ 'S', 'F', 'P' },
          (byte) 0,
          MessageTypes.ANNOUNCE,
          NodeIds.BROADCAST,
          NodeIds.SERVER,
          0,
          UUID.randomUUID()
    );

    AnnounceBody body = new AnnounceBody(
          requestId,
          new NodeDescriptor(null, 0, null, null, null, null)
    );

    return new SmartFarmingProtocol(header, body);
  }

  /**
   * Builds a SUBSCRIBE packet. This instructs the server to start forwarding
   * DATA_REPORT messages for the given sensor node.
   *
   * @param sourceId     controller ID
   * @param requestId    unique request identifier
   * @param sensorNodeId ID of the node the controller wants to subscribe to
   * @return the constructed SUBSCRIBE packet
   */

  public static SmartFarmingProtocol subscribeNode(int sourceId, int requestId, int sensorNodeId) {
    Header header = new Header(
          new byte[]{ 'S', 'F', 'P' },
          (byte) 1,
          MessageTypes.SUBSCRIBE,
          sourceId,
          NodeIds.SERVER,
          0,
          UUID.randomUUID()
    );

    SubscribeBody body = new SubscribeBody(
          requestId,
          sensorNodeId
    );

    return new SmartFarmingProtocol(header, body);
  }

  /**
   * Builds an UNSUBSCRIBE packet. This instructs the server to stop sending
   * DATA_REPORT updates for the given sensor node.
   *
   * @param sourceId     controller ID
   * @param requestId    unique request identifier
   * @param sensorNodeId ID of the node the controller wants to unsubscribe from
   * @return the constructed UNSUBSCRIBE packet
   */

  public static SmartFarmingProtocol unSubscribeNode(int sourceId,
                                                     int requestId, int sensorNodeId) {
    Header header = new Header(
          new byte[]{ 'S', 'F', 'P' },
          (byte) 1,
          MessageTypes.UNSUBSCRIBE,
          sourceId,
          NodeIds.SERVER,
          0,
          UUID.randomUUID()
    );

    UnsubscribeBody body = new UnsubscribeBody(
          requestId,
          sensorNodeId
    );

    return new SmartFarmingProtocol(header, body);
  }

  /**
   * Builds a COMMAND packet to send actuator updates to a specific sensor node.
   *
   * @param sourceId      controller ID
   * @param sensorNodeId  target sensor node ID
   * @param requestId     unique request identifier
   * @param parts         list of actuator updates (value changes)
   * @return the constructed COMMAND protocol packet
   */

  public static SmartFarmingProtocol command(int sourceId, int sensorNodeId,
                                             int requestId, List<CommandBody.CommandPart> parts) {
    Header header = new Header(
          new byte[]{ 'S', 'F', 'P' },
          (byte) 1,
          MessageTypes.COMMAND,
          sourceId,
          sensorNodeId,
          0,
          UUID.randomUUID()
    );


    CommandBody body = new CommandBody(
          requestId,
          parts
    );

    return new SmartFarmingProtocol(header, body);
  }

}
