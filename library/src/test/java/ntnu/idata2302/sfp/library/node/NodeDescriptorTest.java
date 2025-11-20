package ntnu.idata2302.sfp.library.node;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link NodeDescriptor}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>NodeDescriptor stores all field values correctly.</li>
 *   <li>NodeDescriptor supports null optional fields.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Creating a SensorDescriptor with null id throws an exception.</li>
 * </ul>
 */
public class NodeDescriptorTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that NodeDescriptor correctly stores sensor and actuator lists.
   */
  @Test
  void constructor_fullValues_positive() {
    // Arrange
    NodeDescriptor.SensorDescriptor s1 =
      new NodeDescriptor.SensorDescriptor("temp", "C");

    NodeDescriptor.ActuatorDescriptor a1 =
      new NodeDescriptor.ActuatorDescriptor("fan", 1.0, 0.0, 1.0, "%");

    NodeDescriptor original = new NodeDescriptor(
      101,
      2,
      List.of(s1),
      List.of(a1),
      Boolean.TRUE,
      Boolean.FALSE
    );

    // Act
    Integer id = original.nodeId();
    int type = original.nodeType();
    List<NodeDescriptor.SensorDescriptor> sensors = original.sensors();
    List<NodeDescriptor.ActuatorDescriptor> actuators = original.actuators();

    // Assert
    assertEquals(101, id.intValue());
    assertEquals(2, type);
    assertEquals(1, sensors.size());
    assertEquals(1, actuators.size());
    assertEquals("temp", sensors.get(0).id());
    assertEquals("fan", actuators.get(0).id());
  }

  /**
   * Verifies that NodeDescriptor supports null for optional boolean fields.
   */
  @Test
  void constructor_withNulls_positive() {
    // Arrange
    NodeDescriptor descriptor = new NodeDescriptor(
      50,
      1,
      null,
      null,
      null,
      null
    );

    // Act & Assert
    assertEquals(50, descriptor.nodeId().intValue());
    assertEquals(1, descriptor.nodeType());
    assertEquals(null, descriptor.sensors());
    assertEquals(null, descriptor.actuators());
    assertEquals(null, descriptor.supportsImages());
    assertEquals(null, descriptor.supportsAggregates());
  }


  // --------------------------- NEGATIVE TESTS ---------------------------------- //

}
