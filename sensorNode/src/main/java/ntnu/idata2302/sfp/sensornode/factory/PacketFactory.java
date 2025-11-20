package ntnu.idata2302.sfp.sensornode.factory;

import java.util.List;
import java.util.UUID;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.command.CommandAckBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeIds;
import ntnu.idata2302.sfp.sensornode.core.Actuator;
import ntnu.idata2302.sfp.sensornode.core.Sensor;
import ntnu.idata2302.sfp.sensornode.core.SensorNode;
import ntnu.idata2302.sfp.sensornode.util.RequestIds;


/**
 * Utility factory that constructs ready\-to\-send {@link SmartFarmingProtocol}
 * packets used by the sensor node simulation.
 *
 * <p>Each static method builds a full protocol message (header and body)
 * for a specific {@link MessageTypes} value. The factory centralizes how
 * headers (protocol signature, version, message type, source/target ids, and
 * UUID) and bodies are created so callers can generate packets consistently
 * for networking or testing.</p>
 *
 * <p>Methods do not perform deep validation of provided objects; callers
 * should ensure the provided {@link SensorNode} or ids are valid for the
 * intended use.</p>
 */
public class PacketFactory {

  /**
   * Build an ANNOUNCE packet describing the provided sensor node.
   *
   * <p>The returned {@link SmartFarmingProtocol} contains a {@link AnnounceBody}
   * with a {@link NodeDescriptor} that lists this node's sensors and actuators,
   * and a header targeting the server. The node id in the descriptor is left
   * {@code null} because the server assigns an id upon acknowledge.</p>
   *
   * @param sensorNode the local {@link SensorNode} to describe; must not be {@code null}
   * @return a {@link SmartFarmingProtocol} containing an ANNOUNCE header and body
   * @throws NullPointerException if {@code sensorNode} is {@code null}
   */
  public static SmartFarmingProtocol buildAnnouncePacket(SensorNode sensorNode) {

    // --- Convert internal sensors to DESCRIPTORS ---
    List<NodeDescriptor.SensorDescriptor> sensorDescriptors =
        sensorNode.getSensors().stream()
            .map(s -> new NodeDescriptor.SensorDescriptor(
                s.getName(),
                s.getUnit(),
                s.getMinValue(),
                s.getMaxValue()))
            .toList();


    // --- Convert internal actuators to DESCRIPTORS ---
    List<NodeDescriptor.ActuatorDescriptor> actuatorDescriptors =
        sensorNode.getActuators().stream()
            .map(a -> new NodeDescriptor.ActuatorDescriptor(
                a.getType().name(),
                a.getCurrentValue(),
                a.getMinValue(),
                a.getMaxValue(),
                a.getUnit()))
            .toList();

    NodeDescriptor descriptor = new NodeDescriptor(
        null,                // nodeId filled by the server
        1,                          // nodeType = SENSOR_NODE
        sensorDescriptors,
        actuatorDescriptors,
        sensorNode.supportsImage(),
        sensorNode.supportsAggregate()
    );

    Header header = new Header(
        new byte[] {'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.ANNOUNCE,
        NodeIds.BROADCAST,
        NodeIds.SERVER,
        0,
        UUID.randomUUID()
    );

    AnnounceBody body = new AnnounceBody(
        RequestIds.next(),
        descriptor
    );

    return new SmartFarmingProtocol(header, body);
  }

  /**
   * Build a COMMAND_ACK packet used to acknowledge command processing.
   *
   * @param sourceId  the source node id (sender of the ack)
   * @param targetId  the target node id (recipient of the ack)
   * @param requestId the id of the original command request being acknowledged
   * @param status    numeric status code (implementation defined)
   * @param message   human\-readable status or error message; may be {@code null}
   * @return a {@link SmartFarmingProtocol} containing a COMMAND_ACK header and body
   */
  public static SmartFarmingProtocol buildCommandAckPacket(int sourceId, int targetId,
                                                           int requestId, int status,
                                                           String message) {
    Header header = new Header(
        new byte[] {'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.COMMAND_ACK,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );

    CommandAckBody body = new CommandAckBody(requestId, status, message);
    return new SmartFarmingProtocol(header, body);
  }


  /**
   * Build a DATA_REPORT packet containing current sensor readings and actuator states.
   *
   * <p>The method converts each {@link Sensor} into a {@link DataReportBody.SensorReading}
   * and each {@link Actuator} into a {@link DataReportBody.ActuatorState}. Timestamps
   * are represented as {@link String} values of {@link System#currentTimeMillis()}.</p>
   *
   * @param sensorNode the {@link SensorNode} whose readings and states are reported;
   *                   must not be {@code null}
   * @return a {@link SmartFarmingProtocol} containing a DATA_REPORT header and body
   * @throws NullPointerException if {@code sensorNode} is {@code null}
   */
  public static SmartFarmingProtocol buildReportPacket(SensorNode sensorNode) {

    // Convert sensors → readings with value
    List<DataReportBody.SensorReading> sensorReadings =
        sensorNode.getSensors().stream()
            .map(s -> new DataReportBody.SensorReading(
                s.getName(),
                s.getValue(),
                s.getMinValue(),
                s.getMaxValue(),
                s.getUnit(),
                String.valueOf(System.currentTimeMillis())
            )).toList();

    // Convert actuators → states
    List<DataReportBody.ActuatorState> actuatorStates =
        sensorNode.getActuators().stream()
            .map(a -> new DataReportBody.ActuatorState(
                a.getType().name(),
                a.getCurrentValue(),
                a.getMinValue(),
                a.getMaxValue(),
                a.getUnit(),
                String.valueOf(System.currentTimeMillis())
            )).toList();

    DataReportBody body = new DataReportBody(
        sensorReadings,
        actuatorStates,
        null   // aggregates unsupported for now
    );

    Header header = new Header(
        new byte[] {'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.DATA_REPORT,
        sensorNode.getId(),        // this SN's assigned ID
        NodeIds.SERVER,
        0,
        UUID.randomUUID()
    );

    return new SmartFarmingProtocol(header, body);
  }

  /**
   * Build an ERROR packet carrying an error code and message.
   *
   * @param sourceId the id of the node generating the error
   * @param targetId the intended recipient of the error packet
   * @param errorId  application specific error identifier
   * @param message  human\-readable description of the error; may be {@code null}
   * @return a {@link SmartFarmingProtocol} containing an ERROR header and body
   */
  public static SmartFarmingProtocol buildErrorPacket(int sourceId, int targetId, int errorId,
                                                      String message) {
    Header header = new Header(
        new byte[] {'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.ERROR,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );

    ErrorBody body = new ErrorBody(errorId, message);
    return new SmartFarmingProtocol(header, body);
  }
}