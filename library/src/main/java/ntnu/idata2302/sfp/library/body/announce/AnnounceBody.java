package ntnu.idata2302.sfp.library.body.announce;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AnnounceBody(
  int requestId,
  NodeDescriptor descriptor
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static AnnounceBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, AnnounceBody.class);
  }
}