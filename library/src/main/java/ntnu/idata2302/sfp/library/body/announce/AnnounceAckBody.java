package ntnu.idata2302.sfp.library.body.announce;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;


@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnnounceAckBody(
  int requestId,
  int status
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static AnnounceAckBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, AnnounceAckBody.class);
  }
}
