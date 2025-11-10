package ntnu.idata2302.sfp.library;

import ntnu.idata2302.sfp.library.body.data.DataReportBody;
import ntnu.idata2302.sfp.library.header.Header;

import java.util.List;
import java.util.UUID;

public class Main {

  public static void main(String[] args) {

    Header header = new Header(
      new byte[] { 'S', 'F', 'P' },
      (byte)1,
      (byte)0,
      1001,
      2002,
      128,
      UUID.randomUUID()
    );

    DataReportBody body = new DataReportBody(
      "SN_TEST_001",
      "2025-01-01T12:00:00Z",
      List.of(
        new DataReportBody.SensorReading("temperature", 22.5, "C", "2025-01-01T11:59:59Z"),
        new DataReportBody.SensorReading("humidity", 55.1, "%", "2025-01-01T11:59:59Z")
      ),
      List.of(
        new DataReportBody.ActuatorState("pump1", "ON",  "2025-01-01T11:59:50Z"),
        new DataReportBody.ActuatorState("pump2", "OFF", "2025-01-01T11:59:50Z")
      ),
      List.of(
        new DataReportBody.AggregateValue("temperature", "1h", 20.0, 25.0, 22.5),
        new DataReportBody.AggregateValue("humidity",    "24h", 40.0, 80.0, 55.0)
      )
    );

    byte[] bodyBytes = body.toCbor();
    System.out.println(toHex(bodyBytes));

    DataReportBody dd2 = DataReportBody.fromCbor(bodyBytes);
    System.out.println(bodyBytes.length);

  }

  private static String toHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder();
    for (byte b : bytes) {
      sb.append(String.format("%02X ", b));
    }
    return sb.toString();
  }
}
