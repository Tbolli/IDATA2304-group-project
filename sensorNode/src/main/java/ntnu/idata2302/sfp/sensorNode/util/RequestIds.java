package ntnu.idata2302.sfp.sensorNode.util;

import java.util.concurrent.atomic.AtomicInteger;

public final class RequestIds {
  private static final AtomicInteger counter = new AtomicInteger(1);

  private RequestIds() {}

  public static int next() {
    return counter.getAndIncrement();
  }
}
