package ntnu.idata2302.sfp.server.factory;

import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeIds;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

/**
 * Test class for {@link HeaderFactory}.
 *
 * <p>This class tests the functionality of the HeaderFactory class.</p>
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>serverHeader sets server as source and correct target/type</li>
 *
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>serverHeader must NOT set targetId equal to server node id</li>
 * </ul>
 */

public class HeaderFactoryTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  @Test
  void serverHeader_defaultVersion_populatesHeaderCorrectly() {
    // Arrange
    MessageTypes type = MessageTypes.CAPABILITIES_LIST;
    int targetId = 42;

    // Act
    Header header = HeaderFactory.serverHeader(type, targetId);

    // Assert
    assertEquals(type, header.getMessageType(), "Message type should match");
    assertEquals(NodeIds.SERVER, header.getSourceId(), "SourceId should be server");
    assertEquals(targetId, header.getTargetId(), "TargetId should match argument");
    assertEquals(0, header.getPayloadLength(), "New headers should start with payloadLength = 0");
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  @Test
  void serverHeader_doesNotUseServerIdAsTarget() {
    // Arrange
    MessageTypes type = MessageTypes.CAPABILITIES_LIST;
    int targetId = 9999; // Some id that is supposed to represent a client node

    // Act
    Header header = HeaderFactory.serverHeader(type, targetId);

    // Assert
    assertNotEquals(
      NodeIds.SERVER,
      header.getTargetId(),
      "TargetId should not be the server's own node id"
    );
  }
}
