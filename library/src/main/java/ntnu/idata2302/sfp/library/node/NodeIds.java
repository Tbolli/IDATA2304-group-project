package ntnu.idata2302.sfp.library.node;

public final class NodeIds {
  public static final int BROADCAST = 0x00000000;
  public static final int SERVER = 0x00000001;

  // Convenience helpers
  public static boolean isBroadcast(int id) { return id == BROADCAST; }
  public static boolean isServer(int id) { return id == SERVER; }
  public static boolean isControlPanel(int id) { return id >= 0x00000002 && id <= 0x0000FFFF; }
  public static boolean isSensorNode(int id) { return id >= 0x00010000; }

  private NodeIds() {}
}