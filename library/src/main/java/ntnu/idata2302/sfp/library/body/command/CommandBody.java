package ntnu.idata2302.sfp.library.body.command;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

public record CommandBody(
  int requestId,
  String actuator,
  double newValue
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static CommandBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CommandBody.class);
  }
}
