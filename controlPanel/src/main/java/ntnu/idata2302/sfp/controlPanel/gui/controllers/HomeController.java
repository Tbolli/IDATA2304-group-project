
package ntnu.idata2302.sfp.controlPanel.gui.controllers;


import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import ntnu.idata2302.sfp.controlPanel.backendlogic.BackendEventBus;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;


/**
 * Controller for the Home / Dashboard view.
 *
 * <p>Responsibilities:
 * - Manage UI state for the selected greenhouse (switching between greenhouse 1..3).
 * - Subscribe to backend sensor messages via {@link BackendEventBus} and update the UI
 * for the currently active greenhouse. Incoming messages arrive at an arbitrary thread
 * and UI updates are dispatched to the JavaFX Application
 * Thread using {@link Platform#runLater(Runnable)}.
 * - Provide handlers for navigation, actuator toggles, and simple detail popups.
 *
 * <p>Threading note:
 * - The {@link #initialize()} method registers an event listener
 * on the JavaFX thread; UI code must run on the JavaFX Application Thread.
 */
public class HomeController {


  public Button homeBtn;
  public Button nodesBtn;
  public Button logsBtn;
  public Label pageTitle;

  @FXML
  private VBox mainCardsBox;
  @FXML
  private AnchorPane homeContentPane;

  @FXML
  private Button windowBtn;
  @FXML
  private Button fanBtn;


  /**
   * Called by the FXMLLoader after the controller is created.
   *
   * <p>Sets up runtime behavior such as subscribing to sensor messages from the backend.
   * Sensor messages are received as JSON strings; the handler dispatches UI updates to the
   * JavaFX Application Thread via {@link Platform#runLater(Runnable)}.
   *
   * <p>This method should be lightweight — heavy initialization should be deferred or
   * executed on background threads where appropriate.
   */
  @FXML
  public void initialize() {
    System.out.println("HomeController loaded!");

  }


  /**
   * Navigate to the Nodes view.
   */

  @FXML
  private void openNodes() {
    SceneManager.switchScene("nodes");
  }

  /**
   * Navigate to the Data Log view.
   */
  @FXML
  private void openDataLog() {
    SceneManager.switchScene("dataLog");
  }


  /**
   * Navigate to the Home view.
   */
  @FXML
  public void openHome() {
    SceneManager.switchScene("home");
  }



  /**
   * Show a simple node details popup inside the home content pane.
   *
   * <p>The existing main cards are hidden while the popup is visible. The popup
   * contains a small "Back to Dashboard" button that restores the previous state.
   */
  @FXML
  public void openNode() {
    mainCardsBox.setVisible(false);

    Label nodeDetails = new Label(
        """
            🌡 Node Details (Temperature Node)
            
            • IP: 192.168.0.10
            • Status: Online
            • Sensor Type: Temperature
            • Value: 24°C"""
    );
    nodeDetails.setStyle(
        "-fx-font-size: 16px; -fx-text-fill: #2B4854; "
        +
        "-fx-background-color: white; -fx-padding: 20; "
        +
        "-fx-background-radius: 10; -fx-border-radius: 10; "
        +
        "-fx-border-color: #3E6151;"
    );

    Button backBtn = new Button("← Back to Dashboard");
    backBtn.setStyle("-fx-background-color: #80ED99; -fx-font-weight: bold;");
    backBtn.setOnAction(e -> {
      homeContentPane.getChildren().removeAll(nodeDetails, backBtn);
      mainCardsBox.setVisible(true);
    });

    homeContentPane.getChildren().addAll(nodeDetails, backBtn);
    AnchorPane.setTopAnchor(backBtn, 30.0);
    AnchorPane.setLeftAnchor(backBtn, 30.0);
    AnchorPane.setTopAnchor(nodeDetails, 100.0);
    AnchorPane.setLeftAnchor(nodeDetails, 100.0);
  }


  /**
   * Show a full details popup inside the home content pane. Similar
   * lifecycle to {@link #openNode()}.
   */
  @FXML
  public void viewDetails() {
    mainCardsBox.setVisible(false);

    Label details = new Label(
        """
            📊 Node Full Details:
            
            • IP: 192.168.0.10
            • Status: Online
            • Sensor: Temperature
            • Current Value: 24°C
            • Last Update: 2 sec ago
            • Alerts: None"""
    );
    details.setStyle(
        "-fx-font-size: 15px; -fx-text-fill: #2B4854; "
        +
        "-fx-background-color: white; -fx-padding: 25; "
        +
        "-fx-background-radius: 10; -fx-border-radius: 10; "
        +
        "-fx-border-color: #3E6151;"
    );

    Button backBtn = new Button("← Back to Dashboard");
    backBtn.setStyle("-fx-background-color: #80ED99; -fx-font-weight: bold;");
    backBtn.setOnAction(e -> {
      homeContentPane.getChildren().removeAll(details, backBtn);
      mainCardsBox.setVisible(true);
    });

    homeContentPane.getChildren().addAll(details, backBtn);
    AnchorPane.setTopAnchor(backBtn, 30.0);
    AnchorPane.setLeftAnchor(backBtn, 30.0);
    AnchorPane.setTopAnchor(details, 100.0);
    AnchorPane.setLeftAnchor(details, 100.0);
  }


  /**
   * Toggle window actuator state and update its button text.
   *
   * <p>This method updates only the UI state and logs the action. Integrate with
   * backend actuator control if required.
   */
  @FXML
  public void toggleWindow() {
    if (windowBtn.getText().equals("ON")) {
      windowBtn.setText("OFF");
      System.out.println("🪟 Window closed");
    } else {
      windowBtn.setText("ON");
      System.out.println("🪟 Window opened");
    }
  }

  /**
   * Toggle fan actuator state and update its button text.
   *
   * <p>This method updates only the UI state and logs the action. Integrate with
   * backend actuator control if required.
   */
  @FXML
  public void toggleFan() {
    if (fanBtn.getText().equals("ON")) {
      fanBtn.setText("OFF");
      System.out.println("🌀 Fan stopped");
    } else {
      fanBtn.setText("ON");
      System.out.println("🌀 Fan running");
    }
  }


}