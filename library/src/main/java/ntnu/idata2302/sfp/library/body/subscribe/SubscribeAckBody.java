package ntnu.idata2302.sfp.library.body.subscribe;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable acknowledgement body for a subscription request.
 *
 * <p>Represents the server's acknowledgement of a subscribing request, containing
 * the original request identifier and a
 * numeric status code indicating success (commonly 1) or an error.</p>
 *
 * @param requestId      client-provided request identifier that this ack corresponds to
 * @param status         numeric status code (e.g. 1 = success, 0 = error)
 */
public record SubscribeAckBody(
    int requestId,
    int status
) implements Body {

  /**
   * Serialize this {@code SubscribeAckBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode a {@code SubscribeAckBody} from CBOR bytes.
   *
   * @param bytes CBOR-encoded input bytes
   * @return the decoded {@code SubscribeAckBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static SubscribeAckBody fromCbor(byte[] bytes) {
    return CborCodec.decode(bytes, SubscribeAckBody.class);
  }
}