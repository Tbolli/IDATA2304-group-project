package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PopulateController {

  @FXML private TextArea systemArgs;
  @FXML private TextArea customArgs;
  @FXML private TextArea spawnLog;

  @FXML private CheckBox checkImages;
  @FXML private CheckBox checkAggregates;

  @FXML private ComboBox<String> presetSelector;
  @FXML private TextFlow helpBox;

  /** Track all spawned sensor node processes */
  private final List<Process> runningProcesses = new ArrayList<>();


  // ============================================================
  // Initialization
  // ============================================================
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

  @FXML
  private void goToNodes() {
    SceneManager.switchScene("nodes");
  }


  // ============================================================
  // PRESETS
  // ============================================================
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
  private boolean validateCustomArgs() {

    spawnLog.appendText("\nValidating input...\n");

    List<String> lines = customArgs.getText().lines().toList();

    for (String line : lines) {

      if (line.isBlank()) continue;

      if (line.startsWith("--sensor=")) {

        String[] p = line.substring(9).split(":");
        if (p.length != 4) return fail("Sensor format must be: id:unit:min:max");
        if (!isDouble(p[2]) || !isDouble(p[3]))
          return fail("Sensor min/max must be numeric: " + line);
      }
      else if (line.startsWith("--actuator=")) {

        String[] p = line.substring(11).split(":");
        if (p.length != 5) return fail("Actuator format: id:value:min:max:unit");
        if (!isDouble(p[1]) || !isDouble(p[2]) || !isDouble(p[3]))
          return fail("Actuator numeric fields invalid: " + line);
      }
      else return fail("Unknown command: " + line);
    }

    spawnLog.appendText("Validation OK.\n");
    return true;
  }

  private boolean isDouble(String s) {
    try { Double.parseDouble(s); return true; }
    catch (Exception e) { return false; }
  }

  private boolean fail(String msg) {
    spawnLog.appendText("ERROR: " + msg + "\n");
    return false;
  }


  // ============================================================
  // SPAWN NODE
  // ============================================================
  @FXML
  private void spawnNode() {

    spawnLog.appendText("\n=== SPAWNING SENSOR NODE ===\n");

    if (customArgs.getText().isBlank()){
      fail("Custom Command Arguments cannot be empty.");
      return;
    }

    if (!validateCustomArgs())
      return;

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

        if (!dead) p.destroyForcibly();

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
  private File findSensorNodeJar() {

    File root = new File(System.getProperty("user.dir"));
    File search = new File(root, "../sensorNode/target");

    if (!search.exists())
      search = new File(root, "sensorNode/target");

    if (!search.exists())
      return null;

    for (File f : search.listFiles()) {
      if (f.getName().endsWith(".jar"))
        return f;
    }

    return null;
  }
}
