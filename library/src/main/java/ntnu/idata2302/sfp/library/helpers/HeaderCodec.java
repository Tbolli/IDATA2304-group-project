package ntnu.idata2302.sfp.library.helpers;

import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public class HeaderCodec {

  public static byte[] encodeHeader(Header h) {
    ByteBuffer buffer = ByteBuffer.allocate(Header.HEADER_SIZE);
    buffer.order(ByteOrder.BIG_ENDIAN);

    buffer.put(h.getProtocolName());          // 3 bytes
    buffer.put(h.getVersion());               // 1 byte
    buffer.put(h.getMessageType().getCode()); // 1 byte
    buffer.putInt(h.getSourceId());           // 4 bytes
    buffer.putInt(h.getTargetId());           // 4 bytes
    buffer.putInt(h.getPayloadLength());      // 4 bytes

    // Convert UUID → 16 bytes
    buffer.putLong(h.getMessageId().getMostSignificantBits());
    buffer.putLong(h.getMessageId().getLeastSignificantBits());

    return buffer.array();
  }

  public static Header decodeHeader(byte[] bytes) {
    if (bytes.length < Header.HEADER_SIZE)
      throw new IllegalArgumentException("Byte array is too small");

    ByteBuffer buffer = ByteBuffer.wrap(bytes);
    buffer.order(ByteOrder.BIG_ENDIAN);

    byte[] protocol = new byte[3];
    buffer.get(protocol);

    byte version = buffer.get();
    byte messageType = buffer.get();
    int sourceId = buffer.getInt();
    int targetId = buffer.getInt();

    int payloadLength = buffer.getInt();

    long msb = buffer.getLong();
    long lsb = buffer.getLong();
    UUID messageId = new UUID(msb, lsb);

    return new Header(protocol, version,
      MessageTypes.fromCode(messageType), sourceId, targetId,
      payloadLength, messageId
    );
  }


}