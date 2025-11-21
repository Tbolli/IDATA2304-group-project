package ntnu.idata2302.sfp.controlPanel.net;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;

/**
 * Simple event bus used by the control panel to broadcast incoming
 * {@link SmartFarmingProtocol} packets to registered listeners.
 *
 * <p>This class acts as a lightweight publish/subscribe system. It stores
 * a thread-safe list of listeners, and when a packet arrives from the
 * network, it notifies each subscriber by invoking {@code accept(packet)}.</p>
 *
 * <p>All methods are static, making this class a global event dispatcher.</p>
 */

public class EventBus {

  /** Thread-safe list of packet listeners. */

  private static final List<Consumer<SmartFarmingProtocol>> listeners =
        new CopyOnWriteArrayList<>();

  /**
   * Registers a listener to receive all future packets posted to the event bus.
   *
   * @param listener a {@link Consumer} that will be notified with every received packet
   */

  public static void subscribe(Consumer<SmartFarmingProtocol> listener) {
    listeners.add(listener);
  }

  /**
   * Removes a listener so it will no longer receive posted packets.
   *
   * @param listener the previously registered listener to remove
   */

  public static void unsubscribe(Consumer<SmartFarmingProtocol> listener) {
    listeners.remove(listener);
  }

  /**
   * Posts an incoming {@link SmartFarmingProtocol} packet to all subscribed listeners.
   *
   * @param packet the packet to broadcast to subscribers
   */

  public static void post(SmartFarmingProtocol packet) {
    for (var listener : listeners) {
      listener.accept(packet);
    }
  }
}
