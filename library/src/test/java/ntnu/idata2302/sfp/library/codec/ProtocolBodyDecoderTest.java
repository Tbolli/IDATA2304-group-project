package ntnu.idata2302.sfp.library.codec;

import ntnu.idata2302.sfp.library.body.command.CommandAckBody;
import ntnu.idata2302.sfp.library.body.command.CommandBody;
import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.body.data.DataRequestBody;
import ntnu.idata2302.sfp.library.body.error.ErrorBody;
import ntnu.idata2302.sfp.library.body.image.ImageChunkBody;
import ntnu.idata2302.sfp.library.body.image.ImageMetadataBody;
import ntnu.idata2302.sfp.library.body.image.ImageTransferAckBody;
import ntnu.idata2302.sfp.library.body.capabilities.CapabilitiesQueryBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.SubscribeBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeAckBody;
import ntnu.idata2302.sfp.library.body.subscribe.UnsubscribeBody;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test class for {@link ProtocolBodyDecoder}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>Decodes DATA_REQUEST bodies to DataRequestBody.</li>
 *   <li>Decodes COMMAND bodies to CommandBody.</li>
 *   <li>Decodes SUBSCRIBE bodies to SubscribeBody.</li>
 *   <li>Decodes IMAGE_METADATA bodies to ImageMetadataBody.</li>
 *   <li>Decodes ERROR bodies to ErrorBody.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>Decoding invalid CBOR for a given message type throws an exception.</li>
 * </ul>
 */
public class ProtocolBodyDecoderTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that a DATA_REQUEST body is decoded into a DataRequestBody instance.
   */
  @Test
  void decode_dataRequest_positive() {
    // Arrange
    DataRequestBody.SensorAggregateSection agg =
      new DataRequestBody.SensorAggregateSection(
        List.of("temp"),
        List.of("1h"),
        List.of("avg")
      );
    DataRequestBody.SensorSection sensors =
      new DataRequestBody.SensorSection(
        List.of("temp"),
        Boolean.TRUE,
        agg
      );
    DataRequestBody.ActuatorSection actuators =
      new DataRequestBody.ActuatorSection(Boolean.TRUE);
    DataRequestBody.ImageSection images =
      new DataRequestBody.ImageSection(Boolean.FALSE);

    DataRequestBody original = new DataRequestBody(
      "req-1",
      sensors,
      actuators,
      images
    );

    byte[] body = original.toCbor();

    // Act
    Object decoded = ProtocolBodyDecoder.decode(MessageTypes.DATA_REQUEST, body);

    // Assert
    DataRequestBody asRequest = (DataRequestBody) decoded;
    assertEquals(original.requestId(), asRequest.requestId());
    assertEquals(original.sensors().metrics(), asRequest.sensors().metrics());
  }

  /**
   * Verifies that a COMMAND body is decoded into a CommandBody instance.
   */
  @Test
  void decode_command_positive() {
    // Arrange
    CommandBody.CommandPart part =
      new CommandBody.CommandPart("fan", 1.0);
    CommandBody original = new CommandBody(5, List.of(part));
    byte[] body = original.toCbor();

    // Act
    Object decoded = ProtocolBodyDecoder.decode(MessageTypes.COMMAND, body);

    // Assert
    CommandBody asCommand = (CommandBody) decoded;
    assertEquals(original.requestId(), asCommand.requestId());
    assertEquals(original.actuators().get(0).name(), asCommand.actuators().get(0).name());
    assertEquals(original.actuators().get(0).newValue(), asCommand.actuators().get(0).newValue());
  }

  /**
   * Verifies that a SUBSCRIBE body is decoded into a SubscribeBody instance.
   */
  @Test
  void decode_subscribe_positive() {
    // Arrange
    SubscribeBody original = new SubscribeBody(9, 101);
    byte[] body = original.toCbor();

    // Act
    Object decoded = ProtocolBodyDecoder.decode(MessageTypes.SUBSCRIBE, body);

    // Assert
    SubscribeBody asSubscribe = (SubscribeBody) decoded;
    assertEquals(original.requestId(), asSubscribe.requestId());
    assertEquals(original.sensorNodeId(), asSubscribe.sensorNodeId());
  }

  /**
   * Verifies that an IMAGE_METADATA body is decoded into an ImageMetadataBody instance.
   */
  @Test
  void decode_imageMetadata_positive() {
    // Arrange
    ImageMetadataBody original = new ImageMetadataBody(
      "img-1",
      "2025-11-16T10:00:00Z",
      "image/jpeg",
      4096,
      4,
      1024,
      "checksum123"
    );
    byte[] body = original.toCbor();

    // Act
    Object decoded = ProtocolBodyDecoder.decode(MessageTypes.IMAGE_METADATA, body);

    // Assert
    ImageMetadataBody asMeta = (ImageMetadataBody) decoded;
    assertEquals(original.imageId(), asMeta.imageId());
    assertEquals(original.totalSize(), asMeta.totalSize());
  }

  /**
   * Verifies that an ERROR body is decoded into an ErrorBody instance.
   */
  @Test
  void decode_error_positive() {
    // Arrange
    ErrorBody original = new ErrorBody(1, "BAD_REQUEST");
    byte[] body = original.toCbor();

    // Act
    Object decoded = ProtocolBodyDecoder.decode(MessageTypes.ERROR, body);

    // Assert
    ErrorBody asError = (ErrorBody) decoded;
    assertEquals(original.errorCode(), asError.errorCode());
    assertEquals(original.errorText(), asError.errorText());
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that decoding invalid CBOR for a given message type
   * results in an exception.
   */
  @Test
  void decode_invalidBody_negative() {
    // Arrange
    byte[] invalid = new byte[]{0x01, 0x02, 0x03};

    // Act
    Executable decode = new Executable() {
      @Override
      public void execute() {
        ProtocolBodyDecoder.decode(MessageTypes.COMMAND, invalid);
      }
    };

    // Assert
    assertThrows(RuntimeException.class, decode);
  }
}
