package ntnu.idata2302.sfp.library.body.data;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link DataReportBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>DataReportBody with sensor, actuator,
 *       and aggregate lists is correctly encoded and decoded.</li>
 *   <li>DataReportBody with null optional fields round-trips correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class DataReportBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a {@link DataReportBody} with populated sensor,
   * actuator, and aggregate lists is correctly encoded to CBOR
   * and decoded back to an equivalent instance.
   */

  @Test
  void toCbor_roundTrip_full_positive() {
    // Arrange
    DataReportBody.SensorReading s = new DataReportBody.SensorReading(
      "temp1", 22.5, 10.0, 30.0, "C", "2025-11-15T10:00:00"
    );

    DataReportBody.ActuatorState a = new DataReportBody.ActuatorState(
      "fan1", 1.0, 0.0, 1.0, "%", "2025-11-15T10:00:00"
    );

    DataReportBody.AggregateValue ag = new DataReportBody.AggregateValue(
      "temp1", "1h", 20.0, 25.0, 22.5
    );

    DataReportBody original = new DataReportBody(
      List.of(s),
      List.of(a),
      List.of(ag)
    );

    // Act
    byte[] cbor = original.toCbor();
    DataReportBody decoded = CborCodec.decode(cbor, DataReportBody.class);

    // Assert
    assertEquals(original.sensors().size(), decoded.sensors().size());
    assertEquals(original.actuators().size(), decoded.actuators().size());
    assertEquals(original.aggregates().size(), decoded.aggregates().size());
  }

  /**
   * Verifies that a {@link DataReportBody} with null optional fields
   * is correctly encoded to CBOR and decoded back without changing the fields.
   */

  @Test
  void toCbor_roundTrip_withNulls_positive() {
    // Arrange
    DataReportBody original = new DataReportBody(null, null, null);

    // Act
    byte[] cbor = original.toCbor();
    DataReportBody decoded = CborCodec.decode(cbor, DataReportBody.class);

    // Assert
    assertEquals(original.sensors(), decoded.sensors());
    assertEquals(original.actuators(), decoded.actuators());
    assertEquals(original.aggregates(), decoded.aggregates());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that attempting to decode invalid CBOR data into a
   * {@link DataReportBody} results in an exception being thrown.
   */

  @Test
  void fromCbor_invalidData_negative() {
    // Arrange
    byte[] invalid = new byte[] { 0x55, 0x66, 0x77 };

    // Act & Assert
    assertThrows(RuntimeException.class,
      () -> CborCodec.decode(invalid, DataReportBody.class));
  }
}
