package ntnu.idata2302.sfp.server.net;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.handlers.MessageHandler;

import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageDispatcher {
  private final Map<MessageTypes, MessageHandler> handlers = new ConcurrentHashMap<>();

  public void registerHandler(MessageTypes type, MessageHandler handler) {
    handlers.put(type, handler);
  }

  public void dispatch(SmartFarmingProtocol packet, Socket client, ServerContext context) {
    MessageTypes type = packet.getHeader().getMessageType();
    MessageHandler handler = handlers.get(type);

    if (handler != null) {
      try {
        handler.handle(packet, client, context);
      } catch (Exception e) {
        System.err.println("Error handling message type " + type + ": " + e.getMessage());
        e.printStackTrace();
      }
    } else {
      System.out.println("No handler registered for message type: " + type);
    }
  }
}
