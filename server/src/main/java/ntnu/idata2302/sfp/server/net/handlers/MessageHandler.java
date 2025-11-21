package ntnu.idata2302.sfp.server.net.handlers;

import java.net.Socket;
import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.server.net.ServerContext;

/**
 * Represents a generic handler for processing incoming SmartFarmingProtocol packets.
 *
 * <p>Each concrete implementation defines logic for a specific message type,
 * such as ANNOUNCE, DATA_REPORT, CAPABILITIES_QUERY, or forwarding packets.
 * Handlers are invoked by the server's MessageDispatcher when a packet arrives.
 */

public interface MessageHandler {

  /**
   * Processes an incoming protocol message from a connected node.
   *
   * <p>Implementations may parse the message, update server state, respond to the
   * client, forward packets, or trigger other actions depending on the protocol type.
   *
   * @param message the parsed SmartFarmingProtocol packet received from the client
   * @param client  the TCP socket representing the remote node connection
   * @param context the shared server context used for registration, routing, and state management
   * @throws Exception if any error occurs during message handling or response sending
   */

  void handle(SmartFarmingProtocol message, Socket client, ServerContext context) throws Exception;
}