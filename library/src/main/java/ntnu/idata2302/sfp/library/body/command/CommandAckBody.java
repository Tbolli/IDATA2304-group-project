package ntnu.idata2302.sfp.library.body.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommandAckBody(
  int requestId,
  int status,
  String message
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static CommandBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CommandBody.class);
  }
}
