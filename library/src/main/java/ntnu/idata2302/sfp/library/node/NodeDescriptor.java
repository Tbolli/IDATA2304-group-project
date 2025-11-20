package ntnu.idata2302.sfp.library.node;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Descriptor for a single node in the system.
 *
 * <p>Immutable data carrier implemented as a Java record. Contains the numeric
 * node identifier, a node type code, lists of sensors and actuators, and
 * boolean flags indicating support for images and aggregate reporting.</p>
 *
 * @param nodeId             nullable numeric identifier for the node; may be {@code null}
 *                           if the node has no assigned id
 * @param nodeType           integer code identifying the node type (protocol-specific)
 * @param sensors            list of sensor descriptors for this node;
 *                           may be empty but not {@code null} when present
 * @param actuators          list of actuator descriptors for this node;
 *                           may be empty but not {@code null} when present
 * @param supportsImages     {@code Boolean} flag indicating if the node supports image transfer;
 *                           may be {@code null} if unknown
 * @param supportsAggregates {@code Boolean} flag indicating if the node supports aggregate data;
 *                           may be {@code null} if unknown
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record NodeDescriptor(
    Integer nodeId,
    int nodeType,
    List<SensorDescriptor> sensors,
    List<ActuatorDescriptor> actuators,
    Boolean supportsImages,
    Boolean supportsAggregates
) {

  /**
   * Descriptor for a sensor attached to a node.
   *
   * @param id   unique sensor identifier within the node
   *             (non\-null, used for addressing and mapping)
   * @param unit human\-readable measurement unit for the sensor values
   *             (maybe {@code null} if unspecified)
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record SensorDescriptor(String id, String unit, Double minValue, Double maxValue) {
  }

  /**
   * Descriptor for an actuator attached to a node.
   *
   * @param id       unique actuator identifier within the node (non\-null,
   *                 used for addressing and mapping)
   * @param value    current numeric value of the actuator
   * @param minValue optional minimum allowed value for the actuator;
   *                 may be {@code null} if not applicable
   * @param maxValue optional maximum allowed value for the actuator;
   *                 may be {@code null} if not applicable
   * @param unit     human\-readable unit for the actuator value
   *                 (maybe {@code null} if unspecified)
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ActuatorDescriptor(String id, double value, Double minValue, Double maxValue,
                                   String unit) {
  }
}