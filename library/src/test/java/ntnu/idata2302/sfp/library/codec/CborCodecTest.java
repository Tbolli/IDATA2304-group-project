package ntnu.idata2302.sfp.library.codec;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link CborCodec}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Encoding and decoding a simple object succeeds.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR input throws an exception.</li>
 * </ul>
 */
public class CborCodecTest {

  // A simple record used for test serialization
  public record SampleObject(int id, String name) {}

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a simple object can be encoded and decoded using CborCodec.
   */
  @Test
  void encodeDecode_roundTrip_positive() {
    // Arrange
    SampleObject original = new SampleObject(10, "Test");

    // Act
    byte[] cbor = CborCodec.encode(original);
    SampleObject decoded = CborCodec.decode(cbor, SampleObject.class);

    // Assert
    assertEquals(original.id(), decoded.id());
    assertEquals(original.name(), decoded.name());
  }


  // --------------------------- NEGATIVE TESTS ---------------------------------- //


  /**
   * Verifies that decoding invalid CBOR input results in an exception.
   */
  @Test
  void decode_invalidData_negative() {
    // Arrange
    byte[] invalid = new byte[] { 0x01, 0x02, 0x03 };

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        CborCodec.decode(invalid, SampleObject.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
