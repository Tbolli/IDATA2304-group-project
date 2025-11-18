package ntnu.idata2302.sfp.controlPanel.gui.model;

import ntnu.idata2302.sfp.library.body.data.DataReportBody;

public record NodeEntry(
  int nodeId,
  DataReportBody data
) {}