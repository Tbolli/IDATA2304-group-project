package ntnu.idata2302.sfp.library.body.capabilities;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

public record CapabilitiesQueryBody(
  int requestId
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static CapabilitiesQueryBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CapabilitiesQueryBody.class);
  }
}
