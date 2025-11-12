package ntnu.idata2302.sfp.library.header;

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
  CAPABILITIES_ANNOUNCE(0x22),

  IDENTIFICATION_ACK(0x1E),

  IMAGE_METADATA(0x08),
  IMAGE_CHUNK(0x09),
  IMAGE_TRANSFER_ACK(0x0A),

  ERROR((byte) 0xFE);

  private final byte code;

  MessageTypes(int code) {
    this.code = (byte) code;  // store as byte
  }

  public byte getCode() {
    return code;
  }

  // Reverse lookup map
  private static final java.util.Map<Byte, MessageTypes> lookup = new java.util.HashMap<>();

  static {
    for (MessageTypes t : MessageTypes.values()) {
      lookup.put(t.code, t);
    }
  }

  public static MessageTypes fromCode(byte code) {
    MessageTypes type = lookup.get(code);
    if (type == null)
      throw new IllegalArgumentException(String.format("Unknown message type: 0x%02X", code));
    return type;
  }
}

