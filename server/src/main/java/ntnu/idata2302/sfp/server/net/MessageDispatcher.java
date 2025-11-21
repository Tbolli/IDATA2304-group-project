package ntnu.idata2302.sfp.server.net;

import java.net.Socket;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.net.handlers.MessageHandler;


/**
 * Routes incoming {@link SmartFarmingProtocol} messages to registered
 * {@link MessageHandler} instances based on their {@link MessageTypes}.
 *
 * <p>This class is thread\-safe: handlers are stored in a {@link ConcurrentHashMap}
 * and may be registered or removed concurrently while dispatching.</p>
 */

public final class MessageDispatcher {
  private static final Logger LOG = Logger.getLogger(MessageDispatcher.class.getName());
  private final Map<MessageTypes, MessageHandler> handlers = new ConcurrentHashMap<>();

  /**
   * Register a handler for a specific message type.
   *
   * @param type    the message type the handler should handle; must not be {@code null}
   * @param handler the handler instance to invoke
   *                for messages of {@code type}; must not be {@code null}
   * @return the previous handler associated with {@code type}, or {@code null} if none
   * @throws NullPointerException if {@code type} or {@code handler} is {@code null}
   */

  public MessageHandler registerHandler(MessageTypes type, MessageHandler handler) {
    Objects.requireNonNull(type, "type");
    Objects.requireNonNull(handler, "handler");
    return handlers.put(type, handler);
  }

  /**
   * Unregister the handler for a given message type.
   *
   * @param type the message type whose handler should be removed; must not be {@code null}
   * @return the removed handler, or {@code null} if none was registered
   * @throws NullPointerException if {@code type} is {@code null}
   */

  public MessageHandler unregisterHandler(MessageTypes type) {
    Objects.requireNonNull(type, "type");
    return handlers.remove(type);
  }

  /**
   * Dispatch a received packet to the handler registered for its message type.
   *
   * <p>If no handler is registered for the packet type the method logs and
   * returns. Any exception thrown by a handler is caught and logged so the
   * dispatching thread can continue processing other messages.</p>
   *
   * @param packet  the received {@link SmartFarmingProtocol}
   *                packet to dispatch; must not be {@code null}
   * @param client  the client {@link Socket} that sent the packet; may be {@code null}
   * @param context the {@link ServerContext} providing server state
   *                and utilities for handlers; must not be {@code null}
   * @throws NullPointerException if {@code packet} or {@code context} is {@code null}
   */

  public void dispatch(SmartFarmingProtocol packet, Socket client, ServerContext context) {
    Objects.requireNonNull(packet, "packet");
    Objects.requireNonNull(context, "context");

    MessageTypes type = packet.getHeader().getMessageType();
    MessageHandler handler = handlers.get(type);

    if (handler == null) {
      LOG.log(Level.FINE, "No handler registered for message type: {0}", type);
      return;
    }

    try {
      handler.handle(packet, client, context);
    } catch (Exception e) {
      LOG.log(Level.SEVERE, "Error handling message type " + type, e);
    }
  }
}