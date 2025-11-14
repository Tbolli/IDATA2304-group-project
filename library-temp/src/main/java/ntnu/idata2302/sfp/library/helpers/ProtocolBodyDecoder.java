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
import ntnu.idata2302.sfp.library.body.image.ImageChunkBody;
import ntnu.idata2302.sfp.library.body.image.ImageMetadataBody;
import ntnu.idata2302.sfp.library.body.image.ImageTransferAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeBody;

public class ProtocolBodyDecoder {

  public static Body decode(byte msgType, byte[] body) {
    return switch (msgType) {
      case 0x01 -> DataReportBody.fromCbor(body);
      case 0x02 -> DataRequestBody.fromCbor(body);

      case 0x12 -> CommandBody.fromCbor(body);
      case 0x13 -> CommandAckBody.fromCbor(body);

      case 0x0B -> SubscribeBody.fromCbor(body);
      case 0x0C -> UnsubscribeBody.fromCbor(body);
      case 0x0D -> SubscribeAckBody.fromCbor(body);
      case 0x0E -> UnsubscribeAckBody.fromCbor(body);

      case 0x21 -> CapabilitiesQueryBody.fromCbor(body);
      case 0x22 -> CapabilitiesAnnounceBody.fromCbor(body);

      case 0x1E -> IdentificationAckBody.fromCbor(body);

      case 0x08 -> ImageMetadataBody.fromCbor(body);
      case 0x09 -> ImageChunkBody.fromCbor(body);
      case 0x0A -> ImageTransferAckBody.fromCbor(body);

      case (byte)0xFE -> ErrorBody.fromCbor(body);

      default -> throw new RuntimeException("Unknown message type: " + msgType);
    };
  }
}

