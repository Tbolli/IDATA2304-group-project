package ntnu.idata2302.sfp.library.body.image;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

public record ImageMetadataBody(
  String imageId,
  String timestamp,
  String contentType,
  int totalSize,
  int chunkCount,
  int chunkSize,
  String checksum     // SHA-256 as hex
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static ImageMetadataBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, ImageMetadataBody.class);
  }
}
