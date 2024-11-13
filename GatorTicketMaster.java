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
