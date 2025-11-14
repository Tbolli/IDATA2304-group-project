package ntnu.idata2302.sfp.library.body.subscribe;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscribeBody(
  int requestId,
  SensorSection sensors,
  ActuatorSection actuators
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static SubscribeBody fromCbor(byte[] bytes) {
    return CborCodec.decode(bytes, SubscribeBody.class);
  }

  public record SensorSection(
    List<String> metrics
  ) {}

  public record ActuatorSection(
    List<String> actuators
  ) {}
}
