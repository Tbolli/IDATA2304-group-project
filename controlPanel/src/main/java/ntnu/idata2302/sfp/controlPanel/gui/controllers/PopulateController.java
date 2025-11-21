package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;

/**
 * Controller for the "Populate" scene, which allows users to spawn
 * multiple sensor node instances with custom configurations.
 */

public class PopulateController {

  @FXML private TextArea systemArgs;
  @FXML private TextArea customArgs;
  @FXML private TextArea spawnLog;

  @FXML private CheckBox checkImages;
  @FXML private CheckBox checkAggregates;

  @FXML private ComboBox<String> presetSelector;
  @FXML private TextFlow helpBox;

  /** Track all spawned sensor node processes. */
  private final List<Process> runningProcesses = new ArrayList<>();


  /**
   * Initializes the controller after its FXML fields have been injected.
   *
   * <p>This method populates the preset selector, initializes the system
   * argument area, loads the help text, and registers change listeners for
   * the image and aggregate support checkboxes.</p>
   */

  @FXML
  public void initialize() {

    loadHelpBox();
    refreshSystemArgs(false, false);

    presetSelector.getItems().addAll(
          "Basic Greenhouse",
          "Irrigation Node",
          "Light Node"
    );
    presetSelector.getSelectionModel().select(0);

    checkImages.selectedProperty().addListener((obs, o, n) ->
          refreshSystemArgs(n, checkAggregates.isSelected()));

    checkAggregates.selectedProperty().addListener((obs, o, n) ->
          refreshSystemArgs(checkImages.isSelected(), n));
  }

  /**
   * Navigates back to the node overview scene.
   *
   * <p>Invoked from the corresponding UI control (e.g., a "Back" button).</p>
   */

  @FXML
  private void goToNodes() {
    SceneManager.switchScene("nodes");
  }


  /**
   * Loads the currently selected preset into the custom arguments text area.
   *
   * <p>The preset determines a predefined set of <code>--sensor</code> and
   * <code>--actuator</code> lines that describe a typical node configuration.
   * The selected preset name is also appended to the spawn log.</p>
   */

  @FXML
  private void loadPreset() {

    String p = presetSelector.getValue();
    customArgs.clear();

    switch (p) {

      case "Basic Greenhouse" -> customArgs.setText("""
                    --sensor=temp:°C:-20:50
                    --actuator=HEATER:0:0:30:°C
                    --actuator=FAN:0:0:100:%                      
                    """);

      case "Irrigation Node" -> customArgs.setText("""
                    --sensor=soilMoist:%:0:100
                    --actuator=SPRINKLER:0:0:100:%   
                    --actuator=VALVE:0:0:100:% 
                    """);

      case "Light Node" -> customArgs.setText("""
                    --sensor=light:lux:0:10000
                    --sensor=humidity:%:0:100
                    --actuator=VENT:0:0:100:% 
                    --actuator=LIGHT:0:0:1:state 
                    """);
    }



    spawnLog.appendText("Loaded preset: " + p + "\n");
  }


  // ============================================================
  // SYSTEM ARG BUILDER
  // ============================================================

  /**
   * Updates the system argument text area based on the image and aggregate
   * support flags.
   *
   * <p>This rebuilds the <code>--supportsImages</code> and
   * <code>--supportsAggregates</code> arguments while keeping a fixed node
   * base configuration.</p>
   *
   * @param img whether the node supports image transfer
   * @param agg whether the node supports aggregate data
   */

  private void refreshSystemArgs(boolean img, boolean agg) {
    systemArgs.setText("""
                --nodeId=null
                --nodeType=1
                --supportsImages=%s
                --supportsAggregates=%s
                """.formatted(img, agg).trim());
  }


  // ============================================================
  // HELP BOX
  // ============================================================

  /**
   * Populates the help box with usage instructions for sensor and actuator
   * command-line arguments.
   *
   * <p>The help text describes the format of <code>--sensor</code> and
   * <code>--actuator</code> definitions as well as the special "state" actuator
   * convention.</p>
   */

  private void loadHelpBox() {
    helpBox.getChildren().clear();
    helpBox.getChildren().add(new Text("""
                Sensors:
                --sensor=id:unit:min:max

                Actuators:
                --actuator=id:value:min:max:unit

                For "state" actuators:
                value = 0 or 1
                min = 0.0   max = 1.0
                """));
  }


  // ============================================================
  // VALIDATION
  // ============================================================

  /**
   * Validates the custom command-line arguments entered by the user.
   *
   * <p>This method checks that each non-blank line either defines a sensor
   * or an actuator with the correct number of fields and numeric values where
   * required. Validation results are appended to the spawn log.</p>
   *
   * @return {@code true} if all lines are valid; {@code false} otherwise
   */

  private boolean validateCustomArgs() {

    spawnLog.appendText("\nValidating input...\n");

    List<String> lines = customArgs.getText().lines().toList();

    for (String line : lines) {

      if (line.isBlank()) {
        continue;
      }

      if (line.startsWith("--sensor=")) {

        String[] p = line.substring(9).split(":");
        if (p.length != 4) {
          return fail("Sensor format must be: id:unit:min:max");
        }

        if (!isDouble(p[2]) || !isDouble(p[3])) {
          return fail("Sensor min/max must be numeric: " + line);
        }

      } else if (line.startsWith("--actuator=")) {

        String[] p = line.substring(11).split(":");
        if (p.length != 5) {
          return fail("Actuator format: id:value:min:max:unit");
        }
        if (!isDouble(p[1]) || !isDouble(p[2]) || !isDouble(p[3])) {
          return fail("Actuator numeric fields invalid: " + line);
        }
      } else {
        return fail("Unknown command: " + line);
      }
    }

    spawnLog.appendText("Validation OK.\n");
    return true;
  }

  private boolean isDouble(String s) {
    try {
      Double.parseDouble(s);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean fail(String msg) {
    spawnLog.appendText("ERROR: " + msg + "\n");
    return false;
  }


  // ============================================================
  // SPAWN NODE
  // ============================================================

  /**
   * Spawns a new sensor node JVM process based on the current system and
   * custom argument configuration.
   *
   * <p>The method validates the custom arguments, locates the sensor node JAR
   * file, builds the full command list, and starts the process using
   * {@link ProcessBuilder}. The created process is tracked so it can be
   * terminated later.</p>
   */

  @FXML
  private void spawnNode() {

    spawnLog.appendText("\n=== SPAWNING SENSOR NODE ===\n");

    if (customArgs.getText().isBlank()) {
      fail("Custom Command Arguments cannot be empty.");
      return;
    }

    if (!validateCustomArgs()) {

      return;
    }

    List<String> cmd = new ArrayList<>();
    cmd.add("java");
    cmd.add("-jar");

    File jar = findSensorNodeJar();
    if (jar == null) {
      fail("Could not locate sensor node jar.");
      return;
    }

    cmd.add(jar.getAbsolutePath());

    systemArgs.getText().lines().forEach(s -> cmd.add(s.trim()));
    customArgs.getText().lines().filter(s -> !s.isBlank()).forEach(cmd::add);

    spawnLog.appendText("Launching with args:\n");
    cmd.forEach(c -> spawnLog.appendText("  " + c + "\n"));

    try {
      Process p = new ProcessBuilder(cmd)
            .inheritIO()
            .start();

      runningProcesses.add(p);

      spawnLog.appendText("✔ Sensor Node Launched.\n");

    } catch (Exception e) {
      fail("Failed to start: " + e.getMessage());
    }
  }


  // ============================================================
  // TERMINATE ALL PROCESSES
  // ============================================================

  /**
   * Terminates all sensor node processes that were previously spawned by
   * this controller.
   *
   * <p>The method attempts a graceful termination first using
   * {@link Process#destroy()}, waits briefly, and then forces termination
   * via {@link Process#destroyForcibly()} if needed. Status messages are
   * appended to the spawn log.</p>
   */

  @FXML
  private void terminateAllNodes() {

    spawnLog.appendText("\n=== TERMINATING ALL SENSOR NODES ===\n");

    if (runningProcesses.isEmpty()) {
      spawnLog.appendText("No running processes.\n");
      return;
    }

    for (Process p : runningProcesses) {
      try {
        p.destroy();
        boolean dead = p.waitFor(500, java.util.concurrent.TimeUnit.MILLISECONDS);

        if (!dead) {
          p.destroyForcibly();
        }

        spawnLog.appendText("Killed process PID " + p.pid() + "\n");

      } catch (Exception e) {
        spawnLog.appendText("Failed to kill process: " + e.getMessage() + "\n");
      }
    }

    runningProcesses.clear();
    spawnLog.appendText("✔ All sensor nodes terminated.\n");
  }


  // ============================================================
  // FIND JAR
  // ============================================================

  /**
   * Attempts to locate the sensor node JAR file in typical build output
   * locations.
   *
   * <p>The search starts from the current working directory and checks
   * <code>../sensorNode/target</code> and <code>sensorNode/target</code>.
   * Among JAR files found, it prefers a non-<code>original-</code> shaded
   * JAR if available.</p>
   *
   * @return the resolved JAR file, or {@code null} if none is found
   */

  private File findSensorNodeJar() {

    File root = new File(System.getProperty("user.dir"));
    File search = new File(root, "../sensorNode/target");

    if (!search.exists()) {
      search = new File(root, "sensorNode/target");

    }

    if (!search.exists()) {
      return null;
    }


    File shaded = null;

    for (File f : search.listFiles()) {

      String name = f.getName();

      // Skip original jar
      if (name.startsWith("original-")) {

        continue;
      }

      // Prefer shaded JAR (the one without "original-")
      if (name.endsWith(".jar")) {
        shaded = f;
      }
    }

    return shaded;
  }
}
