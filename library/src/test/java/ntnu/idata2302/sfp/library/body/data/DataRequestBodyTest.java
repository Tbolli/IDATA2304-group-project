package ntnu.idata2302.sfp.library.body.data;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link DataRequestBody}.
 *
 * <p>This class verifies correct CBOR serialization/deserialization and
 * error handling.</p>
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>DataRequestBody with all sections populated is correctly encoded and decoded.</li>
 *   <li>DataRequestBody with null sections round-trips through CBOR correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class DataRequestBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a {@link DataRequestBody} with all sections populated
   * is correctly encoded to CBOR and decoded back to an equivalent instance.
   */

  @Test
  void toCbor_roundTrip_full_positive() {
    // Arrange
    DataRequestBody.SensorAggregateSection aggregateSection =
      new DataRequestBody.SensorAggregateSection(
        List.of("temp", "humidity"),
        List.of("1h", "24h"),
        List.of("min", "max", "avg")
      );

    DataRequestBody.SensorSection sensorSection =
      new DataRequestBody.SensorSection(
        List.of("temp", "humidity"),
        Boolean.TRUE,
        aggregateSection
      );

    DataRequestBody.ActuatorSection actuatorSection =
      new DataRequestBody.ActuatorSection(Boolean.TRUE);

    DataRequestBody.ImageSection imageSection =
      new DataRequestBody.ImageSection(Boolean.FALSE);

    DataRequestBody original = new DataRequestBody(
      "req-123",
      sensorSection,
      actuatorSection,
      imageSection
    );

    // Act
    byte[] cbor = original.toCbor();
    DataRequestBody decoded = CborCodec.decode(cbor, DataRequestBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.sensors().metrics(), decoded.sensors().metrics());
    assertEquals(original.sensors().includeAggregates(), decoded.sensors().includeAggregates());
    assertEquals(
      original.sensors().aggregates().metrics(),
      decoded.sensors().aggregates().metrics()
    );
    assertEquals(
      original.sensors().aggregates().periods(),
      decoded.sensors().aggregates().periods()
    );
    assertEquals(
      original.sensors().aggregates().types(),
      decoded.sensors().aggregates().types()
    );
    assertEquals(
      original.actuators().includeStates(),
      decoded.actuators().includeStates()
    );
    assertEquals(
      original.images().includeLatest(),
      decoded.images().includeLatest()
    );
  }

  /**
   * Verifies that a {@link DataRequestBody} with null sections
   * is correctly encoded to CBOR and decoded back without changing the fields.
   */

  @Test
  void toCbor_roundTrip_withNullSections_positive() {
    // Arrange
    DataRequestBody original = new DataRequestBody(
      "req-null",
      null,
      null,
      null
    );

    // Act
    byte[] cbor = original.toCbor();
    DataRequestBody decoded = CborCodec.decode(cbor, DataRequestBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.sensors(), decoded.sensors());
    assertEquals(original.actuators(), decoded.actuators());
    assertEquals(original.images(), decoded.images());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that attempting to decode invalid CBOR data into a
   * {@link DataRequestBody} results in an exception being thrown.
   */

  @Test
  void fromCbor_invalidData_negative() {
    // Arrange
    byte[] invalid = new byte[] { 0x01, 0x02, 0x03 };

    // Act & Assert
    assertThrows(RuntimeException.class,
      () -> CborCodec.decode(invalid, DataRequestBody.class));
  }
}
