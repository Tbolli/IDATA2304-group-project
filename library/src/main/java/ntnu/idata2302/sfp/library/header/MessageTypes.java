package ntnu.idata2302.sfp.library.header;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of protocol message types and their on-the-wire byte codes.
 *
 * <p>Each enum constant represents a distinct message kind used by the
 * protocol and carries the single-byte code sent in the wire-format
 * header. The enum provides helpers to get the code for a constant and to
 * perform a reverse lookup from a byte code back to the corresponding enum
 * value.</p>
 */
public enum MessageTypes {
  DATA_REPORT(0x01),
  DATA_REQUEST(0x02),

  COMMAND(0x12),
  COMMAND_ACK(0x13),

  SUBSCRIBE(0x0B),
  UNSUBSCRIBE(0x0C),
  SUBSCRIBE_ACK(0x0D),
  UNSUBSCRIBE_ACK(0x0E),

  CAPABILITIES_QUERY(0x21),
  CAPABILITIES_LIST(0x22),

  ANNOUNCE(0x1E),
  ANNOUNCE_ACK(0x1D),

  IMAGE_METADATA(0x07),
  IMAGE_CHUNK(0x08),
  IMAGE_TRANSFER_ACK(0x09),

  ERROR((byte) 0xFE);

  private final byte code;

  /**
   * Construct a message type with the given integer code.
   *
   * <p>The provided integer is cast to a byte and stored. The constructor is
   * invoked for each enum constant during class initialization.</p>
   *
   * @param code numeric code (will be stored as a single byte)
   */
  MessageTypes(int code) {
    this.code = (byte) code;  // store as byte
  }

  /**
   * Return the single-byte code associated with this message type.
   *
   * @return the wire-format byte code for this message type
   */
  public byte getCode() {
    return code;
  }

  /**
   * Map for reverse lookup from byte code to enum constant.
   *
   * <p>Populated in the static initializer so that {@link #fromCode(byte)}
   * can perform an efficient lookup.</p>
   */
  private static final Map<Byte, MessageTypes> lookup = new HashMap<>();

  static {
    for (MessageTypes t : MessageTypes.values()) {
      lookup.put(t.code, t);
    }
  }

  /**
   * Resolve a {@link MessageTypes} from its byte code.
   *
   * @param code the wire-format byte code
   * @return the corresponding {@code MessageTypes} constant
   * @throws IllegalArgumentException if no matching message type exists for {@code code}
   */
  public static MessageTypes fromCode(byte code) {
    MessageTypes type = lookup.get(code);
    if (type == null) {
      throw new IllegalArgumentException(String.format("Unknown message type: 0x%02X", code));
    }
    return type;
  }
}