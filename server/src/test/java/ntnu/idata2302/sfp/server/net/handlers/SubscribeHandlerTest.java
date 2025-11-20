package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.net.ServerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link SubscribeHandler}.
 *
 * <p>These tests verify that the handler:
 * <ul>
 *   <li>Registers subscriptions in {@link ServerContext} with incrementing IDs</li>
 *   <li>Sends a {@link MessageTypes#SUBSCRIBE_ACK} response back to the client</li>
 *   <li>Propagates {@link IOException} from {@link ServerContext#sendTo(Socket, SmartFarmingProtocol)}</li>
 * </ul>
 * </p>
 */
public class SubscribeHandlerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a SUBSCRIBE request:
   * <ul>
   *   <li>Calls {@link ServerContext#setSubscriptions(int, List)} with a subscription ID starting at 1</li>
   *   <li>Uses the same node list from the request body</li>
   *   <li>Sends a SUBSCRIBE_ACK response to the same client socket</li>
   *   <li>Copies the requestId from {@link SubscribeBody} into {@link SubscribeAckBody}</li>
   *   <li>Sets the response header message type to SUBSCRIBE_ACK and targetId to the original sourceId</li>
   * </ul>
   */
  @Test
  public void handle_registersSubscriptionAndSendsAck_positive() throws IOException {
    // Arrange
    int sourceId = 123;
    int targetId = 999;
    int requestId = 10;

    Header requestHeader = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.SUBSCRIBE,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );

    List<SubscribeBody.NodeSubscription> nodes =
        new ArrayList<SubscribeBody.NodeSubscription>();

    SubscribeBody requestBody = new SubscribeBody(requestId, nodes);
    SmartFarmingProtocol requestPacket = new SmartFarmingProtocol(requestHeader, requestBody);

    RecordingServerContext context = new RecordingServerContext();
    Socket client = null; // handler passes this directly to context; we just record it
    SubscribeHandler handler = new SubscribeHandler();

    // Act
    handler.handle(requestPacket, client, context);

    // Assert
    // Subscriptions registered
    Assertions.assertEquals(
        1,
        context.getLastSubscriptionId(),
        "First subscription ID should start at 1"
    );
    Assertions.assertSame(
        nodes,
        context.getLastSubscriptions(),
        "Handler should register the same node list from the request body"
    );

    // Response sent
    SmartFarmingProtocol responsePacket = context.getLastSentPacket();
    Assertions.assertNotNull(
        responsePacket,
        "Handler should send a response packet"
    );
    Assertions.assertSame(
        client,
        context.getLastSocket(),
        "Response should be sent to the same client socket"
    );

    // Response header checks
    Header responseHeader = responsePacket.getHeader();
    Assertions.assertEquals(
        MessageTypes.SUBSCRIBE_ACK,
        responseHeader.getMessageType(),
        "Response message type should be SUBSCRIBE_ACK"
    );
    Assertions.assertEquals(
        sourceId,
        responseHeader.getTargetId(),
        "Response targetId should match original request sourceId"
    );

    // Response body checks
    Assertions.assertTrue(
        responsePacket.getBody() instanceof SubscribeAckBody,
        "Response body should be a SubscribeAckBody"
    );
    SubscribeAckBody ackBody = (SubscribeAckBody) responsePacket.getBody();
    Assertions.assertEquals(
        requestId,
        ackBody.requestId(),
        "Ack should copy the requestId from the subscribe body"
    );
  }

  /**
   * Verifies that the internal subscription ID counter is incremented
   * for each call to {@link SubscribeHandler#handle(SmartFarmingProtocol, Socket, ServerContext)}.
   */
  @Test
  public void handle_incrementsSubscriptionIdOnMultipleCalls_positive() throws IOException {
    // Arrange
    int sourceId = 321;
    int targetId = 111;

    Header header1 = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.SUBSCRIBE,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );
    Header header2 = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.SUBSCRIBE,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );

    List<SubscribeBody.NodeSubscription> nodes1 =
        new ArrayList<SubscribeBody.NodeSubscription>();
    List<SubscribeBody.NodeSubscription> nodes2 =
        new ArrayList<SubscribeBody.NodeSubscription>();

    SubscribeBody body1 = new SubscribeBody(1, nodes1);
    SubscribeBody body2 = new SubscribeBody(2, nodes2);

    SmartFarmingProtocol packet1 = new SmartFarmingProtocol(header1, body1);
    SmartFarmingProtocol packet2 = new SmartFarmingProtocol(header2, body2);

    RecordingServerContext context = new RecordingServerContext();
    SubscribeHandler handler = new SubscribeHandler();
    Socket client = null;

    // Act
    handler.handle(packet1, client, context);
    int firstSubId = context.getLastSubscriptionId();

    handler.handle(packet2, client, context);
    int secondSubId = context.getLastSubscriptionId();

    // Assert
    Assertions.assertEquals(
        1,
        firstSubId,
        "First subscription ID should be 1"
    );
    Assertions.assertEquals(
        2,
        secondSubId,
        "Second subscription ID should be 2"
    );
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that if {@link ServerContext#sendTo(Socket, SmartFarmingProtocol)}
   * throws an {@link IOException}, the exception is propagated by
   * {@link SubscribeHandler#handle(SmartFarmingProtocol, Socket, ServerContext)}.
   */
  @Test
  public void handle_propagatesIOExceptionFromSendTo_negative() {
    // Arrange
    int sourceId = 55;
    int targetId = 66;
    int requestId = 5;

    Header requestHeader = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.SUBSCRIBE,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );

    List<SubscribeBody.NodeSubscription> nodes =
        new ArrayList<SubscribeBody.NodeSubscription>();
    SubscribeBody requestBody = new SubscribeBody(requestId, nodes);
    SmartFarmingProtocol requestPacket = new SmartFarmingProtocol(requestHeader, requestBody);

    FailingServerContext context = new FailingServerContext();
    SubscribeHandler handler = new SubscribeHandler();
    Socket client = null;
    boolean ioExceptionThrown = false;

    // Act
    try {
      handler.handle(requestPacket, client, context);
    } catch (IOException e) {
      ioExceptionThrown = true;
    }

    // Assert
    Assertions.assertTrue(
        ioExceptionThrown,
        "IOException from ServerContext.sendTo should be propagated by the handler"
    );
  }

  /**
   * Test double for {@link ServerContext} that records:
   * <ul>
   *   <li>The last subscription ID and node list passed to {@link #setSubscriptions(int, List)}</li>
   *   <li>The last socket and packet passed to {@link #sendTo(Socket, SmartFarmingProtocol)}</li>
   * </ul>
   */
  private static class RecordingServerContext extends ServerContext {

    private int lastSubscriptionId;
    private List<SubscribeBody.NodeSubscription> lastSubscriptions;
    private Socket lastSocket;
    private SmartFarmingProtocol lastSentPacket;

    @Override
    public void setSubscriptions(int subId, List<SubscribeBody.NodeSubscription> subs) {
      this.lastSubscriptionId = subId;
      this.lastSubscriptions = subs;
    }

    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      this.lastSocket = socket;
      this.lastSentPacket = packet;
    }

    int getLastSubscriptionId() {
      return this.lastSubscriptionId;
    }

    List<SubscribeBody.NodeSubscription> getLastSubscriptions() {
      return this.lastSubscriptions;
    }

    Socket getLastSocket() {
      return this.lastSocket;
    }

    SmartFarmingProtocol getLastSentPacket() {
      return this.lastSentPacket;
    }
  }

  /**
   * Test double for {@link ServerContext} that always throws an
   * {@link IOException} from {@link #sendTo(Socket, SmartFarmingProtocol)}
   * to simulate a failure when sending the acknowledgement.
   */
  private static class FailingServerContext extends ServerContext {

    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      throw new IOException("Simulated send failure");
    }
  }
}
