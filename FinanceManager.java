import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Finance Manager - Handles financial transactions and refunds
 * 
 * SQLITE VERSION - Changes:
 * - Replaced local ArrayLists with SQLite tables in resort.db
 * - Tables: financeTransactions, refunds
 */
public class FinanceManager {

    private static final Logger LOGGER = Logger.getLogger(FinanceManager.class.getName());
    private static final String DB_URL = "jdbc:sqlite:resort.db";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    static {
        initializeTables();
    }

    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private static void initializeTables() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS financeTransactions ("
                    + "transactionID INTEGER PRIMARY KEY,"
                    + "transactionDate TEXT,"
                    + "description TEXT,"
                    + "amount REAL,"
                    + "transactionType TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS refunds ("
                    + "refundID INTEGER PRIMARY KEY,"
                    + "refundDate TEXT,"
                    + "originalAmount REAL,"
                    + "refundReason TEXT,"
                    + "status TEXT DEFAULT 'PENDING')");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "FinanceManager table init failed", e);
        }
    }

    // ============ FINANCIAL REPORT / INCOME STATEMENT ============

    public static void generateFinancialReport() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║         📊 RESORT FINANCIAL REPORT / INCOME STATEMENT          ║");
        System.out.println("║              " + getFormattedDate() + "                          ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝\n");

        // --- Resort Financial Data (from financeTransactions) ---
        double totalRevenue = getTotalRevenue();
        double totalRefunds = getTotalRefunds();
        int txCount = getTransactionCount();
        int refundCount = getRefundCount();

        // --- Hotel Room Data (from roomBookings + rooms) ---
        double totalRoomRevenue = calculateTotalRoomRevenue();
        double totalRoomRefunds = getTotalRoomRefunds();
        int totalBookings = getTotalRoomBookings();
        int totalNights = getTotalRoomNightsSold();
        double adr = totalNights > 0 ? totalRoomRevenue / totalNights : 0.0;
        int totalRooms = getTotalRoomCount();
        int occupiedRooms = getOccupiedRoomCount();
        double occupancyRate = totalRooms > 0 ? (occupiedRooms * 100.0 / totalRooms) : 0.0;
        double revpar = totalRooms > 0 ? totalRoomRevenue / totalRooms : 0.0;

        // --- Combined / Overall Income ---
        double overallGrossIncome = totalRevenue + totalRoomRevenue;
        double overallTotalRefunds = totalRefunds + totalRoomRefunds;
        double overallNetIncome = overallGrossIncome - overallTotalRefunds;

        System.out.println("OVERALL COMBINED INCOME");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf("%-40s: PHP %,10.2f%n", "Resort Financial Revenue", totalRevenue);
        System.out.printf("%-40s: PHP %,10.2f%n", "Hotel Room Revenue", totalRoomRevenue);
        System.out.printf("%-40s: PHP %,10.2f%n", "Overall Gross Income", overallGrossIncome);
        System.out.printf("%-40s: PHP %,10.2f%n", "Overall Refunds Issued", overallTotalRefunds);
        System.out.printf("%-40s: PHP %,10.2f%n", "OVERALL NET INCOME", overallNetIncome);
        System.out.println();

        System.out.println("RESORT FINANCIAL REVENUE");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf("%-40s: PHP %,10.2f%n", "Total Revenue", totalRevenue);
        System.out.printf("%-40s: PHP %,10.2f%n", "Total Refunds", totalRefunds);
        System.out.printf("%-40s: PHP %,10.2f%n", "Net Resort Revenue", totalRevenue - totalRefunds);
        System.out.printf("%-40s: %,15d%n", "Total Transactions", txCount);
        System.out.printf("%-40s: %,15d%n", "Total Refund Requests", refundCount);
        System.out.println();

        System.out.println("HOTEL ROOM REVENUE");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf("%-40s: PHP %,10.2f%n", "Gross Room Revenue", totalRoomRevenue);
        System.out.printf("%-40s: PHP %,10.2f%n", "Room Refunds & Cancellations", totalRoomRefunds);
        System.out.printf("%-40s: PHP %,10.2f%n", "Net Room Revenue", totalRoomRevenue - totalRoomRefunds);
        System.out.printf("%-40s: %,15d%n", "Total Bookings", totalBookings);
        System.out.printf("%-40s: %,15d%n", "Total Room Nights Sold", totalNights);
        System.out.printf("%-40s: PHP %,10.2f%n", "Average Daily Rate (ADR)", adr);
        System.out.printf("%-40s: PHP %,10.2f%n", "RevPAR (Revenue Per Available Room)", revpar);
        System.out.println();

        // Revenue by Room Type
        System.out.println("REVENUE BY ROOM TYPE");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf("%-15s %12s %15s %18s%n", "Room Type", "Bookings", "Nights Sold", "Revenue (PHP)");
        System.out.println("───────────────────────────────────────────────────────────────");
        String[][] roomTypeRevenue = getRevenueByRoomType();
        if (roomTypeRevenue != null) {
            for (String[] row : roomTypeRevenue) {
                System.out.printf("%-15s %,12d %,15d %,18.2f%n",
                        row[0],
                        Integer.parseInt(row[1]),
                        Integer.parseInt(row[2]),
                        Double.parseDouble(row[3])
                );
            }
        } else {
            System.out.println("  No room booking data available.");
        }
        System.out.println();

        // Occupancy Metrics
        System.out.println("OCCUPANCY METRICS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf("%-40s: %,15d%n", "Total Rooms in Inventory", totalRooms);
        System.out.printf("%-40s: %,15d%n", "Currently Occupied", occupiedRooms);
        System.out.printf("%-40s: %,14.1f%%%n", "Current Occupancy Rate", occupancyRate);
        System.out.printf("%-40s: %,14.1f%%%n", "Vacancy Rate", 100.0 - occupancyRate);
        System.out.println();

        // Booking Status Breakdown
        System.out.println("BOOKING STATUS BREAKDOWN");
        System.out.println("═══════════════════════════════════════════════════════════════");
        int confirmedCount = getBookingStatusCount("CONFIRMED");
        int checkedOutCount = getBookingStatusCount("CHECKED_OUT");
        int cancelledCount = getBookingStatusCount("CANCELLED");
        System.out.printf("%-40s: %,15d%n", "Confirmed (Active)", confirmedCount);
        System.out.printf("%-40s: %,15d%n", "Checked Out", checkedOutCount);
        System.out.printf("%-40s: %,15d%n", "Cancelled", cancelledCount);
        if (totalBookings > 0) {
            System.out.printf("%-40s: %,14.1f%%%n", "Cancellation Rate", (cancelledCount * 100.0 / totalBookings));
        }
        System.out.println();

        // Recent Transactions
        if (txCount > 0) {
            System.out.println("RECENT TRANSACTIONS");
            System.out.println("═══════════════════════════════════════════════════════════════");
            String[][] txs = getRecentTransactions(10);
            if (txs != null) {
                for (int i = 0; i < txs.length; i++) {
                    System.out.printf("%3d. %-30s PHP %,10.2f  %s%n",
                        i + 1,
                        txs[i][1],
                        Double.parseDouble(txs[i][2]),
                        txs[i][3]);
                }
            }
            System.out.println();
        }

        // Refund Status
        if (refundCount > 0) {
            System.out.println("REFUND STATUS");
            System.out.println("═══════════════════════════════════════════════════════════════");
            String[][] refunds = getAllRefunds();
            if (refunds != null) {
                for (int i = 0; i < refunds.length; i++) {
                    System.out.printf("%3d. %-15s PHP %,10.2f  %-20s  %s%n",
                        i + 1,
                        refunds[i][0],
                        Double.parseDouble(refunds[i][1]),
                        refunds[i][2],
                        refunds[i][3]);
                }
            }
            System.out.println();
        }

        // Recent Room Bookings
        System.out.println("RECENT ROOM BOOKINGS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        String[][] recentBookings = getRecentRoomBookings(10);
        if (recentBookings != null && recentBookings.length > 0) {
            System.out.printf("%-8s %-10s %-12s %-12s %-10s %-12s%n",
                    "Booking", "Room", "Type", "Check-In", "Nights", "Status");
            System.out.println("───────────────────────────────────────────────────────────────");
            for (String[] b : recentBookings) {
                System.out.printf("%-8s %-10s %-12s %-12s %-10s %-12s%n",
                        b[0], b[1], b[2], b[3], b[4], b[5]);
            }
        } else {
            System.out.println("  No room bookings found.");
        }
        System.out.println();

        // Room Refund Summary
        System.out.println("ROOM REFUND SUMMARY");
        System.out.println("═══════════════════════════════════════════════════════════════");
        String[][] roomRefunds = getRoomRefunds();
        if (roomRefunds != null && roomRefunds.length > 0) {
            System.out.printf("%-5s %-20s %-15s %-30s%n", "#", "Date", "Amount (PHP)", "Reason");
            System.out.println("───────────────────────────────────────────────────────────────");
            for (int i = 0; i < roomRefunds.length; i++) {
                System.out.printf("%-5d %-20s %,15.2f %-30s%n",
                        i + 1, roomRefunds[i][0], Double.parseDouble(roomRefunds[i][1]),
                        truncate(roomRefunds[i][2], 28));
            }
        } else {
            System.out.println("  No room refunds recorded.");
        }
        System.out.println();

        // Combined Analytics
        System.out.println("COMBINED RESORT ANALYTICS");
        System.out.println("═══════════════════════════════════════════════════════════════");
        System.out.printf("%-40s: %14.1f%%%n", "Current Occupancy Rate", occupancyRate);
        System.out.printf("%-40s: PHP %,10.2f%n", "Average Transaction Value",
            txCount > 0 ? totalRevenue / txCount : 0.0);
        System.out.printf("%-40s: %14.1f%%%n", "Refund Rate",
            txCount > 0 ? (refundCount * 100.0 / txCount) : 0.0);
        System.out.printf("%-40s: %14.1f%%%n", "Room Revenue Share of Overall",
            overallGrossIncome > 0 ? (totalRoomRevenue * 100.0 / overallGrossIncome) : 0.0);
    }

    // Hotel Income Statement Helper Methods

    private static double calculateTotalRoomRevenue() {
        String sql = "SELECT r.pricePerNight, rb.checkInDate, rb.checkOutDate, rb.bookingStatus " +
                     "FROM roomBookings rb JOIN rooms r ON rb.roomNumber = r.roomNumber " +
                     "WHERE rb.bookingStatus != 'CANCELLED'";
        double revenue = 0.0;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                double pricePerNight = rs.getDouble("pricePerNight");
                String checkIn = rs.getString("checkInDate");
                String checkOut = rs.getString("checkOutDate");
                long nights = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.parse(checkIn),
                        java.time.LocalDate.parse(checkOut));
                if (nights > 0) {
                    revenue += nights * pricePerNight;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "calculateTotalRoomRevenue failed", e);
        }
        return revenue;
    }

    private static int getTotalRoomBookings() {
        String sql = "SELECT COUNT(*) FROM roomBookings";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTotalRoomBookings failed", e);
        }
        return 0;
    }

    private static int getTotalRoomNightsSold() {
        String sql = "SELECT checkInDate, checkOutDate, bookingStatus FROM roomBookings WHERE bookingStatus != 'CANCELLED'";
        int totalNights = 0;
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String checkIn = rs.getString("checkInDate");
                String checkOut = rs.getString("checkOutDate");
                long nights = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.parse(checkIn),
                        java.time.LocalDate.parse(checkOut));
                if (nights > 0) totalNights += (int) nights;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTotalRoomNightsSold failed", e);
        }
        return totalNights;
    }

    private static String[][] getRevenueByRoomType() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT r.roomType, r.pricePerNight, rb.checkInDate, rb.checkOutDate, rb.bookingStatus " +
                     "FROM roomBookings rb JOIN rooms r ON rb.roomNumber = r.roomNumber";
        java.util.Map<String, RoomTypeStats> statsMap = new java.util.HashMap<>();

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String roomType = rs.getString("roomType");
                double pricePerNight = rs.getDouble("pricePerNight");
                String checkIn = rs.getString("checkInDate");
                String checkOut = rs.getString("checkOutDate");
                String status = rs.getString("bookingStatus");
                long nights = java.time.temporal.ChronoUnit.DAYS.between(
                        java.time.LocalDate.parse(checkIn),
                        java.time.LocalDate.parse(checkOut));
                if (nights < 1) nights = 1;

                RoomTypeStats stats = statsMap.computeIfAbsent(roomType, k -> new RoomTypeStats());
                stats.bookings++;
                stats.nights += (int) nights;
                if (!"CANCELLED".equals(status)) {
                    stats.revenue += nights * pricePerNight;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getRevenueByRoomType failed", e);
        }

        for (java.util.Map.Entry<String, RoomTypeStats> entry : statsMap.entrySet()) {
            RoomTypeStats s = entry.getValue();
            result.add(new String[]{entry.getKey(), String.valueOf(s.bookings), String.valueOf(s.nights), String.format("%.2f", s.revenue)});
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    private static int getTotalRoomCount() {
        String sql = "SELECT COUNT(*) FROM rooms";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTotalRoomCount failed", e);
        }
        return 0;
    }

    private static int getOccupiedRoomCount() {
        String sql = "SELECT COUNT(*) FROM rooms WHERE status = 'OCCUPIED'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getOccupiedRoomCount failed", e);
        }
        return 0;
    }

    private static int getBookingStatusCount(String status) {
        String sql = "SELECT COUNT(*) FROM roomBookings WHERE bookingStatus = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getBookingStatusCount failed", e);
        }
        return 0;
    }

    private static double getTotalRoomRefunds() {
        String sql = "SELECT COALESCE(SUM(originalAmount), 0) FROM refunds WHERE refundReason LIKE '%ROOM%' OR refundReason LIKE '%Room%'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTotalRoomRefunds failed", e);
        }
        return 0.0;
    }

    private static String[][] getRecentRoomBookings(int limit) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT rb.bookingID, rb.roomNumber, r.roomType, rb.checkInDate, rb.checkOutDate, rb.bookingStatus " +
                     "FROM roomBookings rb JOIN rooms r ON rb.roomNumber = r.roomNumber " +
                     "ORDER BY rb.bookingID DESC LIMIT ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String checkIn = rs.getString("checkInDate");
                    String checkOut = rs.getString("checkOutDate");
                    long nights = java.time.temporal.ChronoUnit.DAYS.between(
                            java.time.LocalDate.parse(checkIn),
                            java.time.LocalDate.parse(checkOut));
                    result.add(new String[]{
                        String.valueOf(rs.getInt("bookingID")),
                        String.valueOf(rs.getInt("roomNumber")),
                        rs.getString("roomType"),
                        checkIn,
                        String.valueOf(nights > 0 ? nights : 1),
                        rs.getString("bookingStatus")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getRecentRoomBookings failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    private static String[][] getRoomRefunds() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT refundDate, originalAmount, refundReason FROM refunds WHERE refundReason LIKE '%ROOM%' OR refundReason LIKE '%Room%' ORDER BY refundID DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("refundDate"),
                    String.valueOf(rs.getDouble("originalAmount")),
                    rs.getString("refundReason")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getRoomRefunds failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    private static String truncate(String s, int maxLen) {
        if (s == null) return "";
        return s.length() > maxLen ? s.substring(0, maxLen - 3) + "..." : s;
    }

    /**
     * Inner helper class for accumulating room type statistics.
     */
    private static class RoomTypeStats {
        int bookings = 0;
        int nights = 0;
        double revenue = 0.0;
    }

    // ============ TRANSACTION METHODS ============

    public static void addTransaction(double amount, String description) {
        addTransactionWithType(amount, description, "Revenue");
    }

    public static void addTransactionWithType(double amount, String description, String type) {
        String sql = "INSERT INTO financeTransactions (transactionID, transactionDate, description, amount, transactionType) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int nextID = getNextTransactionID(conn);
            ps.setInt(1, nextID);
            ps.setString(2, LocalDateTime.now().format(FORMATTER));
            ps.setString(3, description);
            ps.setDouble(4, amount);
            ps.setString(5, type);
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "addTransactionWithType failed", e);
        }
    }

    private static int getNextTransactionID(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(transactionID), 0) + 1 FROM financeTransactions")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 1;
    }

    public static double getTotalRevenue() {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM financeTransactions";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTotalRevenue failed", e);
        }
        return 0.0;
    }

    public static int getTransactionCount() {
        String sql = "SELECT COUNT(*) FROM financeTransactions";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTransactionCount failed", e);
        }
        return 0;
    }

    public static String[][] getRecentTransactions(int limit) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT transactionDate, description, amount, transactionType FROM financeTransactions ORDER BY transactionID DESC LIMIT ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new String[]{
                        rs.getString("transactionDate"),
                        rs.getString("description"),
                        String.valueOf(rs.getDouble("amount")),
                        rs.getString("transactionType")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getRecentTransactions failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    // ============ REFUND METHODS ============

    public static boolean processRefund(double originalAmount, String reason) {
        if (originalAmount <= 0) {
            System.out.println("\n[!] Invalid refund amount.");
            return false;
        }

        double refundAmount = originalAmount * 0.85;
        System.out.printf("\nOriginal Amount: PHP %.2f%n", originalAmount);
        System.out.printf("Refund Amount (85%%): PHP %.2f%n", refundAmount);
        System.out.printf("Processing Fee (15%%): PHP %.2f%n", (originalAmount - refundAmount));
        System.out.println("\nRefund processed successfully!");
        System.out.printf("Refund Amount: PHP %.2f%n", refundAmount);

        recordRefund(refundAmount, reason);
        addTransactionWithType(refundAmount, "Refund: " + reason, "Refund");
        return true;
    }

    public static void recordRefund(double amount, String reason) {
        String sql = "INSERT INTO refunds (refundID, refundDate, originalAmount, refundReason, status) VALUES (?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int nextID = getNextRefundID(conn);
            ps.setInt(1, nextID);
            ps.setString(2, LocalDateTime.now().format(FORMATTER));
            ps.setDouble(3, amount);
            ps.setString(4, reason);
            ps.setString(5, "PROCESSED");
            ps.executeUpdate();
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "recordRefund failed", e);
        }
    }

    private static int getNextRefundID(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(refundID), 0) + 1 FROM refunds")) {
            if (rs.next()) return rs.getInt(1);
        }
        return 1;
    }

    public static double getTotalRefunds() {
        String sql = "SELECT COALESCE(SUM(originalAmount), 0) FROM refunds";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getDouble(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTotalRefunds failed", e);
        }
        return 0.0;
    }

    public static int getRefundCount() {
        String sql = "SELECT COUNT(*) FROM refunds";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getRefundCount failed", e);
        }
        return 0;
    }

    public static String[][] getAllRefunds() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT refundDate, originalAmount, refundReason, status FROM refunds ORDER BY refundID DESC";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                    rs.getString("refundDate"),
                    String.valueOf(rs.getDouble("originalAmount")),
                    rs.getString("refundReason"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAllRefunds failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    // ============ M4 PAYMENT FRAMEWORK INTEGRATION METHODS ============

    /**
     * Record a transaction from the Payment Framework (called from checkIn)
     * @param amount The transaction amount
     * @param discountRate The discount rate applied
     * @param paymentMethod The payment method used
     */
    public static void recordTransaction(double amount, double discountRate, String paymentMethod) {
        double vatAmount = amount * 1.12;
        double finalAmount = vatAmount - (vatAmount * discountRate);
        String description = String.format("Ticket Purchase via %s (VAT: %.2f, Discount: %.0f%%)",
                paymentMethod, vatAmount - amount, discountRate * 100);
        addTransactionWithType(finalAmount, description, "Revenue");
    }

    /**
     * Record a membership upgrade transaction (called from membership)
     * @param amount The upgrade fee amount
     * @param paymentMethod The payment method used
     */
    public static void recordMembershipTransaction(double amount, String paymentMethod) {
        String description = String.format("VIP Membership Upgrade via %s", paymentMethod);
        addTransactionWithType(amount, description, "Revenue");
    }

    /**
     * Process a refund request with full details (called from CRMSystem and checkIn)
     * @param originalAmount The original transaction amount
     * @param reason The reason for refund
     * @param customerID The customer ID requesting refund
     * @param customerName The customer name
     * @param refundType The type of refund (TICKET, ROOM, MEMBERSHIP)
     * @param paymentMethod The original payment method
     * @return true if refund was processed successfully
     */
    public static boolean processRefund(double originalAmount, String reason, int customerID,
                                         String customerName, String refundType, String paymentMethod) {
        if (originalAmount <= 0) {
            System.out.println("\n[!] Invalid refund amount.");
            return false;
        }

        double refundAmount = originalAmount * 0.85;
        System.out.printf("\nOriginal Amount: PHP %.2f%n", originalAmount);
        System.out.printf("Refund Amount (85%%): PHP %.2f%n", refundAmount);
        System.out.printf("Processing Fee (15%%): PHP %.2f%n", (originalAmount - refundAmount));
        System.out.println("\nRefund request submitted for admin approval!");
        System.out.printf("Refund Amount: PHP %.2f%n", refundAmount);

        recordRefund(refundAmount, "[" + refundType + "] " + reason + " (Customer: " + customerName + ", ID: " + customerID + ")");
        addTransactionWithType(refundAmount, "Refund [" + refundType + "]: " + reason + " - " + customerName, "Refund");
        return true;
    }

    // ============ ANALYTICS ============

    private static double calculateOccupancyRate() {
        int total = 0, occupied = 0;
        String sql = "SELECT status FROM rooms";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                total++;
                String s = rs.getString("status");
                if ("OCCUPIED".equals(s)) occupied++;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "calculateOccupancyRate failed", e);
        }
        return total > 0 ? (occupied * 100.0 / total) : 0.0;
    }

    private static String getFormattedDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }
}
