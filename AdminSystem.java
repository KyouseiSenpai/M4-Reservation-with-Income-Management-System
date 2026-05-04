/**
 * Administrator System - Controls administrative functions for the resort
 * All database operations go through Repository
 *
 * NEW UPDATES TO THE SYSTEM
 * - Added full admin dashboard with Finance Management (M4)
 * - Added Facility Management, Lost & Found, and Financial Reports
 */

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class adminSystem {
    private static final Logger LOGGER = Logger.getLogger(adminSystem.class.getName());
    private static Repository repository = Repository.getInstance();

    /**
     * Show admin login screen - Entry point called from Main.java case 9
     * @param scanner Shared Scanner instance
     */
    public static void showAdminLogin(Scanner scanner) {
        System.out.println("\n+==============================================================+");
        System.out.println("|                                                              |");
        System.out.println("|              STAFF LOGIN PORTAL                              |");
        System.out.println("|                                                              |");
        System.out.println("+==============================================================+");

        System.out.print("\nEnter Admin ID: ");
        String inputID = scanner.nextLine();
        System.out.print("Enter Password: ");
        String inputPass = scanner.nextLine();

        if (!inputID.matches("\\d{6}")) {
            System.out.println("\n[!] Invalid ID format! Must be 6 digits.");
            pauseScreen(scanner);
            return;
        }

        if (inputID.equals("999999") && inputPass.equals("admin123")) {
            LOGGER.info("Admin 999999 logged in successfully");
            System.out.println("\n✅ Login Successful!");
            pauseScreen(scanner);
            showAdminDashboard(scanner);
        } else {
            LOGGER.warning("Failed login attempt for ID: " + inputID);
            System.out.println("\n❌ Authentication failed. Invalid credentials.");
            pauseScreen(scanner);
        }
    }

    /**
     * Main admin dashboard with full menu
     * @param scanner Shared Scanner instance
     */
    private static void showAdminDashboard(Scanner scanner) {
        boolean running = true;

        while (running) {
            displayAdminMenu();
            int choice = getValidIntInput(scanner, "Enter choice: ", 0, 7);

            switch (choice) {
                case 1:
                    searchAllCustomers();
                    pauseScreen(scanner);
                    break;
                case 2:
                    FinanceManager.generateFinancialReport();
                    pauseScreen(scanner);
                    break;
                case 3:
                    FacilitySystem.showFacilityMenu(scanner);
                    break;
                case 4:
                    RoomControlSystem.showAllRoomStatuses();
                    pauseScreen(scanner);
                    break;
                case 5:
                    SmartRoomAssignment.showAssignmentAnalytics();
                    pauseScreen(scanner);
                    break;
                case 6:
                    transactionHistory.showHistory(-1); // -1 shows all
                    pauseScreen(scanner);
                    break;
                case 7:
                    LostFoundSystem.showLostFoundMenu(scanner);
                    break;
                case 0:
                    System.out.println("\nLogging out...");
                    System.out.println("Goodbye, Admin!");
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice.");
            }
        }
    }

    private static void displayAdminMenu() {
        System.out.println("\n+==============================================================+");
        System.out.println("|              ADMIN DASHBOARD                                 |");
        System.out.println("+==============================================================+");
        System.out.println("|                                                              |");
        System.out.println("|  [1] View All Registered Customers                           |");
        System.out.println("|  [2] Financial Report / Income Statement                     |");
        System.out.println("|  [3] Facility & Housekeeping Management                      |");
        System.out.println("|  [4] Room Status Dashboard                                   |");
        System.out.println("|  [5] Smart Assignment Analytics                              |");
        System.out.println("|  [6] View All Transaction History                            |");
        System.out.println("|  [7] Lost & Found Center                                     |");
        System.out.println("|                                                              |");
        System.out.println("|  [0] Logout                                                  |");
        System.out.println("|                                                              |");
        System.out.println("+==============================================================+");
    }

    private static void searchAllCustomers() {
        String[][] customers = repository.getAllCustomers();

        if (customers == null || customers.length == 0) {
            System.out.println("\nNo registered customers found.");
            return;
        }

        System.out.println("\n══════════ All Registered Customers ══════════");
        System.out.printf("%-8s %-15s %-14s %-4s %-12s %-20s %-20s%n",
                "ID", "Name", "Contact", "Age", "Created", "Last Login", "Last Logout");
        System.out.println("───────────────────────────────────────────────────────────────────────────────────────");

        for (String[] c : customers) {
            String loginTime = c[5] != null ? c[5] : "Never";
            String logoutTime = c[6] != null ? c[6] : "N/A";
            System.out.printf("%-8s %-15s %-14s %-4s %-12s %-20s %-20s%n",
                    c[0], c[1], c[2], c[3], c[4], loginTime, logoutTime);
        }
        System.out.println("═══════════════════════════════════════════════════════════════════════════════════════");
        System.out.println("Total customers: " + customers.length);
    }

    private static int getValidIntInput(Scanner scanner, String prompt, int min, int max) {
        System.out.print(prompt);
        int input = -1;
        boolean valid = false;

        while (!valid) {
            try {
                input = scanner.nextInt();
                scanner.nextLine();
                if (input >= min && input <= max) {
                    valid = true;
                } else {
                    System.out.print("[!] Enter " + min + "-" + max + ": ");
                }
            } catch (Exception e) {
                System.out.print("[!] Invalid input. Enter a number: ");
                scanner.nextLine();
            }
        }
        return input;
    }

    private static void pauseScreen(Scanner scanner) {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
