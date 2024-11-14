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

    /**
     * Initialize the ticketing system with the given number of seats.
     * @param seatCount the number of seats to initialize the system with
     */
    public void initialize(int seatCount) {
        if (seatCount <= 0) {
            outputWriter.println("Invalid input. Please provide a valid number of seats.");
            return;
        }

        totalSeats = seatCount;
        for (int i = 1; i <= seatCount; i++) {
            // Add each seat to the heap
            availableSeats.insert(i);
        }
        outputWriter.println(seatCount + " Seats are made available for reservation");
    }

    /**
     * Prints the number of seats available and the number of users in the waitlist
     */
    public void available() {
        outputWriter.println("Total Seats Available : " + availableSeats.size() + ", Waitlist : " + waitlist.size());
    }

    /**
     * Reserve a seat for the given user
     * @param userID the ID of the user to reserve a seat for
     * @param userPriority the priority of the user
     */
    public void reserve(int userID, int userPriority) {
        // If there are available seats, assign one to the user
        if (!availableSeats.isEmpty()) {
            int seatID = (int) availableSeats.extractMin();
            reservations.insert(userID, seatID);
            outputWriter.println("User " + userID + " reserved seat " + seatID);
        } else {
            // Otherwise, add the user to the waitlist
            waitlist.insert(new WaitlistEntry(userID, userPriority));
            outputWriter.println("User " + userID + " is added to the waiting list");
        }
    }

    /**
     * Cancel the reservation of the user for the given seat
     * If the user has no reservation, print an error message
     * @param seatID the ID of the seat to cancel the reservation
     * @param userID the ID of the user to cancel the reservation
     */
    public void cancel(int seatID, int userID) {
        // Find the seat with the given userID
        RedBlackTree.Node node = reservations.findNode(userID);

        // User has no reservation to cancel
        if (node == null) {
            outputWriter.println("User " + userID + " has no reservation to cancel");
            return;
        }

        // User has no reservation for the given seat to cancel
        if (node.seatID != seatID) {
            outputWriter.println("User " + userID + " has no reservation for seat " + seatID + " to cancel");
            return;
        }

        // Delete the seat from the tree
        reservations.deleteNode(userID);
        outputWriter.println("User " + userID + " canceled their reservation");

        // If there are users in the waitlist, assign the new seat to the user with the highest priority
        if (!waitlist.isEmpty()) {
            WaitlistEntry entry = (WaitlistEntry) waitlist.extractMin();
            reservations.insert(entry.userID, seatID);
            outputWriter.println("User " + entry.userID + " reserved seat " + seatID);
        } else {
            // Otherwise, add the seat back to the available seats
            availableSeats.insert(seatID);
        }
    }

    /**
     * Remove the user from the waitlist
     * @param userID the ID of the user to remove
     */
    public void exitWaitlist(int userID) {
        if (waitlist.remove(userID)) {
            outputWriter.println("User " + userID + " is removed from the waiting list");
        } else {
            outputWriter.println("User " + userID + " is not in waitlist");
        }
    }

    /**
     * Update the priority of the user
     * @param userID the ID of the user to update
     * @param newPriority the new priority of the user
     */
    public void updatePriority(int userID, int newPriority) {
        if (waitlist.updatePriority(userID, newPriority)) {
            outputWriter.println("User " + userID + " priority has been updated to " + newPriority);
        } else {
            outputWriter.println("User " + userID + " priority is not updated");
        }
    }

    /**
     * Add the specified number of seats to the total count of available seats
     * If there are users in the waitlist, assign the new seats to the users with the highest priority
     * If there are no users in the waitlist, add the new seats to the available seats
     * @param count the number of seats to add
     */
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

        // Loop through the new seats and assign them to the highest priority users in the waitlist
        // If there are no users in the waitlist, add the seats to the available seats
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

    /**
     * Prints the current reservations in the system, sorted by seat ID
     */
    public void printReservations() {
        // Get the list of reservations from the Red Black Tree
        List<RedBlackTree.Node> nodes = reservations.inorderTraversal();
        
        // Sort the list by seat ID
        nodes.sort((a, b) -> Integer.compare(a.seatID, b.seatID));
        
        // Print the sorted list
        for (RedBlackTree.Node node : nodes) {
            outputWriter.println("Seat " + node.seatID + ", User " + node.userID);
        }
    }

    /**
     * Releases the reservations of the users in the range [userID1, userID2]
     * If the waitlist is empty, the released seats are added back to the available seats
     * Otherwise, the released seats are assigned to the users with the highest priority in the waitlist
     * @param userID1 the start of the user range
     * @param userID2 the end of the user range
     */
    public void releaseSeats(int userID1, int userID2) {
        if (userID1 > userID2) {
            outputWriter.println("Invalid input. Please provide a valid range of users.");
            return;
        }

        List<Integer> releasedSeats = new ArrayList<>();

        // Collect all seats that will be released
        for (int userID = userID1; userID <= userID2; userID++) {
            RedBlackTree.Node node = reservations.findNode(userID);
            if (node != null) {
                releasedSeats.add(node.seatID);
                reservations.deleteNode(userID);
            }

            // Remove the user from the waitlist
            waitlist.remove(userID);
        }

        // Waitlist is empty
        if (waitlist.isEmpty()) {
            outputWriter.println("Reservations/waitlist of the users in the range [" + userID1 + ", " + userID2+ "] have been released");

            // Add released seats back to available seats
            for (int seatID : releasedSeats) {
                availableSeats.insert(seatID);
            }
        } else { // Waitlist is not empty
            outputWriter
                    .println("Reservations of the Users in the range [" + userID1 + ", " + userID2 + "] are released");

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

    /**
     * Terminates the program and closes the output writer.
     * This method ensures that all resources are properly released and the output file is closed before the program exits.
     */
    public void quit() {
        // Print termination message
        outputWriter.print("Program Terminated!!");
        
        // Close the output writer to release resources
        outputWriter.close();
    }

    /**
     * Main method for running the program from the command line.
     * @param args arguments passed to the program from the command line
     */
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

            // Read the input file line by line
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("[(),]");
                String command = parts[0].trim();

                // Process the command
                switch (command) {
                    case "Initialize":
                        // Initialize the ticketing system with the given number of seats
                        ticketMaster.initialize(Integer.parseInt(parts[1].trim()));
                        break;
                    case "Available":
                        // Print the number of available seats and the number of users in the waitlist
                        ticketMaster.available();
                        break;
                    case "Reserve":
                        // Reserve a seat for the given user
                        ticketMaster.reserve(
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim()));
                        break;
                    case "Cancel":
                        // Cancel the reservation for the given user
                        ticketMaster.cancel(
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim()));
                        break;
                    case "ExitWaitlist":
                        // Remove the user from the waitlist
                        ticketMaster.exitWaitlist(Integer.parseInt(parts[1].trim()));
                        break;
                    case "UpdatePriority":
                        // Update the priority of the user
                        ticketMaster.updatePriority(
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim()));
                        break;
                    case "AddSeats":
                        // Add the specified number of seats to the total count of available seats
                        ticketMaster.addSeats(Integer.parseInt(parts[1].trim()));
                        break;
                    case "PrintReservations":
                        // Print the current reservations in the system, sorted by seat ID
                        ticketMaster.printReservations();
                        break;
                    case "ReleaseSeats":
                        ticketMaster.releaseSeats(
                        // Release the seats reserved by users in the given range
                                Integer.parseInt(parts[1].trim()),
                                Integer.parseInt(parts[2].trim()));
                        break;
                    case "Quit":
                        ticketMaster.quit();
                        // Terminate the program
                        return;
                }
            }
            // ticketMaster.quit();
            // Close the output writer to release resources
            outputWriter.close();
            reader.close();
        } catch (IOException e) { // Error handling
            System.err.println("Error processing the file: " + e.getMessage());
        }
    }
}
