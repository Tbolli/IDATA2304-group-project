package ntnu.idata2302.sfp.controlPanel.backendlogic;

import java.io.*;
import java.net.*;

public class MockServer {
  public static void main(String[] args) throws IOException {
    int port = 5050;
    try (ServerSocket serverSocket = new ServerSocket(port)) {
      System.out.println("🖥️ Mock Smart Farming Server listening on port " + port);
      Socket client = serverSocket.accept();
      System.out.println("✅ Control Panel connected: " + client.getInetAddress());

      BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
      PrintWriter out = new PrintWriter(client.getOutputStream(), true);

      String line;
      while ((line = in.readLine()) != null) {
        System.out.println("📩 Received: " + line);
        out.println("{\"ack\":true,\"received\":\"" + line + "\"}");
      }
    }
  }
}
