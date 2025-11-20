package ntnu.idata2302.sfp.library;

import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.codec.HeaderCodec;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link SmartFarmingProtocol}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Protocol with a body round-trips correctly through toBytes() and fromBytes().</li>
 *   <li>Protocol with an empty body is handled correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Deserializing an incomplete packet throws an exception.</li>
 * </ul>
 */
public class SmartFarmingProtocolTest {


  // --------------------------- POSITIVE TESTS ---------------------------------- //


  /**
   * Verifies that a protocol with a body round-trips correctly.
   */
  @Test
  void toBytes_fromBytes_roundTrip_positive() {
    // Arrange
    ErrorBody body = new ErrorBody(5, "FAIL");

    Header header = new Header(
      new byte[]{'S','F','P'},
      (byte)1,
      MessageTypes.ERROR,
      123,
      456,
      0,
      UUID.randomUUID()
    );

    SmartFarmingProtocol original = new SmartFarmingProtocol(header, body);

    // Act
    byte[] packet = original.toBytes();
    SmartFarmingProtocol decoded = SmartFarmingProtocol.fromBytes(packet);

    // Assert
    assertEquals(original.getHeader().getMessageType(), decoded.getHeader().getMessageType());
    assertEquals(5, ((ErrorBody)decoded.getBody()).errorCode());
    assertEquals("FAIL", ((ErrorBody)decoded.getBody()).errorText());
  }



  // --------------------------- NEGATIVE TESTS ---------------------------------- //


  /**
   * Verifies that trying to decode an incomplete packet results in an exception.
   */
  @Test
  void fromBytes_incompletePacket_negative() {
    // Arrange
    Header header = new Header(
      new byte[]{'S','F','P'},
      (byte)1,
      MessageTypes.ERROR,
      1,
      1,
      10,
      UUID.randomUUID()
    );

    byte[] headerBytes = HeaderCodec.encodeHeader(header);
    byte[] packet = new byte[headerBytes.length + 3]; // too small

    System.arraycopy(headerBytes, 0, packet, 0, headerBytes.length);

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        SmartFarmingProtocol.fromBytes(packet);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
