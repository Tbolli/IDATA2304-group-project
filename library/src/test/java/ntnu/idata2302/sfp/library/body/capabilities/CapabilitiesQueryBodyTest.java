package ntnu.idata2302.sfp.library.body.capabilities;

import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link CapabilitiesQueryBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Verifies that CapabilitiesQueryBody is correctly encoded and decoded using CBOR</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Ensures that decoding a null byte array throws an exception</li>
 *   <li>Ensures that decoding corrupted CBOR data throws an exception</li>
 * </ul>
 */
class CapabilitiesQueryBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that encoding a CapabilitiesQueryBody to CBOR
   * and decoding it back results in an object with the same field values.
   */
  @Test
  void toCborAndFromCbor_roundTrip_positive() {
    // Arrange
    CapabilitiesQueryBody original = new CapabilitiesQueryBody(99);

    // Act
    byte[] encoded = original.toCbor();
    CapabilitiesQueryBody decoded = CapabilitiesQueryBody.fromCbor(encoded);

    // Assert
    assertNotNull(encoded);
    assertNotNull(decoded);
    assertEquals(original.requestId(), decoded.requestId());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that providing null input to fromCbor
   * results in an exception being thrown.
   */
  @Test
  void fromCbor_nullInput_negative() {
    // Arrange
    final byte[] input = null;

    // Act & Assert
    assertThrows(RuntimeException.class, new Executable() {
      @Override
      public void execute() throws Throwable {
        CapabilitiesQueryBody.fromCbor(input);
      }
    });
  }

  /**
   * Verifies that corrupted CBOR input
   * results in an exception during decoding.
   */
  @Test
  void fromCbor_corruptedData_negative() {
    // Arrange
    final byte[] corrupted = new byte[]{0x11, 0x22, 0x33};

    // Act & Assert
    assertThrows(RuntimeException.class, new Executable() {
      @Override
      public void execute() throws Throwable {
        CapabilitiesQueryBody.fromCbor(corrupted);
      }
    });
  }
}
