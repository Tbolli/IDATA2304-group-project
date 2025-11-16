package ntnu.idata2302.sfp.library.body.data;

  import com.fasterxml.jackson.annotation.JsonInclude;
  import ntnu.idata2302.sfp.library.body.Body;

  import java.util.List;

  import ntnu.idata2302.sfp.library.codec.CborCodec;

  /**
   * Immutable body representing a data report.
   *
   * <p>Holds lists of sensor readings, actuator states, and aggregate values.
   * Instances are serialized to and from CBOR using {@link CborCodec}.</p>
   *
   * @param sensors list of sensor readings (may be null)
   * @param actuators list of actuator states (may be null)
   * @param aggregates list of aggregate values (may be null)
   */
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public record DataReportBody(
      List<SensorReading> sensors,
      List<ActuatorState> actuators,
      List<AggregateValue> aggregates
  ) implements Body {

    /**
     * Serialize this {@code DataReportBody} to CBOR bytes.
     *
     * @return a byte array containing the CBOR-encoded representation of this instance
     */
    @Override
    public byte[] toCbor() {
      return CborCodec.encode(this);
    }

    /**
     * Decode a {@code DataReportBody} from CBOR bytes.
     *
     * @param cbor CBOR-encoded input bytes
     * @return the decoded {@code DataReportBody} instance
     * @throws RuntimeException if decoding fails (decoder may throw more specific exceptions)
     */
    public static DataReportBody fromCbor(byte[] cbor) {
      return CborCodec.decode(cbor, DataReportBody.class);
    }

    // ================================================================
    // Nested Records
    // ================================================================

    /**
     * Represents a single sensor reading.
     *
     * @param id sensor identifier
     * @param value current sensor value (may be null)
     * @param minValue observed minimum value (may be null)
     * @param maxValue observed maximum value (may be null)
     * @param unit measurement unit (may be null)
     * @param timestamp ISO-8601 timestamp of the reading (may be null)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record SensorReading(
        String id,
        Double value,
        Double minValue,
        Double maxValue,
        String unit,
        String timestamp
    ) {}

    /**
     * Represents a single actuator state.
     *
     * @param id actuator identifier
     * @param value current actuator value (may be null)
     * @param minValue configured minimum value (may be null)
     * @param maxValue configured maximum value (may be null)
     * @param unit actuator value unit (may be null)
     * @param timestamp ISO-8601 timestamp of the state (may be null)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ActuatorState(
        String id,
        Double value,
        Double minValue,
        Double maxValue,
        String unit,
        String timestamp
    ) {}

    /**
     * Represents an aggregate value computed over a period.
     *
     * @param id identifier for the aggregated metric
     * @param period aggregation period (e.g., "hour", "day")
     * @param min minimum observed value in the period (may be null)
     * @param max maximum observed value in the period (may be null)
     * @param avg average value in the period (may be null)
     */
    public record AggregateValue(
        String id,
        String period,
        Double min,
        Double max,
        Double avg
    ) {}
  }