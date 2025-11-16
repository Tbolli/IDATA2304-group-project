package ntnu.idata2302.sfp.library.body.capabilities;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body representing a capabilities query request.
 *
 * <p>Holds the original request identifier for a capabilities' query.
 * Instances can be serialized to and from CBOR using {@link CborCodec}.</p>
 *
 * @param requestId the identifier of the capabilities' request
 */

public record CapabilitiesQueryBody(
    int requestId
) implements Body {

  /**
   * Serialize this {@code CapabilitiesQueryBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }


  /**
   * Decode a {@code CapabilitiesQueryBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code CapabilitiesQueryBody} instance
   * @throws RuntimeException if decoding fails
   */

  public static CapabilitiesQueryBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CapabilitiesQueryBody.class);
  }
}
