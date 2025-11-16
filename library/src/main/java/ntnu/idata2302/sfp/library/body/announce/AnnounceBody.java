package ntnu.idata2302.sfp.library.body.announce;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;

/**
 * Immutable body representing an announcement request.
 *
 * <p>Contains the original request identifier and a {@link NodeDescriptor}
 * describing the announcing node. Instances are serialized to and from CBOR
 * using {@link CborCodec}.</p>
 *
 * @param requestId  the identifier of the announcement request
 * @param descriptor the descriptor of the announcing node (may be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnnounceBody(
    int requestId,
    NodeDescriptor descriptor
) implements Body {

  /**
   * Serialize this {@code AnnounceBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode an {@code AnnounceBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code AnnounceBody} instance
   * @throws RuntimeException if decoding fails (decoder may throw more specific exceptions)
   */
  public static AnnounceBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, AnnounceBody.class);
  }
}