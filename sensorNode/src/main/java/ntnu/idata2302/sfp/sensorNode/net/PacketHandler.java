package ntnu.idata2302.sfp.sensorNode.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.sensorNode.core.Actuator;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.entity.ActuatorType;
import ntnu.idata2302.sfp.sensorNode.factory.PacketFactory;

public class PacketHandler {

  public static void handle(SensorNodeContext client, SmartFarmingProtocol packet){
    MessageTypes type = packet.getHeader().getMessageType();
    System.out.println("Handling packet: " + type.toString());

    switch (type) {
      case ANNOUNCE_ACK -> PacketHandler.AnnounceHandle(client, packet);
      case COMMAND -> PacketHandler.CommandHandle(client, packet);
    }
  }

  private static void AnnounceHandle(SensorNodeContext client, SmartFarmingProtocol packet){
    Header header = packet.getHeader();
    AnnounceBody body = (AnnounceBody) packet.getBody();

    // Set id for sensor node
    client.setId(header.getTargetId());

    // Send ack packet
    SmartFarmingProtocol resPacket = PacketFactory.buildAnnounceAckPacket(header.getTargetId(),body.requestId());
    client.sendPacket(resPacket);
  }

  private static void CommandHandle(SensorNodeContext client, SmartFarmingProtocol packet){
    Header header = packet.getHeader();
    CommandBody body = (CommandBody) packet.getBody();

    // Validate actuators
    if (body.actuators() == null) {
      SmartFarmingProtocol errorBody = PacketFactory.buildErrorPacket(
        header.getSourceId(),
        header.getTargetId(),
        1,
        String.format("BAD_REQUEST: missing required field 'actuators' in body: %s", body)
      );
      client.sendPacket(errorBody);
      return;
    }

    // Set a new value for the actuators
    body.actuators().forEach(inAct -> {
      Actuator act = client.getSensorNode().findActuator(inAct.name());
      act.act(inAct.newValue());
    });

    // Send ack packet
    SmartFarmingProtocol resBody = PacketFactory.buildCommandAckPacket(
      header.getSourceId(),
      header.getTargetId(),
      body.requestId(),
      1,
      "OK"
    );
    client.sendPacket(resBody);
  }

}

// DATA_REQUEST