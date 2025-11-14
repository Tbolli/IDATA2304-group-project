package ntnu.idata2302.sfp.library.header;

import ntnu.idata2302.sfp.library.codec.HeaderCodec;

import java.util.UUID;

public class Header {

  public static final int HEADER_SIZE = 33;

  private final byte[] protocolName;  // 3 bytes (fixed)
  private final byte version;         // 1 byte
  private final MessageTypes messageType;     // 1 byte
  private final int sourceId;         // 4 bytes
  private final int targetId;         // 4 bytes
  private int payloadLength;    // 4 bytes
  private final UUID messageId;       // 16 bytes

  public Header(byte[] protocolName,
                byte version,
                MessageTypes messageType,
                int sourceId,
                int targetId,
                int payloadLength,
                UUID messageId) {

    if (protocolName.length != 3)
      throw new IllegalArgumentException("Protocol name must be 3 bytes");
    if (protocolName[0] != 0x53 || protocolName[1] != 0x46 || protocolName[2] != 0x50) {
      throw new IllegalArgumentException("Invalid protocol prefix");
    }

    this.protocolName = protocolName;
    this.version = version;
    this.messageType = messageType;
    this.sourceId = sourceId;
    this.targetId = targetId;
    this.payloadLength = payloadLength;
    this.messageId = messageId;
  }

  public static boolean validateHeader(Header header) {
    byte[] protocol = header.getProtocolName();
    return protocol[0] == 0x53 && protocol[1] == 0x46 && protocol[2] == 0x50;
  }

  public byte[] toBytes(){
    return HeaderCodec.encodeHeader(this);
  }

  public static Header fromBytes(byte[] bytes){
    return HeaderCodec.decodeHeader(bytes);
  }

  public byte[] getProtocolName() {
    return protocolName;
  }

  public byte getVersion() {
    return version;
  }

  public MessageTypes getMessageType() {
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

  public void setPayloadLength(int payloadLength) {
    this.payloadLength = payloadLength;
  }

}