package ntnu.idata2302.sfp.controlPanel.factory;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeIds;

import java.util.ArrayList;
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

  public static SmartFarmingProtocol Announce(int requestId){
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

  public static SmartFarmingProtocol SubscribeSingularNode(int sourceId, int requestId, int nodeId, List<String> sensorNames, List<String> actuatorNames){
    Header header = new Header(
      new byte[]{ 'S', 'F', 'P' },
      (byte) 1,
      MessageTypes.SUBSCRIBE,
      sourceId,
      NodeIds.SERVER,
      0,
      UUID.randomUUID()
    );

    SubscribeBody.NodeSubscription ns = new SubscribeBody.NodeSubscription(
      nodeId,
      sensorNames,
      actuatorNames
    );

    SubscribeBody body = new SubscribeBody(
      requestId,
      List.of(ns)
    );

    return new SmartFarmingProtocol(header, body);
  }
}
