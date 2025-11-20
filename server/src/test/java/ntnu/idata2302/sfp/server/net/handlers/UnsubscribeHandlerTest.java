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
 *
 * <p>These tests verify that the handler:
 * <ul>
 *   <li>Removes subscriptions in {@link ServerContext} based on the request body</li>
 *   <li>Sends an {@link MessageTypes#UNSUBSCRIBE_ACK} response back to the client</li>
 *   <li>Uses an internal counter for the acknowledgement subscription ID</li>
 *   <li>Propagates {@link IOException} from {@link ServerContext#sendTo(Socket, SmartFarmingProtocol)}</li>
 * </ul>
 * </p>
 */
public class UnsubscribeHandlerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that an UNSUBSCRIBE request:
   * <ul>
   *   <li>Calls {@link ServerContext#removeSubscriptions(int)} with the subscription ID from the request</li>
   *   <li>Sends an UNSUBSCRIBE_ACK response to the same client socket</li>
   *   <li>Copies the requestId from {@link UnsubscribeBody} into {@link SubscribeAckBody}</li>
   *   <li>Sets the response header message type to UNSUBSCRIBE_ACK and targetId to the original sourceId</li>
   * </ul>
   */
  @Test
  public void handle_removesSubscriptionAndSendsAck_positive() throws IOException {
    // Arrange
    int sourceId = 123;
    int targetId = 999;
    int requestId = 10;
    int subscriptionId = 77;

    Header requestHeader = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.UNSUBSCRIBE,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );

    UnsubscribeBody requestBody = new UnsubscribeBody(requestId, subscriptionId);
    SmartFarmingProtocol requestPacket = new SmartFarmingProtocol(requestHeader, requestBody);

    RecordingServerContext context = new RecordingServerContext();
    Socket client = null;
    UnSubscribeHandler handler = new UnSubscribeHandler();

    // Act
    handler.handle(requestPacket, client, context);

    // Assert
    // Subscription removal
    Assertions.assertEquals(
        subscriptionId,
        context.getLastRemovedSubscriptionId(),
        "Handler should remove the subscription with the ID from the request body"
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
        MessageTypes.UNSUBSCRIBE_ACK,
        responseHeader.getMessageType(),
        "Response message type should be UNSUBSCRIBE_ACK"
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
        "Ack should copy the requestId from the unsubscribe body"
    );
  }

  /**
   * Verifies that the internal acknowledgement subscription ID counter
   * is incremented for each call to
   * {@link UnSubscribeHandler#handle(SmartFarmingProtocol, Socket, ServerContext)}.
   */
  @Test
  public void handle_incrementsAckSubscriptionIdOnMultipleCalls_positive() throws IOException {
    // Arrange
    int sourceId = 1;
    int targetId = 2;

    Header header1 = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.UNSUBSCRIBE,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );
    Header header2 = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.UNSUBSCRIBE,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );

    UnsubscribeBody body1 = new UnsubscribeBody(1, 10);
    UnsubscribeBody body2 = new UnsubscribeBody(2, 20);

    SmartFarmingProtocol packet1 = new SmartFarmingProtocol(header1, body1);
    SmartFarmingProtocol packet2 = new SmartFarmingProtocol(header2, body2);

    RecordingServerContext context = new RecordingServerContext();
    UnSubscribeHandler handler = new UnSubscribeHandler();
    Socket client = null;

    // Act
    handler.handle(packet1, client, context);
    SmartFarmingProtocol response1 = context.getLastSentPacket();
    SubscribeAckBody ack1 = (SubscribeAckBody) response1.getBody();

    handler.handle(packet2, client, context);
    SmartFarmingProtocol response2 = context.getLastSentPacket();
    SubscribeAckBody ack2 = (SubscribeAckBody) response2.getBody();

    // Assert
    Assertions.assertEquals(
        1,
        ack1.subscriptionId(),
        "First acknowledgement subscriptionId should be 1"
    );
    Assertions.assertEquals(
        2,
        ack2.subscriptionId(),
        "Second acknowledgement subscriptionId should be 2"
    );
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that if {@link ServerContext#sendTo(Socket, SmartFarmingProtocol)}
   * throws an {@link IOException}, the exception is propagated by
   * {@link UnSubscribeHandler#handle(SmartFarmingProtocol, Socket, ServerContext)}.
   */
  @Test
  public void handle_propagatesIOExceptionFromSendTo_negative() {
    // Arrange
    int sourceId = 5;
    int targetId = 6;
    int requestId = 3;
    int subscriptionId = 99;

    Header requestHeader = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.UNSUBSCRIBE,
        sourceId,
        targetId,
        0,
        UUID.randomUUID()
    );

    UnsubscribeBody requestBody = new UnsubscribeBody(requestId, subscriptionId);
    SmartFarmingProtocol requestPacket = new SmartFarmingProtocol(requestHeader, requestBody);

    FailingServerContext context = new FailingServerContext();
    UnSubscribeHandler handler = new UnSubscribeHandler();
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
   *   <li>The last subscription ID passed to {@link #removeSubscriptions(int)}</li>
   *   <li>The last socket and packet passed to {@link #sendTo(Socket, SmartFarmingProtocol)}</li>
   * </ul>
   */
  private static class RecordingServerContext extends ServerContext {

    private int lastRemovedSubscriptionId;
    private Socket lastSocket;
    private SmartFarmingProtocol lastSentPacket;

    @Override
    public void removeSubscriptions(int subscriptionId) {
      this.lastRemovedSubscriptionId = subscriptionId;
    }

    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      this.lastSocket = socket;
      this.lastSentPacket = packet;
    }

    int getLastRemovedSubscriptionId() {
      return this.lastRemovedSubscriptionId;
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
