package ntnu.idata2302.sfp.library.body;

/**
 * Marker interface for message bodies in the Smart Farming Protocol.
 */

public interface Body {

  /**
   * Serialize this body to CBOR bytes.
   *
   * @return a byte array containing the CBOR-encoded representation of this instance
   */

  byte[] toCbor();
}