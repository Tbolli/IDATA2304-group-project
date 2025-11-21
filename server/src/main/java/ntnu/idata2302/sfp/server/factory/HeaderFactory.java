package ntnu.idata2302.sfp.server.factory;

import java.util.UUID;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeIds;


/**
 * Utility class for constructing standardized server-side protocol headers.
 *
 * <p>This avoids repeating boilerplate when the server constructs replies
 * to clients. All headers built here automatically:
 * <ul>
 *     <li>Use the protocol prefix "SFP"</li>
 *     <li>Assign the server's logical node ID as the SourceId</li>
 *     <li>Generate a unique UUID messageId</li>
 *     <li>Start with payloadLength = 0 (updated later when encoding)</li>
 * </ul>
 * </p>
 */
public class HeaderFactory {

  /**
   * Creates a standard server → client response header using protocol version 1.
   *
   * @param type     The protocol message type (DATA_REPORT, CAPABILITIES_ANNOUNCE_ACK, etc.)
   * @param targetId The logical ID of the recipient node
   * @return A fully populated {@link Header} with server defaults applied
   */
  public static Header serverHeader(MessageTypes type, int targetId) {
    return new Header(
           new byte[]{ 'S', 'F', 'P' },   // Protocol signature
           (byte) 1,                      // Default server protocol version
             type,                          // Message type being sent
             NodeIds.SERVER,                // Source = server
              targetId,                      // Target = client node
               0,                             // Payload length (assigned during encoding)
               UUID.randomUUID()              // Unique message identifier
    );
  }

  /**
   * Creates a server → client response header with a custom protocol version.
   *
   * <p>This is useful when supporting multiple protocol versions,
   * or when negotiating capabilities with older clients.</p>
   *
   * @param version  Protocol version number to embed in the header
   * @param type     The message type being sent
   * @param targetId The logical ID of the recipient node
   * @return A configured {@link Header} instance
   */
  public static Header serverHeader(int version, MessageTypes type, int targetId) {
    return new Header(
            new byte[]{ 'S', 'F', 'P' },   // Protocol signature
           (byte) version,                // Custom protocol version
            type,                          // Message type
            NodeIds.SERVER,                // Source = server
             targetId,                      // Destination
               0,                             // Updated when packet is encoded
                  UUID.randomUUID()              // Unique message identifier
    );
  }
}
