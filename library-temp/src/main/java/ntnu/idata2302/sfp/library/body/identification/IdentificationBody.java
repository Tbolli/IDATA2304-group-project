package ntnu.idata2302.sfp.library.body.identification;



import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.helpers.CborCodec;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record IdentificationBody(
  int processId,
  int assignedId,
  int status
) implements Body {

  @Override
  public byte[] toCbor() {
    return ntnu.idata2302.sfp.library.helpers.CborCodec.encode(this);
  }

  public static IdentificationBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, IdentificationBody.class);
  }
}

