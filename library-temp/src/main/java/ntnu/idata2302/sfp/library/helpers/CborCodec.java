package ntnu.idata2302.sfp.library.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;

public final class CborCodec {

  private static final ObjectMapper MAPPER = new ObjectMapper(new CBORFactory());

  private CborCodec() {} // utility class

  public static byte[] encode(Object obj) {
    try {
      return MAPPER.writeValueAsBytes(obj);
    } catch (Exception e) {
      throw new RuntimeException("CBOR encode failed", e);
    }
  }

  public static <T> T decode(byte[] data, Class<T> type) {
    try {
      return MAPPER.readValue(data, type);
    } catch (Exception e) {
      throw new RuntimeException("CBOR decode failed for " + type.getSimpleName(), e);
    }
  }
}