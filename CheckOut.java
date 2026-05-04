/**
 * CheckOut Class - Handles receipt display and transaction finalization
 */
  /** 
     * M4 NEW FEATURES
     * Display the final receipt
     * @param customerID The customer ID
     * @param membershipType The membership type
     * @param receiptLines The receipt lines
     * @param totalAmount The total amount
     * @param totalPoints The total points earned
     * @param appointmentDate The appointment date
     * @param paymentStatus The payment status
     */

public class checkOut {

  
    public static void showReceipt(int customerID, String membershipType,
                                   StringBuilder receiptLines, double totalAmount,
                                   int totalPoints, String appointmentDate,
                                   String paymentStatus) {
        
        // Validate inputs
        if (receiptLines == null) {
            receiptLines = new StringBuilder();
        }
        if (membershipType == null) {
            membershipType = "Regular";
        }
        if (appointmentDate == null) {
            appointmentDate = "Not scheduled";
        }
        if (paymentStatus == null) {
            paymentStatus = "PENDING";
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                                                              ║");
        System.out.println("║           🎢 THEME PARK RESORT                               ║");
        System.out.println("║              OFFICIAL RECEIPT                                ║");
        System.out.println("║                                                              ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Customer ID  : %-44d ║%n", customerID);
        System.out.printf("║  Membership   : %-44s ║%n", membershipType);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println(receiptLines.toString());
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║  Total Amount  : PHP %40.2f  ║%n", totalAmount);
        System.out.printf("║  Points Earned : %44d  ║%n", totalPoints);
        System.out.printf("║  Appointment   : %-44s ║%n", appointmentDate);
        System.out.printf("║  Status        : %-44s ║%n", paymentStatus);
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        
        if ("PAID".equals(paymentStatus)) {
            System.out.println("║                                                              ║");
            System.out.println("║              ✅ PAYMENT SUCCESSFUL!                          ║");
            System.out.println("║                                                              ║");
        } else {
            System.out.println("║                                                              ║");
            System.out.println("║         ⏳ PLEASE PAY AT THE COUNTER                        ║");
            System.out.println("║                                                              ║");
        }
        
        System.out.println("║              Thank you for visiting!                         ║");
        System.out.println("║         We hope you have a magical day!                      ║");
        System.out.println("║                                                              ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        // Save to transaction history
        transactionHistory.save(customerID, receiptLines.toString(), totalAmount, totalPoints);
    }
}
