package ntnu.idata2302.sfp.library;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.HeaderCodec;
import ntnu.idata2302.sfp.library.codec.ProtocolBodyDecoder;
import ntnu.idata2302.sfp.library.header.Header;


/**
 * Represents a complete Smart Farming Protocol packet consisting of a
 * {@link Header} and an optional CBOR-encoded {@link Body}.
 *
 * <p>This class provides helpers to serialize the in-memory representation
 * to the wire format and to deserialize wire-format bytes back into a
 * {@code SmartFarmingProtocol} instance.</p>
 */
public class SmartFarmingProtocol {

  private final Header header;
  private final Body body;

  /**
   * Construct a protocol packet from the supplied header and body.
   *
   * @param header the message header; must not be {@code null}
   * @param body   the message body, may be {@code null} for message types without a body
   */
  public SmartFarmingProtocol(Header header, Body body) {
    this.header = header;
    this.body = body;
  }

  /**
   * Return the packet header.
   *
   * @return the {@link Header} instance carried by this packet
   */
  public Header getHeader() {
    return header;
  }

  /**
   * Return the packet body.
   *
   * @return the {@link Body} instance or {@code null} if nobody is present
   */
  public Body getBody() {
    return body;
  }

  // ============================================================
  //                       SERIALIZE
  // ============================================================

  /**
   * Serialize this packet to the wire-format byte array.
   *
   * <p>Steps performed:
   * 1. Encode the body to CBOR (empty array if {@code body} is {@code null}).
   * 2. Encode the header using {@link HeaderCodec#encodeHeader(Header)}.
   * 3. Concatenate header and body bytes into a single packet array.</p>
   *
   * @return a byte array ready for transmission containing header followed by body
   */
  public byte[] toBytes() {
    // 1. Encode body CBOR
    byte[] bodyBytes = body != null ? body.toCbor() : new byte[0];

    // 2. Update header with correct payloadLength
    header.setPayloadLength(bodyBytes.length);

    // 3. Encode header
    byte[] headerBytes = HeaderCodec.encodeHeader(header);

    // 4. Join header and body
    byte[] packet = new byte[headerBytes.length + bodyBytes.length];
    System.arraycopy(headerBytes, 0, packet, 0, headerBytes.length);
    System.arraycopy(bodyBytes, 0, packet, headerBytes.length, bodyBytes.length);

    return packet;
  }

  // ============================================================
  //                       DESERIALIZE
  // ============================================================

  /**
   * Deserialize a full packet from the provided wire-format bytes.
   *
   * <p>The method decoding the header (using {@link HeaderCodec#decodeHeader(byte[])}),
   * validates that the packet contains the expected number of bytes, extracts the body
   * bytes and decodes them using {@link ProtocolBodyDecoder#decode} based on the header's
   * {@link ntnu.idata2302.sfp.library.header.MessageTypes}.</p>
   *
   * @param packet the full packet bytes containing header followed by body
   * @return a {@link SmartFarmingProtocol} instance representing the decoded packet
   * @throws RuntimeException if the packet is shorter
   *                          than the header plus the declared payload length
   */
  public static SmartFarmingProtocol fromBytes(byte[] packet) {
    // 1. Decode header
    Header header = HeaderCodec.decodeHeader(packet);

    int headerSize = Header.HEADER_SIZE;
    int bodyLength = header.getPayloadLength();

    if (packet.length < headerSize + bodyLength) {
      throw new RuntimeException("Incomplete packet: expected "
          + (headerSize + bodyLength) + " bytes but got " + packet.length);
    }

    // 2. Extract body bytes
    byte[] bodyBytes = new byte[bodyLength];
    System.arraycopy(packet, headerSize, bodyBytes, 0, bodyLength);

    // 3. Decode body based on messageType
    Body body = ProtocolBodyDecoder.decode(header.getMessageType(), bodyBytes);

    return new SmartFarmingProtocol(header, body);
  }

  /**
   * Create a {@link SmartFarmingProtocol} from a pre-decoded header and raw body bytes.
   *
   * <p>This variant is useful when the header has already been read separately
   * (for example, when streaming). The method checks that {@code bodyBytes}
   * contains at least the number of bytes declared in {@code header.getPayloadLength()},
   * then decodes the body using {@link ProtocolBodyDecoder#decode}.</p>
   *
   * @param header    the already-decoded {@link Header}
   * @param bodyBytes the byte array containing the body
   *                  data (may be larger than the declared length)
   * @return a {@link SmartFarmingProtocol} instance
   * @throws RuntimeException if {@code bodyBytes} is shorter than {@code header.getPayloadLength()}
   */
  public static SmartFarmingProtocol fromBytes(Header header, byte[] bodyBytes) {
    int bodyLength = header.getPayloadLength();

    if (bodyBytes.length < bodyLength) {
      throw new RuntimeException("Incomplete packet: expected "
          + (bodyLength) + " bytes but got " + bodyBytes.length);
    }

    // Decode body based on messageType
    Body body = ProtocolBodyDecoder.decode(header.getMessageType(), bodyBytes);

    return new SmartFarmingProtocol(header, body);
  }
}