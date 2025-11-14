package ntnu.idata2302.sfp.sensorNode;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.body.announce.AnnounceBody;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.library.node.NodeDescriptor;
import ntnu.idata2302.sfp.library.node.NodeIds;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class StartUp {

  private static final String SERVER_HOST = "localhost";
  private static final int SERVER_PORT = 5050;
  private static final AtomicInteger counter = new AtomicInteger(0);

  private Socket socket;
  private InputStream in;
  private OutputStream out;

  public static void main(String[] args) {
    System.out.println("I am a StartUp");
  }

  private void connect() throws IOException {
    socket = new Socket(SERVER_HOST, SERVER_PORT);
    in = socket.getInputStream();
    out = socket.getOutputStream();
    System.out.println("Connected to server.");
  }

  private void sendAnnounce() {
    try {
      Header header = new Header(
        new byte[]{'S', 'F', 'P'},
        (byte) 1,
        MessageTypes.ANNOUNCE,
        NodeIds.BROADCAST,
        NodeIds.SERVER,
        0,
        UUID.randomUUID()
      );

      NodeDescriptor nodeDescriptor = new NodeDescriptor(
        null, 1, null, null, null, null
      );

      AnnounceBody body = new AnnounceBody(
        counter.incrementAndGet(),
        nodeDescriptor
      );

      SmartFarmingProtocol packet = new SmartFarmingProtocol(header, body);

      out.write(packet.toBytes());
      out.flush();

      System.out.println("Packet sent.");
    } catch (Exception e) {
      System.err.println("Failed to send packet: " + e.getMessage());
    }
  }

}
