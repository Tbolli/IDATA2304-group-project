package ntnu.idata2302.sfp.library.body.capabilities;

import com.fasterxml.jackson.annotation.JsonInclude;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CapabilitiesListBody(
  int requestId,
  List<NodeDescriptor> nodes
) implements Body {

  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  public static CapabilitiesListBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CapabilitiesListBody.class);
  }


}
