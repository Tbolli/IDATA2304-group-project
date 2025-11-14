package ntnu.idata2302.sfp.library.body.image;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.helpers.CborCodec;

public record ImageChunkBody(
  String imageId,
  int chunkIndex,
  byte[] data
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static ImageChunkBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, ImageChunkBody.class);
  }
}
