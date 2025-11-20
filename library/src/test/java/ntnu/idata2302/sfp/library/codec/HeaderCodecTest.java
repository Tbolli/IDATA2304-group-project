package ntnu.idata2302.sfp.library.codec;

import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link HeaderCodec}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Header is encoded and decoded correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding a byte array that is too small throws an exception.</li>
 * </ul>
 */
public class HeaderCodecTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a full Header round-trips correctly through encode and decode.
   */
  @Test
  void encodeDecode_roundTrip_positive() {
    // Arrange
    Header original = new Header(
      new byte[]{'S','F','P'},
      (byte) 1,
      MessageTypes.DATA_REPORT,
      1234,
      5678,
      99,
      UUID.randomUUID()
    );

    // Act
    byte[] encoded = HeaderCodec.encodeHeader(original);
    Header decoded = HeaderCodec.decodeHeader(encoded);

    // Assert
    assertEquals(new String(original.getProtocolName()), new String(decoded.getProtocolName()));
    assertEquals(original.getVersion(), decoded.getVersion());
    assertEquals(original.getMessageType(), decoded.getMessageType());
    assertEquals(original.getSourceId(), decoded.getSourceId());
    assertEquals(original.getTargetId(), decoded.getTargetId());
    assertEquals(original.getPayloadLength(), decoded.getPayloadLength());
    assertEquals(original.getMessageId(), decoded.getMessageId());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding a byte array shorter than Header.HEADER_SIZE
   * results in an exception.
   */
  @Test
  void decodeHeader_invalidSize_negative() {
    // Arrange
    byte[] tooSmall = new byte[5];

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        HeaderCodec.decodeHeader(tooSmall);
      }
    };

    // Assert
    assertThrows(IllegalArgumentException.class, decode);
  }
}
