package ntnu.idata2302.sfp.sensorNode.net;

import ntnu.idata2302.sfp.sensorNode.core.Actuator;
import ntnu.idata2302.sfp.sensorNode.core.Sensor;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link SensorNodeContext}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>setId() and getId() delegate correctly to the underlying SensorNode.</li>
 *   <li>getSensorNode() returns the same SensorNode instance passed to the constructor.</li>
 *   <li>isConnected() returns false when no socket has been created.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>readOnePacket() throws IOException when the context is not connected.</li>
 * </ul>
 */
public class SensorNodeContextTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that setId() and getId() delegate to the underlying SensorNode.
   */
  @Test
  void setIdAndGetId_positive() {
    // Arrange
    SensorNode node = createEmptyNode();
    SensorNodeContext context = new SensorNodeContext("localhost", 1234, node);
    int expectedId = 42;

    // Act
    context.setId(expectedId);
    int actualContextId = context.getId();
    int actualNodeId = node.getId();

    // Assert
    assertEquals(expectedId, actualContextId);
    assertEquals(expectedId, actualNodeId);
  }

  /**
   * Verifies that getSensorNode() returns the same instance that was provided
   * to the constructor.
   */
  @Test
  void getSensorNode_positive() {
    // Arrange
    SensorNode node = createEmptyNode();
    SensorNodeContext context = new SensorNodeContext("localhost", 1234, node);

    // Act
    SensorNode result = context.getSensorNode();

    // Assert
    assertSame(node, result);
  }

  /**
   * Verifies that isConnected() returns false before any connection is established.
   */
  @Test
  void isConnected_initiallyFalse_positive() {
    // Arrange
    SensorNode node = createEmptyNode();
    SensorNodeContext context = new SensorNodeContext("localhost", 1234, node);

    // Act
    boolean connected = context.isConnected();

    // Assert
    assertFalse(connected);
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that readOnePacket() throws IOException when the context is not connected.
   */
  @Test
  void readOnePacket_notConnected_negative() {
    // Arrange
    SensorNode node = createEmptyNode();
    final SensorNodeContext context = new SensorNodeContext("localhost", 1234, node);

    // Act
    Executable executable = new Executable() {
      @Override
      public void execute() throws Throwable {
        context.readOnePacket();
      }
    };

    // Assert
    assertThrows(IOException.class, executable);
  }

  // --------------------------- HELPER METHODS ---------------------------------- //

  /**
   * Creates a SensorNode with no sensors or actuators for testing.
   */
  private SensorNode createEmptyNode() {
    List<Sensor> sensors = new ArrayList<Sensor>();
    List<Actuator> actuators = new ArrayList<Actuator>();
    return new SensorNode(sensors, actuators, false, false);
  }
}
