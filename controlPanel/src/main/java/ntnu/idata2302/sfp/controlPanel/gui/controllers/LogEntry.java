package ntnu.idata2302.sfp.controlPanel.gui.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * View-model representing a single log entry for the control panel GUI.
 *
 * <p>This class exposes its fields as JavaFX {@link StringProperty} instances so they can be
 * observed and bound to UI controls (for example in a TableView). The property references are
 * final; their contained string values may be changed via the property API.</p>
 */
public class LogEntry {

  private final StringProperty timestamp = new SimpleStringProperty();
  private final StringProperty temperature = new SimpleStringProperty();
  private final StringProperty humidity = new SimpleStringProperty();

  /**
   * Create a new LogEntry populated with the provided values.
   *
   * @param t    the timestamp string (for example, "2025-11-13 10:00:00"); may be {@code null}
   * @param temp the temperature value as a string (for example, "21.5°C"); may be {@code null}
   * @param hum  the humidity value as a string (for example, "45%"); may be {@code null}
   */
  public LogEntry(String t, String temp, String hum) {
    timestamp.set(t);
    temperature.set(temp);
    humidity.set(hum);
  }

  /**
   * Returns the {@link StringProperty} that holds the timestamp for this entry.
   *
   * <p>The returned property can be observed or bound to UI controls.</p>
   *
   * @return the timestamp property
   */
  public StringProperty timestampProperty() {
    return timestamp;
  }

  /**
   * Returns the {@link StringProperty} that holds the temperature value for this entry.
   *
   * <p>The returned property can be observed or bound to UI controls.</p>
   *
   * @return the temperature property
   */
  public StringProperty temperatureProperty() {
    return temperature;
  }

  /**
   * Returns the {@link StringProperty} that holds the humidity value for this entry.
   *
   * <p>The returned property can be observed or bound to UI controls.</p>
   *
   * @return the humidity property
   */
  public StringProperty humidityProperty() {
    return humidity;
  }
}