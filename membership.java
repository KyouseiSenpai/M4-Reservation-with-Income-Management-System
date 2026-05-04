import java.util.Scanner;

/**
 * Membership Class - Handles membership upgrades and benefits
 */
public class membership {

    private static final Repository repo = Repository.getInstance();
    private static final double UPGRADE_FEE = 150.0;
    private static final double VIP_DISCOUNT_RATE = 0.20;

    public static void membershipUpgrade(int customerID, Scanner sc) {
        String currentMembership = repo.getMembershipType(customerID);
        String customerName = repo.getCustomerName(customerID);

        System.out.println("\n+==============================================================+");
        System.out.println("|              MEMBERSHIP UPGRADE                              |");
        System.out.println("+==============================================================+");
        System.out.println("|                                                              |");
        System.out.println("|  Current Status: " + padRight(currentMembership, 40) + "|");
        System.out.println("|                                                              |");

        if ("VIP".equals(currentMembership)) {
            System.out.println("|  * You are already a VIP member!                             |");
            System.out.println("|                                                              |");
            System.out.println("|  Your VIP Benefits:                                          |");
            displayVIPBenefits();
            System.out.println("+==============================================================+");
            pauseScreen(sc);
            return;
        }

        System.out.println("|  * UPGRADE TO VIP MEMBERSHIP!                                |");
        System.out.println("|                                                              |");
        System.out.println("|  VIP Benefits Include:                                       |");
        displayVIPBenefits();
        System.out.println("|                                                              |");
        System.out.printf ("|  Upgrade Fee: PHP %.2f (One-time payment)%n", UPGRADE_FEE);
        System.out.println("|                                                              |");
        System.out.println("+==============================================================+");

        System.out.println("\n+----------------------------------+");
        System.out.println("|  Would you like to upgrade?      |");
        System.out.println("+----------------------------------+");
        System.out.println("|  [1] Yes, upgrade to VIP         |");
        System.out.println("|  [2] No, stay as Regular         |");
        System.out.println("|  [0] Back / Cancel               |");
        System.out.println("+----------------------------------+");

        int upgradeChoice = getValidIntInput(sc, "Enter your choice: ", 0, 2);

        if (upgradeChoice == 0) {
            System.out.println("\n* Returning to previous menu.");
            pauseScreen(sc);
            return;
        }
        if (upgradeChoice == 1) {
            processUpgrade(customerID, sc);
        } else {
            System.out.println("\n* Upgrade was not completed.");
            System.out.println("You remain a Regular member.");
            System.out.println("You can upgrade anytime from the main portal.");
            pauseScreen(sc);
        }
    }

    private static void displayVIPBenefits() {
        System.out.println("|    * 20% discount on all services (Permanent)                |");
        System.out.println("|    * Priority Lane access (Permanent)                        |");
        System.out.println("|    * VIP Lounge, Early Entry & Exclusive Events              |");
        System.out.println("|    * 2x Points on every transaction (Permanent)              |");
        System.out.println("|    * 1 Free VIP Ticket freebie (One-time)                    |");
    }

    /**
     * M4: Process upgrade with Payment Framework integration
     */
    private static void processUpgrade(int customerID, Scanner sc) {
        System.out.println("\n+------------------------------------------+");
        System.out.println("|  Select payment method for upgrade:      |");
        System.out.println("+------------------------------------------+");
        System.out.println("|  [1] Credit / Debit Card                 |");
        System.out.println("|  [2] E-Wallet (GCash/PayMaya/GrabPay)    |");
        System.out.println("+------------------------------------------+");

        int method = getValidIntInput(sc, "Choose: ", 1, 2);

        boolean paymentSuccess = false;

        if (method == 1) {
            paymentSuccess = CreditCardPayment.process(UPGRADE_FEE, 0.0, sc);
        } else {
            paymentSuccess = OnlinePayment.process(UPGRADE_FEE, 0.0, sc);
        }

        if (!paymentSuccess) {
            System.out.println("\n* Payment failed. Upgrade cancelled.");
            pauseScreen(sc);
            return;
        }

        // Record in Finance Manager
        FinanceManager.recordMembershipTransaction(UPGRADE_FEE,
                method == 1 ? "Credit/Debit Card" : "E-Wallet");

        boolean updated = repo.updateMembershipType(customerID, "VIP");

        if (updated) {
            System.out.println("\n+==============================================================+");
            System.out.println("|         * UPGRADE SUCCESSFUL!                                |");
            System.out.println("+==============================================================+");
            System.out.println("|                                                              |");
            System.out.println("|  Your Membership Status is now: VIP                          |");
            System.out.println("|                                                              |");
            System.out.println("|  * You have received 1 freebie count as a VIP benefit!       |");
            System.out.println("|                                                              |");
            System.out.println("|  Thank you for upgrading! Enjoy your VIP privileges!         |");
            System.out.println("|                                                              |");
            System.out.println("+==============================================================+");
        } else {
            System.out.println("\n* Upgrade failed. Please try again or contact support.");
        }

        // Post-upgrade refund option
        showMembershipRefundOption(customerID, sc);
    }

    /**
     * Show refund option for membership upgrade
     */
    private static void showMembershipRefundOption(int customerID, Scanner sc) {
        System.out.println("\n+--------------------------------------------------------------+");
        System.out.println("|         MEMBERSHIP UPGRADE COMPLETE                          |");
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|  [1] Request Refund for Upgrade                              |");
        System.out.println("|  [2] Back to Portal (Done)                                   |");
        System.out.println("+--------------------------------------------------------------+");

        int choice = getValidIntInput(sc, "Choice: ", 1, 2);

        if (choice == 1) {
            System.out.println("\n+==============================================================+");
            System.out.println("|         REQUEST MEMBERSHIP UPGRADE REFUND                    |");
            System.out.println("+==============================================================+");
            System.out.println("|  Note: VIP benefits will be revoked upon refund approval.    |");
            System.out.println("+==============================================================+");

            System.out.println("\n[1] Continue with refund request");
            System.out.println("[2] Go back");
            int confirm = getValidIntInput(sc, "Choice: ", 1, 2);

            if (confirm == 1) {
                System.out.print("\nReason for refund: ");
                String reason = sc.nextLine().trim();
                while (reason.isEmpty()) {
                    System.out.print("[!] Please provide a reason: ");
                    reason = sc.nextLine().trim();
                }

                String customerName = repo.getCustomerName(customerID);
                boolean submitted = FinanceManager.processRefund(
                    UPGRADE_FEE, reason, customerID, customerName, "MEMBERSHIP", "Online Payment"
                );

                if (submitted) {
                    System.out.println("\n+==============================================================+");
                    System.out.println("|      * UPGRADE REFUND REQUEST SUBMITTED!                     |");
                    System.out.println("|                                                              |");
                    System.out.printf("|  Refund Amount: PHP %40.2f  |%n", UPGRADE_FEE);
                    System.out.println("|  Status: Under review by admin                               |");
                    System.out.println("+==============================================================+");
                } else {
                    System.out.println("\n* Failed to submit refund request.");
                }
                pauseScreen(sc);
            }
        }
    }

    public static void handleQueueAndDiscount(int customerID, double totalAmount) {
        String membershipType = repo.getMembershipType(customerID);

        System.out.println("\n+==============================================================+");

        if ("VIP".equals(membershipType)) {
            System.out.println("|              * VIP ACCESS                                    |");
            System.out.println("+==============================================================+");
            System.out.println("|                                                              |");
            System.out.println("|  * Proceeding directly to counter. No queue!                 |");
            System.out.println("|                                                              |");

            double discount = totalAmount * VIP_DISCOUNT_RATE;
            double finalAmount = totalAmount - discount;

            System.out.printf("|  Original Amount : PHP %37.2f  |%n", totalAmount);
            System.out.printf("|  VIP Discount    : PHP %37.2f  |%n", discount);
            System.out.printf("|  Final Amount    : PHP %37.2f  |%n", finalAmount);
        } else {
            System.out.println("|              REGULAR ACCESS                                  |");
            System.out.println("+==============================================================+");
            System.out.println("|                                                              |");
            System.out.println("|  Please proceed to the queue.                                |");
            System.out.printf("|  Total Amount: PHP %41.2f  |%n", totalAmount);
            System.out.println("|                                                              |");
            System.out.println("|  * Tip: Upgrade to VIP for 20% discount and priority access! |");
        }

        System.out.println("|                                                              |");
        System.out.println("+==============================================================+");
    }

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
                    System.out.print("[!] Please enter " + min + " or " + max + ": ");
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

    private static String padRight(String s, int n) {
        if (s == null) s = "";
        return String.format("%-" + n + "s", s);
    }
}
