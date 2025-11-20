package ntnu.idata2302.sfp.server.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.server.entity.Subscription;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link ServerContext}.
 */
public class ServerContextTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that sendTo(Socket, SmartFarmingProtocol) writes bytes to the remote socket.
   */
  @Test
  public void sendTo_withSocketOverload_writesBytesToGivenSocket_positive() throws Exception {
    // Arrange
    ServerSocket serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();
    Socket clientSide = new Socket("localhost", port);
    Socket serverSide = serverSocket.accept();

    ServerContext context = new ServerContext();
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
    clientSide.setSoTimeout(1000);

    // Act
    context.sendTo(serverSide, packet);
    InputStream in = clientSide.getInputStream();
    byte[] headerBytes = in.readNBytes(Header.HEADER_SIZE);

    // Assert
    Assertions.assertEquals(
      Header.HEADER_SIZE,
      headerBytes.length,
      "Client should receive full header bytes"
    );

    clientSide.close();
    serverSide.close();
    serverSocket.close();
  }

  /**
   * Verifies that setSubscription and getSubscribersForSensorNode cooperate correctly.
   */
  @Test
  public void setSubscription_andGetSubscribersForSensorNode_positive() {
    // Arrange
    ServerContext context = new ServerContext();

    int cp1 = 10;
    int cp2 = 20;
    int cpOther = 30;
    int sensorNodeId = 5;

    context.setSubscription(new Subscription(cp1, sensorNodeId));
    context.setSubscription(new Subscription(cp2, sensorNodeId));
    // unrelated subscription
    context.setSubscription(new Subscription(cpOther, 999));

    // Act
    List<Integer> subscribers = context.getSubscribersForSensorNode(sensorNodeId);

    // Assert
    Assertions.assertEquals(
      2,
      subscribers.size(),
      "Two control panels should subscribe to this sensor"
    );
    Assertions.assertTrue(subscribers.contains(cp1));
    Assertions.assertTrue(subscribers.contains(cp2));
  }

  /**
   * Verifies that getServerNodeDescriptors returns only nodeType == 1.
   */
  @Test
  public void getServerNodeDescriptors_filtersByNodeType_positive() {
    // Arrange
    ServerContext context = new ServerContext();

    NodeDescriptor node1 = new NodeDescriptor(1, 1, null, null, null, null);
    NodeDescriptor node2 = new NodeDescriptor(2, 2, null, null, null, null);
    NodeDescriptor node3 = new NodeDescriptor(3, 1, null, null, null, null);

    context.registerNode(1, node1, new Socket());
    context.registerNode(2, node2, new Socket());
    context.registerNode(3, node3, new Socket());

    // Act
    List<NodeDescriptor> result = context.getServerNodeDescriptors();

    // Assert
    Assertions.assertEquals(2, result.size());
    Assertions.assertTrue(result.contains(node1));
    Assertions.assertTrue(result.contains(node3));
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  @Test
  public void sendTo_withMissingTargetId_doesNotThrow_negative() throws IOException {
    ServerContext context = new ServerContext();
    int missingTarget = 999;

    Header header = new Header(
      new byte[]{'S','F','P'},
      (byte)1,
      MessageTypes.COMMAND,
      1,
      missingTarget,
      0,
      UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);

    // Should not throw
    context.sendTo(packet);
  }

  @Test
  public void getSubscribersForSensorNode_noSubscriptions_returnsEmptyList_negative() {
    ServerContext context = new ServerContext();
    List<Integer> subs = context.getSubscribersForSensorNode(123);

    Assertions.assertNotNull(subs);
    Assertions.assertTrue(subs.isEmpty());
  }

  @Test
  public void broadcast_withNoRegisteredNodes_doesNotThrow_negative() {
    ServerContext context = new ServerContext();
    Header header = new Header(
      new byte[]{'S','F','P'},
      (byte)1,
      MessageTypes.COMMAND,
      1,
      0,
      0,
      UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);

    // Should not throw
    context.broadcast(packet);
  }
}
