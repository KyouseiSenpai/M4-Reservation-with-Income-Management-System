/**
 * CheckIn Class - Handles ticket purchase and check-in process
 * SYSTEM UPDATES:
 * - Replaced simple payment simulation with PaymentFramework classes
 * - CreditCardPayment: validates 16-digit card numbers
 * - OnlinePayment: single unified class for GCash, PayMaya, GrabPay, etc.
 * - All transactions now go through the Payment Framework (VAT + Discount applied)
 */
import java.util.Scanner;

public class checkIn {

    private static final Repository repo = Repository.getInstance();
    private static final double REGULAR_TICKET_PRICE = 500.00;
    private static final double VIP_TICKET_PRICE = 800.00;
    private static final double VIP_DISCOUNT = 300.00;

    public static double totalAmount = 0;
    public static int totalPoints = 0;
    public static StringBuilder receiptLines = new StringBuilder();

    private static double getAgeDiscount(int age, double price) {
        if (age <= 3) return 0;
        if (age <= 12) return price * 0.50;
        if (age >= 60) return price * 0.80;
        return price;
    }

    private static String getAgeCategory(int age) {
        if (age <= 3) return "Infant (FREE)";
        if (age <= 12) return "Child (50% off)";
        if (age >= 60) return "Senior (20% off)";
        return "Adult";
    }

    public static void startCheckIn(int customerID, QueueChecker queueChecker, Scanner sc) {
        String membershipType = repo.getMembershipType(customerID);
        boolean isVIP = "VIP".equals(membershipType);
        String customerName = repo.getCustomerName(customerID);
        int customerAge = repo.getCustomerAge(customerID);

        totalAmount = 0;
        totalPoints = 0;
        receiptLines.setLength(0);

        System.out.println("\n+==============================================================+");
        System.out.println("|              BUY TICKET - YOUR TICKET                        |");
        System.out.println("+==============================================================+");
        System.out.println("\nWelcome, " + customerName + " (Age: " + customerAge + ")");
        System.out.println("Membership: " + membershipType);

        // Account Holder Ticket
        System.out.println("\n+----------------------------------+");
        System.out.println("|  Select your ticket type:        |");
        System.out.println("+----------------------------------+");
        System.out.printf("|  [1] Regular Ticket - PHP %.2f   |%n", REGULAR_TICKET_PRICE);
        System.out.printf("|  [2] VIP Ticket     - PHP %.2f   |%n", VIP_TICKET_PRICE);
        System.out.println("+----------------------------------+");

        int choice = getValidIntInput(sc, "Choose: ", 1, 2);

        double price;
        String label;
        int points;
        String ticketType;

        if (choice == 1) {
            price = REGULAR_TICKET_PRICE;
            label = "Regular Ticket";
            points = isVIP ? 100 : 50;
            ticketType = "Regular";
        } else {
            price = VIP_TICKET_PRICE;
            label = "VIP Ticket";
            int freebies = repo.getFreebiesCount(customerID);
            if (isVIP && freebies > 0) {
                price -= VIP_DISCOUNT;
                repo.useFreebies(customerID);
                System.out.println("\n* VIP Freebie applied! -PHP " + String.format("%.2f", VIP_DISCOUNT));
            }
            points = isVIP ? 200 : 100;
            ticketType = "VIP";
        }

        double originalPrice = price;
        price = getAgeDiscount(customerAge, price);
        String ageCategory = getAgeCategory(customerAge);

        if (price < originalPrice) {
            System.out.printf("\n* Age discount (%s): PHP %.2f -> PHP %.2f%n", ageCategory, originalPrice, price);
        }

        repo.saveTicketRecord(customerID, customerName, customerAge, ticketType, price);
        repo.updateTicketSummary(customerID, price);

        totalAmount += price;
        totalPoints += points;
        receiptLines.append(String.format("  %-20s | %-15s | %s | PHP %.2f | +%d pts%n",
                customerName, ageCategory, label, price, points));

        System.out.printf("\n* Your ticket added | +%d pts%n", points);

        // Guest Tickets
        handleGuestTickets(sc, isVIP, customerID);

        // Display summary
        displaySummary();

        // ===== M4: PAYMENT FRAMEWORK INTEGRATION =====
        String paymentStatus = handlePayment(sc, membershipType);

        // Schedule appointment
        String appointmentDate = appointmentSystem.pickDate(sc);
        if (appointmentDate != null) {
            appointmentSystem.saveAppointment(customerID, appointmentDate, paymentStatus);
            repo.saveAppointmentRecord(customerID, appointmentDate, paymentStatus);
        }

        // Add loyalty points
        repo.loyaltyPoints(customerID, totalPoints);

        // Show final receipt
        checkOut.showReceipt(customerID, membershipType, receiptLines,
                totalAmount, totalPoints, appointmentDate, paymentStatus);

        // Queue check-in if paid
        if ("PAID".equals(paymentStatus)) {
            queueChecker.checkInClass(customerID, "Theme Park Entry");
        }

        // Post-purchase options (back/refund)
        showPostPurchaseOptions(customerID, membershipType, totalAmount, paymentStatus, sc);
    }

    /**
     * Show post-purchase options including refund request
     */
    private static void showPostPurchaseOptions(int customerID, String membershipType,
                                                  double totalAmount, String paymentStatus, Scanner sc) {
        if (totalAmount <= 0) {
            pauseScreen(sc);
            return;
        }

        System.out.println("\n+--------------------------------------------------------------+");
        System.out.println("|         WHAT WOULD YOU LIKE TO DO NEXT?                      |");
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("|  [1] Request a Refund                                        |");
        System.out.println("|  [2] Back to Portal (Done)                                   |");
        System.out.println("+--------------------------------------------------------------+");

        int choice = getValidIntInput(sc, "Choice: ", 1, 2);

        if (choice == 1) {
            handleRefundRequest(customerID, membershipType, totalAmount, paymentStatus, sc);
        }
        // choice 2 just returns (goes back to portal)
    }

    /**
     * Handle refund request from customer
     */
    private static void handleRefundRequest(int customerID, String membershipType,
                                             double totalAmount, String paymentStatus, Scanner sc) {
        System.out.println("\n+==============================================================+");
        System.out.println("|              REQUEST A REFUND                                |");
        System.out.println("+==============================================================+");
        System.out.println("|  Refund Eligibility:                                         |");
        System.out.println("|  • Must be requested within 24 hours of purchase             |");
        System.out.println("|  • Tickets must not have been used                           |");
        System.out.println("|  • Admin approval required                                   |");
        System.out.println("+==============================================================+");

        System.out.println("\n[1] Continue with refund request");
        System.out.println("[2] Go back");
        int confirm = getValidIntInput(sc, "Choice: ", 1, 2);

        if (confirm == 2) {
            System.out.println("\n* Refund request cancelled.");
            pauseScreen(sc);
            return;
        }

        System.out.print("\nReason for refund (e.g., 'Changed plans', 'Wrong purchase'): ");
        String reason = sc.nextLine().trim();
        while (reason.isEmpty()) {
            System.out.print("[!] Please provide a reason: ");
            reason = sc.nextLine().trim();
        }

        String customerName = repo.getCustomerName(customerID);
        String paymentMethod = "PAID".equals(paymentStatus) ? "Online Payment" : "Pending Payment";

        boolean submitted = FinanceManager.processRefund(
            totalAmount, reason, customerID, customerName, "TICKET", paymentMethod
        );

        if (submitted) {
            System.out.println("\n+==============================================================+");
            System.out.println("|         * REFUND REQUEST SUBMITTED!                          |");
            System.out.println("+==============================================================+");
            System.out.println("|                                                              |");
            System.out.println("|  Your refund request has been submitted for review.          |");
            System.out.println("|  You will be contacted once it's processed.                  |");
            System.out.println("|                                                              |");
            System.out.printf("|  Refund Amount: PHP %40.2f  |%n", totalAmount);
            System.out.println("|                                                              |");
            System.out.println("+==============================================================+");
        } else {
            System.out.println("\n* Failed to submit refund request. Please contact support.");
        }

        pauseScreen(sc);
    }

    private static void handleGuestTickets(Scanner sc, boolean isVIP, int customerID) {
        System.out.println("\n+------------------------------------------+");
        System.out.println("|  Would you like to buy tickets for       |");
        System.out.println("|  someone else?                           |");
        System.out.println("+------------------------------------------+");
        System.out.println("|  [1] Yes                                 |");
        System.out.println("|  [2] No - Proceed to Payment             |");
        System.out.println("|  [0] Cancel - Go Back to Portal          |");
        System.out.println("+------------------------------------------+");

        int again = getValidIntInput(sc, "Enter your choice: ", 0, 2);

        if (again == 0) {
            System.out.println("\n* Ticket purchase cancelled.");
            totalAmount = 0;
            totalPoints = 0;
            receiptLines.setLength(0);
            return;
        }
        if (again == 1) {
            int personCount = getValidIntInput(sc, "\nHow many tickets? ", 1, 20);

            for (int i = 1; i <= personCount; i++) {
                System.out.println("\n+==============================================================+");
                System.out.printf("|              GUEST %d OF %d%n", i, personCount);
                System.out.println("+==============================================================+");

                String guestName = getValidStringInput(sc, "\nTicket holder name: ");
                int guestAge = getValidIntInput(sc, "Ticket holder age: ", 0, 120);

                System.out.println("\n+----------------------------------+");
                System.out.println("|  Select ticket type:             |");
                System.out.println("+----------------------------------+");
                System.out.printf("|  [1] Regular - PHP %.2f           |%n", REGULAR_TICKET_PRICE);
                System.out.printf("|  [2] VIP     - PHP %.2f           |%n", VIP_TICKET_PRICE);
                System.out.println("+----------------------------------+");

                int guestChoice = getValidIntInput(sc, "Choose: ", 1, 2);

                double guestPrice;
                String guestLabel;
                int guestPoints;
                String guestTicketType;

                if (guestChoice == 1) {
                    guestPrice = REGULAR_TICKET_PRICE;
                    guestLabel = "Regular Ticket";
                    guestPoints = isVIP ? 100 : 50;
                    guestTicketType = "Regular";
                } else {
                    guestPrice = VIP_TICKET_PRICE;
                    guestLabel = "VIP Ticket";
                    guestPoints = isVIP ? 200 : 100;
                    guestTicketType = "VIP";
                }

                double originalGuestPrice = guestPrice;
                guestPrice = getAgeDiscount(guestAge, guestPrice);
                String guestCategory = getAgeCategory(guestAge);

                if (guestPrice < originalGuestPrice) {
                    System.out.printf("\n* Age discount (%s): PHP %.2f -> PHP %.2f%n",
                            guestCategory, originalGuestPrice, guestPrice);
                }

                repo.saveTicketRecord(customerID, guestName, guestAge, guestTicketType, guestPrice);
                repo.updateTicketSummary(customerID, guestPrice);

                totalAmount += guestPrice;
                totalPoints += guestPoints;
                receiptLines.append(String.format("  %-20s | %-15s | %s | PHP %.2f | +%d pts%n",
                        guestName, guestCategory, guestLabel, guestPrice, guestPoints));

                System.out.printf("\n* Ticket added for %s (%s) | +%d pts%n",
                        guestName, guestCategory, guestPoints);
            }
        }
    }

    private static void displaySummary() {
        System.out.println("\n+==============================================================+");
        System.out.println("|                  PURCHASE SUMMARY                            |");
        System.out.println("+==============================================================+");
        System.out.println(receiptLines.toString());
        System.out.println("+--------------------------------------------------------------+");
        System.out.printf("|  Total Amount : PHP %40.2f  |%n", totalAmount);
        System.out.printf("|  Total Points : %44d  |%n", totalPoints);
        System.out.println("+==============================================================+");
    }

    /**
     * M4: Payment Framework Integration
     * All transactions now use PaymentFramework subclasses
     */
    private static String handlePayment(Scanner sc, String membershipType) {
        System.out.println("\n+==============================================================+");
        System.out.println("|                  PAYMENT                                     |");
        System.out.println("+==============================================================+");
        System.out.println("|  [1] Pay Online (GCash/PayMaya/Card)                         |");
        System.out.println("|  [2] Walk-In (Pay at Counter)                                |");
        System.out.println("+==============================================================+");

        int payChoice = getValidIntInput(sc, "Choose: ", 1, 2);

        if (payChoice == 1) {
            return handleOnlinePayment(sc, membershipType);
        } else {
            System.out.println("\n* Please pay at the counter on your appointment date.");
            return "PENDING";
        }
    }

    /**
     * M4: Uses Payment Framework - OnlinePayment (unified) or CreditCardPayment
     */
    private static String handleOnlinePayment(Scanner sc, String membershipType) {
        // Calculate discount rate based on membership
        double discountRate = "VIP".equals(membershipType) ? 0.20 : 0.0;

        System.out.println("\n+------------------------------------------+");
        System.out.println("|  Select payment method:                  |");
        System.out.println("+------------------------------------------+");
        System.out.println("|  [1] Credit / Debit Card                 |");
        System.out.println("|  [2] E-Wallet (GCash/PayMaya/GrabPay)    |");
        System.out.println("+------------------------------------------+");

        int method = getValidIntInput(sc, "Choose: ", 1, 2);

        boolean paymentSuccess = false;

        if (method == 1) {
            // Credit/Debit Card - validates 16-digit card number
            paymentSuccess = CreditCardPayment.process(totalAmount, discountRate, sc);
        } else {
            // Online Payment - single class for all e-wallets
            paymentSuccess = OnlinePayment.process(totalAmount, discountRate, sc);
        }

        if (paymentSuccess) {
            // Record the financial transaction
            FinanceManager.recordTransaction(totalAmount, discountRate,
                    method == 1 ? "Credit/Debit Card" : "E-Wallet");
            return "PAID";
        }

        return "PENDING";
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

    private static String getValidStringInput(Scanner sc, String prompt) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        while (input.isEmpty()) {
            System.out.print("[!] This field cannot be empty. Please try again: ");
            input = sc.nextLine().trim();
        }
        return input;
    }

    private static void pauseScreen(Scanner sc) {
        System.out.println("\nPress Enter to continue...");
        sc.nextLine();
    }
}
