package ntnu.idata2302.sfp.sensorNode.factory;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.command.CommandAckBody;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeIds;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test class for {@link PacketFactory}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>buildAnnounceAckPacket creates a SmartFarmingProtocol with ANNOUNCE_ACK header and correct body.</li>
 *   <li>buildCommandAckPacket creates a SmartFarmingProtocol with COMMAND_ACK header and correct body.</li>
 *   <li>buildErrorPacket creates a SmartFarmingProtocol with ERROR header and correct body.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>No negative behavior is defined for these factory methods.</li>
 * </ul>
 */
public class PacketFactoryTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that buildAnnounceAckPacket creates a packet with the expected header and body.
   */
  @Test
  void buildAnnounceAckPacket_positive() {
    // Arrange
    int givenId = 42;
    int requestId = 7;

    // Act
    SmartFarmingProtocol protocol = PacketFactory.buildAnnounceAckPacket(givenId, requestId);
    Header header = protocol.getHeader();
    AnnounceAckBody body = (AnnounceAckBody) protocol.getBody();

    // Assert
    assertEquals(MessageTypes.ANNOUNCE_ACK, header.getMessageType());
    assertEquals(givenId, header.getSourceId());
    assertEquals(NodeIds.SERVER, header.getTargetId());
    assertEquals(requestId, body.requestId());
    assertEquals(1, body.status());
  }

  /**
   * Verifies that buildCommandAckPacket creates a packet with the expected header and body.
   */
  @Test
  void buildCommandAckPacket_positive() {
    // Arrange
    int sourceId = 10;
    int targetId = 20;
    int requestId = 5;
    int status = 200;
    String message = "OK";

    // Act
    SmartFarmingProtocol protocol =
      PacketFactory.buildCommandAckPacket(sourceId, targetId, requestId, status, message);
    Header header = protocol.getHeader();
    CommandAckBody body = (CommandAckBody) protocol.getBody();

    // Assert
    assertEquals(MessageTypes.COMMAND_ACK, header.getMessageType());
    assertEquals(sourceId, header.getSourceId());
    assertEquals(targetId, header.getTargetId());
    assertEquals(requestId, body.requestId());
    assertEquals(status, body.status());
    assertEquals(message, body.message());
  }

  /**
   * Verifies that buildErrorPacket creates a packet with the expected header and body.
   */
  @Test
  void buildErrorPacket_positive() {
    // Arrange
    int sourceId = 99;
    int targetId = NodeIds.SERVER;
    int errorId = 1;
    String message = "BAD_REQUEST";

    // Act
    SmartFarmingProtocol protocol =
      PacketFactory.buildErrorPacket(sourceId, targetId, errorId, message);
    Header header = protocol.getHeader();
    ErrorBody body = (ErrorBody) protocol.getBody();

    // Assert
    assertEquals(MessageTypes.ERROR, header.getMessageType());
    assertEquals(sourceId, header.getSourceId());
    assertEquals(targetId, header.getTargetId());
    assertEquals(errorId, body.errorCode());
    assertEquals(message, body.errorText());
  }
}
