package ntnu.idata2302.sfp.library.body.error;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link ErrorBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>ErrorBody with all fields populated is correctly encoded and decoded.</li>
 *   <li>ErrorBody with null optional fields round-trips correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class ErrorBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that ErrorBody with both error code and text
   * is correctly encoded and decoded.
   */
  @Test
  void toCbor_roundTrip_full_positive() {
    // Arrange
    ErrorBody original = new ErrorBody(100, "Internal server error");

    // Act
    byte[] cbor = original.toCbor();
    ErrorBody decoded = CborCodec.decode(cbor, ErrorBody.class);

    // Assert
    assertEquals(original.errorCode(), decoded.errorCode());
    assertEquals(original.errorText(), decoded.errorText());
  }

  /**
   * Verifies that ErrorBody with a null text field
   * round-trips correctly through CBOR.
   */
  @Test
  void toCbor_roundTrip_withNullText_positive() {
    // Arrange
    ErrorBody original = new ErrorBody(3, null);

    // Act
    byte[] cbor = original.toCbor();
    ErrorBody decoded = CborCodec.decode(cbor, ErrorBody.class);

    // Assert
    assertEquals(original.errorCode(), decoded.errorCode());
    assertEquals(original.errorText(), decoded.errorText());
  }


  // --------------------------- NEGATIVE TESTS ---------------------------------- //


  /**
   * Verifies that decoding invalid CBOR input results in an exception.
   */
  @Test
  void fromCbor_invalid_negative() {
    // Arrange
    byte[] invalid = new byte[]{ 0x55, 0x10 };

    // Act
    org.junit.jupiter.api.function.Executable decode = new org.junit.jupiter.api.function.Executable() {
      @Override
      public void execute() {
        CborCodec.decode(invalid, ErrorBody.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }

}
