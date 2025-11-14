package ntnu.idata2302.sfp.controlPanel.gui;

import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

  @Override
  public void start(Stage stage) {
    // Register the main stage in SceneManager
    SceneManager.setStage(stage);

    // Load the first scene (Home)
    SceneManager.switchScene("home");
  }

  public static void main(String[] args) {
    launch();
  }
}
