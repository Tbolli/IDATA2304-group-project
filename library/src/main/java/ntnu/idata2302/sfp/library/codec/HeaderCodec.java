package ntnu.idata2302.sfp.library.codec;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;



/**
 * Utility for encoding and decoding {@link Header} instances to and from
 * their binary wire format.
 *
 * <p>Encodes a {@code Header} into a fixed-size byte array of length
 * {@link Header#HEADER_SIZE} using big-endian byte order. The message UUID
 * is serialized as two 64-bit longs (most/the least significant bits).</p>
 */
public class HeaderCodec {

  /**
   * Encode the provided {@link Header} to a byte array suitable for transmission.
   *
   * <p>The produced array has length {@link Header#HEADER_SIZE} and contains:
   * protocol name (3 bytes), version (1 byte), message type code (1 byte),
   * source id (4 bytes), target id (4 bytes), payload length (4 bytes),
   * and the 16-byte message UUID (two 8-byte longs).</p>
   *
   * @param h the {@code Header} to encode; must not be {@code null}
   * @return a byte array of length {@link Header#HEADER_SIZE} containing the encoded header
   */
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

  /**
   * Decode a {@link Header} from the given byte array.
   *
   * <p>The input array must be at least {@link Header#HEADER_SIZE} bytes long;
   * only the first {@code HEADER_SIZE} bytes are interpreted. The byte order
   * used when decoding is big-endian.</p>
   *
   * @param bytes the input byte array containing an encoded header
   * @return the decoded {@link Header} instance
   * @throws IllegalArgumentException if {@code bytes.length < Header.HEADER_SIZE}
   */
  public static Header decodeHeader(byte[] bytes) {
    if (bytes.length < Header.HEADER_SIZE) {
      throw new IllegalArgumentException("Byte array is too small");
    }

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