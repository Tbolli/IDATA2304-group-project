package ntnu.idata2302.sfp.library.header;

import java.util.UUID;
import ntnu.idata2302.sfp.library.codec.HeaderCodec;

/**
 * Represents the fixed-format binary message header used by the protocol.
 *
 * <p>The header is a fixed-size structure of {@link #HEADER_SIZE} bytes and
 * contains the following fields (in order, big-endian where applicable):
 * protocol name (3 bytes), version (1 byte), message type (1 byte),
 * source id (4 bytes), target id (4 bytes), payload length (4 bytes),
 * and message UUID (16 bytes).</p>
 *
 * <p>This class provides helpers to validate the protocol prefix and to
 * convert between the in-memory representation and the wire format using
 * {@link HeaderCodec}.</p>
 */
public class Header {

  public static final int HEADER_SIZE = 33;

  private final byte[] protocolName;  // 3 bytes (fixed)
  private final byte version;         // 1 byte
  private final MessageTypes messageType;     // 1 byte
  private final int sourceId;         // 4 bytes
  private final int targetId;         // 4 bytes
  private int payloadLength;    // 4 bytes
  private final UUID messageId;       // 16 bytes

  /**
   * Create a new {@code Header} instance.
   *
   * @param protocolName  a 3-byte array identifying the protocol; must equal \{0x53, 0x46, 0x50\}
   * @param version       protocol version byte
   * @param messageType   message type enum value
   * @param sourceId      numeric source identifier
   * @param targetId      numeric target identifier
   * @param payloadLength length of the following payload in bytes
   * @param messageId     UUID uniquely identifying the message
   * @throws IllegalArgumentException if {@code protocolName} is not 3 bytes
   *                                  or does not match the expected
   *                                  protocol prefix
   */
  public Header(byte[] protocolName,
                byte version,
                MessageTypes messageType,
                int sourceId,
                int targetId,
                int payloadLength,
                UUID messageId) {

    if (protocolName.length != 3) {
      throw new IllegalArgumentException("Protocol name must be 3 bytes");
    }
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

  /**
   * Validate that the given {@link Header} uses the expected protocol prefix.
   *
   * @param header the header to validate; must not be {@code null}
   * @return {@code true} if the header's protocol bytes equal
   *      \{0x53, 0x46, 0x50\}, otherwise {@code false}
   */
  public static boolean validateHeader(Header header) {
    byte[] protocol = header.getProtocolName();
    return protocol[0] == 0x53 && protocol[1] == 0x46 && protocol[2] == 0x50;
  }

  /**
   * Serialize this header to its wire-format byte array.
   *
   * <p>The returned array has length {@link #HEADER_SIZE} and is encoded using
   * {@link HeaderCodec#encodeHeader(Header)}.</p>
   *
   * @return a byte array suitable for transmission
   */
  public byte[] toBytes() {
    return HeaderCodec.encodeHeader(this);
  }

  /**
   * Parse a {@link Header} from the provided wire-format bytes.
   *
   * <p>The input must contain at least {@link #HEADER_SIZE} bytes; extra bytes
   * are ignored. Decoding is delegated to {@link HeaderCodec#decodeHeader(byte[])}.</p>
   *
   * @param bytes the byte array containing the encoded header
   * @return the decoded {@link Header} instance
   * @throws IllegalArgumentException if {@code bytes.length < HEADER_SIZE} (delegated from codec)
   */
  public static Header fromBytes(byte[] bytes) {
    return HeaderCodec.decodeHeader(bytes);
  }

  /**
   * Return the 3-byte protocol name.
   *
   * @return a 3-byte array containing the protocol identifier
   */
  public byte[] getProtocolName() {
    return protocolName;
  }

  /**
   * Return the protocol version byte.
   *
   * @return version
   */
  public byte getVersion() {
    return version;
  }

  /**
   * Return the message type.
   *
   * @return message type enum
   */
  public MessageTypes getMessageType() {
    return messageType;
  }

  /**
   * Return the numeric source identifier.
   *
   * @return source id
   */
  public int getSourceId() {
    return sourceId;
  }

  /**
   * Return the numeric target identifier.
   *
   * @return target id
   */
  public int getTargetId() {
    return targetId;
  }

  /**
   * Return the payload length in bytes.
   *
   * @return payload length
   */
  public int getPayloadLength() {
    return payloadLength;
  }

  /**
   * Return the message UUID.
   *
   * @return message id
   */
  public UUID getMessageId() {
    return messageId;
  }

  /**
   * Update the stored payload length. This modifies the in-memory header only;
   * call {@link #toBytes()} to get an updated wire-format representation.
   *
   * @param payloadLength new payload length in bytes
   */
  public void setPayloadLength(int payloadLength) {
    this.payloadLength = payloadLength;
  }

  /**
   * Placeholder setter for target id. Currently implemented as a no-op.
   *
   * <p>If mutability for {@code targetId} is required in the future, implement
   * the setter to update the field and document thread-safety concerns.</p>
   *
   * @param targetId new target id (ignored)
   */
  public void setTargetId(int targetId) {
  }
}