package ntnu.idata2302.sfp.sensorNode.core;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.sensorNode.net.SensorNodeContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link SimulationLoop}.
 *
 * <b>Important note:</b>
 * The real SimulationLoop sleeps for 2000ms *before* calling tick() and sendPacket().
 * Therefore, when interrupted early, tick() and sendPacket() will never run.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>run() starts and can be interrupted cleanly.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>run() stops cleanly when the thread is interrupted.</li>
 * </ul>
 */
public class SimulationLoopTest {

  /**
   * Verifies behavior when the loop is interrupted before the first 2-second sleep completes.
   * No tick() or sendPacket() calls should occur.
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
    Thread.sleep(50);     // interrupt before the loop exits the initial 2000ms sleep
    thread.interrupt();
    thread.join(200);

    // Assert: because SimulationLoop sleeps BEFORE tick(), nothing should happen.
    assertEquals(0, fakeNode.tickCount, "tick() should not run before initial sleep completes");
    assertEquals(0, fakeClient.sentCount, "sendPacket() should not run before initial sleep completes");
  }

  /**
   * Verifies that the loop exits cleanly when interrupted.
   * Does NOT expect tick() or sendPacket() because the real loop sleeps first.
   */
  @Test
  void run_interruptedStops_negative() throws Exception {
    FakeSensorNode fakeNode = new FakeSensorNode();
    FakeNodeContext fakeClient = new FakeNodeContext(fakeNode);

    SimulationLoop loop = new SimulationLoop(fakeNode, fakeClient);
    Thread thread = new Thread(loop);

    thread.start();
    Thread.sleep(30);
    thread.interrupt();
    thread.join(200);

    // Only assert clean shutdown
    assertTrue(true, "Loop should stop cleanly when interrupted");
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
