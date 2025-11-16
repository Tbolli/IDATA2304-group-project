
package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import ntnu.idata2302.sfp.controlPanel.backendlogic.BackendEventBus;
import ntnu.idata2302.sfp.controlPanel.gui.SceneManager;
import org.json.JSONObject;

/**
 * Controller for the Data Log view.
 *
 * <p>Responsibilities:
 * - Bind table columns to the {@link LogEntry} properties.
 * - Maintain an observable list of recent log entries shown in the table.
 * - Subscribe to sensor messages from {@link BackendEventBus} and append relevant
 * entries to the table model on the JavaFX application thread.
 *
 * <p>Threading and JSON notes:
 * - the event bus delivers Incoming messages on an arbitrary thread.
 * All UI updates are dispatched via {@link Platform#runLater(Runnable)}.
 * - Sensor payloads are parsed with {@link JSONObject}. Malformed JSON is caught
 * and logged to standard output to avoid throwing on the UI thread.
 */
public class DataLogController {

  @FXML
  private TableView<LogEntry> logTable;
  @FXML
  private TableColumn<LogEntry, String> colTime;
  @FXML
  private TableColumn<LogEntry, String> colTemp;
  @FXML
  private TableColumn<LogEntry, String> colHum;

  /**
   * Observable list backing the table view.
   * New entries are appended here on the JavaFX application thread.
   */
  private final ObservableList<LogEntry> logList = FXCollections.observableArrayList();

  /**
   * The greenhouse id that this controller filters log entries for.
   * Only sensor messages whose "greenhouse" field matches this value are shown.
   */
  private final int activeGreenhouse = 1;

  /**
   * Initialize the controller.
   *
   * <p>Actions performed:
   * - Bind table columns to the properties of {@link LogEntry}.
   * - Set the table items to the internal observable list.
   * - Subscribe to sensor messages
   * via {@link BackendEventBus#onSensorMessage(java.util.function.Consumer)}.
   * Each incoming message is parsed as JSON, filtered by the current greenhouse id,
   * and converted to a {@link LogEntry} which is appended to the list on the JavaFX thread.
   *
   * <p>Error handling:
   * - Malformed or unexpected messages are caught and logged. They do not abort the subscription.
   */
  @FXML
  public void initialize() {

    colTime.setCellValueFactory(cell -> cell.getValue().timestampProperty());
    colTemp.setCellValueFactory(cell -> cell.getValue().temperatureProperty());
    colHum.setCellValueFactory(cell -> cell.getValue().humidityProperty());

    logTable.setItems(logList);


    BackendEventBus.onSensorMessage(json -> Platform.runLater(() -> {
      try {
        JSONObject obj = new JSONObject(json);

        int gh = obj.optInt("greenhouse", -1);
        if (gh != activeGreenhouse) {
          return; // ignore other greenhouses
        }

        String ts = obj.optString("timestamp", "--");
        String temp = obj.optString("temperature", "--") + "°C";
        String hum = obj.optString("humidity", "--") + "%";

        logList.add(new LogEntry(ts, temp, hum));

        if (logList.size() > 200) {
          logList.removeFirst();
        }

      } catch (Exception e) {
        System.out.println("Invalid sensor data: " + json);
      }
    }));
  }

  /**
   * Handle the Home button action by switching to the home scene.
   *
   */
  @FXML
  public void openHome() {
    SceneManager.switchScene("home");
  }

  /**
   * Handle the Nodes button action by switching to the nodes' scene.
   */
  @FXML
  public void openNodes() {
    SceneManager.switchScene("nodes");
  }

}