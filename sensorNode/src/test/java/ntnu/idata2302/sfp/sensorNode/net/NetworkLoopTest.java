package ntnu.idata2302.sfp.sensorNode.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link NetworkLoop}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>NetworkLoop calls readOnePacket() while connected.</li>
 *   <li>NetworkLoop stops when client.isConnected() becomes false.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>NetworkLoop stops cleanly when readOnePacket() throws an exception.</li>
 * </ul>
 */
public class NetworkLoopTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that NetworkLoop calls readOnePacket() at least once while connected.
   */
  @Test
  void run_readsPackets_positive() throws Exception {
    // Arrange
    FakeContext client = new FakeContext(false);
    NetworkLoop loop = new NetworkLoop(client);
    Thread thread = new Thread(loop);

    // Act
    thread.start();
    Thread.sleep(60); // allow a few loop iterations
    client.connected = false; // stop loop
    thread.join(200);

    // Assert
    assertTrue(client.readCount > 0);
  }

  /**
   * Verifies that NetworkLoop stops when the client becomes disconnected.
   */
  @Test
  void run_stopsWhenDisconnected_positive() throws Exception {
    // Arrange
    FakeContext client = new FakeContext(false);
    NetworkLoop loop = new NetworkLoop(client);
    Thread thread = new Thread(loop);

    // Act
    thread.start();
    Thread.sleep(40);
    client.connected = false; // disconnect
    thread.join(200);

    // Assert: loop should not continue reading forever
    int reads = client.readCount;
    Thread.sleep(40);
    assertEquals(reads, client.readCount);
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that NetworkLoop stops if readOnePacket() throws an exception.
   */
  @Test
  void run_stopsOnException_negative() throws Exception {
    // Arrange
    FakeContext client = new FakeContext(true); // will throw
    NetworkLoop loop = new NetworkLoop(client);
    Thread thread = new Thread(loop);

    // Act
    thread.start();
    thread.join(200);

    // Assert
    assertEquals(0, client.readCount);
  }

  // --------------------------- FAKE CONTEXT ---------------------------------- //

  /**
   * Fake SensorNodeContext for testing NetworkLoop without networking.
   */
  private static class FakeContext extends SensorNodeContext {

    boolean connected = true;
    boolean throwOnRead = false;
    int readCount = 0;

    FakeContext(boolean throwOnRead) {
      super("localhost", 1234, null);
      this.throwOnRead = throwOnRead;
    }

    @Override
    public boolean isConnected() {
      return connected;
    }

    @Override
    public SmartFarmingProtocol readOnePacket() {
      if (throwOnRead) {
        throw new RuntimeException("Simulated read() failure");
      }
      readCount++;
      return dummyPacket(); // safe packet ignored by PacketHandler
    }

    private SmartFarmingProtocol dummyPacket() {
      Header header = new Header(
        new byte[]{'S','F','P'},
        (byte) 1,
        MessageTypes.DATA_REPORT, // ignored by PacketHandler
        0,
        0,
        0,
        UUID.randomUUID()
      );
      return new SmartFarmingProtocol(header, null);
    }
  }

}
