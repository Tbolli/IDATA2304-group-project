package ntnu.idata2302.sfp.sensorNode.factory;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.command.CommandAckBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeIds;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.core.Sensor;
import ntnu.idata2302.sfp.sensorNode.core.Actuator;
import ntnu.idata2302.sfp.sensorNode.entity.ActuatorType;
import ntnu.idata2302.sfp.sensorNode.util.RequestIds;

import java.util.List;
import java.util.UUID;

public class PacketFactory {

  // ANNOUNCE PACKET
  public static SmartFarmingProtocol buildAnnouncePacket(SensorNode sensorNode) {

    // --- Convert internal sensors to DESCRIPTORS ---
    List<NodeDescriptor.SensorDescriptor> sensorDescriptors =
      sensorNode.getSensors().stream()
        .map(s -> new NodeDescriptor.SensorDescriptor(
          s.getName(),     // ID / sensor name
          s.getUnit()))     // unit only
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
      new byte[]{'S', 'F', 'P'},
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


  public static SmartFarmingProtocol buildAnnounceAckPacket(int givenId, int requestId) {
    Header header = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.ANNOUNCE_ACK,
      givenId,
      NodeIds.SERVER,
      0,
      UUID.randomUUID()
    );

    AnnounceAckBody body = new AnnounceAckBody(requestId, 1);
    return new SmartFarmingProtocol(header, body);
  }

  public static SmartFarmingProtocol buildCommandAckPacket(int sourceId, int targetId, int requestId, int status, String message) {
    Header header = new Header(
      new byte[]{'S', 'F', 'P'},
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


  // DATA REPORT PACKET
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
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.DATA_REPORT,
      sensorNode.getId(),        // this SN's assigned ID
      NodeIds.SERVER,
      0,
      UUID.randomUUID()
    );

    return new SmartFarmingProtocol(header, body);
  }

  public static SmartFarmingProtocol buildErrorPacket(int sourceId, int targetId, int errorId, String message) {
    Header header = new Header(
      new byte[]{'S', 'F', 'P'},
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