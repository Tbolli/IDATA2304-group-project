package ntnu.idata2302.sfp.library.header;

import ntnu.idata2302.sfp.library.codec.HeaderCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link Header}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Header constructor stores all values correctly.</li>
 *   <li>Header validates the correct SFP protocol name.</li>
 *   <li>Header round-trips correctly using toBytes() and fromBytes().</li>
 *   <li>setPayloadLength() updates the payload length value.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Constructor throws exception for protocol name not equal to 3 bytes.</li>
 *   <li>Constructor throws exception for invalid protocol prefix.</li>
 * </ul>
 */
public class HeaderTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that the constructor correctly stores all fields.
   */
  @Test
  void constructor_storesValues_positive() {
    // Arrange
    byte[] protocol = new byte[] { 'S', 'F', 'P' };
    UUID id = UUID.randomUUID();

    // Act
    Header header = new Header(
      protocol,
      (byte) 1,
      MessageTypes.DATA_REPORT,
      10,
      20,
      99,
      id
    );

    // Assert
    assertEquals('S', header.getProtocolName()[0]);
    assertEquals((byte) 1, header.getVersion());
    assertEquals(MessageTypes.DATA_REPORT, header.getMessageType());
    assertEquals(10, header.getSourceId());
    assertEquals(20, header.getTargetId());
    assertEquals(99, header.getPayloadLength());
    assertEquals(id, header.getMessageId());
  }

  /**
   * Verifies that validateHeader() correctly identifies proper SFP headers.
   */
  @Test
  void validateHeader_positive() {
    // Arrange
    Header header = new Header(
      new byte[] {'S','F','P'},
      (byte) 1,
      MessageTypes.COMMAND,
      1,
      2,
      3,
      UUID.randomUUID()
    );

    // Act
    boolean valid = Header.validateHeader(header);

    // Assert
    assertTrue(valid);
  }

  /**
   * Verifies that the header round-trips correctly using toBytes() and fromBytes().
   */
  @Test
  void roundTrip_viaBytes_positive() {
    // Arrange
    Header original = new Header(
      new byte[]{'S','F','P'},
      (byte) 1,
      MessageTypes.SUBSCRIBE,
      101,
      202,
      55,
      UUID.randomUUID()
    );

    // Act
    byte[] encoded = original.toBytes();
    Header decoded = Header.fromBytes(encoded);

    // Assert
    assertEquals(original.getVersion(), decoded.getVersion());
    assertEquals(original.getMessageType(), decoded.getMessageType());
    assertEquals(original.getSourceId(), decoded.getSourceId());
    assertEquals(original.getTargetId(), decoded.getTargetId());
    assertEquals(original.getPayloadLength(), decoded.getPayloadLength());
    assertEquals(original.getMessageId(), decoded.getMessageId());
  }

  /**
   * Verifies that setPayloadLength updates the payload length.
   */
  @Test
  void setPayloadLength_positive() {
    // Arrange
    Header header = new Header(
      new byte[]{'S','F','P'},
      (byte) 1,
      MessageTypes.DATA_REQUEST,
      15,
      25,
      0,
      UUID.randomUUID()
    );

    // Act
    header.setPayloadLength(123);

    // Assert
    assertEquals(123, header.getPayloadLength());
  }


  // --------------------------- NEGATIVE TESTS ---------------------------------- //


  /**
   * Verifies that the constructor rejects protocol names that are not 3 bytes.
   */
  @Test
  void constructor_invalidProtocolLength_negative() {
    // Arrange
    byte[] protocol = new byte[] { 'S', 'F' };

    // Act
    Executable exec = new Executable() {
      @Override
      public void execute() {
        new Header(
          protocol,
          (byte) 1,
          MessageTypes.ERROR,
          1,
          1,
          1,
          UUID.randomUUID()
        );
      }
    };

    // Assert
    assertThrows(IllegalArgumentException.class, exec);
  }

  /**
   * Verifies that the constructor rejects an invalid SFP prefix.
   */
  @Test
  void constructor_invalidProtocolPrefix_negative() {
    // Arrange
    byte[] protocol = new byte[] { 'X', 'Y', 'Z' };

    // Act
    Executable exec = new Executable() {
      @Override
      public void execute() {
        new Header(
          protocol,
          (byte) 1,
          MessageTypes.ERROR,
          1,
          1,
          1,
          UUID.randomUUID()
        );
      }
    };

    // Assert
    assertThrows(IllegalArgumentException.class, exec);
  }
}
