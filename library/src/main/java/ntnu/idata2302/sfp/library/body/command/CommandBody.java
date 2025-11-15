package ntnu.idata2302.sfp.library.body.command;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

import java.util.List;

public record CommandBody(
  int requestId,
  List<CommandPart> actuators
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static CommandBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CommandBody.class);
  }

  public record CommandPart(String name, double newValue) {}
}
