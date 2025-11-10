package ntnu.idata2302.sfp.library.body.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;

import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record DataReportBody(
  String sourceId,
  String timestamp,
  List<SensorReading> sensors,
  List<ActuatorState> actuators,
  List<AggregateValue> aggregates
) implements Body {

  @Override
  public byte[] toCbor() {
    try {
      ObjectMapper mapper = new ObjectMapper(new CBORFactory());
      return mapper.writeValueAsBytes(this);
    } catch (Exception e) {
      throw new RuntimeException("Failed to serialize DataReportBody to CBOR", e);
    }
  }

  public static DataReportBody fromCbor(byte[] cbor) {
    try {
      ObjectMapper mapper = new ObjectMapper(new CBORFactory());
      return mapper.readValue(cbor, DataReportBody.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to deserialize CBOR to DataReportBody", e);
    }
  }

  // ================================================================
  // Nested Records
  // ================================================================

  public record SensorReading(
    String id,
    Double value,
    String unit,
    String timestamp
  ) {}

  public record ActuatorState(
    String id,
    String state,
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