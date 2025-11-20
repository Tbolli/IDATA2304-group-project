package ntnu.idata2302.sfp.controlpanel.net;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Global application context for the control panel.
 *
 * <p>This class stores shared state such as the active {@link SfpClient},
 * the controller's assigned ID, and an atomic counter used to generate
 * unique request IDs for outgoing Smart Farming Protocol messages.</p>
 *
 * <p>All members are static, and the class cannot be instantiated.</p>
 */

public final class AppContext {

  private static SfpClient client;
  private static Integer controllerId;
  private static AtomicInteger counter = new AtomicInteger(1);


  private AppContext() {
    // prevent instantiation
  }

  /**
   * Sets the active {@link SfpClient} instance for the application.
   *
   * @param c the client instance to store
   */

  public static void setClient(SfpClient c) {
    client = c;
  }

  /**
   * Returns the active {@link SfpClient} for the application.
   *
   * @return the current client, or {@code null} if not yet set
   */

  public static SfpClient getClient() {
    return client;
  }

  /**
   * Generates and returns a new unique request ID.
   *
   * <p>Each outgoing packet from the control panel must contain a unique
   * request identifier. This method increments an atomic counter to ensure
   * uniqueness across threads.</p>
   *
   * @return a new sequential request ID
   */

  public static int getRequestId() {
    return counter.getAndIncrement();
  }

  /**
   * Sets the controller ID for this control-panel application.
   *
   * @param id the identifier assigned by the server
   */

  public static void setControllerId(int id) {
    controllerId = id;
  }

  /**
   * Returns the controller ID assigned to this control panel.
   *
   * @return the assigned controller ID, or {@code null} if not set
   */

  public static Integer getControllerId() {
    return controllerId;
  }

  /**
   * Indicates whether the controller ID has been assigned.
   *
   * @return {@code true} if the controller has an assigned ID; {@code false} otherwise
   */

  public static boolean hasControllerId() {
    return controllerId != null;
  }
}
