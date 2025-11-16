package ntnu.idata2302.sfp.library.body.image;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link ImageTransferAckBody}.
 *
 * <p>This class verifies correct CBOR serialization/deserialization and
 * error handling.</p>
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>ImageTransferAckBody with image id and status is correctly encoded and decoded.</li>
 *   <li>ImageTransferAckBody with different status codes round-trips correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class ImageTransferAckBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that ImageTransferAckBody with a successful status
   * is correctly encoded and decoded.
   */
  @Test
  void toCbor_roundTrip_successStatus_positive() {
    // Arrange
    ImageTransferAckBody original = new ImageTransferAckBody("img-123", 0);

    // Act
    byte[] cbor = original.toCbor();
    ImageTransferAckBody decoded = CborCodec.decode(cbor, ImageTransferAckBody.class);

    // Assert
    assertEquals(original.imageId(), decoded.imageId());
    assertEquals(original.status(), decoded.status());
  }

  /**
   * Verifies that ImageTransferAckBody with a failure status
   * round-trips correctly through CBOR.
   */
  @Test
  void toCbor_roundTrip_errorStatus_positive() {
    // Arrange
    ImageTransferAckBody original = new ImageTransferAckBody("img-err", 1);

    // Act
    byte[] cbor = original.toCbor();
    ImageTransferAckBody decoded = CborCodec.decode(cbor, ImageTransferAckBody.class);

    // Assert
    assertEquals(original.imageId(), decoded.imageId());
    assertEquals(original.status(), decoded.status());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding invalid CBOR input results in an exception.
   */
  @Test
  void fromCbor_invalid_negative() {
    // Arrange
    byte[] invalid = new byte[]{0x11, 0x22, 0x33};

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        CborCodec.decode(invalid, ImageTransferAckBody.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
