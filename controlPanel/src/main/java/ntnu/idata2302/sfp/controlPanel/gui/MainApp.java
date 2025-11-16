package ntnu.idata2302.sfp.controlPanel.gui;

import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main JavaFX application entry point for the control panel.
 *
 * <p>This class registers the primary {@link Stage} with the {@link SceneManager}
 * and loads the initial scene (home) when the application starts.</p>
 *
 * @since 1.0
 */
public class MainApp extends Application {

  /**
   * Called when the JavaFX application is started.
   *
   * <p>This implementation registers the provided {@code stage} with the
   * {@link SceneManager} and instructs the {@link SceneManager} to switch to
   * the initial "home" scene.</p>
   *
   * @param stage the primary stage provided by the JavaFX runtime
   */
  @Override
  public void start(Stage stage) {
    // Register the main stage in SceneManager
    SceneManager.setStage(stage);

    // Load the first scene (Home)
    SceneManager.switchScene("home");
  }

  /**
   * Standard Java entry point for the application.
   *
   * <p>Delegates to the JavaFX runtime to launch the application. Any command-line
   * arguments passed to the program are forwarded to the JavaFX launch mechanism.</p>
   *
   * @param args command-line arguments passed to the JVM
   */
  public static void main(String[] args) {
    launch();
  }
}