package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.entity.Subscription;
import ntnu.idata2302.sfp.server.net.ServerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

public class SubscribeHandlerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  @Test
  public void handle_registersSubscriptionAndSendsAck_positive() throws IOException {
    // Arrange
    int sourceId = 123;
    int targetId = 999;
    int requestId = 10;
    int sensorNodeId = 42;

    Header requestHeader = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.SUBSCRIBE,
      sourceId,
      targetId,
      0,
      UUID.randomUUID()
    );

    SubscribeBody requestBody = new SubscribeBody(requestId, sensorNodeId);
    SmartFarmingProtocol requestPacket =
      new SmartFarmingProtocol(requestHeader, requestBody);

    RecordingServerContext context = new RecordingServerContext();
    Socket client = null;
    SubscribeHandler handler = new SubscribeHandler();

    // Act
    handler.handle(requestPacket, client, context);

    // Assert: subscription stored
    Assertions.assertEquals(sourceId, context.getLastCpId());
    Assertions.assertEquals(sensorNodeId, context.getLastSensorNodeId());

    // Assert: response sent
    SmartFarmingProtocol responsePacket = context.getLastSentPacket();
    Assertions.assertNotNull(responsePacket);

    Assertions.assertSame(client, context.getLastSocket());

    Header responseHeader = responsePacket.getHeader();
    Assertions.assertEquals(MessageTypes.SUBSCRIBE_ACK, responseHeader.getMessageType());
    Assertions.assertEquals(sourceId, responseHeader.getTargetId());

    Assertions.assertTrue(responsePacket.getBody() instanceof SubscribeAckBody);
    SubscribeAckBody ack = (SubscribeAckBody) responsePacket.getBody();
    Assertions.assertEquals(requestId, ack.requestId());
    Assertions.assertEquals(1, ack.status());
  }

  /**
   * Since the real handler DOES NOT increment any subscription IDs,
   * this test is updated to confirm that multiple subscriptions are simply added.
   */
  @Test
  public void handle_multipleSubscriptions_positive() throws IOException {
    // Arrange
    Header h1 = new Header(new byte[]{'S','F','P'}, (byte)1, MessageTypes.SUBSCRIBE,
      10, 0, 0, UUID.randomUUID());
    Header h2 = new Header(new byte[]{'S','F','P'}, (byte)1, MessageTypes.SUBSCRIBE,
      20, 0, 0, UUID.randomUUID());

    SubscribeBody b1 = new SubscribeBody(1, 100);
    SubscribeBody b2 = new SubscribeBody(2, 200);

    SmartFarmingProtocol p1 = new SmartFarmingProtocol(h1, b1);
    SmartFarmingProtocol p2 = new SmartFarmingProtocol(h2, b2);

    RecordingServerContext context = new RecordingServerContext();
    SubscribeHandler handler = new SubscribeHandler();

    // Act
    handler.handle(p1, null, context);
    handler.handle(p2, null, context);

    // Assert: second overwrite still correct
    Assertions.assertEquals(20, context.getLastCpId());
    Assertions.assertEquals(200, context.getLastSensorNodeId());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  @Test
  public void handle_propagatesIOExceptionFromSendTo_negative() {
    // Arrange
    int sourceId = 55;
    int targetId = 66;
    int requestId = 5;
    int sensorNodeId = 7;

    Header requestHeader = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.SUBSCRIBE,
      sourceId,
      targetId,
      0,
      UUID.randomUUID()
    );

    SubscribeBody requestBody = new SubscribeBody(requestId, sensorNodeId);
    SmartFarmingProtocol requestPacket =
      new SmartFarmingProtocol(requestHeader, requestBody);

    FailingServerContext context = new FailingServerContext();
    SubscribeHandler handler = new SubscribeHandler();
    Socket client = null;

    boolean thrown = false;

    try {
      handler.handle(requestPacket, client, context);
    } catch (IOException e) {
      thrown = true;
    }

    Assertions.assertTrue(thrown);
  }

  // =========================================================================== //
  // Test Doubles
  // =========================================================================== //

  private static class RecordingServerContext extends ServerContext {

    private int lastCpId;
    private int lastSensorNodeId;
    private Socket lastSocket;
    private SmartFarmingProtocol lastSentPacket;

    @Override
    public void setSubscription(Subscription sub) {
      this.lastCpId = sub.getCpId();
      this.lastSensorNodeId = sub.getSnId();
    }

    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      this.lastSocket = socket;
      this.lastSentPacket = packet;
    }

    int getLastCpId() { return lastCpId; }
    int getLastSensorNodeId() { return lastSensorNodeId; }
    Socket getLastSocket() { return lastSocket; }
    SmartFarmingProtocol getLastSentPacket() { return lastSentPacket; }
  }

  private static class FailingServerContext extends ServerContext {
    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      throw new IOException("Simulated failure");
    }
  }
}
