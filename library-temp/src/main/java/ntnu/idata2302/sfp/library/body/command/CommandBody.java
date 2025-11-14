package ntnu.idata2302.sfp.library.body.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.helpers.CborCodec;

public record CommandBody(
  String commandId,
  String actuator,
  int action,
  String timestamp
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static CommandBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CommandBody.class);
  }
}
