import java.io.*;
import java.util.*;

class GatorTicketMaster {
    private RedBlackTree reservations;
    private MinHeap waitlist;
    private MinHeap availableSeats;
    private int totalSeats;
    private static PrintWriter outputWriter;
    
        public GatorTicketMaster(String outputFile) throws IOException {
            reservations = new RedBlackTree();
            waitlist = new MinHeap();
            availableSeats = new MinHeap();
            outputWriter = new PrintWriter(new FileWriter(outputFile));
        }
    
        // Node class for waitlist entries
        static class WaitlistEntry implements Comparable<WaitlistEntry> {
            int userID;
            int priority;
            long timestamp;
    
            public WaitlistEntry(int userID, int priority) {
                this.userID = userID;
                this.priority = priority;
                this.timestamp = System.nanoTime();
            }
    
            @Override
            public int compareTo(WaitlistEntry other) {
                if (this.priority != other.priority) {
                    return other.priority - this.priority; // Higher priority first
                }
                return Long.compare(this.timestamp, other.timestamp); // Earlier timestamp first
            }
        }
    
        public void initialize(int seatCount) {
            if (seatCount <= 0) {
                outputWriter.println("Invalid input. Please provide a valid number of seats.");
                return;
            }
            
            totalSeats = seatCount;
            for (int i = 1; i <= seatCount; i++) {
                availableSeats.insert(i);
            }
            outputWriter.println(seatCount + " Seats are made available for reservation");
        }
    
        public void available() {
            outputWriter.println("Total Seats Available : " + availableSeats.size() + ", Waitlist : " + waitlist.size());
        }
    
        public void reserve(int userID, int userPriority) {
            if (!availableSeats.isEmpty()) {
                int seatID = (int) availableSeats.extractMin();
                reservations.insert(userID, seatID);
                outputWriter.println("User " + userID + " reserved seat " + seatID);
            } else {
                waitlist.insert(new WaitlistEntry(userID, userPriority));
                outputWriter.println("User " + userID + " is added to the waiting list");
            }
        }
    
        public void cancel(int seatID, int userID) {
            RedBlackTree.Node node = reservations.find(userID);
            if (node == null) {
                outputWriter.println("User " + userID + " has no reservation to cancel");
                return;
            }
            
            if (node.seatID != seatID) {
                outputWriter.println("User " + userID + " has no reservation for seat " + seatID + " to cancel");
                return;
            }
    
            reservations.delete(userID);
            outputWriter.println("User " + userID + " canceled their reservation");
    
            if (!waitlist.isEmpty()) {
                WaitlistEntry entry = (WaitlistEntry) waitlist.extractMin();
                reservations.insert(entry.userID, seatID);
                outputWriter.println("User " + entry.userID + " reserved seat " + seatID);
            } else {
                availableSeats.insert(seatID);
            }
        }
    
        public void exitWaitlist(int userID) {
            if (waitlist.remove(userID)) {
                outputWriter.println("User " + userID + " is removed from the waiting list");
            } else {
                outputWriter.println("User " + userID + " is not in waitlist");
            }
        }
    
        public void updatePriority(int userID, int newPriority) {
            if (waitlist.updatePriority(userID, newPriority)) {
                outputWriter.println("User " + userID + " priority has been updated to " + newPriority);
            } else {
                outputWriter.println("User " + userID + " priority is not updated");
            }
        }
    
        public void addSeats(int count) {
            if (count <= 0) {
                outputWriter.println("Invalid input. Please provide a valid number of seats.");
                return;
            }
    
            outputWriter.println("Additional " + count + " Seats are made available for reservation");
            
            int startSeat = totalSeats + 1;
            // System.out.println("totalSeats: " + totalSeats);
            // System.out.println("count: " + count);
            totalSeats += count;
    
            for (int i = startSeat; i <= totalSeats; i++) {
                if (!waitlist.isEmpty()) {
                    WaitlistEntry entry = (WaitlistEntry) waitlist.extractMin();
                    // System.out.println("entry: " + entry.userID);
                    reservations.insert(entry.userID, i);
                    outputWriter.println("User " + entry.userID + " reserved seat " + i);
                } else {
                    availableSeats.insert(i);
                }
            }
        }
    
        public void printReservations() {
            List<RedBlackTree.Node> nodes = reservations.inorderTraversal();
            nodes.sort((a, b) -> Integer.compare(a.seatID, b.seatID));
            for (RedBlackTree.Node node : nodes) {
                outputWriter.println("Seat " + node.seatID + ", User " + node.userID);
            }
        }
    
        public void releaseSeats(int userID1, int userID2) {
            if (userID1 > userID2) {
                outputWriter.println("Invalid input. Please provide a valid range of users.");
                return;
            }
    
            List<Integer> releasedSeats = new ArrayList<>();
    
            // Collect all seats that will be released
            for (int userID = userID1; userID <= userID2; userID++) {
                RedBlackTree.Node node = reservations.find(userID);
                if (node != null) {
                    releasedSeats.add(node.seatID);
                    reservations.delete(userID);
                }
                waitlist.remove(userID);
            }
    
            // Waitlist is empty
            if (waitlist.isEmpty()) {
                outputWriter.println("Reservations/waitlist of the users in the range [" + userID1 + ", " + userID2 + "] have been released");
    
                // Add released seats back to available seats
                for (int seatID : releasedSeats) {
                    availableSeats.insert(seatID);
                }
            } else { // Waitlist is not empty
                outputWriter.println("Reservations of the Users in the range [" + userID1 + ", " + userID2 + "] are released");
    
                for (int seatID : releasedSeats) {
                    if (!waitlist.isEmpty()) {
                        WaitlistEntry entry = (WaitlistEntry) waitlist.extractMin();
                        reservations.insert(entry.userID, seatID);
                        outputWriter.println("User " + entry.userID + " reserved seat " + seatID);
                    } else {
                        availableSeats.insert(seatID);
                    }
                }
            }
        }
    
        //Terminates the program and closes the output writer.
        public void quit() {
            outputWriter.print("Program Terminated!!");
            outputWriter.close();
        }
    
        public static void main(String[] args) {
            if (args.length != 1) {
                System.out.println("Usage: java GatorTicketMaster <input_file>");
                return;
            }
    
            String inputFile = args[0];
            String outputFile = inputFile.substring(0, inputFile.lastIndexOf('.')) + "_output_file.txt";
    
            try {
                GatorTicketMaster ticketMaster = new GatorTicketMaster(outputFile);
                BufferedReader reader = new BufferedReader(new FileReader(inputFile));
                String line;
    
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.split("[(),]");
                    String command = parts[0].trim();
    
                    switch (command) {
                        case "Initialize":
                            ticketMaster.initialize(Integer.parseInt(parts[1].trim()));
                            break;
                        case "Available":
                            ticketMaster.available();
                            break;
                        case "Reserve":
                            ticketMaster.reserve(
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim())
                            );
                            break;
                        case "Cancel":
                            ticketMaster.cancel(
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim())
                            );
                            break;
                        case "ExitWaitlist":
                            ticketMaster.exitWaitlist(Integer.parseInt(parts[1].trim()));
                            break;
                        case "UpdatePriority":
                            ticketMaster.updatePriority(
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim())
                            );
                            break;
                        case "AddSeats":
                            ticketMaster.addSeats(Integer.parseInt(parts[1].trim()));
                            break;
                        case "PrintReservations":
                            ticketMaster.printReservations();
                            break;
                        case "ReleaseSeats":
                            ticketMaster.releaseSeats(
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim())
                            );
                            break;
                        case "Quit":
                            ticketMaster.quit();
                            return;
                    }
                }
                //ticketMaster.quit();
                outputWriter.close(); // Closing the output writer
            reader.close();
        } catch (IOException e) { // Error handling
            System.err.println("Error processing file: " + e.getMessage());
        }
    }
}

// Red-Black Tree implementation
class RedBlackTree {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    static class Node {
        int userID;
        int seatID;
        boolean color;
        Node left, right, parent;

        Node(int userID, int seatID) {
            this.userID = userID;
            this.seatID = seatID;
            this.color = RED;
        }
    }

    private Node root;

    private void rotateLeft(Node node) {
        Node rightChild = node.right;
        node.right = rightChild.left;
        
        if (rightChild.left != null) {
            rightChild.left.parent = node;
        }
        
        rightChild.parent = node.parent;
        
        if (node.parent == null) {
            root = rightChild;
        } else if (node == node.parent.left) {
            node.parent.left = rightChild;
        } else {
            node.parent.right = rightChild;
        }
        
        rightChild.left = node;
        node.parent = rightChild;
    }

    private void rotateRight(Node node) {
        Node leftChild = node.left;
        node.left = leftChild.right;
        
        if (leftChild.right != null) {
            leftChild.right.parent = node;
        }
        
        leftChild.parent = node.parent;
        
        if (node.parent == null) {
            root = leftChild;
        } else if (node == node.parent.right) {
            node.parent.right = leftChild;
        } else {
            node.parent.left = leftChild;
        }
        
        leftChild.right = node;
        node.parent = leftChild;
    }

    public void insert(int userID, int seatID) {
        Node node = new Node(userID, seatID);
        Node parent = null;
        Node current = root;

        while (current != null) {
            parent = current;
            if (userID < current.userID) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        node.parent = parent;
        
        if (parent == null) {
            root = node;
        } else if (userID < parent.userID) {
            parent.left = node;
        } else {
            parent.right = node;
        }

        fixInsert(node);
    }

    private void fixInsert(Node node) {
        while (node != root && node.parent.color == RED) {
            if (node.parent == node.parent.parent.left) {
                Node uncle = node.parent.parent.right;
                
                if (uncle != null && uncle.color == RED) {
                    node.parent.color = BLACK;
                    uncle.color = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.right) {
                        node = node.parent;
                        rotateLeft(node);
                    }
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    rotateRight(node.parent.parent);
                }
            } else {
                Node uncle = node.parent.parent.left;
                
                if (uncle != null && uncle.color == RED) {
                    node.parent.color = BLACK;
                    uncle.color = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.left) {
                        node = node.parent;
                        rotateRight(node);
                    }
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    rotateLeft(node.parent.parent);
                }
            }
        }
        root.color = BLACK;
    }

    public Node find(int userID) {
        Node current = root;
        while (current != null) {
            if (userID == current.userID) {
                return current;
            }
            current = userID < current.userID ? current.left : current.right;
        }
        return null;
    }

    public void delete(int userID) {
        Node node = find(userID);
        if (node == null) return;

        Node x, y;
        if (node.left == null || node.right == null) {
            y = node;
        } else {
            y = successor(node);
        }

        if (y.left != null) {
            x = y.left;
        } else {
            x = y.right;
        }

        if (x != null) {
            x.parent = y.parent;
        }

        if (y.parent == null) {
            root = x;
        } else if (y == y.parent.left) {
            y.parent.left = x;
        } else {
            y.parent.right = x;
        }

        if (y != node) {
            node.userID = y.userID;
            node.seatID = y.seatID;
        }

        if (y.color == BLACK) {
            fixDelete(x, y.parent);
        }
    }

    private Node successor(Node node) {
        if (node.right != null) {
            node = node.right;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }

        Node parent = node.parent;
        while (parent != null && node == parent.right) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    private void fixDelete(Node node, Node parent) {
        while (node != root && (node == null || node.color == BLACK)) {
            if (node == parent.left) {
                Node sibling = parent.right;
                
                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    parent.color = RED;
                    rotateLeft(parent);
                    sibling = parent.right;
                }

                if ((sibling.left == null || sibling.left.color == BLACK) &&
                    (sibling.right == null || sibling.right.color == BLACK)) {
                    sibling.color = RED;
                    node = parent;
                    parent = node.parent;
                } else {
                    if (sibling.right == null || sibling.right.color == BLACK) {
                        if (sibling.left != null) {
                            sibling.left.color = BLACK;
                        }
                        sibling.color = RED;
                        rotateRight(sibling);
                        sibling = parent.right;
                    }
                    sibling.color = parent.color;
                    parent.color = BLACK;
                    if (sibling.right != null) {
                        sibling.right.color = BLACK;
                    }
                    rotateLeft(parent);
                    node = root;
                }
            } else {
                Node sibling = parent.left;
                
                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    parent.color = RED;
                    rotateRight(parent);
                    sibling = parent.left;
                }

                if ((sibling.right == null || sibling.right.color == BLACK) &&
                    (sibling.left == null || sibling.left.color == BLACK)) {
                    sibling.color = RED;
                    node = parent;
                    parent = node.parent;
                } else {
                    if (sibling.left == null || sibling.left.color == BLACK) {
                        if (sibling.right != null) {
                            sibling.right.color = BLACK;
                        }
                        sibling.color = RED;
                        rotateLeft(sibling);
                        sibling = parent.left;
                    }
                    sibling.color = parent.color;
                    parent.color = BLACK;
                    if (sibling.left != null) {
                        sibling.left.color = BLACK;
                    }
                    rotateRight(parent);
                    node = root;
                }
            }
        }
        if (node != null) {
            node.color = BLACK;
        }
    }

    public List<Node> inorderTraversal() {
        List<Node> result = new ArrayList<>();
        inorderTraversalHelper(root, result);
        return result;
    }

    private void inorderTraversalHelper(Node node, List<Node> result) {
        if (node != null) {
            inorderTraversalHelper(node.left, result);
            result.add(node);
            inorderTraversalHelper(node.right, result);
        }
    }
}

// Min Heap implementation
class MinHeap {
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