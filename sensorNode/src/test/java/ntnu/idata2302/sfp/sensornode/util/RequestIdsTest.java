package ntnu.idata2302.sfp.sensornode.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for {@link RequestIds}.
 *
 * <b>Positive Tests:</b>
 * <ul>
 *   <li>next() returns values that increase by 1 on consecutive calls.</li>
 * </ul>
 *
 * <b>Negative Tests:</b>
 * <ul>
 *   <li>next() never returns a value smaller than the previous one.</li>
 * </ul>
 */
public class RequestIdsTest {

  // --------------------------- POSITIVE TESTS ---------------------------------- //

  /**
   * Verifies that next() returns values that increase by exactly 1
   * between consecutive calls.
   */
  @Test
  void next_incrementsByOne_positive() {
    // Arrange
    // No special setup, we just call next() several times in a row.

    // Act
    int id1 = RequestIds.next();
    int id2 = RequestIds.next();
    int id3 = RequestIds.next();

    // Assert
    assertEquals(id1 + 1, id2);
    assertEquals(id2 + 1, id3);
  }

  // --------------------------- NEGATIVE TESTS ---------------------------------- //

  /**
   * Verifies that next() never returns a value less than the previous one.
   */
  @Test
  void next_neverDecreases_negative() {
    // Arrange
    // Again, we just call next() several times.

    // Act
    int first = RequestIds.next();
    int second = RequestIds.next();

    // Assert
    assertTrue(second > first);
  }
}
