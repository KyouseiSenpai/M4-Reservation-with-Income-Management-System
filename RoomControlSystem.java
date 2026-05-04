import java.util.Scanner;

/**
 * Room Control System Class - Simulates IoT smart room controls
 * Guests can control room settings via the customer portal
 * 
 * REVISED VERSION - Bug Fixes:
 * - Fixed Scanner resource leak by accepting Scanner as parameter
 * - Added input validation
 * - Fixed potential null pointer exceptions
 * - Added proper error handling
 * - Improved display formatting
 */
public class RoomControlSystem {

    private static final Repository repo = Repository.getInstance();
    private static final double MIN_TEMP = 16.0;
    private static final double MAX_TEMP = 30.0;
    private static final double DEFAULT_TEMP = 22.0;

    /**
     * Show room controls for a guest
     * @param customerID The customer ID
     * @param sc Scanner instance
     */
    public static void showRoomControls(int customerID, Scanner sc) {
        // Check if guest has an active booking
        int roomNumber = repo.getCurrentGuestRoom(customerID);
        if (roomNumber == -1) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║  ℹ️  You don't have an active room booking.                  ║");
            System.out.println("║     Book a room to access room controls.                     ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            pauseScreen(sc);
            return;
        }

        int choice;
        boolean inMenu = true;

        do {
            // Get current room status from repository
            Repository.RoomStatus status = getRoomStatus(roomNumber);

            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.printf("║              🎛️  ROOM %d CONTROLS%n", roomNumber);
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║                                                              ║");
            System.out.println("║  --- Climate ---                                             ║");
            System.out.printf("║  [1] Temperature: %.1f°C %s%n", status.temperature, getTempEmoji(status.temperature));
            System.out.println("║                                                              ║");
            System.out.println("║  --- Lighting ---                                            ║");
            System.out.println("║  [2] Lights: " + (status.lightsOn ? "ON 💡" : "OFF 🌑"));
            System.out.println("║                                                              ║");
            System.out.println("║  --- Privacy ---                                             ║");
            System.out.println("║  [3] Do Not Disturb: " + (status.dndStatus ? "ON 🔴" : "OFF 🟢"));
            System.out.println("║                                                              ║");
            System.out.println("║  --- Services ---                                            ║");
            System.out.println("║  [4] Request Housekeeping                                    ║");
            System.out.println("║  [5] Request Fresh Towels                                    ║");
            System.out.println("║  [6] Report Maintenance Issue                                ║");
            System.out.println("║                                                              ║");
            System.out.println("║  [0] Back to Portal                                          ║");
            System.out.println("║                                                              ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");

            choice = getValidIntInput(sc, "Select option: ", 0, 6);

            switch (choice) {
                case 1:
                    adjustTemperature(roomNumber, status.temperature, sc);
                    break;
                case 2:
                    toggleLights(roomNumber, status.lightsOn);
                    break;
                case 3:
                    toggleDND(roomNumber, status.dndStatus);
                    break;
                case 4:
                    requestHousekeeping(roomNumber, customerID, sc);
                    break;
                case 5:
                    requestTowels(roomNumber, customerID, sc);
                    break;
                case 6:
                    reportRoomIssue(roomNumber, customerID, sc);
                    break;
                case 0:
                    System.out.println("\nReturning to portal...");
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid option.");
            }
        } while (inMenu);
    }

    /**
     * Adjust room temperature
     * @param roomNumber The room number
     * @param currentTemp Current temperature from Repository.RoomStatus
     * @param sc Scanner instance
     */
    private static void adjustTemperature(int roomNumber, double currentTemp, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🌡️  TEMPERATURE CONTROL                            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Current: %.1f°C%n", currentTemp);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  [1] Increase (+1°C)                                         ║");
        System.out.println("║  [2] Decrease (-1°C)                                         ║");
        System.out.println("║  [3] Set to 22°C (Comfort)                                   ║");
        System.out.println("║  [4] Set to 18°C (Cool)                                      ║");
        System.out.println("║  [5] Set to 26°C (Warm)                                      ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int choice = getValidIntInput(sc, "Choice: ", 1, 5);

        double newTemp = currentTemp;
        switch (choice) {
            case 1:
                newTemp = Math.min(currentTemp + 1, MAX_TEMP);
                break;
            case 2:
                newTemp = Math.max(currentTemp - 1, MIN_TEMP);
                break;
            case 3:
                newTemp = 22;
                break;
            case 4:
                newTemp = 18;
                break;
            case 5:
                newTemp = 26;
                break;
        }

        if (repo.updateRoomTemperature(roomNumber, newTemp)) {
            System.out.printf("\n✅ Temperature set to %.1f°C%n", newTemp);
            System.out.println("   Your room will reach this temperature shortly.");
        } else {
            System.out.println("\n❌ Failed to set temperature.");
        }
        
        pauseScreen(sc);
    }

    /**
     * Toggle room lights
     * @param roomNumber The room number
     * @param currentlyOn Current light state
     */
    private static void toggleLights(int roomNumber, boolean currentlyOn) {
        boolean newState = !currentlyOn;
        
        if (repo.updateRoomLights(roomNumber, newState)) {
            if (newState) {
                System.out.println("\n✅ Lights turned ON 💡");
                System.out.println("   Welcome back to a well-lit room!");
            } else {
                System.out.println("\n✅ Lights turned OFF 🌑");
                System.out.println("   Sweet dreams!");
            }
        } else {
            System.out.println("\n❌ Failed to toggle lights.");
        }
    }

    /**
     * Toggle Do Not Disturb
     * @param roomNumber The room number
     * @param currentlyOn Current DND state
     */
    private static void toggleDND(int roomNumber, boolean currentlyOn) {
        boolean newState = !currentlyOn;
        
        if (repo.updateRoomDND(roomNumber, newState)) {
            if (newState) {
                System.out.println("\n✅ Do Not Disturb is ON 🔴");
                System.out.println("   Housekeeping will skip your room.");
                System.out.println("   Hang the DND sign on your door.");
            } else {
                System.out.println("\n✅ Do Not Disturb is OFF 🟢");
                System.out.println("   Housekeeping may clean your room.");
            }
        } else {
            System.out.println("\n❌ Failed to toggle DND.");
        }
    }

    /**
     * Request housekeeping service
     * @param roomNumber The room number
     * @param customerID The customer ID
     * @param sc Scanner instance
     */
    private static void requestHousekeeping(int roomNumber, int customerID, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🧹 HOUSEKEEPING REQUEST                            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  When would you like service?                                ║");
        System.out.println("║  [1] Right now (if DND is off)                               ║");
        System.out.println("║  [2] In 1 hour                                               ║");
        System.out.println("║  [3] In 2 hours                                              ║");
        System.out.println("║  [4] Tomorrow morning                                        ║");
        System.out.println("║  [5] Specific time                                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int choice = getValidIntInput(sc, "Choice: ", 1, 5);

        String when;
        switch (choice) {
            case 1:
                when = "NOW";
                break;
            case 2:
                when = "1_HOUR";
                break;
            case 3:
                when = "2_HOURS";
                break;
            case 4:
                when = "TOMORROW_MORNING";
                break;
            case 5:
                System.out.print("Enter time (e.g., 3:00 PM): ");
                when = sc.nextLine().trim();
                if (when.isEmpty()) when = "NOW";
                break;
            default:
                when = "NOW";
        }

        System.out.print("\nAny special requests? (e.g., extra towels, toiletries): ");
        String notes = sc.nextLine().trim();

        if (repo.createGuestHousekeepingRequest(roomNumber, customerID, when, notes)) {
            System.out.println("\n✅ Housekeeping request submitted!");
            System.out.println("   Room: " + roomNumber);
            System.out.println("   When: " + when);
            if (!notes.isEmpty()) {
                System.out.println("   Notes: " + notes);
            }
            System.out.println("   You'll receive a notification when we're on our way.");
        } else {
            System.out.println("\n❌ Failed to submit housekeeping request.");
        }
        
        pauseScreen(sc);
    }

    /**
     * Request fresh towels
     * @param roomNumber The room number
     * @param customerID The customer ID
     * @param sc Scanner instance
     */
    private static void requestTowels(int roomNumber, int customerID, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🧺 FRESH TOWELS REQUEST                            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  How many towels?                                            ║");
        System.out.println("║  [1] 2 towels (standard)                                     ║");
        System.out.println("║  [2] 4 towels                                                ║");
        System.out.println("║  [3] 6 towels                                                ║");
        System.out.println("║  [4] Custom amount                                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int choice = getValidIntInput(sc, "Choice: ", 1, 4);

        int towelCount = 2;
        switch (choice) {
            case 1:
                towelCount = 2;
                break;
            case 2:
                towelCount = 4;
                break;
            case 3:
                towelCount = 6;
                break;
            case 4:
                towelCount = getValidIntInput(sc, "Enter amount: ", 1, 20);
                break;
        }

        if (repo.createTowelRequest(roomNumber, customerID, towelCount)) {
            System.out.println("\n✅ Towel request submitted!");
            System.out.println("   " + towelCount + " fresh towels will be delivered shortly.");
        } else {
            System.out.println("\n❌ Failed to submit towel request.");
        }
        
        pauseScreen(sc);
    }

    /**
     * Report maintenance issue from room
     * @param roomNumber The room number
     * @param customerID The customer ID
     * @param sc Scanner instance
     */
    private static void reportRoomIssue(int roomNumber, int customerID, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🔧 REPORT ROOM ISSUE                               ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  What type of issue?                                         ║");
        System.out.println("║  [1] Air Conditioning / Heating                              ║");
        System.out.println("║  [2] Plumbing (toilet, shower, sink)                         ║");
        System.out.println("║  [3] Electrical (lights, outlets, TV)                        ║");
        System.out.println("║  [4] WiFi / Internet                                         ║");
        System.out.println("║  [5] Furniture / Fixtures                                    ║");
        System.out.println("║  [6] Cleanliness                                             ║");
        System.out.println("║  [7] Other                                                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int choice = getValidIntInput(sc, "Choice: ", 1, 7);

        String[] issueTypes = {"HVAC", "Plumbing", "Electrical", "WiFi", "Furniture", "Cleanliness", "Other"};
        String issueType = issueTypes[choice - 1];

        System.out.print("\nPlease describe the issue: ");
        String description = sc.nextLine().trim();
        while (description.isEmpty()) {
            System.out.print("[!] Description cannot be empty. Please describe: ");
            description = sc.nextLine().trim();
        }

        System.out.println("\nHow urgent is this?");
        System.out.println("[1] Emergency (safety issue, no water, etc.)");
        System.out.println("[2] Urgent (affecting comfort significantly)");
        System.out.println("[3] Standard (minor inconvenience)");
        int urgency = getValidIntInput(sc, "Choice: ", 1, 3);

        String[] severities = {"CRITICAL", "HIGH", "MEDIUM"};
        String severity = severities[urgency - 1];

        String guestName = repo.getCustomerName(customerID);
        String facilityName = "Room " + roomNumber;

        if (repo.reportMaintenanceIssueWithSeverity(facilityName, issueType, description, guestName, severity)) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              ✅ ISSUE REPORTED!                              ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.printf("║  Type: %-54s ║%n", issueType);
            System.out.printf("║  Severity: %-50s ║%n", severity);
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            
            if (urgency == 1) {
                System.out.println("║  ⚠️  EMERGENCY: Maintenance will respond within 15 minutes!  ║");
            } else if (urgency == 2) {
                System.out.println("║  Maintenance will respond within 1 hour.                     ║");
            } else {
                System.out.println("║  Maintenance will address this today.                        ║");
            }
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        } else {
            System.out.println("\n❌ Failed to report issue.");
        }
        
        pauseScreen(sc);
    }

    /**
     * Get room status from repository
     * @param roomNumber The room number
     * @return RoomStatus object
     */
    private static Repository.RoomStatus getRoomStatus(int roomNumber) {
        return repo.getRoomStatus(roomNumber);
    }

    /**
     * Get emoji for temperature
     * @param temp Temperature value
     * @return Emoji string
     */
    private static String getTempEmoji(double temp) {
        if (temp < 18) return "❄️";
        if (temp > 25) return "🔥";
        return "😊";
    }

    /**
     * Admin: View all room statuses
     */
    public static void showAllRoomStatuses() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                    📊 LIVE ROOM STATUS DASHBOARD                       ║");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-8s │ %-10s │ %-8s │ %-20s │ %-12s ║%n",
                "Room", "Temp", "Lights", "DND", "Last Request", "Status");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        String[][] statuses = repo.getAllRoomStatuses();
        if (statuses != null) {
            for (String[] s : statuses) {
                String dnd = "true".equalsIgnoreCase(s[3]) ? "🔴" : "";
                String lights = "true".equalsIgnoreCase(s[2]) ? "💡 ON" : "🌑 OFF";
                String lastRequest = s[4] != null ? truncate(s[4], 18) : "-";
                System.out.printf("║ %-8s │ %-8s │ %-10s │ %-8s │ %-20s │ %-12s ║%n",
                        s[0], s[1] + "°C", lights, dnd, lastRequest,
                        s[5] != null ? s[5] : "-");
            }
        } else {
            System.out.println("║                    No room status data available.                      ║");
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
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
    
    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 3) + "..." : s;
    }
}
