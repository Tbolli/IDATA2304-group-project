package ntnu.idata2302.sfp.library.node;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test class for {@link NodeIds}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Correctly identifies broadcast ID.</li>
 *   <li>Correctly identifies server ID.</li>
 *   <li>Correctly identifies control panel ID range.</li>
 *   <li>Correctly identifies sensor node ID range.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Non-broadcast IDs are not treated as broadcast.</li>
 *   <li>Non-server IDs are not treated as server.</li>
 * </ul>
 */
public class NodeIdsTest {


  // --------------------------- POSITIVE TESTS ---------------------------------- //


  /**
   * Verifies that BROADCAST ID is identified correctly.
   */
  @Test
  void isBroadcast_positive() {
    // Arrange
    int id = NodeIds.BROADCAST;

    // Act & Assert
    assertTrue(NodeIds.isBroadcast(id));
  }

  /**
   * Verifies that SERVER ID is identified correctly.
   */
  @Test
  void isServer_positive() {
    // Arrange
    int id = NodeIds.SERVER;

    // Act & Assert
    assertTrue(NodeIds.isServer(id));
  }

  /**
   * Verifies that control panel IDs fall within the valid range.
   */
  @Test
  void isControlPanel_positive() {
    // Arrange
    int id = 0x00000010; // in range

    // Act & Assert
    assertTrue(NodeIds.isControlPanel(id));
  }

  /**
   * Verifies that sensor node IDs fall within the valid range.
   */
  @Test
  void isSensorNode_positive() {
    // Arrange
    int id = 0x00010005; // >= 0x00010000

    // Act & Assert
    assertTrue(NodeIds.isSensorNode(id));
  }


  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that a non-broadcast ID is not identified as broadcast.
   */
  @Test
  void isBroadcast_negative() {
    // Arrange
    int id = 10;

    // Act & Assert
    assertFalse(NodeIds.isBroadcast(id));
  }

  /**
   * Verifies that a non-server ID is not identified as server.
   */
  @Test
  void isServer_negative() {
    // Arrange
    int id = 5;

    // Act & Assert
    assertFalse(NodeIds.isServer(id));
  }
}
