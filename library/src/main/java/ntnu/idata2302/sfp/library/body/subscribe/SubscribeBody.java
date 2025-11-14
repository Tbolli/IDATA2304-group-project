package ntnu.idata2302.sfp.library.body.subscribe;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscribeBody(
  int requestId,
  List<NodeSubscription> nodes
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static SubscribeBody fromCbor(byte[] bytes) {
    return CborCodec.decode(bytes, SubscribeBody.class);
  }

  //  Per-node subscription
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record NodeSubscription(
    int sensorNodeId,
    List<String> metrics,      // e.g. ["temperature", "humidity"]
    List<String> actuators    // e.g. ["valve1", "pump"]
  ) {}
}
