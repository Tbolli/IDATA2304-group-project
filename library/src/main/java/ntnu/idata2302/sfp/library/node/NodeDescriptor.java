package ntnu.idata2302.sfp.library.node;

import java.util.List;

public record NodeDescriptor(
  Integer nodeId,
  int nodeType,
  List<SensorDescriptor> sensors,
  List<ActuatorDescriptor> actuators,
  Boolean supportsImages,
  Boolean supportsAggregates
) {
  public record SensorDescriptor (String id, String unit) {}
  public record ActuatorDescriptor (String id, List<String> actions) {}
}