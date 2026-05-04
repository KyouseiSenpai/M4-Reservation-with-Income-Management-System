import java.util.Scanner;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Facility System Class - Manages rooms, housekeeping, maintenance, and facilities
 * 
 */
public class FacilitySystem {

    private static final Repository repo = Repository.getInstance();
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Show facility menu for admin
     */
    public static void showFacilityMenu(Scanner sc) {

        int choice;
        boolean inMenu = true;

        do {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         🏨 FACILITY & HOUSEKEEPING (MILESTONE 3)             ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] Room Management                                         ║");
            System.out.println("║  [2] Housekeeping Management                                 ║");
            System.out.println("║  [3] Maintenance Management                                  ║");
            System.out.println("║  [4] Facility Management                                     ║");
            System.out.println("║  [5] Predictive Maintenance                                  ║");
            System.out.println("║  [6] Lost & Found Center                                     ║");
            System.out.println("║  [7] Room Status Dashboard                                   ║");
            System.out.println("║  [0] Back to Staff Dashboard                                 ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            choice = getValidIntInput(sc, "Enter your choice: ", 0, 7);

            switch (choice) {
                case 1:
                    roomManagement(sc);
                    break;
                case 2:
                    housekeepingManagement(sc);
                    break;
                case 3:
                    maintenanceManagement(sc);
                    break;
                case 4:
                    facilityManagement(sc);
                    break;
                case 5:
                    showPredictiveMaintenance(sc);
                    break;
                case 6:
                    LostFoundSystem.showLostFoundMenu(sc);
                    break;
                case 7:
                    RoomControlSystem.showAllRoomStatuses();
                    pauseScreen(sc);
                    break;
                case 0:
                    System.out.println("\nReturning to Staff Dashboard...");
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }
        } while (inMenu);
    }

    // ============ ROOM MANAGEMENT ============

    private static void roomManagement(Scanner sc) {
        int choice;
        boolean inMenu = true;

        do {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              🏨 ROOM MANAGEMENT                              ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] View All Rooms                                          ║");
            System.out.println("║  [2] View Available Rooms                                    ║");
            System.out.println("║  [3] View Room Details                                       ║");
            System.out.println("║  [4] Check-in Guest (Walk-in)                                ║");
            System.out.println("║  [5] Check-out Guest                                         ║");
            System.out.println("║  [6] Mark Room as Cleaned                                    ║");
            System.out.println("║  [7] Smart Room Assignment                                   ║");
            System.out.println("║  [0] Back                                                    ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            choice = getValidIntInput(sc, "Enter your choice: ", 0, 7);

            switch (choice) {
                case 1:
                    viewAllRooms();
                    pauseScreen(sc);
                    break;
                case 2:
                    viewAvailableRooms();
                    pauseScreen(sc);
                    break;
                case 3:
                    viewRoomDetails(sc);
                    break;
                case 4:
                    checkInGuest(sc);
                    break;
                case 5:
                    checkOutGuest(sc);
                    break;
                case 6:
                    markRoomCleaned(sc);
                    break;
                case 7:
                    showSmartAssignmentMenu(sc);
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }
        } while (inMenu);
    }

    private static void viewAllRooms() {
        String[][] rooms = repo.getAllRooms();
        if (rooms == null || rooms.length == 0) {
            System.out.println("\n❌ No rooms found.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         ALL ROOMS                                      ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-12s │ %-10s │ %-15s │ %-12s ║%n",
                "Room #", "Type", "Capacity", "Price/Night", "Status");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        for (String[] room : rooms) {
            System.out.printf("║ %-8s │ %-12s │ %-10s │ PHP %-12s │ %-12s ║%n",
                    room[0], room[1], room[2], room[3], room[4]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
    }

    private static void viewAvailableRooms() {
        String[][] rooms = repo.getAvailableRooms();
        if (rooms == null || rooms.length == 0) {
            System.out.println("\n❌ No available rooms at the moment.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    AVAILABLE ROOMS                             ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-12s │ %-10s │ %-15s ║%n",
                "Room #", "Type", "Capacity", "Price/Night");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");

        for (String[] room : rooms) {
            System.out.printf("║ %-8s │ %-12s │ %-10s │ PHP %-12s ║%n",
                    room[0], room[1], room[2], room[3]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════╝");
    }

    private static void viewRoomDetails(Scanner sc) {
        int roomNumber = getValidIntInput(sc, "\nEnter room number: ", 100, 999);

        String[] room = repo.getRoomDetails(roomNumber);
        if (room == null) {
            System.out.println("\n❌ Room not found.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                  🏨 ROOM DETAILS                             ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Room Number   : %-45s ║%n", room[0]);
        System.out.printf("║  Room Type     : %-45s ║%n", room[1]);
        System.out.printf("║  Capacity      : %-45s ║%n", room[2] + " guests");
        System.out.printf("║  Price/Night   : PHP %-41s ║%n", room[3]);
        System.out.printf("║  Status        : %-45s ║%n", room[4]);
        System.out.printf("║  Current Guest : %-45s ║%n", room[5]);
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        pauseScreen(sc);
    }

    private static void checkInGuest(Scanner sc) {
        int customerID = getValidIntInput(sc, "\nEnter Customer ID: ", 10000, 99999);

        String name = repo.getCustomerName(customerID);
        if ("Unknown".equals(name)) {
            System.out.println("\n❌ Customer not found.");
            return;
        }

        System.out.println("\nCustomer: " + name);
        viewAvailableRooms();

        int roomNumber = getValidIntInput(sc, "\nEnter room number to book: ", 100, 999);

        String checkInDate = getValidDate(sc, "Enter check-in date (YYYY-MM-DD): ");
        if (checkInDate == null) return;

        String checkOutDate = getValidDate(sc, "Enter check-out date (YYYY-MM-DD): ");
        if (checkOutDate == null) return;

        // Validate dates
        if (!validateDates(checkInDate, checkOutDate)) return;

        int guests = getValidIntInput(sc, "Enter number of guests: ", 1, 20);

        if (repo.bookRoom(roomNumber, customerID, checkInDate, checkOutDate, guests)) {
            long nights = ChronoUnit.DAYS.between(
                    LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate));
            String[] room = repo.getRoomDetails(roomNumber);
            double pricePerNight = Double.parseDouble(room[3]);
            double totalCost = nights * pricePerNight;

            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              ✅ ROOM BOOKED SUCCESSFULLY!                    ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.printf("║  Room: %-54s ║%n", roomNumber + " (" + room[1] + ")");
            System.out.printf("║  Check-in: %-50s ║%n", checkInDate);
            System.out.printf("║  Check-out: %-49s ║%n", checkOutDate);
            System.out.printf("║  Nights: %-52d ║%n", nights);
            System.out.printf("║  Total Cost: PHP %-43.2f ║%n", totalCost);
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        } else {
            System.out.println("\n❌ Failed to book room. Room may not be available.");
        }
        
        pauseScreen(sc);
    }

    private static void checkOutGuest(Scanner sc) {
        int roomNumber = getValidIntInput(sc, "\nEnter room number to check out: ", 100, 999);

        String[] room = repo.getRoomDetails(roomNumber);
        if (room == null) {
            System.out.println("\n❌ Room not found.");
            return;
        }

        if (!"OCCUPIED".equals(room[4])) {
            System.out.println("\n❌ Room is not occupied.");
            return;
        }

        System.out.print("\nConfirm check-out for room " + roomNumber + "? [1] Yes [2] No: ");
        int confirm = getValidIntInput(sc, "", 1, 2);

        if (confirm != 1) {
            System.out.println("\nCheck-out cancelled.");
            return;
        }

        if (repo.checkOutRoom(roomNumber)) {
            System.out.println("\n✅ Check-out successful!");
            System.out.println("Room " + roomNumber + " is now scheduled for cleaning.");
            repo.assignCleaningTask(roomNumber, "Auto-Assigned", "HIGH");
            System.out.println("Cleaning task has been automatically assigned.");

            System.out.print("\nCheck for left items? [1] Yes [2] No: ");
            int check = getValidIntInput(sc, "", 1, 2);
            if (check == 1) {
                LostFoundSystem.quickCheckDuringCleaning(roomNumber, "Checkout Staff", sc);
            }
        } else {
            System.out.println("\n❌ Failed to check out room.");
        }
        
        pauseScreen(sc);
    }

    private static void markRoomCleaned(Scanner sc) {
        int roomNumber = getValidIntInput(sc, "\nEnter room number that has been cleaned: ", 100, 999);

        if (repo.setRoomCleaned(roomNumber)) {
            System.out.println("\n✅ Room " + roomNumber + " is now available for booking.");
        } else {
            System.out.println("\n❌ Failed to update room status. Room may not be in CLEANING status.");
        }
        
        pauseScreen(sc);
    }

    private static void showSmartAssignmentMenu(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🤖 SMART ROOM ASSIGNMENT                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        int customerID = getValidIntInput(sc, "\nEnter Customer ID: ", 10000, 99999);

        String name = repo.getCustomerName(customerID);
        if ("Unknown".equals(name)) {
            System.out.println("\n❌ Customer not found.");
            return;
        }

        int guests = getValidIntInput(sc, "Number of guests: ", 1, 20);

        System.out.println("\nPreferences:");
        System.out.println("[1] Quiet room");
        System.out.println("[2] Great view");
        System.out.println("[3] Near elevator");
        System.out.println("[4] No preference");
        int pref = getValidIntInput(sc, "Choice: ", 1, 4);

        String[] prefs = {"quiet", "view", "elevator", "none"};
        int recommended = SmartRoomAssignment.recommendBestRoom(customerID, guests, prefs[pref - 1], sc);

        if (recommended != -1) {
            System.out.println("\n✅ Recommended Room: " + recommended);
            System.out.print("\nBook this room? [1] Yes [2] No: ");
            int book = getValidIntInput(sc, "", 1, 2);

            if (book == 1) {
                String checkIn = getValidDate(sc, "Check-in date (YYYY-MM-DD): ");
                if (checkIn == null) return;

                String checkOut = getValidDate(sc, "Check-out date (YYYY-MM-DD): ");
                if (checkOut == null) return;

                if (!validateDates(checkIn, checkOut)) return;

                if (repo.bookRoom(recommended, customerID, checkIn, checkOut, guests)) {
                    System.out.println("\n✅ Room " + recommended + " booked successfully!");
                } else {
                    System.out.println("\n❌ Failed to book room.");
                }
            }
        } else {
            System.out.println("\n❌ No suitable rooms available for your preferences.");
        }
        
        pauseScreen(sc);
    }

    // ============ HOUSEKEEPING MANAGEMENT ============

    private static void housekeepingManagement(Scanner sc) {
        int choice;
        boolean inMenu = true;

        do {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         🧹 HOUSEKEEPING MANAGEMENT                           ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] View Pending Cleaning Tasks                             ║");
            System.out.println("║  [2] View Completed Tasks                                    ║");
            System.out.println("║  [3] Assign Cleaning Task                                    ║");
            System.out.println("║  [4] Complete Cleaning Task                                  ║");
            System.out.println("║  [5] Real-Time Dashboard                                     ║");
            System.out.println("║  [0] Back                                                    ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            choice = getValidIntInput(sc, "Enter your choice: ", 0, 5);

            switch (choice) {
                case 1:
                    viewCleaningTasks("PENDING", sc);
                    break;
                case 2:
                    viewCleaningTasks("COMPLETED", sc);
                    break;
                case 3:
                    assignCleaningTask(sc);
                    break;
                case 4:
                    completeCleaningTask(sc);
                    break;
                case 5:
                    showHousekeepingDashboard();
                    pauseScreen(sc);
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }
        } while (inMenu);
    }

    private static void viewCleaningTasks(String status, Scanner sc) {
        String[][] tasks = repo.getCleaningTasks(status);
        if (tasks == null || tasks.length == 0) {
            System.out.println("\nℹ️  No " + status.toLowerCase() + " tasks found.");
            pauseScreen(sc);
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║                    %s CLEANING TASKS%n", status.toUpperCase());
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-10s │ %-12s │ %-15s │ %-12s │ %-10s ║%n",
                "Task ID", "Room #", "Room Type", "Staff", "Date", "Priority");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        for (String[] task : tasks) {
            System.out.printf("║ %-8s │ %-10s │ %-12s │ %-15s │ %-12s │ %-10s ║%n",
                    task[0], task[1], task[2], task[3], task[4], task[5]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
        
        pauseScreen(sc);
    }

    private static void assignCleaningTask(Scanner sc) {
        int roomNumber = getValidIntInput(sc, "\nEnter room number: ", 100, 999);

        System.out.print("Enter staff name: ");
        String staffName = sc.nextLine().trim();
        
        while (staffName.isEmpty()) {
            System.out.print("[!] Staff name cannot be empty. Please enter: ");
            staffName = sc.nextLine().trim();
        }

        System.out.println("\nSelect priority:");
        System.out.println("[1] LOW (Regular cleaning)");
        System.out.println("[2] MEDIUM (Touch-up needed)");
        System.out.println("[3] HIGH (Deep cleaning required)");
        System.out.println("[4] URGENT (Immediate attention)");
        int priorityChoice = getValidIntInput(sc, "Choice: ", 1, 4);

        String[] priorities = {"LOW", "MEDIUM", "HIGH", "URGENT"};
        String priority = priorities[priorityChoice - 1];

        if (repo.assignCleaningTask(roomNumber, staffName, priority)) {
            System.out.println("\n✅ Cleaning task assigned successfully!");
        } else {
            System.out.println("\n❌ Failed to assign cleaning task.");
        }
        
        pauseScreen(sc);
    }

    private static void completeCleaningTask(Scanner sc) {
        int taskID = getValidIntInput(sc, "\nEnter task ID to complete: ", 1, 99999);

        System.out.print("Enter completion notes: ");
        String notes = sc.nextLine().trim();

        if (repo.completeCleaningTask(taskID, notes)) {
            System.out.println("\n✅ Task marked as completed!");
        } else {
            System.out.println("\n❌ Failed to complete task. Task ID may not exist.");
        }
        
        pauseScreen(sc);
    }

    private static void showHousekeepingDashboard() {
        String[][] pending = repo.getCleaningTasks("PENDING");
        int pendingCount = pending != null ? pending.length : 0;

        String[][] completed = repo.getCleaningTasks("COMPLETED");
        int completedCount = completed != null ? completed.length : 0;

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         🧹 HOUSEKEEPING COMMAND CENTER                       ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Pending Tasks:   %-42d ║%n", pendingCount);
        System.out.printf("║  Completed Tasks: %-42d ║%n", completedCount);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        if (pending != null && pending.length > 0) {
            System.out.println("║  Current Priority Tasks:                                     ║");
            for (int i = 0; i < Math.min(3, pending.length); i++) {
                String status = "HIGH".equals(pending[i][5]) || "URGENT".equals(pending[i][5]) ? "PRIORITY" : "PENDING";
                System.out.printf("║    Room %s: %s%n", pending[i][1], status);
            }
        }

        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    // ============ MAINTENANCE MANAGEMENT ============

    private static void maintenanceManagement(Scanner sc) {
        int choice;
        boolean inMenu = true;

        do {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         🔧 MAINTENANCE MANAGEMENT                            ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] View Pending Issues                                     ║");
            System.out.println("║  [2] View In-Progress Issues                                 ║");
            System.out.println("║  [3] View Completed Issues                                   ║");
            System.out.println("║  [4] Report New Issue                                        ║");
            System.out.println("║  [5] Assign Issue to Staff                                   ║");
            System.out.println("║  [6] Complete Issue                                          ║");
            System.out.println("║  [7] View by Severity                                        ║");
            System.out.println("║  [0] Back                                                    ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            choice = getValidIntInput(sc, "Enter your choice: ", 0, 7);

            switch (choice) {
                case 1:
                    viewMaintenanceIssues("PENDING", sc);
                    break;
                case 2:
                    viewMaintenanceIssues("IN_PROGRESS", sc);
                    break;
                case 3:
                    viewMaintenanceIssues("COMPLETED", sc);
                    break;
                case 4:
                    reportMaintenanceIssue(sc);
                    break;
                case 5:
                    assignMaintenanceIssue(sc);
                    break;
                case 6:
                    completeMaintenanceIssue(sc);
                    break;
                case 7:
                    viewMaintenanceBySeverity(sc);
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }
        } while (inMenu);
    }

    private static void viewMaintenanceIssues(String status, Scanner sc) {
        String[][] issues = repo.getMaintenanceIssues(status);
        if (issues == null || issues.length == 0) {
            System.out.println("\nℹ️  No " + status.toLowerCase().replace("_", " ") + " issues found.");
            pauseScreen(sc);
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║                    %s MAINTENANCE ISSUES%n", status.replace("_", " ").toUpperCase());
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-15s │ %-12s │ %-20s │ %-12s │ %-15s ║%n",
                "ID", "Facility", "Type", "Description", "Reported", "Assigned To");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════╣");

        for (String[] issue : issues) {
            String desc = issue[3].length() > 18 ? issue[3].substring(0, 15) + "..." : issue[3];
            System.out.printf("║ %-8s │ %-15s │ %-12s │ %-20s │ %-12s │ %-15s ║%n",
                    issue[0], issue[1], issue[2], desc, issue[5], issue[6]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
        
        pauseScreen(sc);
    }

    private static void reportMaintenanceIssue(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🔧 REPORT MAINTENANCE ISSUE                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        System.out.print("\nEnter facility/area name: ");
        String facilityName = sc.nextLine().trim();
        while (facilityName.isEmpty()) {
            System.out.print("[!] Facility name cannot be empty. Please enter: ");
            facilityName = sc.nextLine().trim();
        }

        System.out.println("\nSelect issue type:");
        System.out.println("[1] Electrical");
        System.out.println("[2] Plumbing");
        System.out.println("[3] HVAC (Air Conditioning)");
        System.out.println("[4] Structural");
        System.out.println("[5] Equipment");
        System.out.println("[6] Other");
        int typeChoice = getValidIntInput(sc, "Choice: ", 1, 6);

        String[] types = {"Electrical", "Plumbing", "HVAC", "Structural", "Equipment", "Other"};
        String issueType = types[typeChoice - 1];

        System.out.print("\nEnter issue description: ");
        String description = sc.nextLine().trim();
        while (description.isEmpty()) {
            System.out.print("[!] Description cannot be empty. Please enter: ");
            description = sc.nextLine().trim();
        }

        System.out.print("Reported by: ");
        String reportedBy = sc.nextLine().trim();
        while (reportedBy.isEmpty()) {
            System.out.print("[!] Reporter name cannot be empty. Please enter: ");
            reportedBy = sc.nextLine().trim();
        }

        String severity = calculateSeverity(facilityName, description);
        System.out.println("\n📊 Auto-detected severity: " + severity);

        if (repo.reportMaintenanceIssueWithSeverity(facilityName, issueType, description, reportedBy, severity)) {
            System.out.println("\n✅ Maintenance issue reported successfully!");
        } else {
            System.out.println("\n❌ Failed to report maintenance issue.");
        }
        
        pauseScreen(sc);
    }

    private static String calculateSeverity(String facilityName, String description) {
        String lower = description.toLowerCase();
        String facility = facilityName.toLowerCase();

        // Critical keywords
        if (lower.contains("smoke") || lower.contains("fire") || lower.contains("flood") ||
                lower.contains("gas leak") || lower.contains("electrical hazard") ||
                lower.contains("injury") || lower.contains("collapse")) {
            return "CRITICAL";
        }

        // High priority for rides
        if (facility.contains("ride") || facility.contains("coaster") || facility.contains("wheel")) {
            if (lower.contains("broken") || lower.contains("stuck") || lower.contains("not working")) {
                return "HIGH";
            }
        }

        // High priority keywords
        if (lower.contains("not working") || lower.contains("broken") || lower.contains("leak") ||
                lower.contains("no power") || lower.contains("unsafe")) {
            return "HIGH";
        }

        // Medium priority
        if (lower.contains("noisy") || lower.contains("slow") || lower.contains("uncomfortable")) {
            return "MEDIUM";
        }

        return "LOW";
    }

    private static void assignMaintenanceIssue(Scanner sc) {
        int issueID = getValidIntInput(sc, "\nEnter issue ID to assign: ", 1, 99999);

        System.out.print("Assign to (staff name): ");
        String assignedTo = sc.nextLine().trim();
        while (assignedTo.isEmpty()) {
            System.out.print("[!] Staff name cannot be empty. Please enter: ");
            assignedTo = sc.nextLine().trim();
        }

        if (repo.assignMaintenance(issueID, assignedTo)) {
            System.out.println("\n✅ Issue assigned successfully!");
        } else {
            System.out.println("\n❌ Failed to assign issue. Issue ID may not exist.");
        }
        
        pauseScreen(sc);
    }

    private static void completeMaintenanceIssue(Scanner sc) {
        int issueID = getValidIntInput(sc, "\nEnter issue ID to complete: ", 1, 99999);

        System.out.print("Enter resolution notes: ");
        String resolution = sc.nextLine().trim();
        while (resolution.isEmpty()) {
            System.out.print("[!] Resolution notes cannot be empty. Please enter: ");
            resolution = sc.nextLine().trim();
        }

        if (repo.completeMaintenance(issueID, resolution)) {
            System.out.println("\n✅ Issue marked as completed!");
        } else {
            System.out.println("\n❌ Failed to complete issue. Issue ID may not exist.");
        }
        
        pauseScreen(sc);
    }

    private static void viewMaintenanceBySeverity(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🔍 VIEW BY SEVERITY                                ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  [1] CRITICAL (5 min response)                               ║");
        System.out.println("║  [2] HIGH (15 min response)                                  ║");
        System.out.println("║  [3] MEDIUM (1 hour response)                                ║");
        System.out.println("║  [4] LOW (end of day)                                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        int choice = getValidIntInput(sc, "Choice: ", 1, 4);

        String[] severities = {"CRITICAL", "HIGH", "MEDIUM", "LOW"};
        String severity = severities[choice - 1];

        String[][] issues = repo.getMaintenanceBySeverity(severity);
        if (issues == null || issues.length == 0) {
            System.out.println("\nℹ️  No " + severity + " issues found.");
            pauseScreen(sc);
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║                    %s ISSUES%n", severity);
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-15s │ %-12s │ %-20s │ %-12s │ %-15s ║%n",
                "ID", "Facility", "Type", "Description", "Reported", "Assigned To");
        System.out.println("╠════════════════════════════════════════════════════════════════════════════════╣");

        for (String[] issue : issues) {
            String desc = issue[3].length() > 18 ? issue[3].substring(0, 15) + "..." : issue[3];
            System.out.printf("║ %-8s │ %-15s │ %-12s │ %-20s │ %-12s │ %-15s ║%n",
                    issue[0], issue[1], issue[2], desc, issue[6], issue[7]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════════════╝");
        
        pauseScreen(sc);
    }

    // ============ FACILITY MANAGEMENT ============

    private static void facilityManagement(Scanner sc) {
        int choice;
        boolean inMenu = true;

        do {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         🏢 FACILITY MANAGEMENT                               ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] View All Facilities                                     ║");
            System.out.println("║  [2] Add New Facility                                        ║");
            System.out.println("║  [3] Update Facility Status                                  ║");
            System.out.println("║  [4] Log Facility Usage                                      ║");
            System.out.println("║  [0] Back                                                    ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            choice = getValidIntInput(sc, "Enter your choice: ", 0, 4);

            switch (choice) {
                case 1:
                    viewAllFacilities();
                    pauseScreen(sc);
                    break;
                case 2:
                    addNewFacility(sc);
                    break;
                case 3:
                    updateFacilityStatus(sc);
                    break;
                case 4:
                    logFacilityUsage(sc);
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }
        } while (inMenu);
    }

    private static void viewAllFacilities() {
        String[][] facilities = repo.getAllFacilities();
        if (facilities == null || facilities.length == 0) {
            System.out.println("\n❌ No facilities found.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         ALL FACILITIES                                 ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-20s │ %-15s │ %-15s │ %-12s ║%n",
                "ID", "Name", "Type", "Location", "Status");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        for (String[] facility : facilities) {
            System.out.printf("║ %-8s │ %-20s │ %-15s │ %-15s │ %-12s ║%n",
                    facility[0], facility[1], facility[2], facility[3], facility[4]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
    }

    private static void addNewFacility(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           ➕ ADD NEW FACILITY                                ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        System.out.print("\nEnter facility name: ");
        String facilityName = sc.nextLine().trim();
        while (facilityName.isEmpty()) {
            System.out.print("[!] Facility name cannot be empty. Please enter: ");
            facilityName = sc.nextLine().trim();
        }

        System.out.print("Enter facility type (e.g., Ride, Restaurant, Shop): ");
        String facilityType = sc.nextLine().trim();
        while (facilityType.isEmpty()) {
            System.out.print("[!] Facility type cannot be empty. Please enter: ");
            facilityType = sc.nextLine().trim();
        }

        System.out.print("Enter location: ");
        String location = sc.nextLine().trim();
        while (location.isEmpty()) {
            System.out.print("[!] Location cannot be empty. Please enter: ");
            location = sc.nextLine().trim();
        }

        System.out.print("Enter operating hours (e.g., 09:00-21:00): ");
        String operatingHours = sc.nextLine().trim();

        if (repo.addFacility(facilityName, facilityType, location, operatingHours)) {
            System.out.println("\n✅ Facility added successfully!");
        } else {
            System.out.println("\n❌ Failed to add facility. It may already exist.");
        }
        
        pauseScreen(sc);
    }

    private static void updateFacilityStatus(Scanner sc) {
        int facilityID = getValidIntInput(sc, "\nEnter facility ID: ", 1, 99999);

        System.out.println("\nSelect new status:");
        System.out.println("[1] OPERATIONAL");
        System.out.println("[2] UNDER_MAINTENANCE");
        System.out.println("[3] CLOSED");
        int statusChoice = getValidIntInput(sc, "Choice: ", 1, 3);

        String[] statuses = {"OPERATIONAL", "UNDER_MAINTENANCE", "CLOSED"};
        String status = statuses[statusChoice - 1];

        if (repo.updateFacilityStatus(facilityID, status)) {
            System.out.println("\n✅ Facility status updated successfully!");
        } else {
            System.out.println("\n❌ Failed to update status. Facility ID may not exist.");
        }
        
        pauseScreen(sc);
    }

    private static void logFacilityUsage(Scanner sc) {
        int facilityID = getValidIntInput(sc, "\nEnter facility ID: ", 1, 99999);

        if (repo.logFacilityUsage(facilityID)) {
            int total = repo.getTotalCycles(facilityID);
            int threshold = repo.getCycleThreshold(facilityID);

            System.out.println("\n✅ Usage logged!");
            System.out.printf("  Total cycles: %d / %d%n", total, threshold);

            if (total >= threshold) {
                System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
                System.out.println("║              ⚠️  MAINTENANCE DUE!                            ║");
                System.out.println("╠══════════════════════════════════════════════════════════════╣");
                System.out.println("║  This facility has reached its maintenance threshold.        ║");
                System.out.println("╚══════════════════════════════════════════════════════════════╝");
                
                int schedule = getValidIntInput(sc, "\nSchedule maintenance now? [1] Yes [2] No: ", 1, 2);

                if (schedule == 1) {
                    if (repo.schedulePredictiveMaintenance(facilityID)) {
                        System.out.println("\n✅ Maintenance scheduled!");
                    } else {
                        System.out.println("\n❌ Failed to schedule maintenance.");
                    }
                }
            }
        } else {
            System.out.println("\n❌ Failed to log usage.");
        }
        
        pauseScreen(sc);
    }

    // ============ PREDICTIVE MAINTENANCE ============

    private static void showPredictiveMaintenance(Scanner sc) {
        int choice;
        boolean inMenu = true;

        do {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         🔮 PREDICTIVE MAINTENANCE                            ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] View Facilities Needing Maintenance                     ║");
            System.out.println("║  [2] Log Facility Usage                                      ║");
            System.out.println("║  [3] Schedule Maintenance                                    ║");
            System.out.println("║  [4] View Maintenance Schedule                               ║");
            System.out.println("║  [0] Back                                                    ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            choice = getValidIntInput(sc, "Choice: ", 0, 4);

            switch (choice) {
                case 1:
                    viewFacilitiesNeedingMaintenance();
                    pauseScreen(sc);
                    break;
                case 2:
                    logFacilityUsage(sc);
                    break;
                case 3:
                    scheduleMaintenance(sc);
                    break;
                case 4:
                    viewMaintenanceSchedule();
                    pauseScreen(sc);
                    break;
                case 0:
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice.");
            }
        } while (inMenu);
    }

    private static void viewFacilitiesNeedingMaintenance() {
        String[][] facilities = repo.getFacilitiesNeedingMaintenance();
        if (facilities == null || facilities.length == 0) {
            System.out.println("\nℹ️  No facilities currently need maintenance.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║              FACILITIES NEEDING MAINTENANCE                            ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-20s │ %-12s │ %-12s │ %-15s ║%n",
                "ID", "Name", "Cycles", "Threshold", "Scheduled");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        for (String[] f : facilities) {
            System.out.printf("║ %-8s │ %-20s │ %-12s │ %-12s │ %-15s ║%n",
                    f[0], f[1], f[2], f[3], f[4] != null ? f[4] : "Not scheduled");
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
    }

    private static void scheduleMaintenance(Scanner sc) {
        int facilityID = getValidIntInput(sc, "\nEnter facility ID: ", 1, 99999);

        if (repo.schedulePredictiveMaintenance(facilityID)) {
            System.out.println("\n✅ Maintenance scheduled for facility " + facilityID);
        } else {
            System.out.println("\n❌ Failed to schedule maintenance.");
        }
        
        pauseScreen(sc);
    }

    private static void viewMaintenanceSchedule() {
        String[][] facilities = repo.getFacilitiesNeedingMaintenance();
        if (facilities == null || facilities.length == 0) {
            System.out.println("\nℹ️  No scheduled maintenance.");
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              📅 UPCOMING MAINTENANCE                         ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        boolean hasSchedule = false;
        for (String[] f : facilities) {
            if (f[4] != null) {
                System.out.printf("  %s - Scheduled: %s%n", f[1], f[4]);
                hasSchedule = true;
            }
        }
        
        if (!hasSchedule) {
            System.out.println("  No upcoming maintenance scheduled.");
        }
    }

    // ============ CUSTOMER ROOM BOOKING METHODS ============

    public static void bookRoomForCustomer(int customerID, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              🏨 ROOM BOOKING                                 ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        System.out.println("\nWelcome, " + repo.getCustomerName(customerID));

        System.out.println("\n[1] Smart Room Assignment (Recommended)");
        System.out.println("[2] Manual Room Selection");
        int choice = getValidIntInput(sc, "Choice: ", 1, 2);

        if (choice == 1) {
            SmartRoomAssignment.smartBookRoom(customerID, sc);
            return;
        }

        String[][] rooms = repo.getAvailableRooms();
        if (rooms == null || rooms.length == 0) {
            System.out.println("\n❌ Sorry, no rooms available at the moment.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    AVAILABLE ROOMS                             ║");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-10s │ %-12s │ %-10s │ %-15s ║%n", "Room #", "Type", "Capacity", "Price/Night");
        System.out.println("╠════════════════════════════════════════════════════════════════╣");

        for (String[] room : rooms) {
            System.out.printf("║ %-10s │ %-12s │ %-10s │ PHP %-12s ║%n",
                    room[0], room[1], room[2], room[3]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════╝");

        int roomNumber = getValidIntInput(sc, "\nEnter room number to book: ", 100, 999);

        String checkInDate = getValidDate(sc, "Enter check-in date (YYYY-MM-DD): ");
        if (checkInDate == null) return;

        String checkOutDate = getValidDate(sc, "Enter check-out date (YYYY-MM-DD): ");
        if (checkOutDate == null) return;

        if (!validateDates(checkInDate, checkOutDate)) return;

        int guests = getValidIntInput(sc, "Enter number of guests: ", 1, 20);

        if (repo.bookRoom(roomNumber, customerID, checkInDate, checkOutDate, guests)) {
            long nights = ChronoUnit.DAYS.between(
                    LocalDate.parse(checkInDate), LocalDate.parse(checkOutDate));
            String[] room = repo.getRoomDetails(roomNumber);
            double pricePerNight = Double.parseDouble(room[3]);
            double totalCost = nights * pricePerNight;

            String membership = repo.getMembershipType(customerID);
            double finalCost = totalCost;
            if ("VIP".equals(membership)) {
                double discount = totalCost * 0.20;
                finalCost = totalCost - discount;
                System.out.printf("\n💰 VIP Discount (20%%): -PHP %.2f%n", discount);
            }

            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              ✅ ROOM BOOKED SUCCESSFULLY!                    ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.printf("║  Room: %-54s ║%n", roomNumber + " (" + room[1] + ")");
            System.out.printf("║  Check-in: %-50s ║%n", checkInDate);
            System.out.printf("║  Check-out: %-49s ║%n", checkOutDate);
            System.out.printf("║  Nights: %-52d ║%n", nights);
            System.out.printf("║  Guests: %-52d ║%n", guests);
            System.out.printf("║  Total Cost: PHP %-43.2f ║%n", finalCost);
            System.out.println("╚══════════════════════════════════════════════════════════════╝");

            // Post-booking options with refund
            showPostBookingOptions(customerID, roomNumber, room[1], finalCost, checkInDate, checkOutDate, sc);
        } else {
            System.out.println("\n❌ Failed to book room. Room may not be available.");
        }
        
        pauseScreen(sc);
    }

    public static void viewMyBookings(int customerID) {
        String[][] bookings = repo.getCustomerBookings(customerID);
        if (bookings == null || bookings.length == 0) {
            System.out.println("\nℹ️  You have no room bookings.");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         MY ROOM BOOKINGS                               ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-10s │ %-12s │ %-12s │ %-12s │ %-12s ║%n",
                "Booking", "Room #", "Type", "Check-in", "Check-out", "Status");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        for (String[] booking : bookings) {
            System.out.printf("║ %-8s │ %-10s │ %-12s │ %-12s │ %-12s │ %-12s ║%n",
                    booking[0], booking[1], booking[2], booking[3], booking[4], booking[5]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
    }

    // ============ HELPER METHODS ============

    private static String getValidDate(Scanner sc, String prompt) {
        System.out.print(prompt);
        String dateStr = sc.nextLine().trim();
        
        try {
            LocalDate date = LocalDate.parse(dateStr, DATE_FORMATTER);
            return dateStr;
        } catch (Exception e) {
            System.out.println("[!] Invalid date format. Please use YYYY-MM-DD.");
            return null;
        }
    }

    private static boolean validateDates(String checkInDate, String checkOutDate) {
        try {
            LocalDate checkIn = LocalDate.parse(checkInDate, DATE_FORMATTER);
            LocalDate checkOut = LocalDate.parse(checkOutDate, DATE_FORMATTER);

            if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
                System.out.println("\n❌ Invalid dates. Check-out must be after check-in.");
                return false;
            }

            if (checkIn.isBefore(LocalDate.now())) {
                System.out.println("\n❌ Check-in date cannot be in the past.");
                return false;
            }

            return true;
        } catch (Exception e) {
            System.out.println("\n❌ Invalid date format.");
            return false;
        }
    }

    private static int getValidIntInput(Scanner sc, String prompt, int min, int max) {
        if (!prompt.isEmpty()) {
            System.out.print(prompt);
        }
        
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

    /**
     * Show post-booking options including refund
     */
    private static void showPostBookingOptions(int customerID, int roomNumber, String roomType,
                                                double finalCost, String checkInDate, String checkOutDate,
                                                Scanner sc) {
        System.out.println("\n+--------------------------------------------------------------+");
        System.out.println("|         WHAT WOULD YOU LIKE TO DO NEXT?                      |");
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|  [1] Request a Refund / Cancel Booking                       |");
        System.out.println("|  [2] Back to Portal (Done)                                   |");
        System.out.println("+--------------------------------------------------------------+");

        int choice = getValidIntInput(sc, "Choice: ", 1, 2);

        if (choice == 1) {
            handleRoomRefundRequest(customerID, roomNumber, roomType, finalCost, checkInDate, checkOutDate, sc);
        }
    }

    /**
     * Handle room booking refund/cancellation
     */
    private static void handleRoomRefundRequest(int customerID, int roomNumber, String roomType,
                                                  double finalCost, String checkInDate, String checkOutDate,
                                                  Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         REQUEST ROOM BOOKING REFUND                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Cancellation Policy:                                        ║");
        System.out.println("║  • Full refund if cancelled 48+ hours before check-in        ║");
        System.out.println("║  • 50% refund if cancelled 24-48 hours before check-in       ║");
        System.out.println("║  • No refund if cancelled less than 24 hours before          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        System.out.println("\n[1] Continue with cancellation and refund request");
        System.out.println("[2] Keep booking and go back");
        int confirm = getValidIntInput(sc, "Choice: ", 1, 2);

        if (confirm == 2) {
            System.out.println("\n* Booking kept. No changes made.");
            pauseScreen(sc);
            return;
        }

        System.out.print("\nReason for cancellation: ");
        String reason = sc.nextLine().trim();
        while (reason.isEmpty()) {
            System.out.print("[!] Please provide a reason: ");
            reason = sc.nextLine().trim();
        }

        String customerName = repo.getCustomerName(customerID);

        // Calculate refund amount based on policy
        double refundAmount = calculateRoomRefundAmount(finalCost, checkInDate);

        boolean submitted = FinanceManager.processRefund(
            refundAmount, "Room cancellation: " + reason, customerID, customerName, "ROOM", "Online Payment"
        );

        if (submitted) {
            // Also cancel the booking in the system
            repo.cancelRoomBooking(customerID, roomNumber, checkInDate);

            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║      * CANCELLATION & REFUND REQUEST SUBMITTED!              ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║                                                              ║");
            System.out.println("║  Your room booking has been cancelled.                       ║");
            System.out.println("║  Refund request submitted for admin review.                  ║");
            System.out.println("║                                                              ║");
            System.out.printf("║  Room: %-54s ║%n", roomNumber + " (" + roomType + ")");
            System.out.printf("║  Refund Amount: PHP %40.2f  ║%n", refundAmount);
            System.out.println("║  Refund Rate: Based on cancellation policy                   ║");
            System.out.println("║                                                              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        } else {
            System.out.println("\n* Failed to submit refund request. Please contact front desk.");
        }

        pauseScreen(sc);
    }

    /**
     * Calculate refund amount based on cancellation policy
     */
    private static double calculateRoomRefundAmount(double totalCost, String checkInDate) {
        try {
            java.time.LocalDate checkIn = java.time.LocalDate.parse(checkInDate);
            java.time.LocalDate today = java.time.LocalDate.now();
            long daysUntilCheckIn = java.time.temporal.ChronoUnit.DAYS.between(today, checkIn);

            if (daysUntilCheckIn >= 2) {
                return totalCost; // Full refund (48+ hours)
            } else if (daysUntilCheckIn >= 1) {
                return totalCost * 0.5; // 50% refund (24-48 hours)
            } else {
                return 0.0; // No refund (less than 24 hours)
            }
        } catch (Exception e) {
            return totalCost * 0.5; // Default to 50% if date parsing fails
        }
    }
}
