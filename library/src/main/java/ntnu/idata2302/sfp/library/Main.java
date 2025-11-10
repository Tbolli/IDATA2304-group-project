package ntnu.idata2302.sfp.library;

import ntnu.idata2302.sfp.library.classes.Header;
import ntnu.idata2302.sfp.library.helpers.ByteHelper;

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

    SmartFarmingProtocol protocol = new SmartFarmingProtocol(header);

    System.out.println("Header created: " + header.getMessageId());
    System.out.println("SmartFarmingProtocol initialized.");

    byte[] vals = ByteHelper.encodeHeader(header);
    System.out.println(vals.length);

    for (byte i : vals){
      System.out.print(i);
    }

    Header h1 = ByteHelper.decodeHeader(vals);

  }
}
