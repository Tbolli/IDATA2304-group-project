package ntnu.idata2302.sfp.library.body.command;

import java.util.List;
import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.codec.CborCodec;

/**
 * Immutable body representing a command request.
 *
 * <p>Holds the original request identifier and a list of actuator updates
 * represented by {@link CommandPart}. Instances are serialized to and from CBOR
 * using {@link CborCodec}.</p>
 *
 * @param requestId the identifier of the command request
 * @param actuators list of actuator updates to apply (may be null)
 */
public record CommandBody(
    int requestId,
    List<CommandPart> actuators
) implements Body {

  /**
   * Serialize this {@code CommandBody} to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */
  @Override
  public byte[] toCbor() {
    return CborCodec.encode(this);
  }

  /**
   * Decode a {@code CommandBody} from CBOR bytes.
   *
   * @param cbor CBOR-encoded input bytes
   * @return the decoded {@code CommandBody} instance
   * @throws RuntimeException if decoding fails (decoder may throw more specific exceptions)
   */
  public static CommandBody fromCbor(byte[] cbor) {
    return CborCodec.decode(cbor, CommandBody.class);
  }

  /**
   * Single actuator command part: identifies the actuator by name and the new
   * value to apply.
   *
   * @param name     the actuator name
   * @param newValue the new value to set for the actuator
   */
  public record CommandPart(String name, double newValue) {
  }
}