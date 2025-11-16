package ntnu.idata2302.sfp.library.body.command;

import ntnu.idata2302.sfp.library.codec.CborCodec;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link CommandBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>CommandBody with actuator list is correctly encoded and decoded.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR data results in an exception.</li>
 * </ul>
 */
public class CommandBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a {@link CommandBody} with a list of actuators
   * is correctly encoded to CBOR and decoded back to an equivalent instance.
   */

  @Test
  void toCbor_roundTrip_positive() {
    // Arrange
    CommandBody.CommandPart p1 = new CommandBody.CommandPart("fan", 1.0);
    CommandBody.CommandPart p2 = new CommandBody.CommandPart("heater", 0.0);
    CommandBody original = new CommandBody(10, List.of(p1, p2));

    // Act
    byte[] cbor = original.toCbor();
    CommandBody decoded = CborCodec.decode(cbor, CommandBody.class);

    // Assert
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.actuators().size(), decoded.actuators().size());
    assertEquals(original.actuators().get(0).name(), decoded.actuators().get(0).name());
    assertEquals(original.actuators().get(0).newValue(), decoded.actuators().get(0).newValue());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding invalid CBOR data
   * results in an exception being thrown.
   */

  @Test
  void fromCbor_invalidData_negative() {
    // Arrange
    byte[] invalid = new byte[] { 0x11, 0x22 };

    // Act & Assert
    assertThrows(RuntimeException.class,
      () -> CborCodec.decode(invalid, CommandBody.class));
  }
}
