// java

package ntnu.idata2302.sfp.library.body.image;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body representing metadata for an image.
 *
 * <p>Contains identifying information and chunking details used when transferring
 * images in chunks. Instances are serialized to and deserialized from CBOR using
 * {@link CborCodec}.</p>
 *
 * @param imageId     unique identifier of the image
 * @param timestamp   ISO-8601 timestamp when the image was created or
 *                    captured (may be {@code null})
 * @param contentType MIME content type of the image (for example {@code "image/jpeg"})
 * @param totalSize   total size in bytes of the full image
 * @param chunkCount  number of chunks the image is split into
 * @param chunkSize   nominal size in bytes of each chunk (the last chunk may be smaller)
 * @param checksum    SHA-256 checksum of the image expressed as a hex string (may be {@code null})
 */
public record ImageMetadataBody(
    String imageId,
    String timestamp,
    String contentType,
    int totalSize,
    int chunkCount,
    int chunkSize,
    String checksum     // SHA-256 as hex
) implements Body {

  /**
   * Serialize this {@code ImageMetadataBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode an {@code ImageMetadataBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code ImageMetadataBody} instance
   * @throws RuntimeException if decoding fails
   */
  public static ImageMetadataBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, ImageMetadataBody.class);
  }
}