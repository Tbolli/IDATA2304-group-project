package ntnu.idata2302.sfp.library.codec;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

/**
 * Utility class for CBOR encoding and decoding using Jackson CBOR.
 *
 * <p>Provides simple static helpers to serialize Java objects to CBOR bytes and
 * to deserialize CBOR bytes back into Java objects. Uses a single shared
 * {@link ObjectMapper} configured with a {@link CBORFactory}. The mapper is
 * thread-safe for read/write operations and therefore safe to reuse.</p>
 */
public final class CborCodec {

  private static final ObjectMapper MAPPER = new ObjectMapper(new CBORFactory());

  private CborCodec() {
  } // utility class

  /**
   * Encode the given object to CBOR bytes.
   *
   * @param obj the object to encode; must be serializable by Jackson
   * @return a byte array containing the CBOR representation of {@code obj}
   * @throws RuntimeException if encoding fails
   */
  public static byte[] encode(Object obj) {
    try {
      return MAPPER.writeValueAsBytes(obj);
    } catch (Exception e) {
      throw new RuntimeException("CBOR encode failed", e);
    }
  }

  /**
   * Decode CBOR bytes into an instance of the specified type.
   *
   * @param data the CBOR-encoded input bytes
   * @param type the target class to deserialize into
   * @param <T>  the target type
   * @return an instance of {@code type} deserialized from {@code data}
   * @throws RuntimeException if decoding fails
   */
  public static <T> T decode(byte[] data, Class<T> type) {
    try {
      return MAPPER.readValue(data, type);
    } catch (Exception e) {
      throw new RuntimeException("CBOR decode failed for " + type.getSimpleName(), e);
    }
  }
}