package ntnu.idata2302.sfp.sensornode.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.sensornode.core.Actuator;
import ntnu.idata2302.sfp.sensornode.core.SensorNode;
import ntnu.idata2302.sfp.sensornode.factory.PacketFactory;

/**
 * Central dispatcher for incoming {@link SmartFarmingProtocol} packets received
 * by a {@link SensorNodeContext}.
 *
 * <p>This class exposes a single public entry point {@link #handle(SensorNodeContext,
 * SmartFarmingProtocol)}
 * which inspects the incoming packet's {@link MessageTypes} and delegates handling
 * to internal methods for each supported message type.</p>
 *
 * <p>Current supported message types:
 * <ul>
 *   <li>{@link MessageTypes#ANNOUNCE_ACK} — handled by
 *   {@link #announceAckHandle(SensorNodeContext, SmartFarmingProtocol)}</li>
 *   <li>{@link MessageTypes#COMMAND} — handled by {@link #commandHandle(SensorNodeContext,
 *   SmartFarmingProtocol)}</li>
 * </ul>
 * </p>
 *
 * <p>Handlers may send response packets using the provided
 * {@link SensorNodeContext#sendPacket(SmartFarmingProtocol)}.</p>
 */
public class PacketHandler {

  /**
   * Inspect the {@code packet} message type and dispatch to the appropriate
   * handler method.
   *
   * <p>This prints a simple log message indicating the handled packet type,
   * then performs a type-based dispatch. Unknown or unsupported message types
   * are currently ignored.</p>
   *
   * @param client the {@link SensorNodeContext} that received the packet; used
   *               to obtain the local {@link SensorNode} and to send responses
   * @param packet the {@link SmartFarmingProtocol} packet to handle; must not be {@code null}
   */
  public static void handle(SensorNodeContext client, SmartFarmingProtocol packet) {
    MessageTypes type = packet.getHeader().getMessageType();
    System.out.println("Handling packet: " + type.toString());

    switch (type) {
      case ANNOUNCE_ACK -> PacketHandler.announceAckHandle(client, packet);
      case COMMAND -> PacketHandler.commandHandle(client, packet);

      default -> System.out.println("Unhandled message type: " + type);
    }
  }

  /**
   * Handle an {@link MessageTypes#ANNOUNCE_ACK} packet.
   *
   * <p>The handler extracts the header and updates the
   * client context with the assigned node id. The method assumes the packet
   * conforms to the protocol (i.e. body is an {@link AnnounceAckBody}).</p>
   *
   * @param client the {@link SensorNodeContext} whose id will be set
   * @param packet the {@link SmartFarmingProtocol} containing the ANNOUNCE_ACK
   */
  private static void announceAckHandle(SensorNodeContext client,
                                        SmartFarmingProtocol packet) {
    Header header = packet.getHeader();
    // Set id for sensor node
    client.setId(header.getTargetId());
  }

  /**
   * Handle a {@link MessageTypes#COMMAND} packet.
   *
   * <p>Processing steps:
   * <ol>
   *   <li>Validate that the command body contains an actuator list; if missing,
   *       send an ERROR packet back to the sender.</li>
   *   <li>For each actuator command, lookup the corresponding {@link Actuator}
   *       on the local {@link SensorNode} and apply the new value via
   *       {@link Actuator act(int)}.</li>
   *   <li>After applying commands, send a COMMAND_ACK packet to acknowledge
   *       successful processing.</li>
   * </ol>
   * </p>
   *
   * <p>Note: this method assumes fields are well-formed; if an actuator cannot
   * be found or {@code act()} throws, an exception may propagate and prevent
   * sending the acknowledgement. Callers may wrap this handler if stricter
   * error handling is required.</p>
   *
   * @param client the {@link SensorNodeContext}
   *              providing access to the local node and send operations
   * @param packet the incoming {@link SmartFarmingProtocol} containing a {@link CommandBody}
   */
  private static void commandHandle(SensorNodeContext client, SmartFarmingProtocol packet) {
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

    // Set new values for the actuators
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