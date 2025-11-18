package ntnu.idata2302.sfp.server.util;

import ntnu.idata2302.sfp.library.SmartFarmingProtocol;
import ntnu.idata2302.sfp.library.header.Header;
import ntnu.idata2302.sfp.library.header.MessageTypes;
import ntnu.idata2302.sfp.server.Server;
import ntnu.idata2302.sfp.server.net.MessageDispatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.UUID;

/**
 * Unit tests for {@link Server}.
 *
 * <p>These tests verify the static configuration of the {@link MessageDispatcher}
 * inside {@link Server} and that the private {@code handleClient} method
 * correctly closes the connection when it receives an incomplete header.</p>
 */
public class ServerTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that the static {@link MessageDispatcher} inside {@link Server}
   * has handlers registered for all expected {@link MessageTypes}.
   */
  @Test
  public void dispatcher_hasHandlersForConfiguredMessageTypes_positive() throws Exception {
    // Arrange
    // Access Server.dispatcher via reflection
    Field dispatcherField = Server.class.getDeclaredField("dispatcher");
    dispatcherField.setAccessible(true);
    MessageDispatcher dispatcher = (MessageDispatcher) dispatcherField.get(null);

    // Access MessageDispatcher.handlers via reflection
    Field handlersField = MessageDispatcher.class.getDeclaredField("handlers");
    handlersField.setAccessible(true);
    @SuppressWarnings("unchecked")
    Map<MessageTypes, ?> handlers =
      (Map<MessageTypes, ?>) handlersField.get(dispatcher);

    // Act
    boolean hasDataReport = handlers.containsKey(MessageTypes.DATA_REPORT);
    boolean hasAnnounce = handlers.containsKey(MessageTypes.ANNOUNCE);
    boolean hasCapabilitiesQuery = handlers.containsKey(MessageTypes.CAPABILITIES_QUERY);
    boolean hasSubscribe = handlers.containsKey(MessageTypes.SUBSCRIBE);
    boolean hasUnsubscribe = handlers.containsKey(MessageTypes.UNSUBSCRIBE);
    boolean hasCommand = handlers.containsKey(MessageTypes.COMMAND);
    boolean hasCommandAck = handlers.containsKey(MessageTypes.COMMAND_ACK);
    boolean hasError = handlers.containsKey(MessageTypes.ERROR);

    // Assert
    Assertions.assertTrue(
      hasDataReport,
      "Dispatcher should have a handler for DATA_REPORT"
    );
    Assertions.assertTrue(
      hasAnnounce,
      "Dispatcher should have a handler for ANNOUNCE"
    );
    Assertions.assertTrue(
      hasCapabilitiesQuery,
      "Dispatcher should have a handler for CAPABILITIES_QUERY"
    );
    Assertions.assertTrue(
      hasSubscribe,
      "Dispatcher should have a handler for SUBSCRIBE"
    );
    Assertions.assertTrue(
      hasUnsubscribe,
      "Dispatcher should have a handler for UNSUBSCRIBE"
    );
    Assertions.assertTrue(
      hasCommand,
      "Dispatcher should have a handler for COMMAND"
    );
    Assertions.assertTrue(
      hasCommandAck,
      "Dispatcher should have a handler for COMMAND_ACK"
    );
    Assertions.assertTrue(
      hasError,
      "Dispatcher should have a handler for ERROR"
    );
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that the private {@code handleClient} method closes the socket
   * when it receives an incomplete header (fewer than {@link Header#HEADER_SIZE} bytes),
   * instead of hanging or throwing to the caller.
   */
  @Test
  public void handleClient_closesConnectionOnIncompleteHeader_negative() throws Exception {
    // Arrange
    // Set up a local TCP connection
    ServerSocket serverSocket = new ServerSocket(0);
    int port = serverSocket.getLocalPort();

    Socket clientSide = new Socket("localhost", port);
    Socket serverSide = serverSocket.accept();

    // Access private static handleClient(Socket) via reflection
    final Method handleClientMethod = Server.class.getDeclaredMethod("handleClient", Socket.class);
    handleClientMethod.setAccessible(true);

    // Start handleClient in a separate thread (no lambda)
    Thread serverThread = new Thread(new Runnable() {
      @Override
      public void run() {
        try {
          handleClientMethod.invoke(null, serverSide);
        } catch (Exception e) {
          // Swallow in test thread; exceptions here would fail
          // via assertions in the main test thread if needed.
        }
      }
    });

    serverThread.start();

    // Send fewer bytes than HEADER_SIZE to simulate an incomplete header
    OutputStream out = clientSide.getOutputStream();
    byte[] incompleteHeader = new byte[Header.HEADER_SIZE / 2];
    // Fill with some data so it is not all zeros
    for (int i = 0; i < incompleteHeader.length; i++) {
      incompleteHeader[i] = (byte) i;
    }

    out.write(incompleteHeader);
    out.flush();
    clientSide.close(); // Force EOF on server side

    // Act
    // Wait for the server thread to finish handling the client
    serverThread.join(2000);

    // Assert
    Assertions.assertFalse(
      serverThread.isAlive(),
      "handleClient thread should terminate after processing incomplete header"
    );
    Assertions.assertTrue(
      serverSide.isClosed(),
      "Server-side socket should be closed in handleClient finally block"
    );

    // Cleanup
    serverSocket.close();
  }
}
