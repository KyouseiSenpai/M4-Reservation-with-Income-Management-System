import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Transaction History Class - Manages customer transaction records
 * 
 * SQLITE VERSION - Changes:
 * - Removed local ArrayList; all data stored in SQLite resort.db
 * - Table: transactionHistory
 */
public class transactionHistory {

    private static final int MAX_HISTORY_SIZE = 1000;
    private static final String DB_URL = "jdbc:sqlite:resort.db";

    static {
        initializeTable();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void initializeTable() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS transactionHistory ("
                    + "historyID INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "customerID INTEGER,"
                    + "receiptData TEXT,"
                    + "totalAmount REAL,"
                    + "totalPoints INTEGER,"
                    + "transactionDate TEXT)");
        } catch (SQLException e) {
            System.err.println("[transactionHistory] Failed to initialize table: " + e.getMessage());
        }
    }

    /**
     * Save a transaction to SQLite history
     * @param customerID The customer ID
     * @param receiptLines The receipt lines
     * @param totalAmount The total amount
     * @param totalPoints The total points
     */
    public static void save(int customerID, String receiptLines, double totalAmount, int totalPoints) {
        if (receiptLines == null) {
            receiptLines = "";
        }

        String receiptData = "\n╔══════════════════════════════════════════════════════════════╗\n" +
                "║                  TRANSACTION RECORD                          ║\n" +
                "╠══════════════════════════════════════════════════════════════╣\n" +
                receiptLines +
                "╠══════════════════════════════════════════════════════════════╣\n" +
                String.format("║  Total Amount : PHP %40.2f  ║%n", totalAmount) +
                String.format("║  Total Points : %44d  ║%n", totalPoints) +
                "╚══════════════════════════════════════════════════════════════╝";

        // Enforce max history size by deleting oldest
        try (Connection conn = getConnection()) {
            try (Statement countStmt = conn.createStatement();
                 ResultSet rs = countStmt.executeQuery("SELECT COUNT(*) FROM transactionHistory")) {
                if (rs.next() && rs.getInt(1) >= MAX_HISTORY_SIZE) {
                    try (Statement del = conn.createStatement()) {
                        del.execute("DELETE FROM transactionHistory WHERE historyID = (SELECT MIN(historyID) FROM transactionHistory)");
                    }
                }
            }
            String sql = "INSERT INTO transactionHistory (customerID, receiptData, totalAmount, totalPoints, transactionDate) VALUES (?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, customerID);
                ps.setString(2, receiptData);
                ps.setDouble(3, totalAmount);
                ps.setInt(4, totalPoints);
                ps.setString(5, LocalDate.now().toString());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.err.println("[transactionHistory] save failed: " + e.getMessage());
        }
    }

    /**
     * Show transaction history for a customer from SQLite
     * @param customerID The customer ID
     */
    public static void showHistory(int customerID) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        if (customerID == -1) {
            System.out.println("║              📜 ALL TRANSACTION HISTORY (ADMIN)              ║");
        } else {
            System.out.println("║              📜 TRANSACTION HISTORY                          ║");
        }
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        boolean found = false;
        int txNum = 1;

        // Read from SQLite - if customerID is -1, show all transactions (admin)
        String sql = customerID == -1 
            ? "SELECT receiptData, customerID FROM transactionHistory ORDER BY historyID"
            : "SELECT receiptData, customerID FROM transactionHistory WHERE customerID = ? ORDER BY historyID";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            if (customerID != -1) {
                ps.setInt(1, customerID);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    if (customerID == -1) {
                        System.out.println("\n--- Transaction #" + txNum++ + " (Customer: " + rs.getInt("customerID") + ") ---");
                    } else {
                        System.out.println("\n--- Transaction #" + txNum++ + " ---");
                    }
                    System.out.println(rs.getString("receiptData"));
                    found = true;
                }
            }
        } catch (SQLException e) {
            System.err.println("[transactionHistory] showHistory failed: " + e.getMessage());
        }

        // Also check database records via Repository (ticketRecords in SQLite)
        if (!found && customerID != -1) {
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
            if (customerID == -1) {
                System.out.println("\n  No transactions found in the system.");
            } else {
                System.out.println("\n  No transactions found.");
                System.out.println("  Purchase tickets to see your transaction history.");
            }
        }
    }

    /**
     * Clear all transaction history (for admin use)
     */
    public static void clearHistory() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM transactionHistory");
        } catch (SQLException e) {
            System.err.println("[transactionHistory] clearHistory failed: " + e.getMessage());
        }
    }

    /**
     * Get the number of stored transactions
     * @return Transaction count
     */
    public static int getHistorySize() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM transactionHistory")) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[transactionHistory] getHistorySize failed: " + e.getMessage());
        }
        return 0;
    }
}

FAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

HAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAFAHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
