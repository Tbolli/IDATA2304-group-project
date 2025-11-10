package ntnu.idata2302.sfp.library.body.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import ntnu.idata2302.sfp.library.helpers.CborCodec;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DataRequestBody(
  String requestId,
  SensorSection sensors,
  ActuatorSection actuators,
  ImageSection images
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static DataRequestBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, DataRequestBody.class);
  }

  // =====================================================================
  // Nested Records — clean, grouped, and Jackson-friendly
  // =====================================================================

  public record SensorSection(
    List<String> metrics,
    Boolean includeAggregates,
    SensorAggregateSection aggregates
  ) {}

  public record SensorAggregateSection(
    List<String> metrics,
    List<String> periods,
    List<String> types
  ) {}

  public record ActuatorSection(
    Boolean includeStates
  ) {}

  public record ImageSection(
    Boolean includeLatest
  ) {}
}
