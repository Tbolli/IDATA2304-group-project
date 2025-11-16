package ntnu.idata2302.sfp.library.body.subscribe;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link UnsubscribeAckBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>UnsubscribeAckBody with a successful status is correctly encoded and decoded.</li>
 *   <li>UnsubscribeAckBody with an error status round-trips correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class UnsubscribeAckBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that UnsubscribeAckBody with a successful status
   * is correctly encoded and decoded.
   */
  @Test
  void toCbor_roundTrip_successStatus_positive() {
    // Arrange
    UnsubscribeAckBody original = new UnsubscribeAckBody(10, 0);

    // Act
    byte[] cbor = original.toCbor();
    UnsubscribeAckBody decoded = CborCodec.decode(cbor, UnsubscribeAckBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.status(), decoded.status());
  }

  /**
   * Verifies that UnsubscribeAckBody with an error status
   * round-trips correctly through CBOR.
   */
  @Test
  void toCbor_roundTrip_errorStatus_positive() {
    // Arrange
    UnsubscribeAckBody original = new UnsubscribeAckBody(11, 1);

    // Act
    byte[] cbor = original.toCbor();
    UnsubscribeAckBody decoded = CborCodec.decode(cbor, UnsubscribeAckBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.status(), decoded.status());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding invalid CBOR input results in an exception.
   */
  @Test
  void fromCbor_invalid_negative() {
    // Arrange
    byte[] invalid = new byte[]{0x01, 0x02, 0x03};

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        CborCodec.decode(invalid, UnsubscribeAckBody.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
