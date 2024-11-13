// Min Heap implementation

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MinHeap {
  private ArrayList<Object> heap;
  private Map<Integer, Integer> userIndices; // Maps userID to index in heap

  public MinHeap() {
      heap = new ArrayList<>();
      userIndices = new HashMap<>();
  }

  public void insert(Object obj) {
      heap.add(obj);
      int current = heap.size() - 1;
      
      if (obj instanceof GatorTicketMaster.WaitlistEntry) {
          userIndices.put(((GatorTicketMaster.WaitlistEntry) obj).userID, current);
      }
      
      bubbleUp(current);
  }

  private void bubbleUp(int current) {
      while (current > 0) {
          int parent = (current - 1) / 2;
          if (compare(heap.get(current), heap.get(parent)) >= 0) {
              break;
          }
          swap(current, parent);
          current = parent;
      }
  }

  public Object extractMin() {
      if (heap.isEmpty()) {
          return null;
      }

      Object min = heap.get(0);
      if (min instanceof GatorTicketMaster.WaitlistEntry) {
          userIndices.remove(((GatorTicketMaster.WaitlistEntry) min).userID);
      }

      int lastIdx = heap.size() - 1;
      if (lastIdx > 0) {
          heap.set(0, heap.get(lastIdx));
          if (heap.get(0) instanceof GatorTicketMaster.WaitlistEntry) {
              userIndices.put(((GatorTicketMaster.WaitlistEntry) heap.get(0)).userID, 0);
          }
      }
      heap.remove(lastIdx);

      if (!heap.isEmpty()) {
          heapify(0);
      }

      return min;
  }

  private void heapify(int idx) {
      int size = heap.size();
      while (true) {
          int smallest = idx;
          int left = 2 * idx + 1;
          int right = 2 * idx + 2;

          if (left < size && compare(heap.get(left), heap.get(smallest)) < 0) {
              smallest = left;
          }

          if (right < size && compare(heap.get(right), heap.get(smallest)) < 0) {
              smallest = right;
          }

          if (smallest == idx) {
              break;
          }

          swap(idx, smallest);
          idx = smallest;
      }
  }

  private int compare(Object a, Object b) {
      if (a instanceof Integer && b instanceof Integer) {
          return ((Integer) a).compareTo((Integer) b);
      } else if (a instanceof GatorTicketMaster.WaitlistEntry && b instanceof GatorTicketMaster.WaitlistEntry) {
          GatorTicketMaster.WaitlistEntry entryA = (GatorTicketMaster.WaitlistEntry) a;
          GatorTicketMaster.WaitlistEntry entryB = (GatorTicketMaster.WaitlistEntry) b;
          if (entryA.priority != entryB.priority) {
              return entryB.priority - entryA.priority; // Higher priority first
          }
          return Long.compare(entryA.timestamp, entryB.timestamp); // Earlier timestamp first
      }
      throw new IllegalArgumentException("Incomparable types");
  }

  private void swap(int i, int j) {
      Object temp = heap.get(i);
      heap.set(i, heap.get(j));
      heap.set(j, temp);

      if (heap.get(i) instanceof GatorTicketMaster.WaitlistEntry) {
          userIndices.put(((GatorTicketMaster.WaitlistEntry) heap.get(i)).userID, i);
      }
      if (heap.get(j) instanceof GatorTicketMaster.WaitlistEntry) {
          userIndices.put(((GatorTicketMaster.WaitlistEntry) heap.get(j)).userID, j);
      }
  }

  public boolean remove(int userID) {
      Integer index = userIndices.get(userID);
      if (index == null) {
          return false;
      }

      int lastIdx = heap.size() - 1;
      swap(index, lastIdx);
      heap.remove(lastIdx);
      userIndices.remove(userID);

      if (index < heap.size()) {
          int parent = (index - 1) / 2;
          if (index > 0 && compare(heap.get(index), heap.get(parent)) < 0) {
              bubbleUp(index);
          } else {
              heapify(index);
          }
      }
      return true;
  }

  public boolean updatePriority(int userID, int newPriority) {
      Integer index = userIndices.get(userID);
      if (index == null) {
          return false;
      }

      GatorTicketMaster.WaitlistEntry entry = (GatorTicketMaster.WaitlistEntry) heap.get(index);
      int oldPriority = entry.priority;
      entry.priority = newPriority;

      if (newPriority < oldPriority) {
          heapify(index);
      } else {
          bubbleUp(index);
      }
      return true;
  }

  public boolean isEmpty() {
      return heap.isEmpty();
  }

  public int size() {
      return heap.size();
  }
}