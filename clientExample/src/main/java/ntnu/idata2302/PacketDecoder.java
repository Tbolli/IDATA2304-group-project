package ntnu.idata2302;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.capabilities.*;
import ntnu.idata2302.sfp.library.body.command.*;
import ntnu.idata2302.sfp.library.body.data.*;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.MessageTypes;

public class PacketDecoder {

  public static void printPacket(SmartFarmingProtocol packet) {

    var header = packet.getHeader();
    var body = packet.getBody();
    var type = header.getMessageType();

    System.out.println("\n======= PACKET RECEIVED =======");
    System.out.println("Protocol : " + new String(header.getProtocolName()));
    System.out.println("Version  : " + header.getVersion());
    System.out.println("Type     : " + type);
    System.out.println("SourceId : " + header.getSourceId());
    System.out.println("TargetId : " + header.getTargetId());
    System.out.println("Length   : " + header.getPayloadLength());
    System.out.println("MsgId    : " + header.getMessageId());
    System.out.println("---------------------------------");

    switch (type) {

      // 🔹 Announce
      case ANNOUNCE_ACK:
        AnnounceAckBody ack = (AnnounceAckBody) body;
        System.out.println("givenId =" + header.getTargetId());
        System.out.println("requestId = " + ack.requestId());
        System.out.println("status    = " + ack.status());
        break;

      // 🔹 Errors
      case ERROR:
        ErrorBody err = (ErrorBody) body;
        System.out.println("Error " + err.errorCode() + ": " + err.errorText());
        break;

      case CAPABILITIES_QUERY:
        CapabilitiesQueryBody query = (CapabilitiesQueryBody) body;
        System.out.println("requestId = " + query.requestId());
        break;

      case CAPABILITIES_LIST:
        CapabilitiesListBody list = (CapabilitiesListBody) body;
        System.out.println("requestId = " + list.requestId());
        System.out.println("nodes:");
        if (list.nodes() != null)
          list.nodes().forEach(n -> System.out.println("  - " + n));
        break;

      // 🔹 Data
      case DATA_REQUEST:
        DataRequestBody dr = (DataRequestBody) body;
        System.out.println("requestId = " + dr.requestId());
        System.out.println("sensors   = " + dr.sensors());
        System.out.println("actuators = " + dr.actuators());
        System.out.println("images    = " + dr.images());
        break;

      case DATA_REPORT:
        DataReportBody rep = (DataReportBody) body;
        System.out.println("requestId = " + rep.requestId());
        System.out.println("timestamp = " + rep.timestamp());
        System.out.println("sensors:");
        if (rep.sensors() != null)
          rep.sensors().forEach(s -> System.out.println("  - " + s));
        System.out.println("actuators:");
        if (rep.actuators() != null)
          rep.actuators().forEach(a -> System.out.println("  - " + a));
        System.out.println("aggregates:");
        if (rep.aggregates() != null)
          rep.aggregates().forEach(a -> System.out.println("  - " + a));
        break;

      // 🔹 Commands
      case COMMAND:
        CommandBody cmd = (CommandBody) body;
        System.out.println("commandId = " + cmd.commandId());
        System.out.println("actuator  = " + cmd.actuator());
        System.out.println("action    = " + cmd.action());
        System.out.println("timestamp = " + cmd.timestamp());
        break;

      case COMMAND_ACK:
        CommandAckBody cmdAck = (CommandAckBody) body;
        System.out.println("commandId = " + cmdAck.commandId());
        System.out.println("status    = " + cmdAck.status());
        System.out.println("action    = " + cmdAck.action());
        break;

      default:
        System.out.println("Unknown body type. Raw body:");
        System.out.println(body);
    }

    System.out.println("======= END PACKET =======\n");
  }
}
