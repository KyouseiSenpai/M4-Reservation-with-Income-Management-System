import java.util.ArrayList;
import java.util.List;

// Transaction History Class - Manages customer transaction records
public class transactionHistory {

    private static final int MAX_HISTORY_SIZE = 1000;
    private static final List<TransactionRecord> history = new ArrayList<>();
    private static final Object lock = new Object();

// Inner class to store transaction records
    private static class TransactionRecord {
        int customerID;
        String receiptData;

        TransactionRecord(int customerID, String receiptData) {
            this.customerID = customerID;
            this.receiptData = receiptData;
        }
    }

// Save a transaction to history
    public static void save(int customerID, String receiptLines, double totalAmount, int totalPoints) {
        if (receiptLines == null) {
            receiptLines = "";
        }

        String receiptData = "\n╔══════════════════════════════════════════════════════════════╗\n" +
                "║                    TRANSACTION RECORD                        ║\n" +
                "╠══════════════════════════════════════════════════════════════╣\n" +
                receiptLines +
                "╠══════════════════════════════════════════════════════════════╣\n" +
                String.format("║  Total Amount : PHP %40.2f  ║%n", totalAmount) +
                String.format("║  Total Points : %44d  ║%n", totalPoints) +
                "╚══════════════════════════════════════════════════════════════╝";

        synchronized (lock) {
            // Remove oldest if at capacity
            if (history.size() >= MAX_HISTORY_SIZE) {
                history.remove(0);
            }
            history.add(new TransactionRecord(customerID, receiptData));
        }
    }

// Show transaction history for a customer
    public static void showHistory(int customerID) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║                    TRANSACTION HISTORY                       ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
        
        boolean found = false;
        int txNum = 1;

        // Check in-memory history first
        synchronized (lock) {
            for (TransactionRecord record : history) {
                if (record.customerID == customerID) {
                    System.out.println("\n--- Transaction #" + txNum++ + " ---");
                    System.out.println(record.receiptData);
                    found = true;
                }
            }
        }

        // Check database records
        if (!found) {
            Repository repo = Repository.getInstance();
            String[][] records = repo.getTransactions(customerID);
            if (records != null && records.length > 0) {
                for (String[] r : records) {
                    System.out.println("\n--- Transaction #" + txNum++ + " ---");
                    System.out.println("  Date      : " + (r.length > 0 ? r[0] : "N/A"));
                    System.out.println("  Ticket    : " + (r.length > 1 ? r[1] : "N/A") + 
                                      " (Age: " + (r.length > 2 ? r[2] : "N/A") + ")");
                    System.out.println("  Appointment Date : " + (r.length > 3 ? r[3] : "N/A"));
                    System.out.println("  Status    : " + (r.length > 4 ? r[4] : "N/A"));
                    if (r.length > 5) {
                        System.out.println("  Price     : PHP " + r[5]);
                    }
                    System.out.println("  ═══════════════════════════════════════");
                    found = true;
                }
            }
        }

        if (!found) {
            System.out.println("\n  No transactions found.");
            System.out.println("  Purchase tickets to see your transaction history.");
        }
    }

// Clear all transaction history (for admin use)
    public static void clearHistory() {
        synchronized (lock) {
            history.clear();
        }
    }

// Get the number of stored transactions
    public static int getHistorySize() {
        synchronized (lock) {
            return history.size();
        }
    }
}
