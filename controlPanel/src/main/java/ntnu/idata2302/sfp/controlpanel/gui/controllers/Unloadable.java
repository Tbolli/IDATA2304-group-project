package ntnu.idata2302.sfp.controlpanel.gui.controllers;

/**
 * Marker interface for controllers that need cleanup when a scene is unloaded.
 * SceneManager should call {@link #onUnload()} when the scene is removed/switched.
 */
public interface Unloadable {
  void onUnload();
}
