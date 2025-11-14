package ntnu.idata2302.sfp.sensorNode.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;

public class PacketHandler {

  public static void handle(SensorNodeContext client, SmartFarmingProtocol packet){
    MessageTypes type = packet.getHeader().getMessageType();
    System.out.println("Handling packet: " + type.toString());

    switch (type) {
      case ANNOUNCE_ACK -> PacketHandler.AnnounceAckHandle(client, packet);
      case COMMAND -> {
        return;
      }
    }
  }

  private static void AnnounceAckHandle(SensorNodeContext client, SmartFarmingProtocol packet){
    Header header = packet.getHeader();
    client.setId(header.getTargetId());
  }
}


// ANNOUNCE_ACK
// COMMAND
// ERROR
// DATA_REQUEST