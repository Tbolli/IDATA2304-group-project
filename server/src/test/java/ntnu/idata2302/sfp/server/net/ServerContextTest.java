package ntnu.idata2302.sfp.server.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Unit tests for {@link ServerContext}.
 *
 * <p>These tests exercise registration of nodes, sending packets by node id
 * and socket, subscriptions and forwarding to subscribers, node descriptor
 * filtering, and simple negative cases where no nodes or subscriptions exist.</p>
 */
public class ServerContextTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //


  /**
   * Verifies that {@link ServerContext#sendTo(Socket, SmartFarmingProtocol)}
   * writes a packet to the given socket.
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
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
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
      "Client should receive a full header from sendTo(Socket, ...)"
    );

    // Cleanup
    clientSide.close();
    serverSide.close();
    serverSocket.close();
  }

  /**
   * Verifies that {@link ServerContext#setSubscriptions(int, java.util.List)}
   * and {@link ServerContext#getSubscribersForSensorNode(int)} cooperate so
   * that the correct control panel ids are returned for a given sensor node.
   */
  @Test
  public void setSubscriptions_andGetSubscribersForSensorNode_positive() {
    // Arrange
    ServerContext context = new ServerContext();
    int controlPanel1 = 10;
    int controlPanel2 = 20;
    int otherControlPanel = 30;
    int sensorNodeId = 5;

    List<String> emptyMetrics = new ArrayList<String>();
    List<String> emptyActuators = new ArrayList<String>();
    SubscribeBody.NodeSubscription subscription =
      new SubscribeBody.NodeSubscription(sensorNodeId, emptyMetrics, emptyActuators);

    List<SubscribeBody.NodeSubscription> subsForSensor1 =
      new ArrayList<SubscribeBody.NodeSubscription>();
    subsForSensor1.add(subscription);

    List<SubscribeBody.NodeSubscription> subsForSensor2 =
      new ArrayList<SubscribeBody.NodeSubscription>();
    subsForSensor2.add(subscription);

    List<SubscribeBody.NodeSubscription> subsForOther =
      new ArrayList<SubscribeBody.NodeSubscription>();

    context.setSubscriptions(controlPanel1, subsForSensor1);
    context.setSubscriptions(controlPanel2, subsForSensor2);
    context.setSubscriptions(otherControlPanel, subsForOther);

    // Act
    List<Integer> subscribers = context.getSubscribersForSensorNode(sensorNodeId);

    // Assert
    Assertions.assertEquals(
      2,
      subscribers.size(),
      "Two control panels should be subscribed to the sensor"
    );
    Assertions.assertTrue(
      subscribers.contains(controlPanel1),
      "Subscribers should contain controlPanel1"
    );
    Assertions.assertTrue(
      subscribers.contains(controlPanel2),
      "Subscribers should contain controlPanel2"
    );
  }


  /**
   * Verifies that {@link ServerContext#getServerNodeDescriptors()} returns
   * only descriptors with {@code nodeType == 1}.
   */
  @Test
  public void getServerNodeDescriptors_filtersByNodeType_positive() {
    // Arrange
    ServerContext context = new ServerContext();

    NodeDescriptor node1 =
      new NodeDescriptor(1, 1, null, null, null, null);
    NodeDescriptor node2 =
      new NodeDescriptor(2, 2, null, null, null, null);
    NodeDescriptor node3 =
      new NodeDescriptor(3, 1, null, null, null, null);

    context.registerNode(1, node1, new Socket());
    context.registerNode(2, node2, new Socket());
    context.registerNode(3, node3, new Socket());

    // Act
    List<NodeDescriptor> result = context.getServerNodeDescriptors();

    // Assert
    Assertions.assertEquals(
      2,
      result.size(),
      "Only two descriptors should have nodeType == 1"
    );
    Assertions.assertTrue(
      result.contains(node1),
      "Result should contain node1"
    );
    Assertions.assertTrue(
      result.contains(node3),
      "Result should contain node3"
    );
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that calling {@link ServerContext#sendTo(SmartFarmingProtocol)}
   * when no socket is registered for the target id does not throw an exception.
   */
  @Test
  public void sendTo_withMissingTargetId_doesNotThrow_negative() throws IOException {
    // Arrange
    ServerContext context = new ServerContext();
    int missingTargetId = 999;
    Header header = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.COMMAND,
      1,
      missingTargetId,
      0,
      UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);
    boolean threw = false;

    // Act
    try {
      context.sendTo(packet);
    } catch (IOException e) {
      threw = true;
    }

    // Assert
    Assertions.assertFalse(
      threw,
      "sendTo should not throw when the target socket is missing"
    );
  }

  /**
   * Verifies that {@link ServerContext#getSubscribersForSensorNode(int)}
   * returns an empty list when no subscriptions exist for the given sensor.
   */
  @Test
  public void getSubscribersForSensorNode_noSubscriptions_returnsEmptyList_negative() {
    // Arrange
    ServerContext context = new ServerContext();
    int sensorId = 123;

    // Act
    List<Integer> subscribers = context.getSubscribersForSensorNode(sensorId);

    // Assert
    Assertions.assertNotNull(
      subscribers,
      "Returned subscriber list should not be null"
    );
    Assertions.assertTrue(
      subscribers.isEmpty(),
      "Subscriber list should be empty when no subscriptions exist"
    );
  }

  /**
   * Verifies that {@link ServerContext#broadcast(SmartFarmingProtocol)}
   * does not throw when there are no registered nodes.
   */
  @Test
  public void broadcast_withNoRegisteredNodes_doesNotThrow_negative() {
    // Arrange
    ServerContext context = new ServerContext();
    Header header = new Header(
      new byte[]{'S', 'F', 'P'},
      (byte) 1,
      MessageTypes.COMMAND,
      1,
      0,
      0,
      UUID.randomUUID()
    );
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, null);
    boolean threw = false;

    // Act
    try {
      context.broadcast(packet);
    } catch (RuntimeException e) {
      threw = true;
    }

    // Assert
    Assertions.assertFalse(
      threw,
      "broadcast should not throw when there are no registered nodes"
    );
  }
}
