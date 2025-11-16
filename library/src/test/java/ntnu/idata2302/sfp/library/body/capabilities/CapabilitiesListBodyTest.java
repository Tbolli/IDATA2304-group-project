package ntnu.idata2302.sfp.library.body.capabilities;

import java.util.ArrayList;
import java.util.List;

import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesListBody;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeDescriptor.SensorDescriptor;
import ntnu.idata2302.sfp.library.node.NodeDescriptor.ActuatorDescriptor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link CapabilitiesListBody}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Verifies that CapabilitiesListBody is correctly encoded to CBOR</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Passing a null byte array to fromCbor throws an exception.</li>
 *   <li>Decoding corrupted CBOR input results in an exception being thrown.</li>
 * </ul>
 */
class CapabilitiesListBodyTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that CapabilitiesListBody is correctly encoded to CBOR
   * and decoded back with equal field values.
   */
  @Test
  void toCborAndFromCbor_roundTrip_positive() {
    // Arrange
    List<SensorDescriptor> sensors = new ArrayList<SensorDescriptor>();
    sensors.add(new SensorDescriptor("temp-1", "C"));

    List<ActuatorDescriptor> actuators = new ArrayList<ActuatorDescriptor>();
    actuators.add(new ActuatorDescriptor("fan-1", 0.0, 0.0, 1.0, "on/off"));

    List<NodeDescriptor> nodeList = new ArrayList<NodeDescriptor>();
    nodeList.add(
      new NodeDescriptor(
        1,
        2,
        sensors,
        actuators,
        Boolean.TRUE,
        Boolean.FALSE
      )
    );

    CapabilitiesListBody original = new CapabilitiesListBody(55, nodeList);

    // Act
    byte[] encoded = original.toCbor();
    CapabilitiesListBody decoded = CapabilitiesListBody.fromCbor(encoded);

    // Assert
    assertNotNull(encoded);
    assertNotNull(decoded);
    assertEquals(original.requestId(), decoded.requestId());
    assertEquals(original.nodes().size(), decoded.nodes().size());
    assertEquals(original.nodes().get(0).nodeId(), decoded.nodes().get(0).nodeId());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that passing a null byte array to fromCbor
   * throws an exception.
   */
  @Test
  void fromCbor_nullInput_negative() {
    // Arrange
    byte[] input = null;

    // Act & Assert
    assertThrows(RuntimeException.class, new org.junit.jupiter.api.function.Executable() {
      @Override
      public void execute() throws Throwable {
        CapabilitiesListBody.fromCbor(input);
      }
    });
  }


  /**
   * Verifies that decoding corrupted CBOR input
   * results in an exception being thrown.
   */
  @Test
  void fromCbor_corruptedData_negative() {
    // Arrange
    byte[] corrupted = new byte[]{0x01, 0x02, 0x03};

    // Act & Assert
    assertThrows(RuntimeException.class, new org.junit.jupiter.api.function.Executable() {
      @Override
      public void execute() throws Throwable {
        CapabilitiesListBody.fromCbor(corrupted);
      }
    });
  }

}
