package ntnu.idata2302.sfp.library.body.subscribe;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

public record UnsubscribeAckBody(
  int requestId,
  int status
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static SubscribeBody fromCbor(byte[] bytes) {
    return CborCodec.decode(bytes, SubscribeBody.class);
  }
}