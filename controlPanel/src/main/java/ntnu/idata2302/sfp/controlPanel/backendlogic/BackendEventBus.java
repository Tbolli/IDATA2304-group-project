// java

package ntnu.idata2302.sfp.controlPanel.backendlogic;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple event bus for passing textual messages from backend sources (sensors and server)
 * to interested listeners within the application.
 *
 * <p>Design notes:
 * - Two separate channels are provided: sensor messages and server messages.
 * - Listeners are stored in thread-safe {@link CopyOnWriteArrayList} instances so that
 * registration/unregistration can safely occur while messages are being published.
 * - Publication invokes registered listeners synchronously in the caller's thread.
 * If asynchronous delivery is required, callers should dispatch to another thread or executor.
 */
public final class BackendEventBus {

  private static final List<Consumer<String>> sensorListeners = new CopyOnWriteArrayList<>();
  private static final List<Consumer<String>> serverListeners = new CopyOnWriteArrayList<>();


  /**
   * Register a listener to receive sensor messages.
   *
   * <p>The listener will be invoked each time {@link #publishSensorMessage(String)} is called.
   * Registration is thread-safe. Duplicate registrations are allowed (the listener will be
   * invoked once per registration).
   *
   * @param listener consumer that handles published sensor messages; must not be {@code null}
   * @throws NullPointerException if {@code listener} is {@code null}
   */
  public static void onSensorMessage(Consumer<String> listener) {
    Objects.requireNonNull(listener, "listener");
    sensorListeners.add(listener);
  }

//      /**
//       * Register a listener to receive server messages.
//       *
//       * <p>The listener will be invoked each time
//       {@link #publishServerMessage(String)} is called.
//       * Registration is thread-safe. Duplicate registrations are allowed (the listener will be
//       * invoked once per registration).
//       *
//       * @param listener consumer that handles published server messages; must not be {@code null}
//       * @throws NullPointerException if {@code listener} is {@code null}
//       */
//      public static void onServerMessage(Consumer<String> listener) {
//        Objects.requireNonNull(listener, "listener");
//        serverListeners.add(listener);
//      }

  /**
   * Publish a sensor message to all registered sensor listeners.
   *
   * <p>Listeners are invoked synchronously in the calling thread. The underlying listener
   * collection is a {@link CopyOnWriteArrayList}, so iteration is safe even if other threads
   * concurrently register or remove listeners. However, listeners themselves should be
   * fast and non-blocking to avoid delaying the publisher.
   *
   * @param message the message to deliver; may be {@code null} if listeners expect it
   */
  public static void publishSensorMessage(String message) {
    for (Consumer<String> l : sensorListeners) {
      l.accept(message);
    }
  }

  /**
   * Publish a server message to all registered server listeners.
   *
   * <p>Listeners are invoked synchronously in the calling thread. The underlying listener
   * collection is a {@link CopyOnWriteArrayList}, so iteration is safe even if other threads
   * concurrently register or remove listeners. However, listeners themselves should be
   * fast and non-blocking to avoid delaying the publisher.
   *
   * @param message the message to deliver; may be {@code null} if listeners expect it
   */
  public static void publishServerMessage(String message) {
    for (Consumer<String> l : serverListeners) {
      l.accept(message);
    }
  }
}