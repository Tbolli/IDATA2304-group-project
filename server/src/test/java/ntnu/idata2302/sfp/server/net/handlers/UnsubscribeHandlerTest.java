package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.net.ServerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

/**
 * Unit tests for {@link UnSubscribeHandler}.
 */
public class UnsubscribeHandlerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  @Test
  public void handle_removesSubscriptionAndSendsAck_positive() throws IOException {
    // Arrange
    int sourceId = 123;
    int targetId = 999;
    int requestId = 10;
    int sensorNodeId = 77;

    Header requestHeader = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.UNSUBSCRIBE,
      sourceId,
      targetId,
      0,
      UUID.randomUUID()
    );

    UnsubscribeBody requestBody = new UnsubscribeBody(requestId, sensorNodeId);
    SmartFarmingProtocol requestPacket = new SmartFarmingProtocol(requestHeader, requestBody);

    RecordingServerContext context = new RecordingServerContext();
    UnSubscribeHandler handler = new UnSubscribeHandler();
    Socket client = null;

    // Act
    handler.handle(requestPacket, client, context);

    // Assert — correct removal
    Assertions.assertEquals(
      sensorNodeId,
      context.getLastRemovedSensorNodeId(),
      "Handler must call removeSubscription with correct snId"
    );

    Assertions.assertEquals(
      sourceId,
      context.getLastRemovedCpId(),
      "Handler must call removeSubscription with correct cpId"
    );

    // Assert — Ack sent
    SmartFarmingProtocol responsePacket = context.getLastSentPacket();
    Assertions.assertNotNull(responsePacket, "Handler must send a response packet");

    Assertions.assertSame(client, context.getLastSocket(), "Ack must be sent to the same socket");

    // Assert — Header
    Header ackHeader = responsePacket.getHeader();
    Assertions.assertEquals(MessageTypes.UNSUBSCRIBE_ACK, ackHeader.getMessageType());
    Assertions.assertEquals(sourceId, ackHeader.getTargetId());

    // Assert — Body (handler uses SubscribeAckBody!)
    Assertions.assertTrue(
      responsePacket.getBody() instanceof SubscribeAckBody,
      "Response body must be SubscribeAckBody"
    );

    SubscribeAckBody ack = (SubscribeAckBody) responsePacket.getBody();
    Assertions.assertEquals(requestId, ack.requestId(), "Ack must copy requestId");
    Assertions.assertEquals(1, ack.status(), "Ack status should normally be 1 for success");
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  @Test
  public void handle_propagatesIOExceptionFromSendTo_negative() {
    // Arrange
    int sourceId = 5;
    int targetId = 6;
    int requestId = 3;
    int sensorNodeId = 99;

    Header requestHeader = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.UNSUBSCRIBE,
      sourceId,
      targetId,
      0,
      UUID.randomUUID()
    );

    UnsubscribeBody requestBody = new UnsubscribeBody(requestId, sensorNodeId);
    SmartFarmingProtocol requestPacket = new SmartFarmingProtocol(requestHeader, requestBody);

    FailingServerContext context = new FailingServerContext();
    UnSubscribeHandler handler = new UnSubscribeHandler();
    Socket client = null;

    boolean thrown = false;

    // Act
    try {
      handler.handle(requestPacket, client, context);
    } catch (IOException e) {
      thrown = true;
    }

    // Assert
    Assertions.assertTrue(thrown, "IOException must propagate from ServerContext.sendTo");
  }

  // =================================================================== //
  // Test Doubles
  // =================================================================== //

  private static class RecordingServerContext extends ServerContext {

    private int lastRemovedSensorNodeId;
    private int lastRemovedCpId;
    private Socket lastSocket;
    private SmartFarmingProtocol lastSentPacket;

    @Override
    public void removeSubscription(int cpId, int snId) {
      this.lastRemovedCpId = cpId;
      this.lastRemovedSensorNodeId = snId;
    }

    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      this.lastSocket = socket;
      this.lastSentPacket = packet;
    }

    int getLastRemovedSensorNodeId() {
      return lastRemovedSensorNodeId;
    }

    int getLastRemovedCpId() {
      return lastRemovedCpId;
    }

    Socket getLastSocket() {
      return lastSocket;
    }

    SmartFarmingProtocol getLastSentPacket() {
      return lastSentPacket;
    }
  }

  private static class FailingServerContext extends ServerContext {
    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      throw new IOException("Simulated failure");
    }
  }
}
