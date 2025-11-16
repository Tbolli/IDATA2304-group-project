package ntnu.idata2302.sfp.library.body.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body representing an error response.
 *
 * <p>Holds an integer error code and an optional human-readable error text.
 * Instances can be serialized to and deserialized from CBOR using
 * {@link CborCodec}.</p>
 *
 * <p>Common error codes:
 * <ul>
 *   <li>1 — BAD_REQUEST (malformed CBOR, missing fields)</li>
 *   <li>2 — UNAUTHORIZED</li>
 *   <li>3 — UNSUPPORTED_OPERATION</li>
 *   <li>4 — NODE_NOT_FOUND</li>
 *   <li>5 — INVALID_ID</li>
 *   <li>6 — CHUNK_MISSING</li>
 *   <li>7 — CHECKSUM_MISMATCH</li>
 *   <li>8 — CAPABILITIES_MISSING</li>
 *   <li>100 — INTERNAL_SERVER_ERROR</li>
 *   <li>101 — UNKNOWN</li>
 * </ul>
 * </p>
 *
 * @param errorCode numeric error identifier
 * @param errorText optional human-readable description of the error (may be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorBody(
    int errorCode,
    String errorText
) implements Body {

  /**
   * Serialize this {@code ErrorBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode an {@code ErrorBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code ErrorBody} instance
   * @throws RuntimeException if decoding fails
   */
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