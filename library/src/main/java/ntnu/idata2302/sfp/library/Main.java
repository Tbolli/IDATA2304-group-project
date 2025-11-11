package ntnu.idata2302.sfp.library;

import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.header.Header;

import java.util.List;
import java.util.UUID;

public class Main {

  public static void main(String[] args) {

    // ---------------------------------------------------------
    // 1. Create DATA_REPORT body
    // ---------------------------------------------------------
    DataReportBody body = new DataReportBody(
      "0000000",
      "2025-01-01T12:00:00Z",
      List.of(
        new DataReportBody.SensorReading("temperature", 22.5, "C", "2025-01-01T11:59:59Z"),
        new DataReportBody.SensorReading("humidity", 55.1, "%", "2025-01-01T11:59:59Z")
      ),
      List.of(
        new DataReportBody.ActuatorState("pump1", "ON", "2025-01-01T11:59:50Z"),
        new DataReportBody.ActuatorState("pump2", "OFF", "2025-01-01T11:59:50Z")
      ),
      List.of(
        new DataReportBody.AggregateValue("temperature", "1h", 20.0, 25.0, 22.5),
        new DataReportBody.AggregateValue("humidity", "24h", 40.0, 80.0, 55.0)
      )
    );

    // Encode body into CBOR
    byte[] bodyBytes = body.toCbor();

    // ---------------------------------------------------------
    // 2. Create header with placeholder payload length (0)
    // SmartFarmingProtocol.toBytes() will fix this.
    // ---------------------------------------------------------
    Header header = new Header(
      new byte[] { 'S', 'F', 'P' },   // Protocol name
      (byte)1,                        // Version
      (byte)0x01,                     // Message Type = DATA_REPORT
      1001,                           // Source ID
      2002,                           // Target ID
      0,                              // payloadLength placeholder
      UUID.randomUUID()               // Message ID
    );

    // ---------------------------------------------------------
    // 3. Build full SFP packet
    // ---------------------------------------------------------
    SmartFarmingProtocol packet = new SmartFarmingProtocol(header, body);

    byte[] encoded = packet.toBytes();

    System.out.println("Packet length: " + encoded.length);
    System.out.println(toHex(encoded));

    // ---------------------------------------------------------
    // 4. Decode back into object (test)
    // ---------------------------------------------------------
    SmartFarmingProtocol parsed = SmartFarmingProtocol.fromBytes(encoded);

    System.out.println("Parsed type = " + parsed.getHeader().getMessageType());
    System.out.println("Parsed body = " + parsed.getBody());
  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) sb.append(String.format("%02X ", b));
    return sb.toString();
  }
}
