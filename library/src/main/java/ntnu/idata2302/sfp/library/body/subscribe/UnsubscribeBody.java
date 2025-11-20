package ntnu.idata2302.sfp.library.body.subscribe;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body representing an unsubscribe request.
 *
 * <p>Contains the client-provided request identifier, controlPanelId, and sensorNodeId.
 * Instances can be serialized to
 * and deserialized from CBOR using {@link CborCodec}.</p>
 *
 * @param requestId      client-provided identifier for this request
 * @param sensorNodeId   identifier of the sensorNode
 */
public record UnsubscribeBody(
    int requestId,
    int sensorNodeId
) implements Body {

  /**
   * Serialize this {@code UnsubscribeBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode an {@code UnsubscribeBody} from CBOR bytes.
   *
   * @param bytes CBOR-encoded input bytes
   * @return the decoded {@code UnsubscribeBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static UnsubscribeBody fromCbor(byte[] bytes) {
    return CborCodec.decode(bytes, UnsubscribeBody.class);
  }
}