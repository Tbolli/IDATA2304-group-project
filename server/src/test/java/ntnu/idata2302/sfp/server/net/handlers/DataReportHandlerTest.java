package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.server.net.ServerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;

/**
 * Unit tests for {@link DataReportHandler}.
 *
 * <p>These tests verify that the handler forwards reports to subscribers
 * via {@link ServerContext#sendToSubscribers(SmartFarmingProtocol)} and
 * how it behaves when the context throws a runtime exception.</p>
 */
public class DataReportHandlerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a received message is forwarded to subscribers by calling
   * {@link ServerContext#sendToSubscribers(SmartFarmingProtocol)} exactly once
   * with the same protocol instance.
   */
  @Test
  public void handle_forwardsMessageToSubscribers_positive() throws IOException {
    // Arrange
    SmartFarmingProtocol packet = new SmartFarmingProtocol(null, null);
    RecordingServerContext context = new RecordingServerContext();
    Socket client = new Socket();
    DataReportHandler handler = new DataReportHandler();

    // Act
    handler.handle(packet, client, context);

    // Assert
    Assertions.assertEquals(
      1,
      context.getCallCount(),
      "sendToSubscribers should be called exactly once"
    );
    Assertions.assertSame(
      packet,
      context.getLastForwarded(),
      "Handler should forward the same SmartFarmingProtocol instance it received"
    );
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that if {@link ServerContext#sendToSubscribers(SmartFarmingProtocol)}
   * throws a {@link RuntimeException}, the exception is propagated by
   * {@link DataReportHandler#handle(SmartFarmingProtocol, Socket, ServerContext)}.
   */
  @Test
  public void handle_propagatesRuntimeExceptionFromContext_negative() {
    // Arrange
    SmartFarmingProtocol packet = new SmartFarmingProtocol(null, null);
    FailingServerContext context = new FailingServerContext();
    Socket client = new Socket();
    DataReportHandler handler = new DataReportHandler();
    boolean exceptionThrown = false;

    // Act
    try {
      handler.handle(packet, client, context);
    } catch (RuntimeException e) {
      exceptionThrown = true;
    } catch (IOException e) {
      // This should not occur in this test, but we must catch it
      // because the handle(...) method declares throws IOException.
      exceptionThrown = true;
    }

    // Assert
    Assertions.assertTrue(
      exceptionThrown,
      "RuntimeException from sendToSubscribers should be propagated by the handler"
    );
  }

  /**
   * Test double for {@link ServerContext} that records calls to
   * {@link #sendToSubscribers(SmartFarmingProtocol)}.
   */
  private static class RecordingServerContext extends ServerContext {

    private SmartFarmingProtocol lastForwarded;
    private int callCount;

    @Override
    public void sendToSubscribers(SmartFarmingProtocol packet) {
      this.lastForwarded = packet;
      this.callCount = this.callCount + 1;
    }

    SmartFarmingProtocol getLastForwarded() {
      return this.lastForwarded;
    }

    int getCallCount() {
      return this.callCount;
    }
  }

  /**
   * Test double for {@link ServerContext} that always throws a
   * {@link RuntimeException} from {@link #sendToSubscribers(SmartFarmingProtocol)}
   * to simulate a failure when forwarding to subscribers.
   */
  private static class FailingServerContext extends ServerContext {

    @Override
    public void sendToSubscribers(SmartFarmingProtocol packet) {
      throw new RuntimeException("Simulated failure in sendToSubscribers");
    }
  }
}
