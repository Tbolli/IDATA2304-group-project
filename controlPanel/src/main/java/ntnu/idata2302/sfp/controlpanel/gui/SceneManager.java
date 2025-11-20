package ntnu.idata2302.sfp.controlpanel.gui;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ntnu.idata2302.sfp.controlpanel.gui.controllers.Unloadable;

/**
 * Utility for managing scenes in the control panel GUI.
 *
 * <p>This class holds a reference to the application's primary {@link Stage} and
 * provides a method to switch views by loading FXML files from the resource path
 * {@code /ntnu/idata2302/sfp/controlPanel/gui/views/{viewName}.fxml}.
 * Loaded {@link Scene} instances
 * are cached in-memory to avoid reloading FXML on later switches. A global
 * application stylesheet located at {@code /ntnu/smartFarm/gui/styles/app.css} is
 * applied to each scene when first used.</p>
 *
 * <p>Note: methods that interact with JavaFX UI state (for example {@link #setStage} and
 * {@link #switchScene}) should be called on the JavaFX Application Thread.</p>
 */
public class SceneManager {

  private static Stage primaryStage;
  private static Object currentController;
  private static final Map<String, Scene> sceneCache = new HashMap<>();

  /**
   * Initialize the SceneManager with the application's primary stage.
   *
   * <p>This method stores the provided {@code stage} reference and configures initial
   * window dimensions and minimum size constraints.</p>
   *
   * @param stage the primary {@link Stage} for the application; must not be {@code null}
   *              and should be provided from the JavaFX Application Thread
   */
  public static void setStage(Stage stage) {
    primaryStage = stage;

    stage.setMinWidth(1200);
    stage.setMinHeight(720);
    stage.setWidth(1440);
    stage.setHeight(900);

  }

  /**
   * Switch the current scene to the view identified by {@code viewName}.
   *
   * <p>The method looks for an FXML file at the resource path
   * {@code /ntnu/idata2302/sfp/controlPanel/gui/views/{viewName}.fxml}.
   * If the view has been loaded before
   * it will be retrieved from an internal cache; otherwise the FXML is loaded and a new
   * {@link Scene} is created and cached. The application stylesheet
   * {@code /ntnu/idata2302/sfp/controlPanel/gui/styles/app.css}
   * is ensured to be present on the scene's
   * stylesheets.</p>
   *
   * <p>If the primary stage has not been set via {@link #setStage(Stage)} this method
   * logs an error to standard error and returns without changing the UI. IO errors
   * encountered while loading the FXML are printed to standard error.</p>
   *
   * @param viewName the name of the view FXML file (without the {@code .fxml} extension),
   *                 for example {@code "mainView"}; must not be {@code null}
   */
  public static void switchScene(String viewName) {
    if (primaryStage == null) {
      System.err.println("SceneManager: Stage not set!");
      return;
    }
    // If previous controller supports unloading, call it
    if (currentController instanceof Unloadable unloadable) {
      unloadable.onUnload();
    }
    try {
      // Check cache first
      Scene scene = sceneCache.get(viewName);
      if (scene == null) {
        FXMLLoader loader = new FXMLLoader(SceneManager.class.getResource(
            "/ntnu/idata2302/sfp/controlpanel/gui/views/" + viewName + ".fxml"));
        Parent root = loader.load();
        scene = new Scene(root);
        sceneCache.put(viewName, scene);
      }


      String style = Objects.requireNonNull(
              SceneManager.class.getResource("/ntnu/idata2302/sfp/controlpanel/gui/styles/app.css"))
          .toExternalForm();
      if (!scene.getStylesheets().contains(style)) {
        scene.getStylesheets().add(style);
      }

      primaryStage.setScene(scene);
      primaryStage.setTitle("Smart Farming System  - " + capitalize(viewName));
      primaryStage.show();

    } catch (IOException e) {
      //noinspection CallToPrintStackTrace
      e.printStackTrace();
    }
  }

  /**
   * Return the input string with its first character converted to upper case.
   *
   * @param str the input string; may be {@code null} or empty
   * @return the input string with the first character upper-cased, or the original
   * value if it is {@code null} or empty
   */
  private static String capitalize(String str) {
    if (str == null || str.isEmpty()) {
      return str;
    }
    return Character.toUpperCase(str.charAt(0)) + str.substring(1);
  }
}