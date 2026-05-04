import java.util.Scanner;

/**
 * OnlinePayment - Single unified class for all digital wallet payments
 * Covers GCash, PayMaya, GrabPay, and other online payment methods
 * No separate classes needed - one class handles all
 * 
 * OOP: Inherits PaymentFramework, implements abstract methods
 */
public class OnlinePayment extends PaymentFramework {

    private String walletType;   // GCash, PayMaya, GrabPay, etc.
    private String mobileNumber; // Linked mobile number
    private String pin;          // 4-digit MPIN

    public OnlinePayment(double amount, double discountRate, String walletType) {
        super(amount, discountRate, 0, walletType);
        this.walletType = walletType;
    }

    @Override
    protected boolean validatePaymentMethod() {
        // Validate mobile number (11 digits starting with 09)
        if (mobileNumber == null || !mobileNumber.matches("09\\d{9}")) {
            System.out.println("[OnlinePayment] Invalid mobile number. Use format: 09XXXXXXXXX");
            return false;
        }
        // Validate 4-digit PIN
        if (pin == null || !pin.matches("\\d{4}")) {
            System.out.println("[OnlinePayment] Invalid PIN. Must be 4 digits.");
            return false;
        }
        System.out.println("[OnlinePayment] " + walletType + " account " + maskMobile(mobileNumber) + " validated.");
        return true;
    }

    @Override
    protected void finalizeTransaction() {
        double finalAmount = applyDiscount(applyVAT(amount));
        System.out.println("[OnlinePayment] Processing PHP " + String.format("%.2f", finalAmount) + " via " + walletType + "...");
        // Simulate processing delay
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("[OnlinePayment] Payment successful! Reference: REF" + System.currentTimeMillis() % 1000000);
    }

    /**
     * Collect payment details from user
     */
    public void collectDetails(Scanner sc) {
        System.out.println("\n--- " + walletType + " Payment ---");

        // Mobile number with validation
        while (true) {
            System.out.print("Enter mobile number (09XXXXXXXXX): ");
            String input = sc.nextLine().trim();
            if (input.matches("09\\d{9}")) {
                this.mobileNumber = input;
                break;
            }
            System.out.println("[!] Invalid. Must be 11 digits starting with 09.");
        }

        // 4-digit PIN
        while (true) {
            System.out.print("Enter " + walletType + " MPIN (4 digits): ");
            String input = sc.nextLine().trim();
            if (input.matches("\\d{4}")) {
                this.pin = input;
                break;
            }
            System.out.println("[!] Invalid PIN. Must be exactly 4 digits.");
        }
    }

    /**
     * Static helper - Select and process any online payment in one call
     * Handles GCash, PayMaya, GrabPay, and other wallets from a single menu
     */
    public static boolean process(double amount, double discountRate, Scanner sc) {
        // Single menu for all online payment methods
        System.out.println("\n+----------------------------------+");
        System.out.println("|    Select Online Payment         |");
        System.out.println("+----------------------------------+");
        System.out.println("|  [1] GCash                       |");
        System.out.println("|  [2] PayMaya / Maya              |");
        System.out.println("|  [3] GrabPay                     |");
        System.out.println("|  [4] Other E-Wallet              |");
        System.out.println("+----------------------------------+");

        int choice = getValidIntInput(sc, "Choose: ", 1, 4);

        String[] wallets = {"GCash", "PayMaya", "GrabPay", "E-Wallet"};
        String selected = wallets[choice - 1];

        // If "Other", ask for the name
        if (choice == 4) {
            System.out.print("Enter e-wallet name: ");
            selected = sc.nextLine().trim();
            if (selected.isEmpty()) selected = "E-Wallet";
        }

        OnlinePayment payment = new OnlinePayment(amount, discountRate, selected);
        payment.collectDetails(sc);
        payment.processInvoice();
        return true;
    }

    // Helper: mask mobile number for privacy
    private static String maskMobile(String mobile) {
        if (mobile == null || mobile.length() != 11) return mobile;
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    private static int getValidIntInput(Scanner sc, String prompt, int min, int max) {
        System.out.print(prompt);
        while (true) {
            try {
                int input = sc.nextInt();
                sc.nextLine();
                if (input >= min && input <= max) return input;
                System.out.print("[!] Enter " + min + "-" + max + ": ");
            } catch (Exception e) {
                System.out.print("[!] Invalid. Enter a number: ");
                sc.nextLine();
            }
        }
    }
}
