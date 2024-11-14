// Min Heap implementation

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MinHeap {
    private ArrayList<Object> heap;
    private Map<Integer, Integer> idToPositionMap; // Maps userID to index in heap

    public MinHeap() {
        heap = new ArrayList<>();
        idToPositionMap = new HashMap<>();
    }

    /**
     * Inserts the given object into the heap
     * @param obj the object to insert
     */
    public void insert(Object obj) {
        // Add the object to the end of the heap
        heap.add(obj);
        int current = heap.size() - 1;

        // If the object is a WaitlistEntry, add its userID to the map
        if (obj instanceof GatorTicketMaster.WaitlistEntry) {
            idToPositionMap.put(((GatorTicketMaster.WaitlistEntry) obj).userID, current);
        }

        // Bubble up the object to its correct position
        promoteElement(current);
    }

    /**
     * Promotes the element at the given index up the heap by swapping it with its parent if the parent is larger.
     * @param current the index of the element to promote
     */
    private void promoteElement(int current) {
        // Keep track of the current index
        while (current > 0) {
            int parent = (current - 1) / 2;
            // If the parent is larger than the current element, swap them
            if (compare(heap.get(current), heap.get(parent)) >= 0) {
                break;
            }
            swap(current, parent);
            // Update the current index
            current = parent;
        }
    }

    /**
     * Extracts the minimum element from the heap and returns it.
     * @return the minimum element from the heap, or null if the heap is empty
     */
    public Object extractMin() {
        if (heap.isEmpty()) {
            return null;
        }

        // Get the minimum element from the top of the heap
        Object min = heap.get(0);
        if (min instanceof GatorTicketMaster.WaitlistEntry) {
            // Remove the userID from the map
            idToPositionMap.remove(((GatorTicketMaster.WaitlistEntry) min).userID);
        }

        // Get the last element from the heap
        int lastIdx = heap.size() - 1;
        if (lastIdx > 0) {
            // Replace the minimum element with the last element
            heap.set(0, heap.get(lastIdx));
            if (heap.get(0) instanceof GatorTicketMaster.WaitlistEntry) {
                // Update the map to reflect the new position of the element
                idToPositionMap.put(((GatorTicketMaster.WaitlistEntry) heap.get(0)).userID, 0);
            }
        }

        // Remove the last element from the heap
        heap.remove(lastIdx);

        // If the heap is not empty, demote the new element to its correct position
        if (!heap.isEmpty()) {
            demoteElement(0);
        }

        return min;
    }

    /**
     * Demotes the element at the given index down the heap by swapping it with its smallest child if the child is smaller.
     * @param index the index of the element to demote
     */
    private void demoteElement(int index) {
        int heapSize = heap.size();
        while (true) {
            int smallest = index;
            int leftNode = 2 * index + 1;
            int rightNode = 2 * index + 2;

            // Find the smallest child of the current element
            if (leftNode < heapSize && compare(heap.get(leftNode), heap.get(smallest)) < 0) {
                smallest = leftNode;
            }

            if (rightNode < heapSize && compare(heap.get(rightNode), heap.get(smallest)) < 0) {
                smallest = rightNode;
            }

            // If the smallest child is the current element, break the loop
            if (smallest == index) {
                break;
            }

            // Swap the current element with its smallest child
            swap(index, smallest);
            // Update the current index
            index = smallest;
        }
    }

    /**
     * Compares two objects which can either be Integers or WaitlistEntry instances.
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
            idToPositionMap.put(((GatorTicketMaster.WaitlistEntry) heap.get(i)).userID, i);
        }
        if (heap.get(j) instanceof GatorTicketMaster.WaitlistEntry) {
            idToPositionMap.put(((GatorTicketMaster.WaitlistEntry) heap.get(j)).userID, j);
        }
    }

    /**
     * Removes the user with the given user ID from the heap.
     * If the user is found, it is removed from the heap and the map is updated.
     * @param userID the user ID of the user to remove
     * @return true if the user was found and removed, otherwise false
     */
    public boolean remove(int userID) {
        Integer index = idToPositionMap.get(userID);
        if (index == null) {
            return false;
        }

        // Swap the user to be removed with the last element in the heap
        int lastIdx = heap.size() - 1;
        swap(index, lastIdx);

        // Remove the last element from the heap
        heap.remove(lastIdx);

        // Remove the user from the map
        idToPositionMap.remove(userID);

        // If the user was not at the root, we need to fix the heap
        if (index < heap.size()) {
            int parent = (index - 1) / 2;
            // If the user was smaller than its parent, promote it
            if (index > 0 && compare(heap.get(index), heap.get(parent)) < 0) {
                promoteElement(index);
            }
            // Otherwise, demote it
            else {
                demoteElement(index);
            }
        }
        return true;
    }

    /**
     * Updates the priority of the user with the given user ID
     * @param userID the user ID of the user to update
     * @param newPriority the new priority of the user
     * @return true if the user was found and updated, otherwise false
     */
    public boolean updatePriority(int userID, int newPriority) {
        Integer index = idToPositionMap.get(userID);
        if (index == null) {
            return false;
        }

        GatorTicketMaster.WaitlistEntry entry = (GatorTicketMaster.WaitlistEntry) heap.get(index);
        int oldPriority = entry.priority;
        entry.priority = newPriority;

        // If the new priority is lower than the old priority, demote the element
        if (newPriority < oldPriority) {
            demoteElement(index);
        }
        // Otherwise, promote the element
        else {
            promoteElement(index);
        }
        return true;
    }

    /**
     * Checks if the heap is empty
     * @return true if the heap is empty, otherwise false
     */
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * Returns the number of elements currently in the heap.
     * @return the size of the heap
     */
    public int size() {
        return heap.size();
    }
}