
package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import ntnu.idata2302.sfp.controlPanel.backendlogic.BackendEventBus;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

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
 * that calls {@link #updateGreenhouseUI(String)}
 * on the JavaFX thread; UI code must run on the JavaFX Application Thread.
 */
public class HomeController {


  private int currentGreenhouse = 1;


  @FXML
  private Label pageTitle;

  @FXML
  private VBox mainCardsBox;
  @FXML
  private AnchorPane homeContentPane;

  @FXML
  private Button gh1Btn;
  @FXML
  private Button gh2Btn;
  @FXML
  private Button gh3Btn;

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

    // Listen for backend sensor messages
    BackendEventBus.onSensorMessage(json ->
        Platform.runLater(() -> updateGreenhouseUI(json)));
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
   * Switch the UI to greenhouse 1.
   */
  @FXML
  public void openGreenhouse1() {
    loadGreenhouseData(1);
  }

  /**
   * Switch the UI to greenhouse 2.
   */

  @FXML
  public void openGreenhouse2() {
    loadGreenhouseData(2);
  }

  /**
   * Switch the UI to greenhouse 3.
   */
  @FXML
  public void openGreenhouse3() {
    loadGreenhouseData(3);
  }

  /**
   * Open the "More" view (legacy behavior: load data log into a center).
   */
  @FXML
  public void openMore() {
    // This is your old behavior
    loadViewIntoCenter("/ntnu/smartFarm/gui/views/dataLog.fxml");
  }


  /**
   * Change the active greenhouse id and refresh UI state to reflect the selection.
   *
   * <p>This updates the page title and button visibility; it does not perform any
   * network or long-running operations.
   *
   * @param greenhouseId the greenhouse id to activate (expected 1..3)
   */
  private void loadGreenhouseData(int greenhouseId) {
    currentGreenhouse = greenhouseId;

    pageTitle.setText("Smart Farming System — Greenhouse " + greenhouseId);

    refreshGreenhouseButtons();

    System.out.println("Switched to Greenhouse " + greenhouseId);
  }


  /**
   * Update the visibility of greenhouse quick-select buttons so the currently active
   * greenhouse button is hidden (prevents re-selecting the same greenhouse visually).
   */
  private void refreshGreenhouseButtons() {
    if (gh1Btn != null) {
      gh1Btn.setVisible(currentGreenhouse != 1);
    }
    if (gh2Btn != null) {
      gh2Btn.setVisible(currentGreenhouse != 2);
    }
    if (gh3Btn != null) {
      gh3Btn.setVisible(currentGreenhouse != 3);
    }
  }


  /**
   * Parse a backend sensor JSON message and update visible UI elements for the
   * currently selected greenhouse.
   *
   * <p>Expected JSON keys (examples): "greenhouse" (int), "temperature" (string/number),
   * "humidity" (string/number). If the message does not target the current greenhouse,
   * this method returns without modifying the UI.
   *
   * <p>Error handling: malformed JSON is caught and logged; the method will not throw.
   *
   * @param json raw JSON string received from the backend event bus
   */
  private void updateGreenhouseUI(String json) {
    try {
      JSONObject obj = new JSONObject(json);

      int target = obj.optInt("greenhouse", -1);
      if (target != currentGreenhouse) {
        return;
      }

      String temp = obj.optString("temperature", "--");
      String hum = obj.optString("humidity", "--");

      System.out.println("Updating greenhouse UI: GH" + target);

    } catch (Exception e) {
      System.err.println("❌ Failed to update Home UI: " + json);
    }
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
        "🌡 Node Details (Temperature Node)\n" +
            "\n" +
            "• IP: 192.168.0.10\n" +
            "• Status: Online\n" +
            "• Sensor Type: Temperature\n" +
            "• Value: 24°C"
    );
    nodeDetails.setStyle(
        "-fx-font-size: 16px; -fx-text-fill: #2B4854; " +
            "-fx-background-color: white; -fx-padding: 20; " +
            "-fx-background-radius: 10; -fx-border-radius: 10; " +
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
        "📊 Node Full Details:\n\n" +
            "• IP: 192.168.0.10\n" +
            "• Status: Online\n" +
            "• Sensor: Temperature\n" +
            "• Current Value: 24°C\n" +
            "• Last Update: 2 sec ago\n" +
            "• Alerts: None"
    );
    details.setStyle(
        "-fx-font-size: 15px; -fx-text-fill: #2B4854; " +
            "-fx-background-color: white; -fx-padding: 25; " +
            "-fx-background-radius: 10; -fx-border-radius: 10; " +
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


  /**
   * Load the given FXML resource and place the resulting view into the center of the
   * application's main BorderPane root. Any IO errors are printed to the console.
   *
   * @param fxmlResourcePath classpath path to the FXML resource (e.g. /ntnu/.../view.fxml)
   */
  private void loadViewIntoCenter(String fxmlResourcePath) {
    try {
      URL resource = getClass().getResource(fxmlResourcePath);
      if (resource == null) {
        System.err.println("FXML not found: " + fxmlResourcePath);
        return;
      }
      Parent view = FXMLLoader.load(resource);

      BorderPane main = (BorderPane) pageTitle.getScene().getRoot();
      main.setCenter(view);

    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}