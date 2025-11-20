package ntnu.idata2302.sfp.sensornode.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class that provides thread-safe, sequential request identifiers
 * for the sensor node components.
 *
 * <p>Ids are generated using an {@link AtomicInteger} to ensure atomic
 * increments across threads. The sequence starts at {@code 1} and increments
 * by one for each call to {@link #next()}.</p>
 *
 * <p>This class is non-instantiable and exposes only static behavior.</p>
 */
public final class RequestIds {

  /**
   * Atomic counter backing the id sequence.
   *
   * <p>Starts at {@code 1}. The counter may wrap around if the {@code int}
   * range is exhausted; callers should treat returned values as application
   * identifiers and handle overflow if necessary.</p>
   */
  private static final AtomicInteger counter = new AtomicInteger(1);

  /**
   * Private constructor to prevent instantiation of this utility class.
   *
   * <p>All functionality is provided via static methods.</p>
   */
  private RequestIds() {
  }

  /**
   * Obtain the next unique request id.
   *
   * <p>This method is thread-safe and returns a distinct {@code int} value on
   * each invocation by atomically incrementing the internal counter.</p>
   *
   * @return the next request identifier
   */
  public static int next() {
    return counter.getAndIncrement();
  }
}