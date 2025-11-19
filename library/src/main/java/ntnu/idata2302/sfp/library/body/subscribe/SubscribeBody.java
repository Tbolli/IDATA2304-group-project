package ntnu.idata2302.sfp.library.body.subscribe;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body representing a subscription request.
 *
 * <p>Holds a client-provided request identifier and a per-node subscription.
 * Instances can be serialized to and
 * deserialized from CBOR using {@link CborCodec}.</p>
 *
 * @param requestId client-provided identifier for this request
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record SubscribeBody(
    int requestId,
    int sensorNodeId
) implements Body {

  /**
   * Serialize this {@code SubscribeBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode a {@code SubscribeBody} from CBOR bytes.
   *
   * @param bytes CBOR-encoded input bytes
   * @return the decoded {@code SubscribeBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static SubscribeBody fromCbor(byte[] bytes) {
    return CborCodec.decode(bytes, SubscribeBody.class);
  }
}