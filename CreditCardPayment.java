import java.util.Scanner;

public class CreditCardPayment extends PaymentFramework {

    private String cardNumber;
    private String cardHolder;
    private String expiryDate;
    private String cvv;

    public CreditCardPayment(double amount, double discountRate) {
        super(amount, discountRate, 0, "Credit/Debit Card");
    }

    @Override
    protected boolean validatePaymentMethod() {
        // Validate 16-digit card number (digits only)
        if (cardNumber == null || !cardNumber.matches("\\d{16}")) {
            System.out.println("[CreditCard] Invalid card number. Must be exactly 16 digits.");
            return false;
        }
        // Validate CVV (3 digits)
        if (cvv == null || !cvv.matches("\\d{3}")) {
            System.out.println("[CreditCard] Invalid CVV. Must be 3 digits.");
            return false;
        }
        // Validate expiry format MM/YY
        if (expiryDate == null || !expiryDate.matches("(0[1-9]|1[0-2])/\\d{2}")) {
            System.out.println("[CreditCard] Invalid expiry date. Use MM/YY format.");
            return false;
        }
        // Masked display for security
        String masked = "****-****-****-" + cardNumber.substring(12);
        System.out.println("[CreditCard] Card " + masked + " validated successfully.");
        return true;
    }

    @Override
    protected void finalizeTransaction() {
        System.out.println("[CreditCard] Charging PHP " + String.format("%.2f", applyDiscount(applyVAT(amount))) + " to card ending in " + cardNumber.substring(12) + "...");
        System.out.println("[CreditCard] Transaction approved.");
    }

    /**
     * Collect card details from user
     */
    public void collectCardDetails(Scanner sc) {
        System.out.println("\n--- Credit/Debit Card Payment ---");

        // Card number with 16-digit validation loop
        while (true) {
            System.out.print("Enter 16-digit card number: ");
            String input = sc.nextLine().trim().replaceAll("\\s|-", ""); // Remove spaces and dashes
            if (input.matches("\\d{16}")) {
                this.cardNumber = input;
                break;
            }
            System.out.println("[!] Invalid. Enter exactly 16 digits (no spaces/dashes).");
        }

        // Card holder name
        System.out.print("Card holder name: ");
        this.cardHolder = sc.nextLine().trim();

        // Expiry date
        while (true) {
            System.out.print("Expiry date (MM/YY): ");
            String input = sc.nextLine().trim();
            if (input.matches("(0[1-9]|1[0-2])/\\d{2}")) {
                this.expiryDate = input;
                break;
            }
            System.out.println("[!] Invalid format. Use MM/YY.");
        }

        // CVV
        while (true) {
            System.out.print("CVV (3 digits): ");
            String input = sc.nextLine().trim();
            if (input.matches("\\d{3}")) {
                this.cvv = input;
                break;
            }
            System.out.println("[!] Invalid CVV. Must be 3 digits.");
        }
    }

    /**
     * Static helper to process a credit card payment in one call
     */
    public static boolean process(double amount, double discountRate, Scanner sc) {
        CreditCardPayment payment = new CreditCardPayment(amount, discountRate);
        payment.collectCardDetails(sc);
        payment.processInvoice();
        return true; // Returns true if validation passed (processInvoice handles the rest)
    }
}
