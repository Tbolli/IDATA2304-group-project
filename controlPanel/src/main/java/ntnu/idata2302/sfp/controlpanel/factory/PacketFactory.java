package ntnu.idata2302.sfp.controlpanel.factory;

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

import java.util.List;
import java.util.UUID;

public class PacketFactory {

  public static SmartFarmingProtocol capabilitiesQuery(int sourceId, int requestId){
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

  public static SmartFarmingProtocol announce(int requestId){
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

  public static SmartFarmingProtocol subscribeNode(int sourceId, int requestId, int sensorNodeId){
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

  public static SmartFarmingProtocol unSubscribeNode(int sourceId, int requestId, int sensorNodeId){
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

  public static SmartFarmingProtocol command(int sourceId, int sensorNodeId, int requestId, List<CommandBody.CommandPart> parts){
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
