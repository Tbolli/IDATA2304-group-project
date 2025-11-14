package ntnu.idata2302.sfp.server.util;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class IdAllocator {

  private static AtomicInteger nextId = new AtomicInteger(2); // IDs start at 1
  private static ConcurrentLinkedQueue<Integer> freeIds = new ConcurrentLinkedQueue<>();

  public static int allocate() {
    Integer id = freeIds.poll();
    if (id != null) {
      return id; // reuse freed id
    }
    return nextId.getAndIncrement(); // generate new id
  }

  public static void release(int id) {
    freeIds.offer(id); // return id to pool
  }
}
