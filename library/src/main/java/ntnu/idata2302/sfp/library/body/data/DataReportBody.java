package ntnu.idata2302.sfp.library.body.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;

import java.util.List;

import ntnu.idata2302.sfp.library.codec.CborCodec;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DataReportBody(
  List<SensorReading> sensors,
  List<ActuatorState> actuators,
  List<AggregateValue> aggregates
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static DataReportBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, DataReportBody.class);
  }

  // ================================================================
  // Nested Records
  // ================================================================

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record SensorReading(
    String id,
    Double value,
    Double minValue,
    Double maxValue,
    String unit,
    String timestamp
  ) {}

  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record ActuatorState(
    String id,
    Double value,
    Double minValue,
    Double maxValue,
    String unit,
    String timestamp
  ) {}

  public record AggregateValue(
    String id,
    String period,
    Double min,
    Double max,
    Double avg
  ) {}
}