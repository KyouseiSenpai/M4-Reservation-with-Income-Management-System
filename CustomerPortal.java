import java.util.Scanner;

/**
 * Customer Portal Class - Main dashboard for logged-in customers
 */
public class customerPortal {

    private static final Repository repo = Repository.getInstance();
    private static QueueChecker queueChecker;

    /**
     * Display the customer portal menu
     * @param customerID The logged-in customer's ID
     * @param sc Scanner instance for input
     */
    public static void portalMenu(int customerID, Scanner sc) {
        // Initialize queue checker
        queueChecker = new QueueChecker();
        
        int checkInChoice;
        boolean loggedIn = true;

        do {
            displayPortalMenu(customerID);
            checkInChoice = getValidIntInput(sc, "Enter your choice: ", 0, 8);

            switch (checkInChoice) {
                case 1:
                    checkIn.startCheckIn(customerID, queueChecker, sc);
                    break;
                    
                case 2:
                    queueChecker.showQueueStatus(customerID);
                    pauseScreen(sc);
                    break;
                    
                case 3:
                    CRMSystem.showCRM(customerID, sc);
                    break;
                    
                case 4:
                    membership.membershipUpgrade(customerID, sc);
                    break;
                    
                case 5:
                    appointmentSystem.showAppointments(customerID);
                    pauseScreen(sc);
                    break;
                    
                case 6:
                    FacilitySystem.bookRoomForCustomer(customerID, sc);
                    break;
                    
                case 7:
                    FacilitySystem.viewMyBookings(customerID);
                    pauseScreen(sc);
                    break;
                    
                case 8:
                    RoomControlSystem.showRoomControls(customerID, sc);
                    break;
                    
                case 0:
                    loggedIn = handleLogout(customerID, sc);
                    break;
                    
                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
                    pauseScreen(sc);
            }

        } while (loggedIn);
    }

    /**
     * Display the portal menu
     */
    private static void displayPortalMenu(int customerID) {
        String customerName = repo.getCustomerName(customerID);
        String membershipType = repo.getMembershipType(customerID);
        int points = repo.getPoints(customerID);
        
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              🎢 CUSTOMER PORTAL                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Welcome, " + padRight(customerName, 45) + "║");
        System.out.println("║  Membership: " + padRight(membershipType, 42) + "║");
        System.out.println("║  Points: " + padRight(String.valueOf(points) + " pts", 46) + "║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║                                                              ║");
        System.out.println("║  🎫  [1] Check-In / Buy Ticket                               ║");
        System.out.println("║  ⏱️  [2] Check Queue Status                                  ║");
        System.out.println("║  👤  [3] Customer Relationship Services                      ║");
        System.out.println("║  ⭐  [4] Upgrade Membership                                  ║");
        System.out.println("║  📅  [5] My Appointments                                     ║");
        System.out.println("║  🏨  [6] Room Booking (Hotel)                                ║");
        System.out.println("║  🏠  [7] My Room Bookings                                    ║");
        System.out.println("║  🎛️  [8] Room Controls                                        ║");
        System.out.println("║                                                              ║");
        System.out.println("║  [0] Logout                                                  ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    /**
     * Handle logout process
     * @param customerID The customer ID to logout
     * @param sc Scanner instance
     * @return false to exit the menu loop
     */
    private static boolean handleLogout(int customerID, Scanner sc) {
        System.out.println("\n┌─────────────────────────────────┐");
        System.out.println("│         LOGOUT                  │");
        System.out.println("├─────────────────────────────────┤");
        System.out.println("│  Are you sure you want to       │");
        System.out.println("│  logout?                        │");
        System.out.println("│                                 │");
        System.out.println("│  [1] Yes, Logout                │");
        System.out.println("│  [2] No, Stay Logged In         │");
        System.out.println("└─────────────────────────────────┘");
        
        int confirm = getValidIntInput(sc, "Enter your choice: ", 1, 2);
        
        if (confirm == 1) {
            System.out.println("\nLogging out customer ID: " + customerID + "...");
            
            // Shutdown queue checker
            if (queueChecker != null) {
                queueChecker.shutdown();
            }
            
            // Track logout in database
            boolean loggedOut = repo.trackLogOut(customerID);
            
            if (loggedOut) {
                System.out.println("\n✅ Logout successful!");
                System.out.println("Thank you for visiting Theme Park Resort!");
                System.out.println("We hope to see you again soon!");
            } else {
                System.out.println("\n⚠️  Logout completed with warnings.");
            }
            
            pauseScreen(sc);
            return false; // Exit the menu loop
        } else {
            System.out.println("\nLogout cancelled. Returning to portal...");
            return true; // Continue the menu loop
        }
    }

    /**
     * Get valid integer input within range
     */
    private static int getValidIntInput(Scanner sc, String prompt, int min, int max) {
        int input = -1;
        boolean valid = false;
        
        while (!valid) {
            System.out.print(prompt);
            try {
                input = sc.nextInt();
                sc.nextLine(); // Consume newline
                
                if (input >= min && input <= max) {
                    valid = true;
                } else {
                    System.out.println("[!] Please enter a number between " + min + " and " + max + ".");
                }
            } catch (Exception e) {
                System.out.println("[!] Invalid input. Please enter a valid number.");
                sc.nextLine(); // Clear invalid input
            }
        }
        
        return input;
    }

    /**
     * Pause screen and wait for user
     */
    private static void pauseScreen(Scanner sc) {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
    
    /**
     * Pad string to the right
     */
    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() > n) s = s.substring(0, n - 3) + "...";
        return String.format("%-" + n + "s", s);
    }
}
