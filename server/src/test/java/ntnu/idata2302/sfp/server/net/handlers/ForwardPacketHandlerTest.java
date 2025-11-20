package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeIds;
import ntnu.idata2302.sfp.server.net.ServerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.UUID;

/**
 * Unit tests for {@link ForwardPacketHandler}.
 *
 * <p>These tests verify that packets are forwarded to their target node,
 * and that packets targeting the server (COMMAND / COMMAND_ACK / ERROR)
 * are handled locally and not forwarded.</p>
 */
public class ForwardPacketHandlerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a packet with a non-server target is forwarded once
   * via {@link ServerContext#sendTo(SmartFarmingProtocol)}.
   */
  @Test
  public void handle_forwardsPacketWhenTargetIsNotServer_positive() throws Exception {
    // Arrange
    int serverId = NodeIds.SERVER;
    int targetId = serverId + 1; // ensure not the server
    Header header = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.COMMAND,
        serverId,
        targetId,
        0,
        UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);

    RecordingServerContext context = new RecordingServerContext();
    Socket client = new Socket();
    ForwardPacketHandler handler = new ForwardPacketHandler();

    // Act
    handler.handle(packet, client, context);

    // Assert
    Assertions.assertEquals(
        1,
        context.getCallCount(),
        "sendTo should be called exactly once for non-server targets"
    );
    Assertions.assertSame(
        packet,
        context.getLastPacket(),
        "Handler should forward the same SmartFarmingProtocol instance it received"
    );
  }

  /**
   * Verifies that a COMMAND packet targeted at the server is not forwarded
   * and that {@link ServerContext#sendTo(SmartFarmingProtocol)} is never called.
   */
  @Test
  public void handle_doesNotForwardCommandTargetedAtServer_positive() throws Exception {
    // Arrange
    int serverId = NodeIds.SERVER;
    Header header = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.COMMAND,
        10,
        serverId,
        0,
        UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);

    RecordingServerContext context = new RecordingServerContext();
    Socket client = new Socket();
    ForwardPacketHandler handler = new ForwardPacketHandler();

    // Act
    handler.handle(packet, client, context);

    // Assert
    Assertions.assertEquals(
        0,
        context.getCallCount(),
        "COMMAND targeted at the server should not be forwarded"
    );
  }

  /**
   * Verifies that an ERROR packet targeted at the server is not forwarded
   * and that {@link ServerContext#sendTo(SmartFarmingProtocol)} is never called.
   */
  @Test
  public void handle_doesNotForwardErrorTargetedAtServer_positive() throws Exception {
    // Arrange
    int serverId = NodeIds.SERVER;
    Header header = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.ERROR,
        20,
        serverId,
        0,
        UUID.randomUUID()
    );
    ErrorBody errorBody = new ErrorBody(1, "Some error");
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, errorBody);

    RecordingServerContext context = new RecordingServerContext();
    Socket client = new Socket();
    ForwardPacketHandler handler = new ForwardPacketHandler();

    // Act
    handler.handle(packet, client, context);

    // Assert
    Assertions.assertEquals(
        0,
        context.getCallCount(),
        "ERROR targeted at the server should not be forwarded"
    );
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that if {@link ServerContext#sendTo(SmartFarmingProtocol)} throws
   * an {@link IOException}, the exception is propagated by
   * {@link ForwardPacketHandler#handle(SmartFarmingProtocol, Socket, ServerContext)}.
   */
  @Test
  public void handle_propagatesIOExceptionFromSendTo_negative() {
    // Arrange
    int serverId = NodeIds.SERVER;
    int targetId = serverId + 1; // ensure not the server so forwarding is attempted
    Header header = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.COMMAND,
        serverId,
        targetId,
        0,
        UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);

    FailingServerContext context = new FailingServerContext();
    Socket client = new Socket();
    ForwardPacketHandler handler = new ForwardPacketHandler();
    boolean exceptionThrown = false;

    // Act
    try {
      handler.handle(packet, client, context);
    } catch (Exception e) {
      exceptionThrown = true;
    }

    // Assert
    Assertions.assertTrue(
        exceptionThrown,
        "IOException from sendTo should be propagated by the handler"
    );
  }

  /**
   * Test double for {@link ServerContext} that records calls to
   * {@link #sendTo(SmartFarmingProtocol)}.
   */
  private static class RecordingServerContext extends ServerContext {

    private SmartFarmingProtocol lastPacket;
    private int callCount;

    @Override
    public void sendTo(SmartFarmingProtocol packet) throws IOException {
      this.lastPacket = packet;
      this.callCount = this.callCount + 1;
    }

    SmartFarmingProtocol getLastPacket() {
      return this.lastPacket;
    }

    int getCallCount() {
      return this.callCount;
    }
  }

  /**
   * Test double for {@link ServerContext} that always throws an
   * {@link IOException} from {@link #sendTo(SmartFarmingProtocol)}
   * to simulate a forwarding failure.
   */
  private static class FailingServerContext extends ServerContext {

    @Override
    public void sendTo(SmartFarmingProtocol packet) throws IOException {
      throw new IOException("Simulated send failure");
    }
  }
}
