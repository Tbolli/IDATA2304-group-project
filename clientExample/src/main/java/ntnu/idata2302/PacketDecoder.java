package ntnu.idata2302;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.capabilities.*;
import ntnu.idata2302.sfp.library.body.command.*;
import ntnu.idata2302.sfp.library.body.data.*;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.MessageTypes;

/**
 * Utility class that prints a human-readable representation of a
 * {@link SmartFarmingProtocol} packet to standard output.
 *
 * <p>This class inspects the packet header and body, formats commonly useful
 * fields (protocol name, version, source/target ids, payload length, message id)
 * and then prints message-type specific details for known body types.</p>
 *
 * <p>The class contains only static behavior and is intended for debugging /
 * logging in example clients; it does not perform validation beyond casting the
 * body to the expected concrete types.</p>
 */
public class PacketDecoder {

  /**
   * Print a formatted view of the supplied {@link SmartFarmingProtocol} packet.
   *
   * <p>The method reads the packet header and body, prints header fields and
   * then attempts to interpret the body based on the header's
   * {@link MessageTypes}. Known body types are cast to their respective
   * classes and key fields are printed. Unknown or unsupported body types
   * will cause the raw {@link Body toString()} to be printed.</p>
   *
   * @param packet the decoded {@link SmartFarmingProtocol} packet to print;
   *               must not be {@code null}
   * @throws NullPointerException if {@code packet} is {@code null}
   * @throws ClassCastException   if the body does not match the expected concrete type
   *                              for the message type
   *                              (this method uses unchecked casts
   *                              when printing message-specific fields)
   */
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
        System.out.println("givenId = " + header.getTargetId());
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
        if (list.nodes() != null) {
          list.nodes().forEach(n -> System.out.println("  - " + n));
        }
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
        System.out.println("sensors:");
        if (rep.sensors() != null) {
          rep.sensors().forEach(s -> System.out.println("  - " + s));
        }
        System.out.println("actuators:");
        if (rep.actuators() != null) {
          rep.actuators().forEach(a -> System.out.println("  - " + a));
        }
        System.out.println("aggregates:");
        if (rep.aggregates() != null) {
          rep.aggregates().forEach(a -> System.out.println("  - " + a));
        }
        break;

      // 🔹 Commands
      case COMMAND:
        break;

      case COMMAND_ACK:
        CommandAckBody cmdAck = (CommandAckBody) body;
        System.out.println("status    = " + cmdAck.status());
        break;

      default:
        System.out.println("Unknown body type. Raw body:");
        System.out.println(body);
    }

    System.out.println("======= END PACKET =======\n");
  }
}