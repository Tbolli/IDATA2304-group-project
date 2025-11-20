package ntnu.idata2302.sfp.controlpanel.net;

import java.util.concurrent.atomic.AtomicInteger;

public final class AppContext {

  private static SfpClient client;
  private static Integer controllerId;
  private static AtomicInteger counter = new AtomicInteger(1);


  private AppContext() { } // prevent instantiation

  // --- Client handling ---
  public static void setClient(SfpClient c) {
    client = c;
  }

  public static SfpClient getClient() {
    return client;
  }

  // --- Process Id handling ---
  public static int getRequestId(){
    return counter.getAndIncrement();
  }

  // --- Controller ID handling ---
  public static void setControllerId(int id) {
    controllerId = id;
  }

  public static Integer getControllerId() {
    return controllerId;
  }

  public static boolean hasControllerId() {
    return controllerId != null;
  }
}
