package ntnu.idata2302.sfp.library.body.command;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link CommandAckBody}.
 *
 * <p>This class verifies correct CBOR serialization/deserialization and
 * error handling.</p>
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Checks that a CommandAckBody with a message is correctly encoded and decoded.</li>
 *   <li>Checks that a CommandAckBody with a null message round-trips through CBOR correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid or corrupted CBOR data results in an exception.</li>
 * </ul>
 */

public class CommandAckBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a {@link CommandAckBody} with a non-null message
   * is correctly encoded to CBOR and decoded back to an equivalent instance.
   */
  @Test
  void toCbor_roundTripWithMessage_positive() {
    // Arrange
    CommandAckBody original = new CommandAckBody(42, 200, "OK");

    // Act
    byte[] cbor = original.toCbor();
    CommandAckBody decoded = CborCodec.decode(cbor, CommandAckBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.status(), decoded.status());
    assertEquals(original.message(), decoded.message());
  }


  /**
   * Verifies that a {@link CommandAckBody} with a {@code null} message
   * is correctly encoded to CBOR and decoded back without changing the fields.
   */
  @Test
  void toCbor_roundTripWithoutMessage_positive() {
    // Arrange
    CommandAckBody original = new CommandAckBody(7, 500, null);

    // Act
    byte[] cbor = original.toCbor();
    CommandAckBody decoded = CborCodec.decode(cbor, CommandAckBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.status(), decoded.status());
    assertNull(decoded.message());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that attempting to decode invalid CBOR data into a
   * {@link CommandAckBody} results in an exception being thrown.
   */
  @Test
  void fromCbor_invalidData_negative() {
    // Arrange
    byte[] invalidCbor = new byte[] { 0x01, 0x02, 0x03 };

    // Act & Assert
    assertThrows(RuntimeException.class,
      () -> CborCodec.decode(invalidCbor, CommandAckBody.class));
  }
}
