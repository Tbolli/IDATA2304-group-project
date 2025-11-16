package ntnu.idata2302.sfp.library.body.announce;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body used to acknowledge an announcement request.
 *
 * <p>Holds the original request identifier and a numeric status code that
 * indicates the result of processing the announcement. Instances are serialized
 * to and from CBOR using {@link CborCodec}.</p>
 *
 * @param requestId the identifier of the request being acknowledged
 * @param status    a numeric status code (implementation-specific)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnnounceAckBody(
    int requestId,
    int status
) implements Body {

  /**
   * Serialize this {@code AnnounceAckBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode an {@code AnnounceAckBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code AnnounceAckBody} instance
   * @throws RuntimeException if decoding fails (decoder may throw more specific exceptions)
   */
  public static AnnounceAckBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, AnnounceAckBody.class);
  }
}