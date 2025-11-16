package ntnu.idata2302.sfp.library.body.capabilities;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;

/**
 * Immutable body representing a capability's list response.
 *
 * <p>Contains the original request identifier and a list of {@link NodeDescriptor}
 * entries describing available nodes. Instances are serialized to and from CBOR
 * using {@link CborCodec}.</p>
 *
 * @param requestId the identifier of the original capabilities request
 * @param nodes     list of node descriptors (may be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CapabilitiesListBody(
    int requestId,
    List<NodeDescriptor> nodes
) implements Body {

  /**
   * Serialize this {@code CapabilitiesListBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode a {@code CapabilitiesListBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code CapabilitiesListBody} instance
   * @throws RuntimeException if decoding fails (decoder may throw more specific exceptions)
   */
  public static CapabilitiesListBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CapabilitiesListBody.class);
  }
}