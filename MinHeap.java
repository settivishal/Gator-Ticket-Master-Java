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

    /**
     * Inserts the given object into the heap
     *
     * @param obj the object to insert
     */
    public void insert(Object obj) {
        // Add the object to the end of the heap
        heap.add(obj);
        int current = heap.size() - 1;

        // If the object is a WaitlistEntry, add its userID to the map
        if (obj instanceof GatorTicketMaster.WaitlistEntry) {
            userIndices.put(((GatorTicketMaster.WaitlistEntry) obj).userID, current);
        }

        // Bubble up the object to its correct position
        bubbleUp(current);
    }

    /**
     * Bubbles up the object at the given index to its correct position in the heap
     * @param current the index of the object to bubble up
     */
    private void bubbleUp(int current) {
        // Continue bubbling up until the object is in its correct position
        while (current > 0) {
            int parent = (current - 1) / 2;
            if (compare(heap.get(current), heap.get(parent)) >= 0) {
                break;
            }
            // Swap the object with its parent
            swap(current, parent);
            current = parent;
        }
    }

    /**
     * Removes and returns the minimum element of the heap
     * @return the minimum element of the heap, or null if the heap is empty
     */
    public Object extractMin() {
        if (heap.isEmpty()) {
            return null;
        }

        Object min = heap.get(0);
        // Remove the user from the map
        if (min instanceof GatorTicketMaster.WaitlistEntry) {
            userIndices.remove(((GatorTicketMaster.WaitlistEntry) min).userID);
        }

        int lastIdx = heap.size() - 1;
        if (lastIdx > 0) {
            // Swap the last element with the root
            heap.set(0, heap.get(lastIdx));
            // Add the new root to the map
            if (heap.get(0) instanceof GatorTicketMaster.WaitlistEntry) {
                userIndices.put(((GatorTicketMaster.WaitlistEntry) heap.get(0)).userID, 0);
            }
        }
        // Remove the last element
        heap.remove(lastIdx);

        if (!heap.isEmpty()) {
            // Heapify the root
            heapify(0);
        }

        return min;
    }

    /**
     * Heapify the element at the given index
     * @param idx the index of the element to heapify
     */
    private void heapify(int idx) {
        int size = heap.size();
        while (true) {
            int smallest = idx;
            int left = 2 * idx + 1;
            int right = 2 * idx + 2;

            // Check if the left child is smaller than the current element
            if (left < size && compare(heap.get(left), heap.get(smallest)) < 0) {
                smallest = left;
            }

            // Check if the right child is smaller than the current element
            if (right < size && compare(heap.get(right), heap.get(smallest)) < 0) {
                smallest = right;
            }

            // If the smallest is the current element, break the loop
            if (smallest == idx) {
                break;
            }

            // Swap the elements at the smallest and current indices
            swap(idx, smallest);
            idx = smallest;
        }
    }

    /**
     * Compares two objects which can either be Integers or WaitlistEntry instances.
     * 
     * @param a the first object to compare
     * @param b the second object to compare
     * @return a negative integer, zero, or a positive integer if the first argument is less than, equal to, or greater than the second
     * @throws IllegalArgumentException if the objects are not comparable types
     */
    private int compare(Object a, Object b) {
        // Check if both objects are instances of Integer
        if (a instanceof Integer && b instanceof Integer) {
            return ((Integer) a).compareTo((Integer) b);
        } 
        // Check if both objects are instances of WaitlistEntry
        else if (a instanceof GatorTicketMaster.WaitlistEntry && b instanceof GatorTicketMaster.WaitlistEntry) {
            GatorTicketMaster.WaitlistEntry entryA = (GatorTicketMaster.WaitlistEntry) a;
            GatorTicketMaster.WaitlistEntry entryB = (GatorTicketMaster.WaitlistEntry) b;
            // Compare based on priority, higher priority first
            if (entryA.priority != entryB.priority) {
                return entryB.priority - entryA.priority;
            }
            // If priorities are equal, compare based on timestamp, earlier timestamp first
            return Long.compare(entryA.timestamp, entryB.timestamp);
        }
        // Throw exception if types are incomparable
        throw new IllegalArgumentException("Incomparable types");
    }

    /**
     * Swap the elements at the given indices in the heap
     * @param i the first index to swap
     * @param j the second index to swap
     */
    private void swap(int i, int j) {
        // Swap the elements at the given indices
        Object temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);

        // Update the map to reflect the new indices of the elements
        if (heap.get(i) instanceof GatorTicketMaster.WaitlistEntry) {
            userIndices.put(((GatorTicketMaster.WaitlistEntry) heap.get(i)).userID, i);
        }
        if (heap.get(j) instanceof GatorTicketMaster.WaitlistEntry) {
            userIndices.put(((GatorTicketMaster.WaitlistEntry) heap.get(j)).userID, j);
        }
    }

    /**
     * Removes the element with the given user ID from the heap
     * @param userID the user ID of the element to remove
     * @return true if the element was found and removed, false otherwise
     */
    public boolean remove(int userID) {
        Integer index = userIndices.get(userID);
        if (index == null) {
            return false;
        }

        int lastIdx = heap.size() - 1;
        swap(index, lastIdx);
        heap.remove(lastIdx);
        userIndices.remove(userID);

        // Check if the element is still in the heap
        if (index < heap.size()) {
            int parent = (index - 1) / 2;
            // If the element is smaller than its parent, bubble it up
            if (index > 0 && compare(heap.get(index), heap.get(parent)) < 0) {
                bubbleUp(index);
            }
            // Otherwise, heapify the element
            else {
                heapify(index);
            }
        }
        return true;
    }

    /**
     * Updates the priority of the user with the given user ID
     * @param userID the user ID of the user to update
     * @param newPriority the new priority of the user
     * @return true if the user was found and updated, false otherwise
     */
    public boolean updatePriority(int userID, int newPriority) {
        Integer index = userIndices.get(userID);
        if (index == null) {
            return false;
        }

        GatorTicketMaster.WaitlistEntry entry = (GatorTicketMaster.WaitlistEntry) heap.get(index);
        int oldPriority = entry.priority;
        entry.priority = newPriority;

        // If the new priority is higher than the old priority, bubble the element up
        if (newPriority < oldPriority) {
            heapify(index);
        }
        // Otherwise, heapify the element
        else {
            bubbleUp(index);
        }
        return true;
    }

    /**
     * Checks if the heap is empty.
     * 
     * @return true if the heap is empty, false otherwise
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Returns the number of elements currently in the heap.
     * 
     * @return the size of the heap
     */
    public int size() {
        return heap.size();
    }
}