package ntnu.idata2302.sfp.library.body.subscribe;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link UnsubscribeBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>UnsubscribeBody with requestId and subscriptionId is correctly encoded and decoded.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR input results in an exception.</li>
 * </ul>
 */
public class UnsubscribeBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that UnsubscribeBody round-trips correctly through CBOR.
   */
  @Test
  void toCbor_roundTrip_positive() {
    // Arrange
    UnsubscribeBody original = new UnsubscribeBody(
      100,
      55
    );

    // Act
    byte[] cbor = original.toCbor();
    UnsubscribeBody decoded = CborCodec.decode(cbor, UnsubscribeBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.sensorNodeId(), decoded.sensorNodeId());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding invalid CBOR data results in an exception.
   */
  @Test
  void fromCbor_invalid_negative() {
    // Arrange
    byte[] invalid = new byte[]{0x11, 0x22, 0x33};

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        CborCodec.decode(invalid, UnsubscribeBody.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
