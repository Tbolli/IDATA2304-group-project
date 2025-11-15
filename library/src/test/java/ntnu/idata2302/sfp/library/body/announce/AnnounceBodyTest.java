package ntnu.idata2302.sfp.library.body.announce;

import java.util.List;

import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeDescriptor.ActuatorDescriptor;
import ntnu.idata2302.sfp.library.node.NodeDescriptor.SensorDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link AnnounceBody}.
 *
 * <p>This class verifies correct CBOR serialization/deserialization
 * and error handling behavior.</p>
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>toCborAndFromCbor_roundTrip_positive</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>fromCbor_nullInput_negative</li>
 *   <li>fromCbor_corruptedData_negative</li>
 * </ul>
 */

class AnnounceBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that encoding and decoding an AnnounceBody
   * returns an object with matching field values.
   */

  @Test
  void toCborAndFromCbor_roundTrip_positive() {
    // Arrange
    List<SensorDescriptor> sensors = List.of(
      new SensorDescriptor("temp-1", "C"),
      new SensorDescriptor("hum-1", "%")
    );
    List<ActuatorDescriptor> actuators = List.of(
      new ActuatorDescriptor("fan-1", 0.0, 0.0, 1.0, "on/off")
    );
    NodeDescriptor descriptor = new NodeDescriptor(
      7,                // nodeId
      1,                // nodeType
      sensors,
      actuators,
      Boolean.TRUE,     // supportsImages
      Boolean.FALSE     // supportsAggregates
    );
    AnnounceBody original = new AnnounceBody(10, descriptor);

    // Act
    byte[] encoded = original.toCbor();
    AnnounceBody decoded = AnnounceBody.fromCbor(encoded);

    // Assert
    assertNotNull(encoded);
    assertNotNull(decoded);
    assertEquals(original.requestId(), decoded.requestId());
    assertNotNull(decoded.descriptor());
    assertEquals(descriptor.nodeId(), decoded.descriptor().nodeId());
    assertEquals(descriptor.nodeType(), decoded.descriptor().nodeType());
    assertEquals(descriptor.sensors().size(), decoded.descriptor().sensors().size());
    assertEquals(descriptor.actuators().size(), decoded.descriptor().actuators().size());
    assertEquals(descriptor.supportsImages(), decoded.descriptor().supportsImages());
    assertEquals(descriptor.supportsAggregates(), decoded.descriptor().supportsAggregates());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding null input throws an exception.
   */

  @Test
  void fromCbor_nullInput_negative() {
    // Arrange
    byte[] input = null;

    // Act & Assert
    assertThrows(RuntimeException.class, () -> AnnounceBody.fromCbor(input));
  }

  /**
   * Verifies that decoding corrupted CBOR data throws an exception.
   */

  @Test
  void fromCbor_corruptedData_negative() {
    // Arrange
    byte[] corrupted = new byte[]{0x05, 0x06, 0x07};

    // Act & Assert
    assertThrows(RuntimeException.class, () -> AnnounceBody.fromCbor(corrupted));
  }
}
