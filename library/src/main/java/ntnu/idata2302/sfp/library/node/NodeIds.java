package ntnu.idata2302.sfp.library.node;

/**
 * Utility class containing well-known node id constants and predicate
 * helpers for identifying node id categories used by the protocol.
 *
 * <p>All members are static and the class is not instantiable. The class
 * documents the numeric ranges used for control panels and sensor nodes.</p>
 */
public final class NodeIds {

  /**
   * Broadcast identifier. Used to address all nodes; value {@code 0x00000000}.
   */
  public static final int BROADCAST = 0x00000000;

  /**
   * Server identifier. Used to address the central server; value {@code 0x00000001}.
   */
  public static final int SERVER = 0x00000001;

  // Convenience helpers

  /**
   * Check whether the given id is the broadcast id.
   *
   * @param id the node identifier to test
   * @return {@code true} if {@code id} equals {@link #BROADCAST}, otherwise {@code false}
   */
  public static boolean isBroadcast(int id) {
    return id == BROADCAST;
  }

  /**
   * Check whether the given id is the server id.
   *
   * @param id the node identifier to test
   * @return {@code true} if {@code id} equals {@link #SERVER}, otherwise {@code false}
   */
  public static boolean isServer(int id) {
    return id == SERVER;
  }

  /**
   * Determine if the given id belongs to a control panel.
   *
   * <p>Control panel ids occupy the inclusive range {@code 0x00000002} to {@code 0x0000FFFF}.</p>
   *
   * @param id the node identifier to test
   * @return {@code true} if {@code id} is within the control panel range, otherwise {@code false}
   */
  public static boolean isControlPanel(int id) {
    return id >= 0x00000002 && id <= 0x0000FFFF;
  }

  /**
   * Determine if the given id belongs to a sensor node.
   *
   * <p>Sensor node ids are any id greater than or equal to {@code 0x00010000}.</p>
   *
   * @param id the node identifier to test
   * @return {@code true} if {@code id} is a sensor node id, otherwise {@code false}
   */
  public static boolean isSensorNode(int id) {
    return id >= 0x00010000;
  }

  /**
   * Private constructor to prevent instantiation of this utility class.
   */
  private NodeIds() {
  }
}