package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesListBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.server.net.ServerContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link CapabilitiesHandler}.
 *
 * <p>These tests verify that the handler creates a CAPABILITIES_LIST response
 * with the expected header and body fields, and that IO failures from the
 * {@link ServerContext} are propagated to the caller.</p>
 */
public class CapabilitiesHandlerTest {


  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a CAPABILITIES_QUERY request produces a CAPABILITIES_LIST
   * response with:
   * <ul>
   *   <li>Message type {@link MessageTypes#CAPABILITIES_LIST}</li>
   *   <li>Target ID equal to the original request's source ID</li>
   *   <li>Request ID copied from the query body</li>
   *   <li>Node descriptors taken from {@link ServerContext#getServerNodeDescriptors()}</li>
   * </ul>
   */
  @Test
  public void handle_buildsCapabilitiesListResponse_positive() throws IOException {
    // Arrange
    int requestSourceId = 1234;
    int requestTargetId = 9999; // arbitrary, not used by handler
    int requestId = 42;

    Header requestHeader = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.CAPABILITIES_QUERY,
      requestSourceId,
      requestTargetId,
      0,
      UUID.randomUUID()
    );

    CapabilitiesQueryBody requestBody = new CapabilitiesQueryBody(requestId);
    SmartFarmingProtocol requestPacket = new SmartFarmingProtocol(requestHeader, requestBody);

    List<NodeDescriptor> expectedDescriptors = new ArrayList<NodeDescriptor>();
    RecordingServerContext context = new RecordingServerContext(expectedDescriptors);

    Socket clientSocket = new Socket();
    CapabilitiesHandler handler = new CapabilitiesHandler();

    // Act
    handler.handle(requestPacket, clientSocket, context);

    // Assert
    SmartFarmingProtocol responsePacket = context.getLastPacket();
    Assertions.assertNotNull(responsePacket, "Handler should send a response packet");

    Header responseHeader = responsePacket.getHeader();
    Assertions.assertEquals(
      MessageTypes.CAPABILITIES_LIST,
      responseHeader.getMessageType(),
      "Response message type should be CAPABILITIES_LIST"
    );
    Assertions.assertEquals(
      requestSourceId,
      responseHeader.getTargetId(),
      "Response targetId should match original request sourceId"
    );

    CapabilitiesListBody responseBody = (CapabilitiesListBody) responsePacket.getBody();
    Assertions.assertNotNull(responseBody, "Response body should not be null");
    Assertions.assertEquals(
      requestId,
      responseBody.requestId(),
      "Response should copy the requestId from the query body"
    );
    Assertions.assertSame(
      expectedDescriptors,
      responseBody.nodes(),
      "Response should use the node list provided by ServerContext"
    );
    Assertions.assertSame(
      clientSocket,
      context.getLastSocket(),
      "Handler should send the response back to the same client socket"
    );
  }


  // --------------------------- NEGATIVE TESTS ---------------------------------- //


  /**
   * Verifies that if {@link ServerContext#sendTo(Socket, SmartFarmingProtocol)}
   * throws an {@link IOException}, the exception is propagated by
   * {@link CapabilitiesHandler#handle(SmartFarmingProtocol, Socket, ServerContext)}.
   */
  @Test
  public void handle_propagatesIOExceptionFromContext_negative() {
    // Arrange
    int requestSourceId = 1234;
    int requestTargetId = 9999;
    int requestId = 7;

    Header requestHeader = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.CAPABILITIES_QUERY,
      requestSourceId,
      requestTargetId,
      0,
      UUID.randomUUID()
    );

    CapabilitiesQueryBody requestBody = new CapabilitiesQueryBody(requestId);
    SmartFarmingProtocol requestPacket = new SmartFarmingProtocol(requestHeader, requestBody);

    List<NodeDescriptor> descriptors = new ArrayList<NodeDescriptor>();
    FailingServerContext context = new FailingServerContext(descriptors);

    Socket clientSocket = new Socket();
    CapabilitiesHandler handler = new CapabilitiesHandler();

    boolean ioExceptionThrown = false;

    // Act
    try {
      handler.handle(requestPacket, clientSocket, context);
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
   * Test double that records the last packet and socket passed to {@link #sendTo(Socket, SmartFarmingProtocol)}
   * and returns a fixed list of node descriptors from {@link #getServerNodeDescriptors()}.
   */
  private static class RecordingServerContext extends ServerContext {

    private final List<NodeDescriptor> descriptors;
    private SmartFarmingProtocol lastPacket;
    private Socket lastSocket;

    RecordingServerContext(List<NodeDescriptor> descriptors) {
      this.descriptors = descriptors;
    }

    @Override
    public List<NodeDescriptor> getServerNodeDescriptors() {
      return this.descriptors;
    }

    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      this.lastSocket = socket;
      this.lastPacket = packet;
    }

    SmartFarmingProtocol getLastPacket() {
      return this.lastPacket;
    }

    Socket getLastSocket() {
      return this.lastSocket;
    }
  }

  /**
   * Test double that always throws an {@link IOException} from
   * {@link #sendTo(Socket, SmartFarmingProtocol)} to simulate a failure when
   * sending a packet back to a client.
   */
  private static class FailingServerContext extends ServerContext {

    private final List<NodeDescriptor> descriptors;

    FailingServerContext(List<NodeDescriptor> descriptors) {
      this.descriptors = descriptors;
    }

    @Override
    public List<NodeDescriptor> getServerNodeDescriptors() {
      return this.descriptors;
    }

    @Override
    public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
      throw new IOException("Simulated send failure");
    }
  }
}
