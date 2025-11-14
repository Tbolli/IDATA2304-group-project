package ntnu.idata2302.sfp.sensorNode.factory;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
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

  // DATA REPORT PACKET
  public static SmartFarmingProtocol buildReportPacket(SensorNode node) {

    // Convert sensors → readings with value
    List<DataReportBody.SensorReading> sensorReadings =
      node.getSensors().stream()
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
      node.getActuators().stream()
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
      node.getId(),        // this SN's assigned ID
      NodeIds.SERVER,
      0,
      UUID.randomUUID()
    );

    return new SmartFarmingProtocol(header, body);
  }
}