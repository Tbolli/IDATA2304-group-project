package ntnu.idata2302.sfp.library.body.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable request body for data queries.
 *
 * <p>Contains optional sections that specify which sensors, actuators, and images
 * should be included in the data response. Instances can be serialized to and
 * deserialized from CBOR using {@link CborCodec}.</p>
 *
 * @param requestId client-provided request identifier (may be null)
 * @param sensors   sensor selection and aggregation options (may be null)
 * @param actuators actuator selection options (may be null)
 * @param images    image selection options (may be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record DataRequestBody(
    String requestId,
    SensorSection sensors,
    ActuatorSection actuators,
    ImageSection images
) implements Body {

  /**
   * Serialize this {@code DataRequestBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode a {@code DataRequestBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code DataRequestBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static DataRequestBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, DataRequestBody.class);
  }

  // =====================================================================
  // Nested Records — clean, grouped, and Jackson-friendly
  // =====================================================================

  /**
   * Selection and aggregation options for sensor data.
   *
   * @param metrics           list of sensor metric identifiers to include (may be null)
   * @param includeAggregates whether aggregate values should be included (may be null)
   * @param aggregates        options specifying which aggregates to compute (may be null)
   */
  public record SensorSection(
      List<String> metrics,
      Boolean includeAggregates,
      SensorAggregateSection aggregates
  ) {
  }

  /**
   * Options describing which sensor aggregates to compute and how.
   *
   * @param metrics metrics to aggregate (may be null)
   * @param periods aggregation periods (e.g., "hour", "day") (may be null)
   * @param types   aggregate types (e.g., "min", "max", "avg") (may be null)
   */
  public record SensorAggregateSection(
      List<String> metrics,
      List<String> periods,
      List<String> types
  ) {
  }

  /**
   * Selection options for actuator data.
   *
   * @param includeStates whether to include actuator states in the response (may be null)
   */
  public record ActuatorSection(
      Boolean includeStates
  ) {
  }

  /**
   * Selection options for image data.
   *
   * @param includeLatest whether to include the latest image per source (may be null)
   */
  public record ImageSection(
      Boolean includeLatest
  ) {
  }
}