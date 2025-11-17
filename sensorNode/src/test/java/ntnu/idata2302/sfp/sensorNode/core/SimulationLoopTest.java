package ntnu.idata2302.sfp.sensorNode.core;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.sensorNode.net.SensorNodeContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link SimulationLoop}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>run() calls node.tick() exactly once before interruption.</li>
 *   <li>run() sends exactly one packet before interruption.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>run() stops cleanly when the thread is interrupted.</li>
 * </ul>
 */
public class SimulationLoopTest {

  /**
   * Verifies that run() calls tick() exactly once and sends
   * a packet before being interrupted.
   */
  @Test
  void run_singleIteration_positive() throws Exception {
    // Arrange
    FakeSensorNode fakeNode = new FakeSensorNode();
    FakeNodeContext fakeClient = new FakeNodeContext(fakeNode);

    SimulationLoop loop = new SimulationLoop(fakeNode, fakeClient);
    Thread thread = new Thread(loop);

    // Act
    thread.start();
    Thread.sleep(50);
    thread.interrupt();
    thread.join(200);

    // Assert
    assertEquals(1, fakeNode.tickCount);
    assertEquals(1, fakeClient.sentCount);
  }


  /**
   * Verifies that run() stops cleanly when interrupted.
   */
  @Test
  void run_interruptedStops_negative() throws Exception {
    // Arrange
    FakeSensorNode fakeNode = new FakeSensorNode();
    FakeNodeContext fakeClient = new FakeNodeContext(fakeNode);

    SimulationLoop loop = new SimulationLoop(fakeNode, fakeClient);
    Thread thread = new Thread(loop);

    // Act
    thread.start();
    Thread.sleep(30);
    thread.interrupt();
    thread.join(200);

    // Assert
    assertTrue(fakeNode.tickCount >= 1);
    assertTrue(fakeClient.sentCount >= 1);
  }


  // ---------------------------------------------------------------------
  // Fake Helper Classes
  // ---------------------------------------------------------------------

  /**
   * Fake SensorNode for tracking tick() calls.
   */
  private static class FakeSensorNode extends SensorNode {
    int tickCount = 0;

    FakeSensorNode() {
      super(new java.util.ArrayList<Sensor>(), new java.util.ArrayList<Actuator>(), false, false);
    }

    @Override
    public void tick() {
      tickCount++;
    }
  }

  /**
   * Fake SensorNodeContext that counts packets sent.
   */
  private static class FakeNodeContext extends SensorNodeContext {
    int sentCount = 0;

    FakeNodeContext(SensorNode node) {
      super("localhost", 1234, node);
    }

    @Override
    public void sendPacket(SmartFarmingProtocol protocol) {
      sentCount++;
    }
  }


}
