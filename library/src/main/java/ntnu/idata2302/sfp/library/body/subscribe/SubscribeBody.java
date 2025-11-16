package ntnu.idata2302.sfp.library.body.subscribe;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;


/**
 * Immutable body representing a subscription request.
 *
 * <p>Holds a client-provided request identifier and a list of per-node
 * subscriptions describing which metrics and actuators the client wishes to
 * subscribe to from specific sensor nodes. Instances can be serialized to and
 * deserialized from CBOR using {@link CborCodec}.</p>
 *
 * @param requestId client-provided identifier for this request
 * @param nodes     list of per-node subscriptions (may be {@code null} or empty)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscribeBody(
    int requestId,
    List<NodeSubscription> nodes
) implements Body {

  /**
   * Serialize this {@code SubscribeBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode a {@code SubscribeBody} from CBOR bytes.
   *
   * @param bytes CBOR-encoded input bytes
   * @return the decoded {@code SubscribeBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static SubscribeBody fromCbor(byte[] bytes) {
    return CborCodec.decode(bytes, SubscribeBody.class);
  }

  // Per-node subscription

  /**
   * Immutable record describing a subscription for a single sensor node.
   *
   * <p>Specifies the target sensor node id and which metrics and actuators the
   * client wants to subscribe to. Either list may be {@code null} or empty if
   * no metrics/actuators are requested for that node.</p>
   *
   * @param sensorNodeId numeric identifier of the sensor node
   * @param metrics      list of metric names to subscribe to (e.g. \["temperature", "humidity"\])
   * @param actuators    list of actuator names to subscribe to (e.g. \["valve1", "pump"\])
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record NodeSubscription(
      int sensorNodeId,
      List<String> metrics,      // e.g. ["temperature", "humidity"]
      List<String> actuators     // e.g. ["valve1", "pump"]
  ) {
  }
}