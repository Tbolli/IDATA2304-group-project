package ntnu.idata2302.sfp.controlPanel.net;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;

public class EventBus {

  private static final List<Consumer<SmartFarmingProtocol>> listeners =
    new CopyOnWriteArrayList<>();

  public static void subscribe(Consumer<SmartFarmingProtocol> listener) {
    listeners.add(listener);
  }

  public static void unsubscribe(Consumer<SmartFarmingProtocol> listener) {
    listeners.remove(listener);
  }

  public static void post(SmartFarmingProtocol packet) {
    for (var listener : listeners) {
      listener.accept(packet);
    }
  }
}
