package ntnu.idata2302.sfp.library.body.subscribe;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable acknowledgement body for an unsubscribed request.
 *
 * <p>Represents the server's acknowledgement of an unsubscribe request,
 * containing the original request identifier and a numeric status code
 * describing the result (for example 0 = success, non-zero = error).</p>
 *
 * @param requestId client-provided request identifier that this ack corresponds to
 * @param status    numeric status code for the unsubscribe result
 */
public record UnsubscribeAckBody(
    int requestId,
    int status
) implements Body {

  /**
   * Serialize this {@code UnsubscribeAckBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode an {@code UnsubscribeAckBody} from CBOR bytes.
   *
   * @param bytes CBOR-encoded input bytes
   * @return the decoded {@code UnsubscribeAckBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static UnsubscribeAckBody fromCbor(byte[] bytes) {
    return CborCodec.decode(bytes, UnsubscribeAckBody.class);
  }
}