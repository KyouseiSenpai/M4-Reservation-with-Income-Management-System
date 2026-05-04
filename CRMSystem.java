import java.util.Scanner;

/**
 * CRM System Class - Customer Relationship Management
 */
public class CRMSystem {

    private static final Repository repo = Repository.getInstance();
    private static final int REGULAR_TICKET_POINTS = 500;
    private static final int VIP_TICKET_POINTS = 1000;

    /**
     * Show CRM menu
     * @param customerID The customer ID
     * @param sc Scanner instance
     */
    public static void showCRM(int customerID, Scanner sc) {
        boolean running = true;

        while (running) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║         👤 CUSTOMER RELATIONSHIP SERVICES                    ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.println("║  [1] View Customer Profile                                   ║");
            System.out.println("║  [2] Redeem Points                                           ║");
            System.out.println("║  [3] Cancel Appointment                                      ║");
            System.out.println("║  [4] Move Appointment                                        ║");
            System.out.println("║  [5] Request a Refund                                        ║");
            System.out.println("║  [0] Back to Portal                                          ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
            
            int choice = getValidIntInput(sc, "Select an option: ", 0, 5);

            switch (choice) {
                case 1:
                    showProfile(customerID);
                    pauseScreen(sc);
                    break;
                case 2:
                    redeemPoints(customerID, sc);
                    break;
                case 3:
                    cancelAppointment(customerID, sc);
                    break;
                case 4:
                    moveAppointment(customerID, sc);
                    break;
                case 5:
                    requestRefundFromCRM(customerID, sc);
                    break;
                case 0:
                    System.out.println("\nReturning to Portal...");
                    running = false;
                    break;
                default:
                    System.out.println("\n[!] Invalid choice. Please try again.");
            }
        }
    }

    /**
     * Request a refund from CRM menu
     */
    private static void requestRefundFromCRM(int customerID, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              💰 REQUEST A REFUND                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // Show recent transactions for context
        transactionHistory.showHistory(customerID);

        System.out.println("\n+--------------------------------------------------------------+");
        System.out.println("|  What type of refund?                                        |");
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|  [1] Ticket Purchase Refund                                  |");
        System.out.println("|  [2] Room Booking Refund                                     |");
        System.out.println("|  [3] Membership Upgrade Refund                               |");
        System.out.println("|  [0] Back to CRM Menu                                        |");
        System.out.println("+--------------------------------------------------------------+");

        int refundType = getValidIntInput(sc, "Choice: ", 0, 3);
        if (refundType == 0) return;

        String typeStr;
        double defaultAmount = 0;
        switch (refundType) {
            case 1:
                typeStr = "TICKET";
                defaultAmount = 500.0;
                break;
            case 2:
                typeStr = "ROOM";
                defaultAmount = 2500.0;
                break;
            case 3:
                typeStr = "MEMBERSHIP";
                defaultAmount = 150.0;
                break;
            default:
                return;
        }

        System.out.print("\nEnter refund amount (PHP): ");
        double amount = 0;
        boolean validAmount = false;
        while (!validAmount) {
            try {
                amount = Double.parseDouble(sc.nextLine().trim());
                if (amount > 0 && amount <= defaultAmount * 10) {
                    validAmount = true;
                } else {
                    System.out.print("[!] Enter a valid positive amount: ");
                }
            } catch (NumberFormatException e) {
                System.out.print("[!] Enter a valid number: ");
            }
        }

        System.out.print("Reason for refund: ");
        String reason = sc.nextLine().trim();
        while (reason.isEmpty()) {
            System.out.print("[!] Please provide a reason: ");
            reason = sc.nextLine().trim();
        }

        String customerName = repo.getCustomerName(customerID);

        boolean submitted = FinanceManager.processRefund(amount, reason, customerID, customerName, typeStr, "Online Payment");

        if (submitted) {
            System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
            System.out.println("║      ✅ REFUND REQUEST SUBMITTED!                            ║");
            System.out.println("╠══════════════════════════════════════════════════════════════╣");
            System.out.printf("║  Amount: PHP %47.2f ║%n", amount);
            System.out.println("║  Type:          " + padRight(typeStr, 45) + "║");
            System.out.println("║  Status:        Pending admin approval                       ║");
            System.out.println("║                                                              ║");
            System.out.println("║  You will be notified once your refund is processed.         ║");
            System.out.println("╚══════════════════════════════════════════════════════════════╝");
        } else {
            System.out.println("\n❌ Failed to submit refund request.");
        }

        pauseScreen(sc);
    }

    // Helper for padding
    private static String padRight(String s, int n) {
        if (s == null) s = "";
        if (s.length() > n) return s.substring(0, n - 3) + "...";
        return String.format("%-" + n + "s", s);
    }

    /**
     * Show customer profile
     * @param customerID The customer ID
     */
    private static void showProfile(int customerID) {
        String name = repo.getCustomerName(customerID);
        String membership = repo.getMembershipType(customerID);
        int points = repo.getPoints(customerID);
        int freebies = repo.getFreebiesCount(customerID);

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              👤 CUSTOMER PROFILE                             ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Customer ID : %-46d ║%n", customerID);
        System.out.printf("║  Name        : %-46s ║%n", name);
        System.out.printf("║  Membership  : %-46s ║%n", membership);
        System.out.printf("║  Points      : %-46d ║%n", points);
        System.out.printf("║  Freebies    : %-46d ║%n", freebies);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Transaction History:                                        ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        transactionHistory.showHistory(customerID);
    }

    /**
     * Redeem points for rewards
     * @param customerID The customer ID
     * @param sc Scanner instance
     */
    private static void redeemPoints(int customerID, Scanner sc) {
        int points = repo.getPoints(customerID);

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              🎁 REDEEM POINTS                                ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Current Points: %-44d ║%n", points);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║  Available Rewards:                                          ║");
        System.out.printf("║  [1] Regular Ticket (%d points)%n", REGULAR_TICKET_POINTS);
        System.out.printf("║  [2] VIP Ticket     (%d points)%n", VIP_TICKET_POINTS);
        System.out.println("║  [0] Cancel");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        int choice = getValidIntInput(sc, "Choose: ", 0, 2);

        if (choice == 1) {
            if (points >= REGULAR_TICKET_POINTS) {
                if (repo.deductPoints(customerID, REGULAR_TICKET_POINTS)) {
                    System.out.println("\n✅ Redeemed 1 Free Regular Ticket!");
                    System.out.printf("Remaining Points: %d pts%n", repo.getPoints(customerID));
                } else {
                    System.out.println("\n❌ Failed to redeem points. Please try again.");
                }
            } else {
                System.out.printf("\n❌ Not enough points. Need %d more pts.%n", 
                    REGULAR_TICKET_POINTS - points);
            }
        } else if (choice == 2) {
            if (points >= VIP_TICKET_POINTS) {
                if (repo.deductPoints(customerID, VIP_TICKET_POINTS)) {
                    System.out.println("\n✅ Redeemed 1 Free VIP Ticket!");
                    System.out.printf("Remaining Points: %d pts%n", repo.getPoints(customerID));
                } else {
                    System.out.println("\n❌ Failed to redeem points. Please try again.");
                }
            } else {
                System.out.printf("\n❌ Not enough points. Need %d more pts.%n", 
                    VIP_TICKET_POINTS - points);
            }
        } else {
            System.out.println("\nRedemption cancelled.");
        }
        
        pauseScreen(sc);
    }

    /**
     * Cancel an appointment
     * @param customerID The customer ID
     * @param sc Scanner instance
     */
    private static void cancelAppointment(int customerID, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              ❌ CANCEL APPOINTMENT                           ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        appointmentSystem.showAppointments(customerID);

        int num = getValidIntInput(sc, "\nEnter appointment number to cancel (0 to go back): ", 0, 100);

        if (num == 0) return;

        System.out.print("\nAre you sure you want to cancel this appointment? [1] Yes [2] No: ");
        int confirm = getValidIntInput(sc, "", 1, 2);

        if (confirm == 1) {
            boolean cancelled = appointmentSystem.cancelAppointment(customerID, num);
            if (cancelled) {
                System.out.println("\n✅ Appointment cancelled successfully.");
            } else {
                System.out.println("\n❌ Invalid appointment number.");
            }
        } else {
            System.out.println("\nCancellation aborted.");
        }
        
        pauseScreen(sc);
    }

    /**
     * Move an appointment to a new date
     * @param customerID The customer ID
     * @param sc Scanner instance
     */
    private static void moveAppointment(int customerID, Scanner sc) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              📅 MOVE APPOINTMENT                             ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        appointmentSystem.showAppointments(customerID);

        int num = getValidIntInput(sc, "\nEnter appointment number to move (0 to go back): ", 0, 100);

        if (num == 0) return;

        String newDate = appointmentSystem.pickDate(sc);
        if (newDate == null) {
            System.out.println("\n❌ Invalid date selection.");
            return;
        }

        boolean moved = appointmentSystem.moveAppointment(customerID, num, newDate);
        if (moved) {
            System.out.println("\n✅ Appointment moved to " + newDate);
        } else {
            System.out.println("\n❌ Invalid appointment number.");
        }
        
        pauseScreen(sc);
    }

    // Helper methods
    
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
