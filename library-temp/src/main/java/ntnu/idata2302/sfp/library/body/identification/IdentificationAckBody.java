package ntnu.idata2302.sfp.library.body.identification;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.helpers.CborCodec;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IdentificationAckBody(
  int processId,
  int status
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static IdentificationAckBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, IdentificationAckBody.class);
  }
}

