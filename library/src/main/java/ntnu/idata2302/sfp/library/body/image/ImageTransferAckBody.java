package ntnu.idata2302.sfp.library.body.image;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

public record ImageTransferAckBody(
  String imageId,
  int status
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static ImageTransferAckBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, ImageTransferAckBody.class);
  }
}
