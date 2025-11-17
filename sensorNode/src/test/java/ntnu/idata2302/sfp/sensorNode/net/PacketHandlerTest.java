package ntnu.idata2302.sfp.sensorNode.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody.CommandPart;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.sensorNode.core.Actuator;
import ntnu.idata2302.sfp.sensorNode.core.Sensor;
import ntnu.idata2302.sfp.sensorNode.core.SensorNode;
import ntnu.idata2302.sfp.sensorNode.entity.ActuatorType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link PacketHandler}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>ANNOUNCE_ACK packets set the sensor node id on the client.</li>
 *   <li>COMMAND packets with actuator updates change actuator target values and send an ACK.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>COMMAND packets with missing actuators field result in an ERROR response.</li>
 * </ul>
 */
public class PacketHandlerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that an ANNOUNCE_ACK packet causes the client to store the targetId as its id.
   */
  @Test
  void handle_announceAck_setsClientId_positive() {
    // Arrange
    FakeSensorNodeContext client = new FakeSensorNodeContext(null);
    int expectedId = 99;

    Header header = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.ANNOUNCE_ACK,
      10,
      expectedId,
      0,
      UUID.randomUUID()
    );
    AnnounceAckBody body = new AnnounceAckBody(123, 1);
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, body);

    // Act
    PacketHandler.handle(client, packet);

    // Assert
    assertEquals(expectedId, client.lastSetId);
    assertEquals(0, client.sentCount);
  }

  /**
   * Verifies that a COMMAND packet with actuator updates changes actuator target values
   * and results in a COMMAND_ACK being sent back.
   */
  @Test
  void handle_command_updatesActuatorAndSendsAck_positive() {
    // Arrange
    // Build a simple node with one FAN actuator
    List<Sensor> sensors = new ArrayList<Sensor>();
    List<Actuator> actuators = new ArrayList<Actuator>();
    Actuator fan = new Actuator(ActuatorType.FAN, 0.0, 100.0);
    actuators.add(fan);
    SensorNode node = new SensorNode(sensors, actuators, false, false);

    FakeSensorNodeContext client = new FakeSensorNodeContext(node);

    double newValue = 80.0;
    double initialTarget = fan.getTargetValue();

    Header header = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.COMMAND,
      101,
      202,
      0,
      UUID.randomUUID()
    );

    List<CommandPart> commandParts = new ArrayList<CommandPart>();
    commandParts.add(new CommandPart("Fan", newValue)); // matches display name of ActuatorType.FAN

    CommandBody body = new CommandBody(55, commandParts);
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, body);

    // Act
    PacketHandler.handle(client, packet);

    // Assert
    // Actuator target should have changed
    assertTrue(fan.getTargetValue() != initialTarget);
    assertEquals(newValue, fan.getTargetValue());

    // One packet sent back, should be COMMAND_ACK
    assertEquals(1, client.sentCount);
    assertNotNull(client.lastSentPacket);
    Header resHeader = client.lastSentPacket.getHeader();
    assertEquals(MessageTypes.COMMAND_ACK, resHeader.getMessageType());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that a COMMAND packet with a null actuators field results in an ERROR response.
   */
  @Test
  void handle_commandMissingActuators_sendsError_negative() {
    // Arrange
    FakeSensorNodeContext client = new FakeSensorNodeContext(null);

    Header header = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.COMMAND,
      5,
      7,
      0,
      UUID.randomUUID()
    );

    CommandBody body = new CommandBody(42, null);
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, body);

    // Act
    PacketHandler.handle(client, packet);

    // Assert
    assertEquals(1, client.sentCount);
    assertNotNull(client.lastSentPacket);

    Header errorHeader = client.lastSentPacket.getHeader();
    assertEquals(MessageTypes.ERROR, errorHeader.getMessageType());

    // Body should be an ErrorBody instance
    Object responseBody = client.lastSentPacket.getBody();
    assertTrue(responseBody instanceof ErrorBody);
  }

  // --------------------------- HELPER FAKE CONTEXT ----------------------------- //

  /**
   * A fake SensorNodeContext that avoids real networking and lets us inspect
   * the effects of PacketHandler.
   */
  private static class FakeSensorNodeContext extends SensorNodeContext {

    int lastSetId = -1;
    int sentCount = 0;
    SmartFarmingProtocol lastSentPacket;
    private final SensorNode testNode;

    FakeSensorNodeContext(SensorNode node) {
      super("localhost", 1234, node);
      this.testNode = node;
    }

    @Override
    public void setId(int id) {
      this.lastSetId = id;
    }

    @Override
    public SensorNode getSensorNode() {
      return testNode;
    }

    @Override
    public void sendPacket(SmartFarmingProtocol packet) {
      sentCount++;
      lastSentPacket = packet;
    }
  }
}
