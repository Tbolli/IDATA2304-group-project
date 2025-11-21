package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import java.io.IOException;
import java.util.Objects;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.controlPanel.net.AppContext;
import ntnu.idata2302.sfp.controlPanel.net.SfpClient;

/**
 * Controller for the login view. Handles IP address input by splitting the
 * address into four separate fields and managing user-friendly navigation
 * between them.
 *
 * <p>Also manages connecting to a Smart Farming Protocol server and forwarding
 * the user to the Nodes view on successful connection.</p>
 */

public class LoginController {
  @FXML
  private TextField ip1;

  @FXML
  private TextField ip2;

  @FXML
  private TextField ip3;

  @FXML
  private TextField ip4;

  @FXML private Label feedbackLabel;

  /**
   * Initializes the controller by attaching validation and auto-navigation
   * logic to the four IP address fields.
   */

  @FXML
  public void initialize() {
    setupField(ip1, ip2);
    setupField(ip2, ip3);
    setupField(ip3, ip4);
    setupField(ip4, null);
  }

  /**
   * Configures an IP segment text field with.
   * <ul>
   *   <li>Digit-only input filtering</li>
   *   <li>Automatic limiting to 3 characters</li>
   *   <li>Auto-advance to the next field</li>
   *   <li>Auto-backspace navigation to the previous field</li>
   * </ul>
   *
   * @param current the TextField being configured
   * @param next    the next TextField in the IP sequence, or {@code null} if last
   */

  private void setupField(TextField current, TextField next) {
    // allow only numbers and max 3 chars
    current.textProperty().addListener((obs, oldVal, newVal) -> {
      if (!newVal.matches("\\d*")) {
        current.setText(newVal.replaceAll("[^\\d]", ""));
        return;
      }
      if (newVal.length() > 3) {
        current.setText(newVal.substring(0, 3));
      }
      if (newVal.length() == 3 && next != null) {
        next.requestFocus();
        next.positionCaret(next.getText().length());
      }
    });

    // auto move back when deleting at start
    current.setOnKeyPressed(event -> {
      if (Objects.requireNonNull(event.getCode()) == KeyCode.BACK_SPACE) {
        if (current.getCaretPosition() == 0 && current.getText().isEmpty()) {
          TextField previous = getPrevious(current);
          if (previous != null) {
            previous.requestFocus();
            previous.positionCaret(previous.getText().length());
          }
        }
      }
    });
  }

  /**
   * Returns the previous IP segment field based on the given field.
   *
   * @param field one of the four IP TextField components
   * @return the preceding field, or {@code null} if none exists
   */

  private TextField getPrevious(TextField field) {
    if (field == ip2) return ip1;
    if (field == ip3) return ip2;
    if (field == ip4) return ip3;
    return null;
  }

  /**
   * Fills all IP fields with the loopback address (127.0.0.1).
   *
   * <p>Convenient for local development and testing.</p>
   */

  @FXML
  private void setLocalHost() {
    ip1.setText("127");
    ip2.setText("0");
    ip3.setText("0");
    ip4.setText("1");
  }

  /**
   * Attempts to connect to the Smart Farming Protocol server using the
   * IP address assembled from the four input fields.
   *
   * <p>On success, the SfpClient is stored in {@link AppContext} and the
   * scene switches to the "nodes" view.
   * On failure, an error message is displayed to the user.</p>
   */

  @FXML
  private void connectToServer() {
    String ip = String.format("%s.%s.%s.%s", ip1.getText(),
          ip2.getText(), ip3.getText(), ip4.getText());
    SfpClient client = new SfpClient(ip, 5050);
    try {
      client.connect();
      AppContext.setClient(client);
      SceneManager.switchScene("nodes");
    } catch (IOException e) {
      feedbackLabel.setText("Unable to connect to " + ip);
      e.printStackTrace();
    }
  }
}
