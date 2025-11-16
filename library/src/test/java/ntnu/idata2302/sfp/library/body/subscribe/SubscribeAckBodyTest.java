package ntnu.idata2302.sfp.library.body.subscribe;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link SubscribeAckBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>SubscribeAckBody with a successful status is correctly encoded and decoded.</li>
 *   <li>SubscribeAckBody with a failure status round-trips correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class SubscribeAckBodyTest {

  // positive tests

  /**
   * Verifies that SubscribeAckBody with a successful status
   * is correctly encoded and decoded.
   */
  @Test
  void toCbor_roundTrip_successStatus_positive() {
    // Arrange
    SubscribeAckBody original = new SubscribeAckBody(1, 42, 0);

    // Act
    byte[] cbor = original.toCbor();
    SubscribeAckBody decoded = CborCodec.decode(cbor, SubscribeAckBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.subscriptionId(), decoded.subscriptionId());
    assertEquals(original.status(), decoded.status());
  }

  /**
   * Verifies that SubscribeAckBody with a failure status
   * round-trips correctly through CBOR.
   */
  @Test
  void toCbor_roundTrip_errorStatus_positive() {
    // Arrange
    SubscribeAckBody original = new SubscribeAckBody(2, 99, 1);

    // Act
    byte[] cbor = original.toCbor();
    SubscribeAckBody decoded = CborCodec.decode(cbor, SubscribeAckBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.subscriptionId(), decoded.subscriptionId());
    assertEquals(original.status(), decoded.status());
  }

  // negative tests

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
        CborCodec.decode(invalid, SubscribeAckBody.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
