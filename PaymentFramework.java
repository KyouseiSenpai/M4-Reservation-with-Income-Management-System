/**
 * PaymentFramework - Abstract base class for all payment methods
 * Implements Template Method pattern for payment processing
 * 
 * OOP Concepts Demonstrated:
 * - Abstraction: Abstract class with template method
 * - Encapsulation: Protected fields with controlled access
 * - Inheritance: Subclasses extend this framework
 * Created by jepreh torkiza
 */
public abstract class PaymentFramework {

    protected double amount;
    protected double discountRate;
    protected double creditBalance;
    protected String paymentMethod;

    public PaymentFramework(double amount, double discountRate, double creditBalance, String paymentMethod) {
        this.amount = amount;
        this.discountRate = discountRate;
        this.creditBalance = creditBalance;
        this.paymentMethod = paymentMethod;
    }

    protected abstract boolean validatePaymentMethod();

    protected abstract void finalizeTransaction();

    public void processInvoice() {
        try {
            if (validatePaymentMethod()) {
                double total = applyVAT(amount);
                total = applyDiscount(total);
                System.out.println("[PaymentFramework] Invoice processed. Final amount: PHP " + String.format("%.2f", total));
                finalizeTransaction();
                logTransaction(total);
            } else {
                System.out.println("[PaymentFramework] Payment validation failed. Transaction cancelled.");
            }
        } catch (Exception e) {
            System.out.println("[PaymentFramework] Error during invoice processing: " + e.getMessage());
        }
    }

    protected double applyVAT(double baseAmount) {
        try {
            return baseAmount * 1.12;
        } catch (Exception e) {
            System.out.println("[PaymentFramework] VAT calculation error: " + e.getMessage());
            return baseAmount;
        }
    }

    protected double applyDiscount(double baseAmount) {
        try {
            return baseAmount - (baseAmount * discountRate);
        } catch (Exception e) {
            System.out.println("[PaymentFramework] Discount calculation error: " + e.getMessage());
            return baseAmount;
        }
    }

    protected boolean hasEnoughCredit(double totalCost) {
        try {
            return creditBalance >= totalCost;
        } catch (Exception e) {
            System.out.println("[PaymentFramework] Credit check error: " + e.getMessage());
            return false;
        }
    }

    protected void logTransaction(double finalAmount) {
        try {
            System.out.println("[PaymentFramework] Transaction logged: Method=" + paymentMethod + ", Amount=" + String.format("%.2f", finalAmount));
        } catch (Exception e) {
            System.out.println("[PaymentFramework] Logging error: " + e.getMessage());
        }
    }

    // Getters for subclasses
    public double getAmount() { return amount; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getDiscountRate() { return discountRate; }
}
