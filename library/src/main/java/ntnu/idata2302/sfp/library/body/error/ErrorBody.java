package ntnu.idata2302.sfp.library.body.error;


import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorBody(
  int errorCode,
  String errorText
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static ErrorBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, ErrorBody.class);
  }
}
/*
| Code | Meaning                                      |
| ---- | -------------------------------------------- |
| 1    | BAD_REQUEST (malformed CBOR, missing fields) |
| 2    | UNAUTHORIZED                                 |
| 3    | UNSUPPORTED_OPERATION                        |
| 4    | NODE_NOT_FOUND                               |
| 5    | INVALID_ID                                   |
| 6    | CHUNK_MISSING                                |
| 7    | CHECKSUM_MISMATCH                            |
| 8    | CAPABILITIES_MISSING                         |
| 100  | INTERNAL_SERVER_ERROR                        |
| 101  | UNKNOWN                                      |
 */