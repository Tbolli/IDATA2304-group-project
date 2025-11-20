package ntnu.idata2302.sfp.library.body.announce;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link AnnounceAckBody}.
 *
 * <p>This class tests the functionality of the AnnounceAckBody class.</p>
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>toCbor and fromCbor round trip works correctly</li>
 *
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>fromCbor with null input throws exception</li>
 *   <li>fromCbor with corrupted data throws exception</li>
 * </ul>
 */

class AnnounceAckBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that encoding and decoding
   * an AnnounceAckBody returns an object with the same field values.
   */

  @Test
  void toCborAndFromCbor_roundTrip_positive() {
    // Arrange
    AnnounceAckBody original = new AnnounceAckBody(42, 1);

    // Act
    byte[] encoded = original.toCbor();
    AnnounceAckBody decoded = AnnounceAckBody.fromCbor(encoded);

    // Assert
    assertNotNull(encoded);
    assertNotNull(decoded);
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.status(), decoded.status());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding null input throws an exception.
   */

  @Test
  void fromCbor_nullInput_negative() {
    // Arrange
    byte[] input = null;

    // Act & Assert
    assertThrows(RuntimeException.class, () -> AnnounceAckBody.fromCbor(input));
  }

  /**
   * Verifies that decoding corrupted CBOR data throws an exception.
   */

  @Test
  void fromCbor_corruptedData_negative() {
    // Arrange
    byte[] corrupted = new byte[]{0x01, 0x02, 0x03};

    // Act & Assert
    assertThrows(RuntimeException.class, () -> AnnounceAckBody.fromCbor(corrupted));
  }
}
