package ntnu.idata2302.sfp.library.helpers;

import ntnu.idata2302.sfp.library.body.Body;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesAnnounceBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.body.command.CommandAckBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.body.data.DataRequestBody;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.body.identification.IdentificationAckBody;
import ntnu.idata2302.sfp.library.body.identification.IdentificationBody;
import ntnu.idata2302.sfp.library.body.image.ImageChunkBody;
import ntnu.idata2302.sfp.library.body.image.ImageMetadataBody;
import ntnu.idata2302.sfp.library.body.image.ImageTransferAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeBody;
import ntnu.idata2302.sfp.library.header.MessageTypes;

public class ProtocolBodyDecoder {

  public static Body decode(MessageTypes msgType, byte[] body) {
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
      case CAPABILITIES_ANNOUNCE -> CapabilitiesAnnounceBody.fromCbor(body);

      case IDENTIFICATION -> IdentificationBody.fromCbor(body);
      case IDENTIFICATION_ACK -> IdentificationAckBody.fromCbor(body);

      case IMAGE_METADATA -> ImageMetadataBody.fromCbor(body);
      case IMAGE_CHUNK -> ImageChunkBody.fromCbor(body);
      case IMAGE_TRANSFER_ACK -> ImageTransferAckBody.fromCbor(body);

      case ERROR -> ErrorBody.fromCbor(body);
    };
  }
}

