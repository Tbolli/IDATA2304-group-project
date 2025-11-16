package ntnu.idata2302.sfp.library.codec;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.body.announce.AnnounceAckBody;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesListBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.body.command.CommandAckBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.body.data.DataRequestBody;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.body.image.ImageChunkBody;
import ntnu.idata2302.sfp.library.body.image.ImageMetadataBody;
import ntnu.idata2302.sfp.library.body.image.ImageTransferAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeBody;
import ntnu.idata2302.sfp.library.header.MessageTypes;

/**
 * Utility class responsible for converting raw message body bytes into
 * concrete {@link Body} instances based on the provided {@link MessageTypes}.
 *
 * <p>The decoder delegates to the static {@code fromCbor(byte[])} methods on
 * the concrete body classes. All methods are static and the class is not
 * intended to be instantiated.</p>
 */
public class ProtocolBodyDecoder {

  private ProtocolBodyDecoder() {
  } // utility class - prevent instantiation

  /**
   * Decode the provided CBOR-encoded body bytes into a concrete {@link Body}
   * instance determined by {@code msgType}.
   *
   * <p>This method selects the appropriate static decoder for the given
   * {@link MessageTypes} value and returns the resulting typed body. The
   * method delegates any decoding errors to the underlying decoders, which
   * typically throw {@link RuntimeException} on failure.</p>
   *
   * @param msgType the message type that identifies which body decoder to use;
   *                must not be {@code null}
   * @param body    the CBOR-encoded body bytes to decode (may be {@code null}
   *                if that body type allows it)
   * @return a concrete {@link Body} instance corresponding to {@code msgType}
   * @throws IllegalArgumentException if {@code msgType} is {@code null}
   * @throws RuntimeException         if the underlying CBOR decoding fails
   */
  public static Body decode(MessageTypes msgType, byte[] body) {
    if (msgType == null) {
      throw new IllegalArgumentException("msgType must not be null");
    }

    return switch (msgType) {
      case DATA_REPORT -> DataReportBody.fromCbor(body);
      case DATA_REQUEST -> DataRequestBody.fromCbor(body);

      case COMMAND -> CommandBody.fromCbor(body);
      case COMMAND_ACK -> CommandAckBody.fromCbor(body);

      case SUBSCRIBE -> SubscribeBody.fromCbor(body);
      case UNSUBSCRIBE -> UnsubscribeBody.fromCbor(body);
      case SUBSCRIBE_ACK -> SubscribeAckBody.fromCbor(body);
      case UNSUBSCRIBE_ACK -> UnsubscribeAckBody.fromCbor(body);

      case CAPABILITIES_QUERY -> CapabilitiesQueryBody.fromCbor(body);
      case CAPABILITIES_LIST -> CapabilitiesListBody.fromCbor(body);

      case ANNOUNCE -> AnnounceBody.fromCbor(body);
      case ANNOUNCE_ACK -> AnnounceAckBody.fromCbor(body);

      case IMAGE_METADATA -> ImageMetadataBody.fromCbor(body);
      case IMAGE_CHUNK -> ImageChunkBody.fromCbor(body);
      case IMAGE_TRANSFER_ACK -> ImageTransferAckBody.fromCbor(body);

      case ERROR -> ErrorBody.fromCbor(body);
    };
  }
}