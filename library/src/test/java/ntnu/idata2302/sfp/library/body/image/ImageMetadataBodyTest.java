package ntnu.idata2302.sfp.library.body.image;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link ImageMetadataBody}.
 *
 * <p>This class verifies correct CBOR serialization/deserialization and
 * error handling.</p>
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>ImageMetadataBody with all fields populated is correctly encoded and decoded.</li>
 *   <li>ImageMetadataBody with zero sizes round-trips correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class ImageMetadataBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that ImageMetadataBody with typical values
   * is correctly encoded and decoded.
   */
  @Test
  void toCbor_roundTrip_full_positive() {
    // Arrange
    ImageMetadataBody original = new ImageMetadataBody(
      "img-123",
      "2025-11-16T10:00:00Z",
      "image/jpeg",
      4096,
      8,
      512,
      "abcdef1234567890"
    );

    // Act
    byte[] cbor = original.toCbor();
    ImageMetadataBody decoded = CborCodec.decode(cbor, ImageMetadataBody.class);

    // Assert
    assertEquals(original.imageId(), decoded.imageId());
    assertEquals(original.timestamp(), decoded.timestamp());
    assertEquals(original.contentType(), decoded.contentType());
    assertEquals(original.totalSize(), decoded.totalSize());
    assertEquals(original.chunkCount(), decoded.chunkCount());
    assertEquals(original.chunkSize(), decoded.chunkSize());
    assertEquals(original.checksum(), decoded.checksum());
  }

  /**
   * Verifies that ImageMetadataBody with zero sizes
   * round-trips correctly through CBOR.
   */
  @Test
  void toCbor_roundTrip_zeroSizes_positive() {
    // Arrange
    ImageMetadataBody original = new ImageMetadataBody(
      "img-zero",
      "2025-11-16T11:00:00Z",
      "image/png",
      0,
      0,
      0,
      "0000000000000000"
    );

    // Act
    byte[] cbor = original.toCbor();
    ImageMetadataBody decoded = CborCodec.decode(cbor, ImageMetadataBody.class);

    // Assert
    assertEquals(original.imageId(), decoded.imageId());
    assertEquals(original.timestamp(), decoded.timestamp());
    assertEquals(original.contentType(), decoded.contentType());
    assertEquals(original.totalSize(), decoded.totalSize());
    assertEquals(original.chunkCount(), decoded.chunkCount());
    assertEquals(original.chunkSize(), decoded.chunkSize());
    assertEquals(original.checksum(), decoded.checksum());
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
        CborCodec.decode(invalid, ImageMetadataBody.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
