import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Scanner;

/**
 * Lost and Found System Class - Manages lost and found items
 * Integrated with housekeeping during room cleaning
 * 
 * REVISED VERSION - Bug Fixes:
 * - Fixed Scanner resource leak by accepting Scanner as parameter
 * - Added input validation
 * - Fixed potential null pointer exceptions
 * - Added proper error handling
 * - Improved display formatting
 * - Added date validation
 */
public class LostFoundSystem {

    private static final Repository repo = Repository.getInstance();
    private static final int DISPOSAL_DAYS = 30;

    /**
     * Main menu for Lost & Found system
     * @param sc Scanner instance
     */
    public static void showLostFoundMenu(Scanner sc) {
        int choice;
        boolean inMenu = true;

        do {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              🔍 LOST & FOUND CENTER                          ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] Report Found Item (Housekeeping)                        ║");
            System.out.println("║  [2] Search for Lost Item                                    ║");
            System.out.println("║  [3] Claim Item                                              ║");
            System.out.println("║  [4] View All Unclaimed Items                                ║");
            System.out.println("║  [5] View Claimed Items                                      ║");
            System.out.println("║  [6] Process Disposal (Admin)                                ║");
            System.out.println("║  [0] Back                                                    ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            choice = getValidIntInput(sc, "Enter choice: ", 0, 6);

            switch (choice) {
                case 1:
                    reportFoundItem(sc);
                    break;
                case 2:
                    searchLostItem(sc);
                    break;
                case 3:
                    claimItem(sc);
                    break;
                case 4:
                    viewUnclaimedItems();
                    pauseScreen(sc);
                    break;
                case 5:
                    viewClaimedItems();
                    pauseScreen(sc);
                    break;
                case 6:
                    processDisposal(sc);
                    break;
                case 0:
                    System.out.println("\nReturning...");
                    inMenu = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice.");
            }
        } while (inMenu);
    }

    /**
     * Housekeeping: Report item found during cleaning
     * @param sc Scanner instance
     */
    private static void reportFoundItem(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           📦 REPORT FOUND ITEM                               ║");
        System.out.println("║         (For Housekeeping Staff)                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int roomNumber = getValidIntInput(sc, "\nRoom number where found: ", 100, 999);

        String dateFound = getValidDate(sc, "Date found (YYYY-MM-DD): ");
        if (dateFound == null) return;

        System.out.println("\nItem category:");
        System.out.println("[1] Electronics (phone, charger, laptop)");
        System.out.println("[2] Jewelry / Watches");
        System.out.println("[3] Clothing / Accessories");
        System.out.println("[4] Documents / IDs");
        System.out.println("[5] Keys / Cards");
        System.out.println("[6] Toys / Personal Items");
        System.out.println("[7] Other");
        int category = getValidIntInput(sc, "Choice: ", 1, 7);

        String[] categories = {"Electronics", "Jewelry", "Clothing", "Documents", "Keys", "Toys", "Other"};
        String itemCategory = categories[category - 1];

        System.out.print("\nItem description (be specific): ");
        String description = sc.nextLine().trim();
        while (description.isEmpty()) {
            System.out.print("[!] Description cannot be empty. Please enter: ");
            description = sc.nextLine().trim();
        }

        System.out.print("Your name (housekeeping staff): ");
        String foundBy = sc.nextLine().trim();
        while (foundBy.isEmpty()) {
            System.out.print("[!] Name cannot be empty. Please enter: ");
            foundBy = sc.nextLine().trim();
        }

        System.out.print("Storage location (e.g., Locker A-3): ");
        String storageLocation = sc.nextLine().trim();
        while (storageLocation.isEmpty()) {
            System.out.print("[!] Storage location cannot be empty. Please enter: ");
            storageLocation = sc.nextLine().trim();
        }

        // Save to database
        int itemID = repo.saveFoundItem(roomNumber, dateFound, itemCategory, description, foundBy, storageLocation);

        if (itemID != -1) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              ✅ ITEM LOGGED SUCCESSFULLY!                    ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.printf("║  Item ID: %-50d ║%n", itemID);
            System.out.printf("║  Category: %-49s ║%n", itemCategory);
            System.out.printf("║  Stored at: %-48s ║%n", storageLocation);
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  Please attach Item ID tag to the item.                      ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        } else {
            System.out.println("\n❌ Failed to log item. Please try again.");
        }
        
        pauseScreen(sc);
    }

    /**
     * Guest/Staff: Search for lost item
     * @param sc Scanner instance
     */
    private static void searchLostItem(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           🔍 SEARCH FOR LOST ITEM                            ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        System.out.println("\nSearch by:");
        System.out.println("[1] Room number (if you remember)");
        System.out.println("[2] Date range");
        System.out.println("[3] Item category");
        System.out.println("[4] Keyword in description");
        System.out.println("[5] Customer ID (for staff)");
        
        int searchType = getValidIntInput(sc, "Choice: ", 1, 5);

        String[][] results = null;

        switch (searchType) {
            case 1:
                int room = getValidIntInput(sc, "\nEnter room number: ", 100, 999);
                results = repo.searchLostItemsByRoom(room);
                break;
            case 2:
                String from = getValidDate(sc, "\nFrom date (YYYY-MM-DD): ");
                if (from == null) return;
                String to = getValidDate(sc, "To date (YYYY-MM-DD): ");
                if (to == null) return;
                results = repo.searchLostItemsByDate(from, to);
                break;
            case 3:
                System.out.println("\nCategory:");
                System.out.println("[1] Electronics [2] Jewelry [3] Clothing");
                System.out.println("[4] Documents [5] Keys [6] Toys [7] Other");
                int cat = getValidIntInput(sc, "Choice: ", 1, 7);
                String[] cats = {"Electronics", "Jewelry", "Clothing", "Documents", "Keys", "Toys", "Other"};
                results = repo.searchLostItemsByCategory(cats[cat - 1]);
                break;
            case 4:
                System.out.print("\nEnter keyword: ");
                String keyword = sc.nextLine().trim();
                if (keyword.isEmpty()) {
                    System.out.println("[!] Keyword cannot be empty.");
                    return;
                }
                results = repo.searchLostItemsByKeyword(keyword);
                break;
            case 5:
                int custID = getValidIntInput(sc, "\nEnter customer ID: ", 10000, 99999);
                results = searchByCustomerHistory(custID);
                break;
        }

        displaySearchResults(results);
        pauseScreen(sc);
    }

    /**
     * Search by customer's room history
     * @param customerID The customer ID
     * @return Search results
     */
    private static String[][] searchByCustomerHistory(int customerID) {
        System.out.println("\n🔍 Searching items from rooms you've stayed in...");

        // Get customer's past room bookings
        int[] pastRooms = repo.getGuestRoomHistory(customerID);

        if (pastRooms.length == 0) {
            System.out.println("ℹ️  No room history found for this customer.");
            return null;
        }

        System.out.print("Rooms you've stayed in: ");
        for (int room : pastRooms) {
            System.out.print(room + " ");
        }
        System.out.println();

        return repo.searchLostItemsByRooms(pastRooms);
    }

    /**
     * Display search results
     * @param results The search results
     */
    private static void displaySearchResults(String[][] results) {
        if (results == null || results.length == 0) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              🔍 NO MATCHING ITEMS FOUND                      ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  Tips:                                                       ║");
            System.out.println("║  • Check if the item was left in a different room            ║");
            System.out.println("║  • Contact front desk for items not yet logged               ║");
            System.out.println("║  • Items may take 24 hours to appear in system               ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            return;
        }

        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║                    🔍 FOUND %d ITEM(S)%n", results.length);
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");
        System.out.printf("║ %-8s │ %-10s │ %-12s │ %-20s │ %-12s │ %-15s ║%n",
                "Item ID", "Room", "Category", "Description", "Date Found", "Status");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        for (String[] item : results) {
            String desc = item[3].length() > 18 ? item[3].substring(0, 15) + "..." : item[3];
            System.out.printf("║ %-8s │ %-10s │ %-12s │ %-20s │ %-12s │ %-15s ║%n",
                    item[0], item[1], item[2], desc, item[4], item[5]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
        
        System.out.println("\n💡 To claim an item, use option [3] and provide the Item ID.");
    }

    /**
     * Claim a lost item
     * @param sc Scanner instance
     */
    private static void claimItem(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              📦 CLAIM ITEM                                   ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int itemID = getValidIntInput(sc, "\nEnter Item ID: ", 1, 999999);

        // Get item details
        String[] item = repo.getLostItemDetails(itemID);

        if (item == null) {
            System.out.println("\n❌ Item not found. Please check the Item ID.");
            pauseScreen(sc);
            return;
        }

        if (!"FOUND".equals(item[5])) {
            System.out.println("\n⚠️  This item has already been " + item[5].toLowerCase() + ".");
            pauseScreen(sc);
            return;
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              📋 ITEM DETAILS                                 ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Item ID: %-50s ║%n", item[0]);
        System.out.printf("║  Found in Room: %-46s ║%n", item[1]);
        System.out.printf("║  Category: %-49s ║%n", item[2]);
        System.out.printf("║  Description: %-46s ║%n", item[3]);
        System.out.printf("║  Date Found: %-47s ║%n", item[4]);
        System.out.printf("║  Storage: %-50s ║%n", item[6]);
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        System.out.print("\nIs this your item? [1] Yes [2] No: ");
        int confirm = getValidIntInput(sc, "", 1, 2);

        if (confirm != 1) {
            System.out.println("\nClaim cancelled.");
            pauseScreen(sc);
            return;
        }

        int customerID = getValidIntInput(sc, "\nEnter your Customer ID: ", 10000, 99999);

        System.out.print("Enter your full name: ");
        String claimantName = sc.nextLine().trim();
        while (claimantName.isEmpty()) {
            System.out.print("[!] Name cannot be empty. Please enter: ");
            claimantName = sc.nextLine().trim();
        }

        System.out.print("Contact number: ");
        String contact = sc.nextLine().trim();

        // Verify identity
        String registeredName = repo.getCustomerName(customerID);
        if (!claimantName.equalsIgnoreCase(registeredName)) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              ⚠️  NAME MISMATCH                               ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  The name doesn't match our records.                         ║");
            System.out.println("║  Please visit the front desk with valid ID to claim.         ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            pauseScreen(sc);
            return;
        }

        // Process claim
        if (repo.claimItem(itemID, customerID, claimantName)) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║              ✅ ITEM CLAIMED SUCCESSFULLY!                   ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  Please collect your item from the Front Desk.               ║");
            System.out.printf("║  Location: %-49s ║%n", item[6]);
            System.out.println("║  Bring a valid ID for verification.                          ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        } else {
            System.out.println("\n❌ Failed to claim item. Please try again or visit front desk.");
        }
        
        pauseScreen(sc);
    }

    /**
     * View all unclaimed items
     */
    private static void viewUnclaimedItems() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                         UNCLAIMED ITEMS                                ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");

        String[][] items = repo.getUnclaimedItems();

        if (items == null || items.length == 0) {
            System.out.println("\n  No unclaimed items in the system.");
            return;
        }

        System.out.printf("\n  Total unclaimed items: %d%n", items.length);
        System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║ %-8s │ %-10s │ %-12s │ %-20s │ %-12s │ %-10s ║%n",
                "Item ID", "Room", "Category", "Description", "Found Date", "Days Old");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        LocalDate today = LocalDate.now();
        int oldItems = 0;

        for (String[] item : items) {
            LocalDate foundDate = LocalDate.parse(item[4]);
            long daysOld = ChronoUnit.DAYS.between(foundDate, today);

            if (daysOld >= DISPOSAL_DAYS) {
                oldItems++;
            }

            String desc = item[3].length() > 18 ? item[3].substring(0, 15) + "..." : item[3];
            System.out.printf("║ %-8s │ %-10s │ %-12s │ %-20s │ %-12s │ %-10s ║%n",
                    item[0], item[1], item[2], desc, item[4], daysOld);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");

        if (oldItems > 0) {
            System.out.printf("\n⚠️  %d item(s) are %d+ days old and eligible for disposal.%n", oldItems, DISPOSAL_DAYS);
        }
    }

    /**
     * View claimed items history
     */
    private static void viewClaimedItems() {
        System.out.println("\n╔════════════════════════════════════════════════════════════════════════╗");
        System.out.println("║                      CLAIMED ITEMS HISTORY                             ║");
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");

        String[][] items = repo.getClaimedItems();

        if (items == null || items.length == 0) {
            System.out.println("\n  No claimed items on record.");
            return;
        }

        System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║ %-8s │ %-12s │ %-20s │ %-15s │ %-12s ║%n",
                "Item ID", "Category", "Description", "Claimed By", "Claim Date");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        for (String[] item : items) {
            String desc = item[2].length() > 18 ? item[2].substring(0, 15) + "..." : item[2];
            System.out.printf("║ %-8s │ %-12s │ %-20s │ %-15s │ %-12s ║%n",
                    item[0], item[1], desc, item[3], item[4]);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");
    }

    /**
     * Admin: Process disposal of old items
     * @param sc Scanner instance
     */
    private static void processDisposal(Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              🗑️  PROCESS DISPOSAL                            ║");
        System.out.println("║         (Admin only - Items held for 30+ days)               ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        String[][] oldItems = repo.getItemsForDisposal();

        if (oldItems == null || oldItems.length == 0) {
            System.out.println("\nℹ️  No items eligible for disposal.");
            pauseScreen(sc);
            return;
        }

        System.out.printf("\n  Items eligible for disposal: %d%n", oldItems.length);
        System.out.println("╔════════════════════════════════════════════════════════════════════════╗");
        System.out.printf("║ %-8s │ %-12s │ %-20s │ %-12s │ %-8s ║%n",
                "Item ID", "Category", "Description", "Found Date", "Days");
        System.out.println("╠════════════════════════════════════════════════════════════════════════╣");

        LocalDate today = LocalDate.now();
        for (String[] item : oldItems) {
            LocalDate foundDate = LocalDate.parse(item[3]);
            long days = ChronoUnit.DAYS.between(foundDate, today);
            String desc = item[2].length() > 18 ? item[2].substring(0, 15) + "..." : item[2];
            System.out.printf("║ %-8s │ %-12s │ %-20s │ %-12s │ %-8s ║%n",
                    item[0], item[1], desc, item[3], days);
        }
        System.out.println("╚════════════════════════════════════════════════════════════════════════╝");

        int itemID = getValidIntInput(sc, "\nEnter Item ID to mark as DISPOSED (0 to cancel): ", 0, 999999);

        if (itemID == 0) {
            System.out.println("\nDisposal cancelled.");
            pauseScreen(sc);
            return;
        }

        System.out.print("Reason for disposal: ");
        String reason = sc.nextLine().trim();
        while (reason.isEmpty()) {
            System.out.print("[!] Reason cannot be empty. Please enter: ");
            reason = sc.nextLine().trim();
        }

        if (repo.markItemDisposed(itemID, reason)) {
            System.out.println("\n✅ Item " + itemID + " marked as DISPOSED.");
            System.out.println("  Reason: " + reason);
        } else {
            System.out.println("\n❌ Failed to mark item as disposed.");
        }
        
        pauseScreen(sc);
    }

    /**
     * Quick check for housekeeping - during room checkout
     * @param roomNumber The room number
     * @param staffName The staff name
     * @param sc Scanner instance
     */
    public static void quickCheckDuringCleaning(int roomNumber, String staffName, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.printf("║         🧹 ROOM %d CHECK%n", roomNumber);
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        int found = getValidIntInput(sc, "\nAny items left behind? [1] Yes [2] No: ", 1, 2);

        if (found == 1) {
            System.out.println("\nOpening item report form...");
            
            System.out.print("Item description: ");
            String desc = sc.nextLine().trim();
            while (desc.isEmpty()) {
                System.out.print("[!] Description cannot be empty. Please enter: ");
                desc = sc.nextLine().trim();
            }

            String today = LocalDate.now().toString();
            int itemID = repo.saveFoundItem(roomNumber, today, "Unknown", desc, staffName, "Front Desk");

            if (itemID != -1) {
                System.out.println("\n✅ Item logged. ID: " + itemID);
                System.out.println("   Please bring item to Front Desk with ID tag.");
            } else {
                System.out.println("\n❌ Failed to log item.");
            }
        }
    }

    // Helper methods
    
    private static String getValidDate(Scanner sc, String prompt) {
        System.out.print(prompt);
        String dateStr = sc.nextLine().trim();
        
        try {
            LocalDate date = LocalDate.parse(dateStr);
            return dateStr;
        } catch (Exception e) {
            System.out.println("[!] Invalid date format. Please use YYYY-MM-DD.");
            return null;
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
}
