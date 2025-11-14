package ntnu.idata2302.sfp.library;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.helpers.HeaderCodec;
import ntnu.idata2302.sfp.library.helpers.ProtocolBodyDecoder;


public class SmartFarmingProtocol {

  private final Header header;
  private final Body body;

  public SmartFarmingProtocol(Header header, Body body) {
    this.header = header;
    this.body = body;
  }

  public Header getHeader() {
    return header;
  }

  public Body getBody() {
    return body;
  }

  // ============================================================
  //                       SERIALIZE
  // ============================================================

  public byte[] toBytes() {
    // 1. Encode body CBOR
    byte[] bodyBytes = body != null ? body.toCbor() : new byte[0];

    // 2. Update header with correct payloadLength
    header.setPayloadLength(bodyBytes.length);

    // 3. Encode header
    byte[] headerBytes = HeaderCodec.encodeHeader(header);

    // 4. Join header + body
    byte[] packet = new byte[headerBytes.length + bodyBytes.length];
    System.arraycopy(headerBytes, 0, packet, 0, headerBytes.length);
    System.arraycopy(bodyBytes, 0, packet, headerBytes.length, bodyBytes.length);

    return packet;
  }

  // ============================================================
  //                       DESERIALIZE
  // ============================================================

  public static SmartFarmingProtocol fromBytes(byte[] packet) {

    // 1. Decode header
    Header header = HeaderCodec.decodeHeader(packet);

    int headerSize = Header.HEADER_SIZE;
    int bodyLength = header.getPayloadLength();

    if (packet.length < headerSize + bodyLength)
      throw new RuntimeException("Incomplete packet: expected "
        + (headerSize + bodyLength) + " bytes but got " + packet.length);

    // 2. Extract body bytes
    byte[] bodyBytes = new byte[bodyLength];
    System.arraycopy(packet, headerSize, bodyBytes, 0, bodyLength);

    // 3. Decode body based on messageType
    Body body = ProtocolBodyDecoder.decode(header.getMessageType(), bodyBytes);

    return new SmartFarmingProtocol(header, body);
  }
}