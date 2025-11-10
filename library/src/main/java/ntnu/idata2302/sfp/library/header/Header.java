package ntnu.idata2302.sfp.library.header;

import java.util.UUID;

public class Header {

  public static final int HEADER_SIZE = 33;

  private final byte[] protocolName;  // 3 bytes (fixed)
  private final byte version;         // 1 byte
  private final byte messageType;     // 1 byte
  private final int sourceId;         // 4 bytes
  private final int targetId;         // 4 bytes
  private final int payloadLength;    // 4 bytes
  private final UUID messageId;       // 16 bytes

  public Header(byte[] protocolName,
                byte version,
                byte messageType,
                int sourceId,
                int targetId,
                int payloadLength,
                UUID messageId) {

    if (protocolName.length != 3)
      throw new IllegalArgumentException("Protocol name must be 3 bytes");

    this.protocolName = protocolName;
    this.version = version;
    this.messageType = messageType;
    this.sourceId = sourceId;
    this.targetId = targetId;
    this.payloadLength = payloadLength;
    this.messageId = messageId;
  }

  public byte[] getProtocolName() {
    return protocolName;
  }

  public byte getVersion() {
    return version;
  }

  public byte getMessageType() {
    return messageType;
  }

  public int getSourceId() {
    return sourceId;
  }

  public int getTargetId() {
    return targetId;
  }

  public int getPayloadLength() {
    return payloadLength;
  }

  public UUID getMessageId() {
    return messageId;
  }

}