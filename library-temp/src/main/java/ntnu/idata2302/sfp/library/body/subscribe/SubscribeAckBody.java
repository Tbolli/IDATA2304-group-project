package ntnu.idata2302.sfp.library.body.subscribe;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.helpers.CborCodec;

public record SubscribeAckBody(
  String subscriptionId,
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