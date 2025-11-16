package ntnu.idata2302.sfp.library.body.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable acknowledgement body for command requests.
 *
 * <p>Contains the original request identifier, a status code, and an optional
 * message. Instances are serialized to and from CBOR using {@link CborCodec}.</p>
 *
 * @param requestId the identifier of the original command request
 * @param status    status code indicating success or error
 * @param message   optional human-readable status message (may be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommandAckBody(
    int requestId,
    int status,
    String message
) implements Body {

  /**
   * Serialize this {@code CommandAckBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode a {@code CommandAckBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code CommandAckBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static CommandAckBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CommandAckBody.class);
  }
}