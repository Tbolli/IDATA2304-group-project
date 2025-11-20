package ntnu.idata2302.sfp.library.body.subscribe;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link SubscribeBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>SubscribeBody with node subscriptions is correctly encoded and decoded.</li>
 *   <li>SubscribeBody with an empty node list round-trips correctly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class SubscribeBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that SubscribeBody with one node subscription
   * is correctly encoded and decoded.
   */
  @Test
  void toCbor_roundTrip_singleNode_positive() {
    // Arrange
    SubscribeBody original = new SubscribeBody(
      42,
      101
    );

    // Act
    byte[] cbor = original.toCbor();
    SubscribeBody decoded = CborCodec.decode(cbor, SubscribeBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.sensorNodeId(), decoded.sensorNodeId());
  }

  /**
   * Verifies that SubscribeBody with an empty node list
   * round-trips correctly.
   */
  @Test
  void toCbor_roundTrip_emptyNodes_positive() {
    // Arrange
    SubscribeBody original = new SubscribeBody(
      7,
      202
    );

    // Act
    byte[] cbor = original.toCbor();
    SubscribeBody decoded = CborCodec.decode(cbor, SubscribeBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.sensorNodeId(), decoded.sensorNodeId());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding invalid CBOR input results in an exception.
   */
  @Test
  void fromCbor_invalid_negative() {
    // Arrange
    byte[] invalid = new byte[]{ 0x01, 0x44, 0x55 };

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        CborCodec.decode(invalid, SubscribeBody.class);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
