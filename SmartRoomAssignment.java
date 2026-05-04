import java.util.Scanner;

// Smart Room Assignment Class - Intelligently recommends the best room
public class SmartRoomAssignment {

    private static final Repository repo = Repository.getInstance();

// Intelligently recommends the best room based on guest profile
    public static int recommendBestRoom(int customerID, int guests, String preferences, Scanner sc) {
        String membership = repo.getMembershipType(customerID);
        String customerName = repo.getCustomerName(customerID);
        int[] pastRooms = getGuestRoomHistory(customerID);

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                   SMART ROOM RECOMMENDATION                  ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Guest: %-52s ║%n", customerName);
        System.out.printf("║  Membership: %-47s ║%n", membership);
        System.out.printf("║  Guests: %-51d ║%n", guests);
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // Display guest history if available
        if (pastRooms.length > 0) {
            System.out.print("\nPrevious rooms: ");
            for (int room : pastRooms) {
                System.out.print(room + " ");
            }
            System.out.println();
        }

        int recommendedRoom = -1;

        // VIP guests get premium rooms
        if ("VIP".equals(membership)) {
            System.out.println("\n[VIP DETECTED] - Prioritizing premium accommodations");
            recommendedRoom = findBestAvailable("Suite", guests);
            if (recommendedRoom == -1) {
                recommendedRoom = findBestAvailable("Deluxe", guests);
            }
        }

        // Large groups need connecting/family rooms
        if (guests >= 4 && recommendedRoom == -1) {
            System.out.println("\n[LARGE GROUP] - Finding family/connecting rooms");
            recommendedRoom = findBestAvailable("Family", guests);
        }

        // Standard recommendation based on preferences
        if (recommendedRoom == -1) {
            System.out.println("\n[STANDARD] - Finding room based on preferences");
            if (preferences.contains("quiet")) {
                recommendedRoom = findQuietRoom(guests);
            } else if (preferences.contains("view")) {
                recommendedRoom = findRoomWithView(guests);
            } else {
                recommendedRoom = findBestAvailable("Standard", guests);
            }
        }

        // If guest stayed before, try same floor
        if (pastRooms.length > 0 && recommendedRoom == -1) {
            int preferredFloor = pastRooms[0] / 100;
            System.out.println("\n[RETURNING GUEST] - Preferring floor " + preferredFloor);
            recommendedRoom = findRoomOnFloor(preferredFloor, guests);
        }

        return recommendedRoom;
    }

// Interactive smart booking for customers
    public static void smartBookRoom(int customerID, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                     SMART ROOM BOOKING                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("\nWelcome, " + repo.getCustomerName(customerID));

        int guests = getValidIntInput(sc, "\nHow many guests? ", 1, 20);

        System.out.println("\nAny preferences?");
        System.out.println("[1] Quiet room (away from elevator)");
        System.out.println("[2] Great view (higher floor)");
        System.out.println("[3] Near elevator (convenience)");
        System.out.println("[4] No preference");
        int prefChoice = getValidIntInput(sc, "Choice: ", 1, 4);

        String[] preferences = {"quiet", "view", "elevator", "none"};
        String preference = preferences[prefChoice - 1];

        // Get smart recommendation
        int recommendedRoom = recommendBestRoom(customerID, guests, preference, sc);

        if (recommendedRoom == -1) {
            System.out.println("\nSorry, no suitable rooms available for your preferences.");
            System.out.println("Showing all available rooms instead...");
            FacilitySystem.bookRoomForCustomer(customerID, sc);
            return;
        }

        // Show recommendation details
        String[] roomDetails = repo.getRoomDetails(recommendedRoom);
        if (roomDetails == null) {
            System.out.println("\nError retrieving room details.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                      RECOMMENDED ROOM                        ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Room: %-54d ║%n", recommendedRoom);
        System.out.printf("║  Type: %-54s ║%n", roomDetails[1]);
        System.out.printf("║  Capacity: %-50s ║%n", roomDetails[2] + " guests");
        System.out.printf("║  Price: PHP %-47s ║%n", roomDetails[3] + "/night");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Why this room?                                              ║");

        String membership = repo.getMembershipType(customerID);
        if ("VIP".equals(membership)) {
            System.out.println("║  • Premium room for VIP guest                                ║");
        }
        if (guests >= 4) {
            System.out.println("║  • Spacious for your group size                              ║");
        }
        if (preference.equals("quiet")) {
            System.out.println("║  • Located in quiet zone                                     ║");
        } else if (preference.equals("view")) {
            System.out.println("║  • Higher floor with better view                             ║");
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        System.out.println("\n[1] Book this room");
        System.out.println("[2] See other options");
        System.out.println("[3] Cancel");
        int choice = getValidIntInput(sc, "Choice: ", 1, 3);

        switch (choice) {
            case 1:
                completeSmartBooking(customerID, recommendedRoom, guests, sc);
                break;
            case 2:
                FacilitySystem.bookRoomForCustomer(customerID, sc);
                break;
            case 3:
                System.out.println("\nBooking cancelled.");
                break;
        }
    }

// Complete the booking with smart recommendation
    private static void completeSmartBooking(int customerID, int roomNumber, int guests, Scanner sc) {
        System.out.print("\nEnter check-in date (YYYY-MM-DD): ");
        String checkInDate = sc.nextLine().trim();
        
        System.out.print("Enter check-out date (YYYY-MM-DD): ");
        String checkOutDate = sc.nextLine().trim();

        if (repo.bookRoom(roomNumber, customerID, checkInDate, checkOutDate, guests)) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║                  ROOM BOOKED SUCCESSFULLY!                   ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.printf("║  Your smart-selected room is: %-32d ║%n", roomNumber);
            System.out.println("╚══════════════════════════════════════════════════════════════╝");

            // Booking is automatically saved to tbl_roomBookings for future recommendations
        } else {
            System.out.println("\nFailed to book room. It may have been taken.");
        }
        
        pauseScreen(sc);
    }

//  Get guest's room history for recommendations
    private static int[] getGuestRoomHistory(int customerID) {
        return repo.getGuestRoomHistory(customerID);
    }

// Find best available room by type
    private static int findBestAvailable(String roomType, int guests) {
        String[][] rooms = repo.getAvailableRoomsByType(roomType);
        if (rooms == null) return -1;

        for (String[] room : rooms) {
            int capacity = Integer.parseInt(room[2]);
            if (capacity >= guests) {
                return Integer.parseInt(room[0]);
            }
        }
        return -1;
    }

// Find a quiet room (lower floor, away from elevators)
    private static int findQuietRoom(int guests) {
        // Prefer rooms ending in 01-08 (away from elevator usually at ends)
        String[][] rooms = repo.getAvailableRooms();
        if (rooms == null) return -1;

        for (String[] room : rooms) {
            int roomNum = Integer.parseInt(room[0]);
            int capacity = Integer.parseInt(room[2]);
            int roomEnding = roomNum % 100;

            // Prefer lower floor, middle rooms (quieter)
            if (capacity >= guests && roomEnding >= 2 && roomEnding <= 6 && roomNum < 400) {
                return roomNum;
            }
        }
        // Fallback to any available
        return findBestAvailable("Standard", guests);
    }

// Find room with good view (higher floor)
    private static int findRoomWithView(int guests) {
        String[][] rooms = repo.getAvailableRooms();
        if (rooms == null) return -1;

        int highestFloor = 0;
        int bestRoom = -1;

        for (String[] room : rooms) {
            int roomNum = Integer.parseInt(room[0]);
            int capacity = Integer.parseInt(room[2]);
            int floor = roomNum / 100;

            if (capacity >= guests && floor > highestFloor) {
                highestFloor = floor;
                bestRoom = roomNum;
            }
        }
        return bestRoom;
    }

// Find room on specific floor
    private static int findRoomOnFloor(int floor, int guests) {
        String[][] rooms = repo.getAvailableRooms();
        if (rooms == null) return -1;

        for (String[] room : rooms) {
            int roomNum = Integer.parseInt(room[0]);
            int capacity = Integer.parseInt(room[2]);
            int roomFloor = roomNum / 100;

            if (roomFloor == floor && capacity >= guests) {
                return roomNum;
            }
        }
        return -1;
    }

// Admin: View smart assignment analytics
    public static void showAssignmentAnalytics() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                  SMART ASSIGNMENT ANALYTICS                  ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int totalBookings = repo.getTotalRoomBookings();
        double smartRate = totalBookings > 0 ? 100.0 : 0; // All bookings through this system are "smart"

        System.out.printf("\n  Total Bookings: %d%n", totalBookings);
        System.out.printf("  Smart Booking Rate: %.1f%%%n", smartRate);

        System.out.println("\n  Top Preferred Room Types:");
        String[][] topTypes = repo.getTopRoomTypes();
        if (topTypes != null) {
            for (int i = 0; i < topTypes.length && i < 5; i++) {
                System.out.printf("    %d. %s - %s bookings%n", i + 1, topTypes[i][0], topTypes[i][1]);
            }
        }
    }

    // Helper methods
    
    private static int getValidIntInput(Scanner sc, String prompt, int min, int max) {
        System.out.print(prompt);
        int input = -1;
        boolean valid = false;
        
        while (!valid) {
            try {
                input = sc.nextInt();
                sc.nextLine();
                
                if (input >= min && input <= max) {
                    valid = true;
                } else {
                    System.out.print("[!] Please enter a number between " + min + " and " + max + ": ");
                }
            } catch (Exception e) {
                System.out.print("[!] Invalid input. Please enter a number: ");
                sc.nextLine();
            }
        }
        
        return input;
    }

    private static void pauseScreen(Scanner sc) {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
}
