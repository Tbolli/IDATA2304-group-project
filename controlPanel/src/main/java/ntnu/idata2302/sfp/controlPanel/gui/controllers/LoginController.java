package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.input.KeyCode;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import ntnu.idata2302.sfp.controlPanel.net.AppContext;
import ntnu.idata2302.sfp.controlPanel.net.SfpClient;

import java.io.IOException;
import java.util.Objects;

public class LoginController {
  @FXML private TextField ip1, ip2, ip3, ip4;
  @FXML private Label feedbackLabel;

  @FXML
  public void initialize() {
    setupField(ip1, ip2);
    setupField(ip2, ip3);
    setupField(ip3, ip4);
    setupField(ip4, null);
  }

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

  private TextField getPrevious(TextField field) {
    if (field == ip2) return ip1;
    if (field == ip3) return ip2;
    if (field == ip4) return ip3;
    return null;
  }

  @FXML
  private void setLocalHost() {
    ip1.setText("127");
    ip2.setText("0");
    ip3.setText("0");
    ip4.setText("1");
  }

  @FXML
  private void connectToServer() {
    String ip = String.format("%s.%s.%s.%s", ip1.getText(), ip2.getText(), ip3.getText(), ip4.getText());
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
