package ntnu.idata2302.sfp.library.body.image;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body acknowledging an image transfer.
 *
 * <p>Holds the identifier of the transferred image and a numeric status code
 * describing the result of the transfer (for example, 0 = success, non-zero = error).</p>
 *
 * @param imageId identifier of the image this acknowledgement refers to
 * @param status  numeric status code for the transfer result
 */
public record ImageTransferAckBody(
    String imageId,
    int status
) implements Body {

  /**
   * Serialize this {@code ImageTransferAckBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode an {@code ImageTransferAckBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code ImageTransferAckBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static ImageTransferAckBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, ImageTransferAckBody.class);
  }
}