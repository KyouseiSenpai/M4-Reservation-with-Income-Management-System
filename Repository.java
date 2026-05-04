import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Repository Class - SQLite Data Access Layer
 * Implements Singleton pattern for SQLite database management
 *
 * All data is stored in a local SQLite file (resort.db) that the professor can inspect.
 * No ArrayLists are used for persistent data storage.
 *
 * TABLES (SQLite):
 *  - customers        (Guest accounts & membership)
 *  - ticketRecords    (Ticket purchases)
 *  - repoTransactions (Financial transactions stored by Repository)
 *  - queue            (Queue entries)
 *  - rooms            (Room inventory)
 *  - roomBookings     (Room reservations)
 *  - roomStatuses     (IoT room controls)
 *  - housekeeping     (Cleaning tasks)
 *  - maintenance      (Facility issues)
 *  - facilities       (Park facilities)
 *  - lostFound        (Lost & found items)
 */
public class Repository {

    private static final Logger LOGGER = Logger.getLogger(Repository.class.getName());
    private static final String DB_URL = "jdbc:sqlite:resort.db";
    private static Repository instance;
    private static final Object lock = new Object();

    // ============ AUTO INCREMENT COUNTERS (kept in memory, IDs stored in DB) ============
    private int nextTicketID = 1;
    private int nextTransactionID = 1;
    private int nextQueueID = 1;
    private int nextBookingID = 1;
    private int nextStatusID = 1;
    private int nextTaskID = 1;
    private int nextIssueID = 1;
    private int nextFacilityID = 1;
    private int nextItemID = 1;

    private Repository() {
        initializeDatabase();
        LOGGER.info("SQLite database initialized successfully at " + DB_URL);
    }

    public static Repository getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new Repository();
                }
            }
        }
        return instance;
    }

    // ============ DATABASE INITIALIZATION ============

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    private void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            conn.setAutoCommit(false);

            // Create customers table
            stmt.execute("CREATE TABLE IF NOT EXISTS customers ("
                    + "customerID INTEGER PRIMARY KEY,"
                    + "customerFullName TEXT NOT NULL,"
                    + "customerContactNumber TEXT,"
                    + "customerAge INTEGER,"
                    + "accountDateCreated TEXT,"
                    + "dateTimeIn TEXT,"
                    + "dateTimeOut TEXT,"
                    + "accountStatus TEXT DEFAULT 'ACTIVE',"
                    + "membershipType TEXT DEFAULT 'Regular',"
                    + "customerPoints INTEGER DEFAULT 0,"
                    + "freebiesCount INTEGER DEFAULT 0,"
                    + "ticketBought INTEGER DEFAULT 0,"
                    + "totalCost REAL DEFAULT 0)");

            // Create ticketRecords table
            stmt.execute("CREATE TABLE IF NOT EXISTS ticketRecords ("
                    + "ticketID INTEGER PRIMARY KEY,"
                    + "customerID INTEGER,"
                    + "dateBought TEXT,"
                    + "ticketAge INTEGER,"
                    + "ticketName TEXT,"
                    + "ticketType TEXT,"
                    + "ticketPrice REAL,"
                    + "appointmentDate TEXT,"
                    + "paymentStatus TEXT)");

            // Create repoTransactions table
            stmt.execute("CREATE TABLE IF NOT EXISTS repoTransactions ("
                    + "transactionID INTEGER PRIMARY KEY,"
                    + "customerID INTEGER,"
                    + "transactionDate TEXT,"
                    + "transactionType TEXT,"
                    + "description TEXT,"
                    + "amount REAL,"
                    + "pointsEarned INTEGER,"
                    + "receiptData TEXT)");

            // Create queue table
            stmt.execute("CREATE TABLE IF NOT EXISTS queue ("
                    + "queueID INTEGER PRIMARY KEY,"
                    + "customerID INTEGER,"
                    + "className TEXT,"
                    + "membershipType TEXT,"
                    + "queuePosition INTEGER,"
                    + "status TEXT,"
                    + "timestamp TEXT)");

            // Create rooms table
            stmt.execute("CREATE TABLE IF NOT EXISTS rooms ("
                    + "roomNumber INTEGER PRIMARY KEY,"
                    + "roomType TEXT,"
                    + "capacity INTEGER,"
                    + "pricePerNight REAL,"
                    + "status TEXT DEFAULT 'AVAILABLE',"
                    + "currentGuestID INTEGER DEFAULT 0,"
                    + "checkInDate TEXT,"
                    + "checkOutDate TEXT,"
                    + "guestCount INTEGER DEFAULT 0,"
                    + "floorNumber INTEGER,"
                    + "hasView INTEGER DEFAULT 0,"
                    + "isQuietZone INTEGER DEFAULT 0,"
                    + "nearElevator INTEGER DEFAULT 0)");

            // Create roomBookings table
            stmt.execute("CREATE TABLE IF NOT EXISTS roomBookings ("
                    + "bookingID INTEGER PRIMARY KEY,"
                    + "roomNumber INTEGER,"
                    + "customerID INTEGER,"
                    + "checkInDate TEXT,"
                    + "checkOutDate TEXT,"
                    + "guestCount INTEGER,"
                    + "bookingStatus TEXT,"
                    + "actualCheckOut TEXT)");

            // Create roomStatuses table
            stmt.execute("CREATE TABLE IF NOT EXISTS roomStatuses ("
                    + "statusID INTEGER PRIMARY KEY,"
                    + "roomNumber INTEGER UNIQUE,"
                    + "temperature REAL DEFAULT 22.0,"
                    + "lightsOn INTEGER DEFAULT 1,"
                    + "dndStatus INTEGER DEFAULT 0,"
                    + "lastGuestRequest TEXT,"
                    + "requestTime TEXT)");

            // Create housekeeping table
            stmt.execute("CREATE TABLE IF NOT EXISTS housekeeping ("
                    + "taskID INTEGER PRIMARY KEY,"
                    + "roomNumber INTEGER,"
                    + "staffName TEXT,"
                    + "taskDate TEXT,"
                    + "priority TEXT,"
                    + "status TEXT DEFAULT 'PENDING',"
                    + "completionDate TEXT,"
                    + "notes TEXT)");

            // Create maintenance table
            stmt.execute("CREATE TABLE IF NOT EXISTS maintenance ("
                    + "issueID INTEGER PRIMARY KEY,"
                    + "facilityName TEXT,"
                    + "issueType TEXT,"
                    + "description TEXT,"
                    + "reportedBy TEXT,"
                    + "reportDate TEXT,"
                    + "assignedTo TEXT,"
                    + "status TEXT DEFAULT 'PENDING',"
                    + "severity TEXT,"
                    + "resolution TEXT,"
                    + "completionDate TEXT,"
                    + "responseTimeMinutes INTEGER DEFAULT 0)");

            // Create facilities table
            stmt.execute("CREATE TABLE IF NOT EXISTS facilities ("
                    + "facilityID INTEGER PRIMARY KEY,"
                    + "facilityName TEXT UNIQUE,"
                    + "facilityType TEXT,"
                    + "location TEXT,"
                    + "operatingHours TEXT,"
                    + "status TEXT DEFAULT 'OPERATIONAL',"
                    + "totalCycles INTEGER DEFAULT 0,"
                    + "cycleThreshold INTEGER DEFAULT 1000,"
                    + "nextScheduledMaintenance TEXT)");

            // Create lostFound table
            stmt.execute("CREATE TABLE IF NOT EXISTS lostFound ("
                    + "itemID INTEGER PRIMARY KEY,"
                    + "roomNumber INTEGER,"
                    + "dateFound TEXT,"
                    + "itemCategory TEXT,"
                    + "itemDescription TEXT,"
                    + "foundBy TEXT,"
                    + "storageLocation TEXT,"
                    + "status TEXT DEFAULT 'FOUND',"
                    + "claimedBy INTEGER DEFAULT 0,"
                    + "claimDate TEXT,"
                    + "claimantName TEXT,"
                    + "disposalReason TEXT,"
                    + "disposalDate TEXT)");

            // Create transactionHistory table (used by transactionHistory.java)
            stmt.execute("CREATE TABLE IF NOT EXISTS transactionHistory ("
                    + "historyID INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "customerID INTEGER,"
                    + "receiptData TEXT,"
                    + "totalAmount REAL,"
                    + "totalPoints INTEGER,"
                    + "transactionDate TEXT)");

            conn.commit();
            initializeDefaults(conn);
            conn.commit();  // Commit room/facility inserts
            loadNextIDs(conn);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization failed", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private void initializeDefaults(Connection conn) throws SQLException {
        // Check if rooms already exist
        try (Statement check = conn.createStatement();
             ResultSet rs = check.executeQuery("SELECT COUNT(*) FROM rooms")) {
            if (rs.next() && rs.getInt(1) > 0) {
                return; // Defaults already initialized
            }
        }

        // Initialize default rooms (5 floors x 4 types)
        String[] roomTypes = {"Standard", "Deluxe", "Suite", "Family"};
        double[] prices = {2500.0, 3500.0, 5500.0, 4500.0};
        int[] capacities = {2, 2, 4, 6};
        int[] hasView = {0, 0, 1, 0};
        int[] isQuiet = {1, 1, 0, 0};
        int[] nearElev = {0, 0, 0, 1};

        String sql = "INSERT INTO rooms (roomNumber, roomType, capacity, pricePerNight, status, floorNumber, hasView, isQuietZone, nearElevator) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int roomNum = 101;
            for (int floor = 1; floor <= 5; floor++) {
                for (int i = 0; i < 4; i++) {
                    ps.setInt(1, roomNum);
                    ps.setString(2, roomTypes[i]);
                    ps.setInt(3, capacities[i]);
                    ps.setDouble(4, prices[i]);
                    ps.setString(5, "AVAILABLE");
                    ps.setInt(6, floor);
                    ps.setInt(7, hasView[i]);
                    ps.setInt(8, isQuiet[i]);
                    ps.setInt(9, nearElev[i]);
                    ps.addBatch();
                    roomNum++;
                }
                roomNum = (floor + 1) * 100 + 1;
            }
            ps.executeBatch();
        }

        // Initialize default facilities
        String[][] facilityData = {
            {"Roller Coaster", "Ride", "Zone A", "09:00-21:00", "500"},
            {"Ferris Wheel", "Ride", "Zone A", "09:00-21:00", "300"},
            {"Water Slide", "Ride", "Zone B", "10:00-18:00", "400"},
            {"Main Restaurant", "Restaurant", "Central Plaza", "07:00-22:00", "1000"},
            {"Gift Shop", "Shop", "Central Plaza", "09:00-21:00", "2000"},
            {"Swimming Pool", "Facility", "Zone B", "08:00-20:00", "500"},
            {"Arcade", "Facility", "Zone C", "10:00-23:00", "1500"}
        };
        String facSql = "INSERT INTO facilities (facilityID, facilityName, facilityType, location, operatingHours, status, cycleThreshold) VALUES (?,?,?,?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(facSql)) {
            for (String[] f : facilityData) {
                ps.setInt(1, nextFacilityID++);
                ps.setString(2, f[0]);
                ps.setString(3, f[1]);
                ps.setString(4, f[2]);
                ps.setString(5, f[3]);
                ps.setString(6, "OPERATIONAL");
                ps.setInt(7, Integer.parseInt(f[4]));
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private void loadNextIDs(Connection conn) throws SQLException {
        // Load max IDs from database to keep counters in sync
        nextTicketID = getMaxID(conn, "ticketRecords", "ticketID") + 1;
        nextTransactionID = getMaxID(conn, "repoTransactions", "transactionID") + 1;
        nextQueueID = getMaxID(conn, "queue", "queueID") + 1;
        nextBookingID = getMaxID(conn, "roomBookings", "bookingID") + 1;
        nextStatusID = getMaxID(conn, "roomStatuses", "statusID") + 1;
        nextTaskID = getMaxID(conn, "housekeeping", "taskID") + 1;
        nextIssueID = getMaxID(conn, "maintenance", "issueID") + 1;
        nextFacilityID = getMaxID(conn, "facilities", "facilityID") + 1;
        nextItemID = getMaxID(conn, "lostFound", "itemID") + 1;
    }

    private int getMaxID(Connection conn, String table, String column) throws SQLException {
        String sql = "SELECT COALESCE(MAX(" + column + "), 0) FROM " + table;
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    // ============ CUSTOMER METHODS ============

    public int generateCustomerID() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COALESCE(MAX(customerID), 9999) FROM customers")) {
            if (rs.next()) {
                int max = rs.getInt(1);
                return Math.max(10000, max + 1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "generateCustomerID failed", e);
        }
        return 10000;
    }

    public boolean saveCustomer(int customerID, String fullName, String contactNumber, int age) {
        if (fullName == null || fullName.trim().isEmpty() ||
            contactNumber == null || contactNumber.trim().isEmpty() ||
            age < 0 || age > 120) {
            LOGGER.warning("Invalid customer data provided");
            return false;
        }

        String sql = "INSERT INTO customers (customerID, customerFullName, customerContactNumber, customerAge, accountDateCreated, accountStatus, membershipType, customerPoints, freebiesCount, ticketBought, totalCost) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            ps.setString(2, fullName.trim());
            ps.setString(3, contactNumber.trim());
            ps.setInt(4, age);
            ps.setString(5, LocalDate.now().toString());
            ps.setString(6, "ACTIVE");
            ps.setString(7, "Regular");
            ps.setInt(8, 0);
            ps.setInt(9, 0);
            ps.setInt(10, 0);
            ps.setDouble(11, 0.0);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "saveCustomer failed", e);
            return false;
        }
    }

    public int findCustomerByID(int loginID) {
        if (loginID < 10000 || loginID > 99999) return -1;
        String sql = "SELECT customerID FROM customers WHERE customerID = ? AND accountStatus = 'ACTIVE'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loginID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String formattedLDT = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
                    try (PreparedStatement upd = conn.prepareStatement("UPDATE customers SET dateTimeIn = ? WHERE customerID = ?")) {
                        upd.setString(1, formattedLDT);
                        upd.setInt(2, loginID);
                        upd.executeUpdate();
                    }
                    LOGGER.info("Customer " + loginID + " logged in successfully");
                    return loginID;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findCustomerByID failed", e);
        }
        return -1;
    }

    public int findCustomerByDetails(String forgotFullName, String forgotContactNumber) {
        if (forgotFullName == null || forgotFullName.trim().isEmpty() ||
            forgotContactNumber == null || forgotContactNumber.trim().isEmpty()) {
            return -1;
        }
        String sql = "SELECT customerID FROM customers WHERE LOWER(customerFullName) = LOWER(?) AND customerContactNumber = ? AND accountStatus = 'ACTIVE'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, forgotFullName.trim());
            ps.setString(2, forgotContactNumber.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("customerID");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "findCustomerByDetails failed", e);
        }
        return -1;
    }

    public boolean trackLogOut(int loginID) {
        String formattedLDT = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String sql = "UPDATE customers SET dateTimeOut = ? WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, formattedLDT);
            ps.setInt(2, loginID);
            int rows = ps.executeUpdate();
            if (rows > 0) {
                LOGGER.info("Customer " + loginID + " logged out successfully");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "trackLogOut failed", e);
        }
        return false;
    }

    public String getCustomerName(int customerID) {
        String sql = "SELECT customerFullName FROM customers WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("customerFullName");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getCustomerName failed", e);
        }
        return "Unknown";
    }

    public int getCustomerAge(int customerID) {
        String sql = "SELECT customerAge FROM customers WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("customerAge");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getCustomerAge failed", e);
        }
        return 0;
    }

    public String[][] getAllCustomers() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT customerID, customerFullName, customerContactNumber, customerAge, accountDateCreated, dateTimeIn, dateTimeOut FROM customers";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                    String.valueOf(rs.getInt("customerID")),
                    rs.getString("customerFullName"),
                    rs.getString("customerContactNumber"),
                    String.valueOf(rs.getInt("customerAge")),
                    rs.getString("accountDateCreated"),
                    rs.getString("dateTimeIn"),
                    rs.getString("dateTimeOut")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAllCustomers failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    // ============ MEMBERSHIP METHODS ============

    public boolean updateMembershipType(int customerID, String membershipType) {
        if (!"Regular".equals(membershipType) && !"VIP".equals(membershipType) && !"Premium".equals(membershipType)) {
            LOGGER.warning("Invalid membership type: " + membershipType);
            return false;
        }
        String sql = "UPDATE customers SET membershipType = ?, freebiesCount = ? WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, membershipType);
            ps.setInt(2, "VIP".equals(membershipType) ? 1 : 0);
            ps.setInt(3, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateMembershipType failed", e);
        }
        return false;
    }

    public String getMembershipType(int customerID) {
        String sql = "SELECT membershipType FROM customers WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String mt = rs.getString("membershipType");
                    return mt != null ? mt : "Regular";
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getMembershipType failed", e);
        }
        return "Regular";
    }

    public int getFreebiesCount(int customerID) {
        String sql = "SELECT freebiesCount FROM customers WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("freebiesCount");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getFreebiesCount failed", e);
        }
        return 0;
    }

    public boolean useFreebies(int customerID) {
        String sql = "UPDATE customers SET freebiesCount = freebiesCount - 1 WHERE customerID = ? AND freebiesCount > 0";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "useFreebies failed", e);
        }
        return false;
    }

    // ============ POINTS METHODS ============

    public boolean loyaltyPoints(int customerID, int totalPoints) {
        if (totalPoints < 0) {
            LOGGER.warning("Cannot add negative points");
            return false;
        }
        String sql = "UPDATE customers SET customerPoints = customerPoints + ? WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, totalPoints);
            ps.setInt(2, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "loyaltyPoints failed", e);
        }
        return false;
    }

    public int getPoints(int customerID) {
        String sql = "SELECT customerPoints FROM customers WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("customerPoints");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getPoints failed", e);
        }
        return 0;
    }

    public boolean deductPoints(int customerID, int points) {
        if (points < 0) {
            LOGGER.warning("Cannot deduct negative points");
            return false;
        }
        String sql = "UPDATE customers SET customerPoints = customerPoints - ? WHERE customerID = ? AND customerPoints >= ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, points);
            ps.setInt(2, customerID);
            ps.setInt(3, points);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "deductPoints failed", e);
        }
        return false;
    }

    // ============ TICKET METHODS ============

    public boolean saveTicketRecord(int customerID, String ticketName, int ticketAge, String ticketType, double ticketPrice) {
        if (ticketName == null || ticketName.trim().isEmpty() || ticketAge < 0 || ticketAge > 120) {
            LOGGER.warning("Invalid ticket data");
            return false;
        }
        String sql = "INSERT INTO ticketRecords (ticketID, customerID, dateBought, ticketAge, ticketName, ticketType, ticketPrice) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextTicketID++);
            ps.setInt(2, customerID);
            ps.setString(3, LocalDate.now().toString());
            ps.setInt(4, ticketAge);
            ps.setString(5, ticketName.trim());
            ps.setString(6, ticketType);
            ps.setDouble(7, ticketPrice);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "saveTicketRecord failed", e);
            return false;
        }
    }

    public boolean updateTicketSummary(int customerID, double price) {
        String sql = "UPDATE customers SET ticketBought = ticketBought + 1, totalCost = totalCost + ? WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, price);
            ps.setInt(2, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateTicketSummary failed", e);
        }
        return false;
    }

    public String[][] getTransactions(int customerID) {
        List<String[]> transactions = new ArrayList<>();
        String sql = "SELECT dateBought, ticketName, ticketAge, appointmentDate, paymentStatus, ticketPrice FROM ticketRecords WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String appt = rs.getString("appointmentDate");
                    String pay = rs.getString("paymentStatus");
                    transactions.add(new String[]{
                        rs.getString("dateBought"),
                        rs.getString("ticketName"),
                        String.valueOf(rs.getInt("ticketAge")),
                        appt != null ? appt : "N/A",
                        pay != null ? pay : "PENDING",
                        String.format("%.2f", rs.getDouble("ticketPrice"))
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTransactions failed", e);
        }
        return transactions.isEmpty() ? null : transactions.toArray(new String[0][]);
    }

    // ============ QUEUE METHODS ============

    public int getQueuePosition(String membership) {
        if ("VIP".equalsIgnoreCase(membership)) return 0;
        String sql = "SELECT COUNT(*) FROM queue WHERE membershipType = 'REGULAR' AND status = 'WAITING'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1) + 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getQueuePosition failed", e);
        }
        return 1;
    }

    public boolean saveQueueEntry(int customerID, String className, String type, int position, String status) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        try (Connection conn = getConnection()) {
            // Remove existing waiting entries for this customer
            try (PreparedStatement del = conn.prepareStatement("DELETE FROM queue WHERE customerID = ? AND status = 'WAITING'")) {
                del.setInt(1, customerID);
                del.executeUpdate();
            }
            String sql = "INSERT INTO queue (queueID, customerID, className, membershipType, queuePosition, status, timestamp) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nextQueueID++);
                ps.setInt(2, customerID);
                ps.setString(3, className);
                ps.setString(4, type);
                ps.setInt(5, position);
                ps.setString(6, status);
                ps.setString(7, timestamp);
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "saveQueueEntry failed", e);
        }
        return false;
    }

    public boolean updateQueueStatus(int customerID, String status) {
        String sql = "UPDATE queue SET status = ? WHERE customerID = ? AND (status = 'WAITING' OR status = 'READY')";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateQueueStatus failed", e);
        }
        return false;
    }

    public String[] getQueueStatus(int customerID) {
        String sql = "SELECT className, status, queuePosition FROM queue WHERE customerID = ? ORDER BY queueID DESC LIMIT 1";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{rs.getString("className"), rs.getString("status"), String.valueOf(rs.getInt("queuePosition"))};
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getQueueStatus failed", e);
        }
        return null;
    }

    // ============ APPOINTMENT METHODS ============

    public String[][] getAppointments(int customerID) {
        List<String[]> appointments = new ArrayList<>();
        String sql = "SELECT appointmentDate, paymentStatus FROM ticketRecords WHERE customerID = ? AND appointmentDate IS NOT NULL";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String pay = rs.getString("paymentStatus");
                    appointments.add(new String[]{
                        rs.getString("appointmentDate"),
                        pay != null ? pay : "PENDING"
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAppointments failed", e);
        }
        return appointments.isEmpty() ? null : appointments.toArray(new String[0][]);
    }

    public boolean saveAppointmentRecord(int customerID, String appointmentDate, String status) {
        if (appointmentDate == null || appointmentDate.trim().isEmpty()) {
            LOGGER.warning("Invalid appointment date");
            return false;
        }
        String sql = "UPDATE ticketRecords SET appointmentDate = ?, paymentStatus = ? WHERE customerID = ? AND appointmentDate IS NULL";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, appointmentDate);
            ps.setString(2, status);
            ps.setInt(3, customerID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "saveAppointmentRecord failed", e);
        }
        return false;
    }

    public boolean cancelAppointment(int customerID, String appointmentDate) {
        String sql = "UPDATE ticketRecords SET paymentStatus = 'CANCELLED' WHERE customerID = ? AND appointmentDate = ? AND paymentStatus != 'CANCELLED'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            ps.setString(2, appointmentDate);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "cancelAppointment failed", e);
        }
        return false;
    }

    public boolean moveAppointment(int customerID, String oldDate, String newDate) {
        String sql = "UPDATE ticketRecords SET appointmentDate = ? WHERE customerID = ? AND appointmentDate = ? AND paymentStatus != 'CANCELLED'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newDate);
            ps.setInt(2, customerID);
            ps.setString(3, oldDate);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "moveAppointment failed", e);
        }
        return false;
    }

    // ============ ROOM MANAGEMENT METHODS ============

    public String[][] getAllRooms() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT roomNumber, roomType, capacity, pricePerNight, status FROM rooms";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                    String.valueOf(rs.getInt("roomNumber")),
                    rs.getString("roomType"),
                    String.valueOf(rs.getInt("capacity")),
                    String.format("%.2f", rs.getDouble("pricePerNight")),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAllRooms failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] getAvailableRooms() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT roomNumber, roomType, capacity, pricePerNight FROM rooms WHERE status = 'AVAILABLE'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                    String.valueOf(rs.getInt("roomNumber")),
                    rs.getString("roomType"),
                    String.valueOf(rs.getInt("capacity")),
                    String.format("%.2f", rs.getDouble("pricePerNight"))
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAvailableRooms failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] getAvailableRoomsByType(String roomType) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT roomNumber, roomType, capacity, pricePerNight FROM rooms WHERE status = 'AVAILABLE' AND roomType = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, roomType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new String[]{
                        String.valueOf(rs.getInt("roomNumber")),
                        rs.getString("roomType"),
                        String.valueOf(rs.getInt("capacity")),
                        String.format("%.2f", rs.getDouble("pricePerNight"))
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAvailableRoomsByType failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public boolean bookRoom(int roomNumber, int customerID, String checkInDate, String checkOutDate, int guests) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            String checkSql = "SELECT status FROM rooms WHERE roomNumber = ? AND status = 'AVAILABLE'";
            try (PreparedStatement check = conn.prepareStatement(checkSql)) {
                check.setInt(1, roomNumber);
                try (ResultSet rs = check.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                }
            }
            String updRoom = "UPDATE rooms SET status = 'OCCUPIED', currentGuestID = ?, checkInDate = ?, checkOutDate = ?, guestCount = ? WHERE roomNumber = ?";
            try (PreparedStatement ps = conn.prepareStatement(updRoom)) {
                ps.setInt(1, customerID);
                ps.setString(2, checkInDate);
                ps.setString(3, checkOutDate);
                ps.setInt(4, guests);
                ps.setInt(5, roomNumber);
                ps.executeUpdate();
            }
            String bookSql = "INSERT INTO roomBookings (bookingID, roomNumber, customerID, checkInDate, checkOutDate, guestCount, bookingStatus) VALUES (?,?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(bookSql)) {
                ps.setInt(1, nextBookingID++);
                ps.setInt(2, roomNumber);
                ps.setInt(3, customerID);
                ps.setString(4, checkInDate);
                ps.setString(5, checkOutDate);
                ps.setInt(6, guests);
                ps.setString(7, "CONFIRMED");
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "bookRoom failed", e);
        }
        return false;
    }

    public boolean checkOutRoom(int roomNumber) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            String updRoom = "UPDATE rooms SET status = 'CLEANING', currentGuestID = 0, checkInDate = NULL, checkOutDate = NULL, guestCount = 0 WHERE roomNumber = ?";
            try (PreparedStatement ps = conn.prepareStatement(updRoom)) {
                ps.setInt(1, roomNumber);
                ps.executeUpdate();
            }
            String updBook = "UPDATE roomBookings SET bookingStatus = 'CHECKED_OUT', actualCheckOut = ? WHERE roomNumber = ? AND bookingStatus = 'CONFIRMED'";
            try (PreparedStatement ps = conn.prepareStatement(updBook)) {
                ps.setString(1, LocalDate.now().toString());
                ps.setInt(2, roomNumber);
                ps.executeUpdate();
            }
            conn.commit();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "checkOutRoom failed", e);
        }
        return false;
    }

    public boolean setRoomCleaned(int roomNumber) {
        String sql = "UPDATE rooms SET status = 'AVAILABLE' WHERE roomNumber = ? AND status = 'CLEANING'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "setRoomCleaned failed", e);
        }
        return false;
    }

    public String[] getRoomDetails(int roomNumber) {
        String sql = "SELECT roomNumber, roomType, capacity, pricePerNight, status, currentGuestID FROM rooms WHERE roomNumber = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int guestID = rs.getInt("currentGuestID");
                    return new String[]{
                        String.valueOf(rs.getInt("roomNumber")),
                        rs.getString("roomType"),
                        String.valueOf(rs.getInt("capacity")),
                        String.format("%.2f", rs.getDouble("pricePerNight")),
                        rs.getString("status"),
                        guestID > 0 ? String.valueOf(guestID) : "None"
                    };
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getRoomDetails failed", e);
        }
        return null;
    }

    // ============ HOUSEKEEPING METHODS ============

    public boolean assignCleaningTask(int roomNumber, String staffName, String priority) {
        String sql = "INSERT INTO housekeeping (taskID, roomNumber, staffName, taskDate, priority, status) VALUES (?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextTaskID++);
            ps.setInt(2, roomNumber);
            ps.setString(3, staffName);
            ps.setString(4, LocalDate.now().toString());
            ps.setString(5, priority);
            ps.setString(6, "PENDING");
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "assignCleaningTask failed", e);
        }
        return false;
    }

    public String[][] getCleaningTasks(String status) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT taskID, roomNumber, staffName, taskDate, priority FROM housekeeping WHERE status = ? ORDER BY CASE priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 WHEN 'LOW' THEN 4 ELSE 5 END";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int rn = rs.getInt("roomNumber");
                    String roomType = getRoomTypeCached(conn, rn);
                    result.add(new String[]{
                        String.valueOf(rs.getInt("taskID")),
                        String.valueOf(rn),
                        roomType,
                        rs.getString("staffName"),
                        rs.getString("taskDate"),
                        rs.getString("priority")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getCleaningTasks failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    private String getRoomTypeCached(Connection conn, int roomNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT roomType FROM rooms WHERE roomNumber = ?")) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("roomType");
            }
        }
        return "Unknown";
    }

    public boolean completeCleaningTask(int taskID, String notes) {
        String sql = "UPDATE housekeeping SET status = 'COMPLETED', completionDate = ?, notes = ? WHERE taskID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            ps.setString(2, notes);
            ps.setInt(3, taskID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "completeCleaningTask failed", e);
        }
        return false;
    }

    // ============ MAINTENANCE METHODS ============

    public boolean reportMaintenanceIssue(String facilityName, String issueType, String description, String reportedBy) {
        return reportMaintenanceIssueWithSeverity(facilityName, issueType, description, reportedBy, "MEDIUM");
    }

    public boolean reportMaintenanceIssueWithSeverity(String facilityName, String issueType, String description, String reportedBy, String severity) {
        String sql = "INSERT INTO maintenance (issueID, facilityName, issueType, description, reportedBy, reportDate, status, severity) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextIssueID++);
            ps.setString(2, facilityName);
            ps.setString(3, issueType);
            ps.setString(4, description);
            ps.setString(5, reportedBy);
            ps.setString(6, LocalDate.now().toString());
            ps.setString(7, "PENDING");
            ps.setString(8, severity);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "reportMaintenanceIssueWithSeverity failed", e);
        }
        return false;
    }

    public String[][] getMaintenanceIssues(String status) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT issueID, facilityName, issueType, description, reportedBy, reportDate, assignedTo, status, severity FROM maintenance WHERE status = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String assigned = rs.getString("assignedTo");
                    result.add(new String[]{
                        String.valueOf(rs.getInt("issueID")),
                        rs.getString("facilityName"),
                        rs.getString("issueType"),
                        rs.getString("description"),
                        rs.getString("reportedBy"),
                        rs.getString("reportDate"),
                        assigned != null ? assigned : "Not Assigned",
                        rs.getString("status"),
                        rs.getString("severity")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getMaintenanceIssues failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] getMaintenanceBySeverity(String severity) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT issueID, facilityName, issueType, description, severity, reportedBy, reportDate, assignedTo, status FROM maintenance WHERE severity = ? AND status != 'COMPLETED'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, severity);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String assigned = rs.getString("assignedTo");
                    result.add(new String[]{
                        String.valueOf(rs.getInt("issueID")),
                        rs.getString("facilityName"),
                        rs.getString("issueType"),
                        rs.getString("description"),
                        rs.getString("severity"),
                        rs.getString("reportedBy"),
                        rs.getString("reportDate"),
                        assigned != null ? assigned : "Not Assigned",
                        rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getMaintenanceBySeverity failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public boolean assignMaintenance(int issueID, String assignedTo) {
        String sql = "UPDATE maintenance SET assignedTo = ?, status = 'IN_PROGRESS' WHERE issueID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, assignedTo);
            ps.setInt(2, issueID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "assignMaintenance failed", e);
        }
        return false;
    }

    public boolean completeMaintenance(int issueID, String resolution) {
        String sql = "UPDATE maintenance SET status = 'COMPLETED', resolution = ?, completionDate = ? WHERE issueID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resolution);
            ps.setString(2, LocalDate.now().toString());
            ps.setInt(3, issueID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "completeMaintenance failed", e);
        }
        return false;
    }

    // ============ FACILITY METHODS ============

    public boolean addFacility(String facilityName, String facilityType, String location, String operatingHours) {
        String checkSql = "SELECT 1 FROM facilities WHERE facilityName = ?";
        try (Connection conn = getConnection(); PreparedStatement check = conn.prepareStatement(checkSql)) {
            check.setString(1, facilityName);
            try (ResultSet rs = check.executeQuery()) {
                if (rs.next()) return false;
            }
            String sql = "INSERT INTO facilities (facilityID, facilityName, facilityType, location, operatingHours, status) VALUES (?,?,?,?,?,?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nextFacilityID++);
                ps.setString(2, facilityName);
                ps.setString(3, facilityType);
                ps.setString(4, location);
                ps.setString(5, operatingHours);
                ps.setString(6, "OPERATIONAL");
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "addFacility failed", e);
        }
        return false;
    }

    public String[][] getAllFacilities() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT facilityID, facilityName, facilityType, location, status FROM facilities";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                    String.valueOf(rs.getInt("facilityID")),
                    rs.getString("facilityName"),
                    rs.getString("facilityType"),
                    rs.getString("location"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAllFacilities failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public boolean updateFacilityStatus(int facilityID, String status) {
        String sql = "UPDATE facilities SET status = ? WHERE facilityID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, facilityID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateFacilityStatus failed", e);
        }
        return false;
    }

    // ============ PREDICTIVE MAINTENANCE METHODS ============

    public boolean logFacilityUsage(int facilityID) {
        String sql = "UPDATE facilities SET totalCycles = totalCycles + 1 WHERE facilityID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "logFacilityUsage failed", e);
        }
        return false;
    }

    public int getTotalCycles(int facilityID) {
        String sql = "SELECT totalCycles FROM facilities WHERE facilityID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("totalCycles");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTotalCycles failed", e);
        }
        return 0;
    }

    public int getCycleThreshold(int facilityID) {
        String sql = "SELECT cycleThreshold FROM facilities WHERE facilityID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("cycleThreshold");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getCycleThreshold failed", e);
        }
        return 1000;
    }

    public boolean schedulePredictiveMaintenance(int facilityID) {
        String nextDate = LocalDate.now().plusDays(7).toString();
        String sql = "UPDATE facilities SET nextScheduledMaintenance = ?, status = 'MAINTENANCE' WHERE facilityID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nextDate);
            ps.setInt(2, facilityID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "schedulePredictiveMaintenance failed", e);
        }
        return false;
    }

    public String[][] getFacilitiesNeedingMaintenance() {
        List<String[]> result = new ArrayList<>();
        String today = LocalDate.now().toString();
        String sql = "SELECT facilityID, facilityName, totalCycles, cycleThreshold, nextScheduledMaintenance FROM facilities";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int cycles = rs.getInt("totalCycles");
                int threshold = rs.getInt("cycleThreshold");
                String nextMaint = rs.getString("nextScheduledMaintenance");
                boolean needsMaint = cycles >= threshold;
                if (nextMaint != null && nextMaint.compareTo(today) <= 0) {
                    needsMaint = true;
                }
                if (needsMaint) {
                    result.add(new String[]{
                        String.valueOf(rs.getInt("facilityID")),
                        rs.getString("facilityName"),
                        String.valueOf(cycles),
                        String.valueOf(threshold),
                        nextMaint
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getFacilitiesNeedingMaintenance failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    // ============ CUSTOMER ROOM BOOKING METHODS ============

    public boolean cancelRoomBooking(int customerID, int roomNumber, String checkInDate) {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            String updRoom = "UPDATE rooms SET status = 'AVAILABLE', currentGuestID = 0, checkInDate = NULL, checkOutDate = NULL, guestCount = 0 WHERE roomNumber = ? AND currentGuestID = ?";
            try (PreparedStatement ps = conn.prepareStatement(updRoom)) {
                ps.setInt(1, roomNumber);
                ps.setInt(2, customerID);
                ps.executeUpdate();
            }
            String updBook = "UPDATE roomBookings SET bookingStatus = 'CANCELLED' WHERE customerID = ? AND roomNumber = ? AND checkInDate = ? AND bookingStatus = 'CONFIRMED'";
            try (PreparedStatement ps = conn.prepareStatement(updBook)) {
                ps.setInt(1, customerID);
                ps.setInt(2, roomNumber);
                ps.setString(3, checkInDate);
                int rows = ps.executeUpdate();
                conn.commit();
                return rows > 0;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "cancelRoomBooking failed", e);
        }
        return false;
    }

    public String[][] getCustomerBookings(int customerID) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT bookingID, roomNumber, checkInDate, checkOutDate, guestCount, bookingStatus FROM roomBookings WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int rn = rs.getInt("roomNumber");
                    String roomType = getRoomTypeCached(conn, rn);
                    result.add(new String[]{
                        String.valueOf(rs.getInt("bookingID")),
                        String.valueOf(rn),
                        roomType,
                        rs.getString("checkInDate"),
                        rs.getString("checkOutDate"),
                        rs.getString("bookingStatus")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getCustomerBookings failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    // ============ SMART ROOM ASSIGNMENT METHODS ============

    public int[] getGuestRoomHistory(int customerID) {
        List<Integer> rooms = new ArrayList<>();
        String sql = "SELECT DISTINCT roomNumber FROM roomBookings WHERE customerID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(rs.getInt("roomNumber"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getGuestRoomHistory failed", e);
        }
        return rooms.stream().mapToInt(Integer::intValue).toArray();
    }

    public int getTotalRoomBookings() {
        String sql = "SELECT COUNT(*) FROM roomBookings";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTotalRoomBookings failed", e);
        }
        return 0;
    }

    public String[][] getTopRoomTypes() {
        java.util.Map<String, Integer> counts = new java.util.HashMap<>();
        String sql = "SELECT roomNumber FROM roomBookings";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int rn = rs.getInt("roomNumber");
                String roomType = getRoomTypeCached(conn, rn);
                counts.put(roomType, counts.getOrDefault(roomType, 0) + 1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getTopRoomTypes failed", e);
        }
        List<String[]> result = new ArrayList<>();
        counts.entrySet().stream()
            .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
            .forEach(e -> result.add(new String[]{e.getKey(), String.valueOf(e.getValue())}));
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public int getCurrentGuestRoom(int customerID) {
        String sql = "SELECT roomNumber FROM rooms WHERE currentGuestID = ? AND status = 'OCCUPIED'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt("roomNumber");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getCurrentGuestRoom failed", e);
        }
        return -1;
    }

    // ============ ROOM CONTROL SYSTEM (IoT) METHODS ============

    public boolean updateRoomTemperature(int roomNumber, double temperature) {
        double clamped = Math.max(16, Math.min(30, temperature));
        String sql = "INSERT INTO roomStatuses (statusID, roomNumber, temperature, lightsOn, dndStatus) VALUES (?,?,?,1,0) "
                + "ON CONFLICT(roomNumber) DO UPDATE SET temperature = excluded.temperature";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextStatusID++);
            ps.setInt(2, roomNumber);
            ps.setDouble(3, clamped);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Fallback for older SQLite without ON CONFLICT
            return updateRoomStatusFallback(roomNumber, clamped, null, null);
        }
    }

    public boolean updateRoomLights(int roomNumber, boolean lightsOn) {
        String sql = "INSERT INTO roomStatuses (statusID, roomNumber, temperature, lightsOn, dndStatus) VALUES (?,?,22.0,?,0) "
                + "ON CONFLICT(roomNumber) DO UPDATE SET lightsOn = excluded.lightsOn";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextStatusID++);
            ps.setInt(2, roomNumber);
            ps.setInt(3, lightsOn ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return updateRoomStatusFallback(roomNumber, null, lightsOn, null);
        }
    }

    public boolean updateRoomDND(int roomNumber, boolean dndStatus) {
        String sql = "INSERT INTO roomStatuses (statusID, roomNumber, temperature, lightsOn, dndStatus) VALUES (?,?,22.0,1,?) "
                + "ON CONFLICT(roomNumber) DO UPDATE SET dndStatus = excluded.dndStatus";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nextStatusID++);
            ps.setInt(2, roomNumber);
            ps.setInt(3, dndStatus ? 1 : 0);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return updateRoomStatusFallback(roomNumber, null, null, dndStatus);
        }
    }

    private boolean updateRoomStatusFallback(int roomNumber, Double temperature, Boolean lightsOn, Boolean dndStatus) {
        try (Connection conn = getConnection()) {
            String check = "SELECT statusID FROM roomStatuses WHERE roomNumber = ?";
            try (PreparedStatement ps = conn.prepareStatement(check)) {
                ps.setInt(1, roomNumber);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        StringBuilder sb = new StringBuilder("UPDATE roomStatuses SET ");
                        if (temperature != null) sb.append("temperature = ?, ");
                        if (lightsOn != null) sb.append("lightsOn = ?, ");
                        if (dndStatus != null) sb.append("dndStatus = ?, ");
                        String sql = sb.toString();
                        sql = sql.substring(0, sql.length() - 2) + " WHERE roomNumber = ?";
                        try (PreparedStatement upd = conn.prepareStatement(sql)) {
                            int idx = 1;
                            if (temperature != null) upd.setDouble(idx++, temperature);
                            if (lightsOn != null) upd.setInt(idx++, lightsOn ? 1 : 0);
                            if (dndStatus != null) upd.setInt(idx++, dndStatus ? 1 : 0);
                            upd.setInt(idx, roomNumber);
                            upd.executeUpdate();
                        }
                    } else {
                        String sql = "INSERT INTO roomStatuses (statusID, roomNumber, temperature, lightsOn, dndStatus) VALUES (?,?,?,?,?)";
                        try (PreparedStatement ins = conn.prepareStatement(sql)) {
                            ins.setInt(1, nextStatusID++);
                            ins.setInt(2, roomNumber);
                            ins.setDouble(3, temperature != null ? temperature : 22.0);
                            ins.setInt(4, lightsOn != null && lightsOn ? 1 : 1);
                            ins.setInt(5, dndStatus != null && dndStatus ? 1 : 0);
                            ins.executeUpdate();
                        }
                    }
                }
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "updateRoomStatusFallback failed", e);
        }
        return false;
    }

    public RoomStatus getRoomStatus(int roomNumber) {
        String sql = "SELECT temperature, lightsOn, dndStatus, lastGuestRequest, requestTime FROM roomStatuses WHERE roomNumber = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new RoomStatus(rs.getDouble("temperature"), rs.getInt("lightsOn") == 1,
                            rs.getInt("dndStatus") == 1, rs.getString("lastGuestRequest"), rs.getString("requestTime"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getRoomStatus failed", e);
        }
        return new RoomStatus(22.0, true, false, null, null);
    }

    public String[][] getAllRoomStatuses() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT roomNumber, temperature, lightsOn, dndStatus, lastGuestRequest FROM roomStatuses";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int rn = rs.getInt("roomNumber");
                String roomStatus = getRoomStatusFromRooms(conn, rn);
                result.add(new String[]{
                    String.valueOf(rn),
                    String.valueOf(rs.getDouble("temperature")),
                    String.valueOf(rs.getInt("lightsOn") == 1),
                    String.valueOf(rs.getInt("dndStatus") == 1),
                    rs.getString("lastGuestRequest"),
                    roomStatus
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getAllRoomStatuses failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    private String getRoomStatusFromRooms(Connection conn, int roomNumber) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT status FROM rooms WHERE roomNumber = ?")) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("status");
            }
        }
        return "UNKNOWN";
    }

    public boolean createGuestHousekeepingRequest(int roomNumber, int customerID, String when, String notes) {
        try (Connection conn = getConnection()) {
            String req = "Housekeeping: " + when + " - " + notes;
            String sql = "INSERT INTO roomStatuses (statusID, roomNumber, temperature, lightsOn, dndStatus, lastGuestRequest, requestTime) VALUES (?,?,22.0,1,0,?,?) "
                    + "ON CONFLICT(roomNumber) DO UPDATE SET lastGuestRequest = excluded.lastGuestRequest, requestTime = excluded.requestTime";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nextStatusID++);
                ps.setInt(2, roomNumber);
                ps.setString(3, req);
                ps.setString(4, LocalDateTime.now().toString());
                ps.executeUpdate();
            }
            assignCleaningTask(roomNumber, "Guest Request", "MEDIUM");
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "createGuestHousekeepingRequest failed", e);
        }
        return false;
    }

    public boolean createTowelRequest(int roomNumber, int customerID, int towelCount) {
        try (Connection conn = getConnection()) {
            String req = "Towels: " + towelCount + " requested";
            String sql = "INSERT INTO roomStatuses (statusID, roomNumber, temperature, lightsOn, dndStatus, lastGuestRequest, requestTime) VALUES (?,?,22.0,1,0,?,?) "
                    + "ON CONFLICT(roomNumber) DO UPDATE SET lastGuestRequest = excluded.lastGuestRequest, requestTime = excluded.requestTime";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, nextStatusID++);
                ps.setInt(2, roomNumber);
                ps.setString(3, req);
                ps.setString(4, LocalDateTime.now().toString());
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "createTowelRequest failed", e);
        }
        return false;
    }

    // ============ LOST & FOUND METHODS ============

    public int saveFoundItem(int roomNumber, String dateFound, String category, String description, String foundBy, String storageLocation) {
        String sql = "INSERT INTO lostFound (itemID, roomNumber, dateFound, itemCategory, itemDescription, foundBy, storageLocation, status) VALUES (?,?,?,?,?,?,?,?)";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            int itemID = nextItemID++;
            ps.setInt(1, itemID);
            ps.setInt(2, roomNumber);
            ps.setString(3, dateFound);
            ps.setString(4, category);
            ps.setString(5, description);
            ps.setString(6, foundBy);
            ps.setString(7, storageLocation);
            ps.setString(8, "FOUND");
            ps.executeUpdate();
            return itemID;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "saveFoundItem failed", e);
        }
        return -1;
    }

    public String[] getLostItemDetails(int itemID) {
        String sql = "SELECT itemID, roomNumber, itemCategory, itemDescription, dateFound, status, storageLocation FROM lostFound WHERE itemID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, itemID);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("itemID")),
                        String.valueOf(rs.getInt("roomNumber")),
                        rs.getString("itemCategory"),
                        rs.getString("itemDescription"),
                        rs.getString("dateFound"),
                        rs.getString("status"),
                        rs.getString("storageLocation")
                    };
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getLostItemDetails failed", e);
        }
        return null;
    }

    public boolean claimItem(int itemID, int customerID, String claimantName) {
        String sql = "UPDATE lostFound SET status = 'CLAIMED', claimedBy = ?, claimDate = ?, claimantName = ? WHERE itemID = ? AND status = 'FOUND'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, customerID);
            ps.setString(2, LocalDate.now().toString());
            ps.setString(3, claimantName);
            ps.setInt(4, itemID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "claimItem failed", e);
        }
        return false;
    }

    public String[][] getUnclaimedItems() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT itemID, roomNumber, itemCategory, itemDescription, dateFound, status, storageLocation FROM lostFound WHERE status = 'FOUND'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(extractLostFoundItem(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getUnclaimedItems failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] getClaimedItems() {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT itemID, itemCategory, itemDescription, claimedBy, claimDate FROM lostFound WHERE status = 'CLAIMED'";
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                result.add(new String[]{
                    String.valueOf(rs.getInt("itemID")),
                    rs.getString("itemCategory"),
                    rs.getString("itemDescription"),
                    String.valueOf(rs.getInt("claimedBy")),
                    rs.getString("claimDate")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getClaimedItems failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] getItemsForDisposal() {
        List<String[]> result = new ArrayList<>();
        String cutoff = LocalDate.now().minusDays(30).toString();
        String sql = "SELECT itemID, itemCategory, itemDescription, dateFound FROM lostFound WHERE status = 'FOUND' AND dateFound <= ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cutoff);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(new String[]{
                        String.valueOf(rs.getInt("itemID")),
                        rs.getString("itemCategory"),
                        rs.getString("itemDescription"),
                        rs.getString("dateFound")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "getItemsForDisposal failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public boolean markItemDisposed(int itemID, String reason) {
        String sql = "UPDATE lostFound SET status = 'DISPOSED', disposalReason = ?, disposalDate = ? WHERE itemID = ?";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, reason);
            ps.setString(2, LocalDate.now().toString());
            ps.setInt(3, itemID);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "markItemDisposed failed", e);
        }
        return false;
    }

    public String[][] searchLostItemsByRoom(int roomNumber) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT itemID, roomNumber, itemCategory, itemDescription, dateFound, status, storageLocation FROM lostFound WHERE roomNumber = ? AND status = 'FOUND'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, roomNumber);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(extractLostFoundItem(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "searchLostItemsByRoom failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] searchLostItemsByDate(String fromDate, String toDate) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT itemID, roomNumber, itemCategory, itemDescription, dateFound, status, storageLocation FROM lostFound WHERE dateFound >= ? AND dateFound <= ? AND status = 'FOUND'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fromDate);
            ps.setString(2, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(extractLostFoundItem(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "searchLostItemsByDate failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] searchLostItemsByCategory(String category) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT itemID, roomNumber, itemCategory, itemDescription, dateFound, status, storageLocation FROM lostFound WHERE itemCategory = ? AND status = 'FOUND'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(extractLostFoundItem(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "searchLostItemsByCategory failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] searchLostItemsByKeyword(String keyword) {
        List<String[]> result = new ArrayList<>();
        String sql = "SELECT itemID, roomNumber, itemCategory, itemDescription, dateFound, status, storageLocation FROM lostFound WHERE LOWER(itemDescription) LIKE ? AND status = 'FOUND'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword.toLowerCase() + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(extractLostFoundItem(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "searchLostItemsByKeyword failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    public String[][] searchLostItemsByRooms(int[] roomNumbers) {
        if (roomNumbers == null || roomNumbers.length == 0) return null;
        List<String[]> result = new ArrayList<>();
        StringBuilder inClause = new StringBuilder("?");
        for (int i = 1; i < roomNumbers.length; i++) inClause.append(",?");
        String sql = "SELECT itemID, roomNumber, itemCategory, itemDescription, dateFound, status, storageLocation FROM lostFound WHERE roomNumber IN (" + inClause + ") AND status = 'FOUND'";
        try (Connection conn = getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < roomNumbers.length; i++) ps.setInt(i + 1, roomNumbers[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) result.add(extractLostFoundItem(rs));
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "searchLostItemsByRooms failed", e);
        }
        return result.isEmpty() ? null : result.toArray(new String[0][]);
    }

    private String[] extractLostFoundItem(ResultSet rs) throws SQLException {
        return new String[]{
            String.valueOf(rs.getInt("itemID")),
            String.valueOf(rs.getInt("roomNumber")),
            rs.getString("itemCategory"),
            rs.getString("itemDescription"),
            rs.getString("dateFound"),
            rs.getString("status"),
            rs.getString("storageLocation")
        };
    }

    // ============ HELPER METHODS ============

    private int indexOf(String[] arr, String val) {
        for (int i = 0; i < arr.length; i++) {
            if (arr[i].equals(val)) return i;
        }
        return arr.length;
    }

    // ============ DATA CLASSES ============

    public static class RoomStatus {
        public final double temperature;
        public final boolean lightsOn;
        public final boolean dndStatus;
        public final String lastRequest;
        public final String requestTime;

        public RoomStatus(double temperature, boolean lightsOn, boolean dndStatus,
                         String lastRequest, String requestTime) {
            this.temperature = temperature;
            this.lightsOn = lightsOn;
            this.dndStatus = dndStatus;
            this.lastRequest = lastRequest;
            this.requestTime = requestTime;
        }
    }
}
