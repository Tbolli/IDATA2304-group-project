package ntnu.idata2302.sfp.library.body.image;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link ImageChunkBody}.
 *
 * <p>This class verifies correct CBOR serialization/deserialization and
 * error handling.</p>
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>ImageChunkBody with all fields populated is correctly encoded and decoded.</li>
 *   <li>ImageChunkBody with null data round-trips correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class ImageChunkBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that ImageChunkBody with image id, chunk index and data
   * is correctly encoded and decoded.
   */
  @Test
  void toCbor_roundTrip_full_positive() {
    // Arrange
    byte[] data = new byte[]{1, 2, 3, 4};
    ImageChunkBody original = new ImageChunkBody("img-123", 5, data);

    // Act
    byte[] cbor = original.toCbor();
    ImageChunkBody decoded = CborCodec.decode(cbor, ImageChunkBody.class);

    // Assert
    assertEquals(original.imageId(), decoded.imageId());
    assertEquals(original.chunkIndex(), decoded.chunkIndex());
    assertArrayEquals(original.data(), decoded.data());
  }

  /**
   * Verifies that ImageChunkBody with a null data field
   * round-trips correctly through CBOR.
   */
  @Test
  void toCbor_roundTrip_withNullData_positive() {
    // Arrange
    ImageChunkBody original = new ImageChunkBody("img-null", 0, null);

    // Act
    byte[] cbor = original.toCbor();
    ImageChunkBody decoded = CborCodec.decode(cbor, ImageChunkBody.class);

    // Assert
    assertEquals(original.imageId(), decoded.imageId());
    assertEquals(original.chunkIndex(), decoded.chunkIndex());
    assertEquals(original.data(), decoded.data());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding invalid CBOR input results in an exception.
   */
  @Test
  void fromCbor_invalid_negative() {
    // Arrange
    byte[] invalid = new byte[]{0x10, 0x20, 0x30};

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        CborCodec.decode(invalid, ImageChunkBody.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
