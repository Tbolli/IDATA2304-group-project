package ntnu.idata2302.sfp.library.body.capabilities;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.helpers.CborCodec;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CapabilitiesAnnounceBody(
  String nodeType,
  String hardwareId,
  int isControlPanel,
  List<SensorDescriptor> sensors,
  List<ActuatorDescriptor> actuators,
  Boolean supportsImages,
  Boolean supportsAggregates
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static CapabilitiesAnnounceBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CapabilitiesAnnounceBody.class);
  }

  public record SensorDescriptor (String id, String unit) {}
  public record ActuatorDescriptor (String id, List<String> actions) {}
}
