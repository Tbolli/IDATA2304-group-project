package ntnu.idata2302.sfp.library.body.image;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body representing a chunk of an image.
 *
 * <p>Contains the image identifier, the zero-based chunk index, and the raw
 * bytes for this chunk. Instances are serialized to and from CBOR using
 * {@link CborCodec}.</p>
 *
 * @param imageId    the identifier of the image this chunk belongs to
 * @param chunkIndex zero-based index of the chunk within the image
 * @param data       raw bytes of the image chunk
 */
public record ImageChunkBody(
    String imageId,
    int chunkIndex,
    byte[] data
) implements Body {

  /**
   * Serialize this {@code ImageChunkBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode an {@code ImageChunkBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code ImageChunkBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static ImageChunkBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, ImageChunkBody.class);
  }
}