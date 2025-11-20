package ntnu.idata2302.sfp.server.net.handlers;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.server.net.ServerContext;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.Socket;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test class for {@link AnnounceHandler}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Handle method registers the node and sends an ANNOUNCE_ACK with the same requestId as the ANNOUNCE.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Handle method throws {@link ClassCastException} if the message body is not an {@link AnnounceBody}.</li>
 * </ul>
 */
public class AnnounceHandlerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that handle() registers the node and sends an ANNOUNCE_ACK
   * with the same requestId as the original ANNOUNCE message.
   */
  @Test
  void handle_registersNodeAndSendsAck_positive() throws IOException {
    // Arrange
    final int requestId = 42;

    NodeDescriptor descriptor = new NodeDescriptor(
      null,
      1,
      Collections.emptyList(),
      Collections.emptyList(),
      Boolean.FALSE,
      Boolean.FALSE
    );

    AnnounceBody announceBody = new AnnounceBody(requestId, descriptor);

    SmartFarmingProtocol request = new SmartFarmingProtocol(
      null,   // Header is not used by AnnounceHandler
      announceBody
    );

    final SmartFarmingProtocol[] capturedResponse = new SmartFarmingProtocol[1];
    final int[] registeredNodeId = new int[1];
    final NodeDescriptor[] registeredDescriptor = new NodeDescriptor[1];
    final Socket[] registeredSocket = new Socket[1];

    ServerContext context = new ServerContext() {
      @Override
      public void registerNode(int nodeId, NodeDescriptor nodeDescriptor, Socket socket) {
        registeredNodeId[0] = nodeId;
        registeredDescriptor[0] = nodeDescriptor;
        registeredSocket[0] = socket;
      }

      @Override
      public void sendTo(Socket socket, SmartFarmingProtocol packet) throws IOException {
        capturedResponse[0] = packet;
      }
    };

    Socket clientSocket = new Socket();
    AnnounceHandler handler = new AnnounceHandler();

    // Act
    handler.handle(request, clientSocket, context);

    // Assert
    // Registered node
    assertNotNull(registeredDescriptor[0], "Node descriptor should be registered");
    // The server should assign a nodeId – cannot remain null
    assertNotNull(registeredDescriptor[0].nodeId(), "Registered descriptor must have a generated nodeId");

    // All other fields must match
    assertEquals(descriptor.nodeType(), registeredDescriptor[0].nodeType());
    assertEquals(descriptor.sensors(), registeredDescriptor[0].sensors());
    assertEquals(descriptor.actuators(), registeredDescriptor[0].actuators());
    assertEquals(descriptor.supportsImages(), registeredDescriptor[0].supportsImages());
    assertEquals(descriptor.supportsAggregates(), registeredDescriptor[0].supportsAggregates());

    assertEquals(clientSocket, registeredSocket[0], "Registered socket should be the client socket");

    // Response
    SmartFarmingProtocol response = capturedResponse[0];
    assertNotNull(response, "Handler should send a response");

    // Header message type
    assertNotNull(response.getHeader(), "Response header must not be null");
    assertEquals(
      MessageTypes.ANNOUNCE_ACK,
      response.getHeader().getMessageType(),
      "Response message type must be ANNOUNCE_ACK"
    );

    // Body
    Object body = response.getBody();
    assertNotNull(body, "Response body must not be null");
    if (!(body instanceof AnnounceAckBody)) {
      fail("Response body should be of type AnnounceAckBody");
    }
    AnnounceAckBody ackBody = (AnnounceAckBody) body;

    // We expect the same requestId as in the original announce
    assertEquals(requestId, ackBody.requestId(), "Ack must contain the same requestId as the announce");
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that handle() throws ClassCastException when the message body
   * is not an instance of {@link AnnounceBody}.
   */
  @Test
  void handle_throwsOnWrongBodyType_negative() throws IOException {
    // Arrange
    // Use an ANNOUNCE_ACK body instead of ANNOUNCE to trigger the cast error.
    AnnounceAckBody wrongBody = new AnnounceAckBody(1, 1);
    SmartFarmingProtocol request = new SmartFarmingProtocol(
      null,   // header not used
      wrongBody
    );

    ServerContext context = new ServerContext();
    Socket clientSocket = new Socket();
    AnnounceHandler handler = new AnnounceHandler();

    // Act / Assert
    try {
      handler.handle(request, clientSocket, context);
      fail("Expected ClassCastException when body is not AnnounceBody");
    } catch (ClassCastException expected) {
      // Expected path: the handler blindly casts to AnnounceBody
    }
  }
}
