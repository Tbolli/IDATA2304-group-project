package ntnu.idata2302.sfp.server.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.net.handlers.MessageHandler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.Socket;
import java.util.UUID;

/**
 * Unit tests for {@link MessageDispatcher}.
 *
 * <p>These tests verify that message handlers are correctly registered,
 * invoked, or skipped when missing, and that exceptions thrown by handlers
 * are caught without crashing the dispatcher.</p>
 */
public class MessageDispatcherTest {


  // --------------------------- POSITIVE TESTS ---------------------------------- //


  /**
   * Verifies that when a handler is registered for a message type,
   * {@link MessageDispatcher#dispatch(SmartFarmingProtocol, Socket, ServerContext)}
   * invokes the correct handler.
   */
  @Test
  public void dispatch_invokesRegisteredHandler_positive() throws Exception {
    // Arrange
    MessageDispatcher dispatcher = new MessageDispatcher();
    RecordingHandler handler = new RecordingHandler();
    dispatcher.registerHandler(MessageTypes.COMMAND, handler);

    Header header = new Header(
      new byte[]{'S','F','P'},
      (byte)1,
      MessageTypes.COMMAND,
      1,
      2,
      0,
      UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);

    Socket client = null;
    ServerContext context = new ServerContext();

    // Act
    dispatcher.dispatch(packet, client, context);

    // Assert
    Assertions.assertTrue(
      handler.wasCalled(),
      "Registered handler should be called when dispatching"
    );
    Assertions.assertSame(packet, handler.getLastPacket());
    Assertions.assertSame(client, handler.getLastClient());
    Assertions.assertSame(context, handler.getLastContext());
  }

  /**
   * Verifies that dispatching a message with no registered handler
   * does not throw an exception and does not call any handler.
   */
  @Test
  public void dispatch_noRegisteredHandler_positive() {
    // Arrange
    MessageDispatcher dispatcher = new MessageDispatcher();
    RecordingHandler handler = new RecordingHandler(); // not registered

    Header header = new Header(
      new byte[]{'S','F','P'},
      (byte)1,
      MessageTypes.DATA_REPORT,
      1,
      2,
      0,
      UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);

    Socket client = null;
    ServerContext context = new ServerContext();

    // Act
    dispatcher.dispatch(packet, client, context);

    // Assert
    Assertions.assertFalse(
      handler.wasCalled(),
      "Handler should not be called if not registered"
    );
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that if a handler throws an exception, the dispatcher
   * catches it and continues without crashing.
   */
  @Test
  public void dispatch_handlerThrowsException_negative() {
    // Arrange
    MessageDispatcher dispatcher = new MessageDispatcher();
    ThrowingHandler handler = new ThrowingHandler();
    dispatcher.registerHandler(MessageTypes.SUBSCRIBE, handler);

    Header header = new Header(
      new byte[]{'S','F','P'},
      (byte)1,
      MessageTypes.SUBSCRIBE,
      1,
      2,
      0,
      UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);

    Socket client = null;
    ServerContext context = new ServerContext();

    boolean threwOutside = false;

    // Act
    try {
      dispatcher.dispatch(packet, client, context);
    } catch (Exception e) {
      threwOutside = true; // Should NOT happen
    }

    // Assert
    Assertions.assertFalse(
      threwOutside,
      "Dispatcher should catch exceptions thrown by handlers"
    );
    Assertions.assertTrue(
      handler.wasCalled(),
      "Throwing handler should still be invoked"
    );
  }

  // --------------------------- TEST DOUBLES ---------------------------------- //

  /**
   * Test double that records whether it was called and with what parameters.
   */
  private static class RecordingHandler implements MessageHandler {

    private boolean called;
    private SmartFarmingProtocol lastPacket;
    private Socket lastClient;
    private ServerContext lastContext;

    @Override
    public void handle(SmartFarmingProtocol message, Socket client, ServerContext context) {
      this.called = true;
      this.lastPacket = message;
      this.lastClient = client;
      this.lastContext = context;
    }

    boolean wasCalled() {
      return this.called;
    }

    SmartFarmingProtocol getLastPacket() {
      return this.lastPacket;
    }

    Socket getLastClient() {
      return this.lastClient;
    }

    ServerContext getLastContext() {
      return this.lastContext;
    }
  }

  /**
   * Test double that always throws an exception when invoked.
   */
  private static class ThrowingHandler implements MessageHandler {

    private boolean called;

    @Override
    public void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws Exception {
      this.called = true;
      throw new Exception("Simulated handler failure");
    }

    boolean wasCalled() {
      return this.called;
    }
  }
}
