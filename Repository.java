import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 Repository Class - Database Access Layer
 Implements Singleton pattern for database connection management
 */

public class Repository {

    private static final Logger LOGGER = Logger.getLogger(Repository.class.getName());
    private final String dbURL;
    private static Repository instance;
    private static final Object lock = new Object();

    private Repository(String dbURL) {
        this.dbURL = dbURL;
        initializeDatabase();
    }

// Thread-safe singleton instance getter
    public static Repository getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    String userHome = System.getProperty("user.home");
                    String dbURL = "jdbc:sqlite:" + userHome + "/theme_park_resort.db";
                    instance = new Repository(dbURL);
                }
            }
        }
        return instance;
    }

// Get database connection with error handling
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbURL);
    }

// Initialize database with required tables
    private void initializeDatabase() {
        String[] createTables = {
            // Customer Details Table
            "CREATE TABLE IF NOT EXISTS tbl_customerDetails (" +
                "customerID INTEGER PRIMARY KEY, " +
                "customerFullName TEXT NOT NULL, " +
                "customerContactNumber TEXT NOT NULL, " +
                "customerAge INTEGER NOT NULL CHECK(customerAge >= 0 AND customerAge <= 120), " +
                "accountDateCreated TEXT NOT NULL, " +
                "dateTimeIn TEXT, " +
                "dateTimeOut TEXT, " +
                "accountStatus TEXT DEFAULT 'ACTIVE')",

            // Customer Records Table
            "CREATE TABLE IF NOT EXISTS tbl_customerRecords (" +
                "recordID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID INTEGER NOT NULL UNIQUE, " +
                "membershipType TEXT DEFAULT 'Regular', " +
                "customerPoints INTEGER DEFAULT 0, " +
                "freebiesCount INTEGER DEFAULT 0, " +
                "ticketBought INTEGER DEFAULT 0, " +
                "totalCost REAL DEFAULT 0.0, " +
                "FOREIGN KEY (customerID) REFERENCES tbl_customerDetails(customerID) ON DELETE CASCADE)",

            // Ticket Records Table
            "CREATE TABLE IF NOT EXISTS tbl_ticketRecords (" +
                "ticketID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID INTEGER NOT NULL, " +
                "dateBought TEXT NOT NULL, " +
                "ticketAge INTEGER NOT NULL, " +
                "ticketName TEXT NOT NULL, " +
                "ticketType TEXT, " +
                "ticketPrice REAL NOT NULL, " +
                "appointmentDate TEXT, " +
                "paymentStatus TEXT DEFAULT 'PENDING', " +
                "FOREIGN KEY (customerID) REFERENCES tbl_customerDetails(customerID) ON DELETE CASCADE)",

            // Transaction History Table
            "CREATE TABLE IF NOT EXISTS tbl_transactionHistory (" +
                "transactionID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID INTEGER NOT NULL, " +
                "transactionDate TEXT NOT NULL, " +
                "transactionType TEXT NOT NULL, " +
                "description TEXT, " +
                "amount REAL NOT NULL, " +
                "pointsEarned INTEGER DEFAULT 0, " +
                "receiptData TEXT, " +
                "FOREIGN KEY (customerID) REFERENCES tbl_customerDetails(customerID) ON DELETE CASCADE)",

            // Queue Table
            "CREATE TABLE IF NOT EXISTS tbl_queue (" +
                "queueID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "customerID INTEGER NOT NULL, " +
                "className TEXT NOT NULL, " +
                "membershipType TEXT DEFAULT 'REGULAR', " +
                "queuePosition INTEGER NOT NULL, " +
                "status TEXT DEFAULT 'WAITING', " +
                "timestamp TEXT NOT NULL, " +
                "FOREIGN KEY (customerID) REFERENCES tbl_customerDetails(customerID) ON DELETE CASCADE)",

            // Rooms Table
            "CREATE TABLE IF NOT EXISTS tbl_rooms (" +
                "roomNumber INTEGER PRIMARY KEY, " +
                "roomType TEXT NOT NULL, " +
                "capacity INTEGER NOT NULL, " +
                "pricePerNight REAL NOT NULL, " +
                "status TEXT DEFAULT 'AVAILABLE', " +
                "currentGuestID INTEGER, " +
                "checkInDate TEXT, " +
                "checkOutDate TEXT, " +
                "guestCount INTEGER DEFAULT 0, " +
                "floorNumber INTEGER, " +
                "hasView INTEGER DEFAULT 0, " +
                "isQuietZone INTEGER DEFAULT 0, " +
                "nearElevator INTEGER DEFAULT 0, " +
                "FOREIGN KEY (currentGuestID) REFERENCES tbl_customerDetails(customerID) ON DELETE SET NULL)",

            // Room Bookings Table
            "CREATE TABLE IF NOT EXISTS tbl_roomBookings (" +
                "bookingID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roomNumber INTEGER NOT NULL, " +
                "customerID INTEGER NOT NULL, " +
                "checkInDate TEXT NOT NULL, " +
                "checkOutDate TEXT NOT NULL, " +
                "guestCount INTEGER NOT NULL, " +
                "bookingStatus TEXT DEFAULT 'CONFIRMED', " +
                "actualCheckOut TEXT, " +
                "FOREIGN KEY (roomNumber) REFERENCES tbl_rooms(roomNumber) ON DELETE CASCADE, " +
                "FOREIGN KEY (customerID) REFERENCES tbl_customerDetails(customerID) ON DELETE CASCADE)",

            // Room Status Table (IoT)
            "CREATE TABLE IF NOT EXISTS tbl_roomStatus (" +
                "statusID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roomNumber INTEGER NOT NULL UNIQUE, " +
                "temperature REAL DEFAULT 22.0, " +
                "lightsOn INTEGER DEFAULT 1, " +
                "dndStatus INTEGER DEFAULT 0, " +
                "lastGuestRequest TEXT, " +
                "requestTime TEXT, " +
                "FOREIGN KEY (roomNumber) REFERENCES tbl_rooms(roomNumber) ON DELETE CASCADE)",

            // Housekeeping Table
            "CREATE TABLE IF NOT EXISTS tbl_housekeeping (" +
                "taskID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roomNumber INTEGER NOT NULL, " +
                "staffName TEXT NOT NULL, " +
                "taskDate TEXT NOT NULL, " +
                "priority TEXT DEFAULT 'MEDIUM', " +
                "status TEXT DEFAULT 'PENDING', " +
                "completionDate TEXT, " +
                "notes TEXT, " +
                "FOREIGN KEY (roomNumber) REFERENCES tbl_rooms(roomNumber) ON DELETE CASCADE)",

            // Maintenance Table
            "CREATE TABLE IF NOT EXISTS tbl_maintenance (" +
                "issueID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "facilityName TEXT NOT NULL, " +
                "issueType TEXT NOT NULL, " +
                "description TEXT NOT NULL, " +
                "reportedBy TEXT NOT NULL, " +
                "reportDate TEXT NOT NULL, " +
                "assignedTo TEXT, " +
                "status TEXT DEFAULT 'PENDING', " +
                "severity TEXT DEFAULT 'LOW', " +
                "resolution TEXT, " +
                "completionDate TEXT, " +
                "responseTimeMinutes INTEGER)",

            // Facilities Table
            "CREATE TABLE IF NOT EXISTS tbl_facilities (" +
                "facilityID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "facilityName TEXT NOT NULL UNIQUE, " +
                "facilityType TEXT NOT NULL, " +
                "location TEXT NOT NULL, " +
                "operatingHours TEXT, " +
                "status TEXT DEFAULT 'OPERATIONAL', " +
                "totalCycles INTEGER DEFAULT 0, " +
                "cycleThreshold INTEGER DEFAULT 1000, " +
                "nextScheduledMaintenance TEXT)",

            // Lost and Found Table
            "CREATE TABLE IF NOT EXISTS tbl_lostFound (" +
                "itemID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "roomNumber INTEGER, " +
                "dateFound TEXT NOT NULL, " +
                "itemCategory TEXT NOT NULL, " +
                "itemDescription TEXT NOT NULL, " +
                "foundBy TEXT NOT NULL, " +
                "storageLocation TEXT NOT NULL, " +
                "status TEXT DEFAULT 'FOUND', " +
                "claimedBy INTEGER, " +
                "claimDate TEXT, " +
                "claimantName TEXT, " +
                "disposalReason TEXT, " +
                "disposalDate TEXT, " +
                "FOREIGN KEY (roomNumber) REFERENCES tbl_rooms(roomNumber) ON DELETE SET NULL, " +
                "FOREIGN KEY (claimedBy) REFERENCES tbl_customerDetails(customerID) ON DELETE SET NULL)"
        };

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Enable foreign keys
            stmt.execute("PRAGMA foreign_keys = ON");

            // Create all tables
            for (String sql : createTables) {
                stmt.execute(sql);
            }

            // Initialize default rooms if empty
            initializeDefaultRooms(conn);
            initializeDefaultFacilities(conn);

            LOGGER.info("Database initialized successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database initialization error: " + e.getMessage(), e);
        }
    }

    private void initializeDefaultRooms(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM tbl_rooms";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String insertSql = "INSERT INTO tbl_rooms (roomNumber, roomType, capacity, pricePerNight, status, floorNumber, hasView, isQuietZone, nearElevator) VALUES (?, ?, ?, ?, 'AVAILABLE', ?, ?, ?, ?)";
        String[] roomTypes = {"Standard", "Deluxe", "Suite", "Family"};
        double[] prices = {2500.0, 3500.0, 5500.0, 4500.0};
        int[] capacities = {2, 2, 4, 6};
        int[] hasView = {0, 0, 1, 0};
        int[] isQuiet = {1, 1, 0, 0};
        int[] nearElev = {0, 0, 0, 1};

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            int roomNum = 101;
            for (int floor = 1; floor <= 5; floor++) {
                for (int i = 0; i < 4; i++) {
                    pstmt.setInt(1, roomNum);
                    pstmt.setString(2, roomTypes[i]);
                    pstmt.setInt(3, capacities[i]);
                    pstmt.setDouble(4, prices[i]);
                    pstmt.setInt(5, floor);
                    pstmt.setInt(6, hasView[i]);
                    pstmt.setInt(7, isQuiet[i]);
                    pstmt.setInt(8, nearElev[i]);
                    pstmt.executeUpdate();
                    roomNum++;
                }
                roomNum = (floor + 1) * 100 + 1;
            }
        }
    }

    private void initializeDefaultFacilities(Connection conn) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM tbl_facilities";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(checkSql)) {
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        String insertSql = "INSERT OR IGNORE INTO tbl_facilities (facilityName, facilityType, location, operatingHours, status, cycleThreshold) VALUES (?, ?, ?, ?, 'OPERATIONAL', ?)";
        String[][] facilities = {
            {"Roller Coaster", "Ride", "Zone A", "09:00-21:00", "500"},
            {"Ferris Wheel", "Ride", "Zone A", "09:00-21:00", "300"},
            {"Water Slide", "Ride", "Zone B", "10:00-18:00", "400"},
            {"Main Restaurant", "Restaurant", "Central Plaza", "07:00-22:00", "1000"},
            {"Gift Shop", "Shop", "Central Plaza", "09:00-21:00", "2000"},
            {"Swimming Pool", "Facility", "Zone B", "08:00-20:00", "500"},
            {"Arcade", "Facility", "Zone C", "10:00-23:00", "1500"}
        };

        try (PreparedStatement pstmt = conn.prepareStatement(insertSql)) {
            for (String[] facility : facilities) {
                pstmt.setString(1, facility[0]);
                pstmt.setString(2, facility[1]);
                pstmt.setString(3, facility[2]);
                pstmt.setString(4, facility[3]);
                pstmt.setInt(5, Integer.parseInt(facility[4]));
                pstmt.executeUpdate();
            }
        }
    }

    // ============ CUSTOMER METHODS ============

    public int generateCustomerID() {
        int customerID = 10000;
        String sql = "SELECT customerID FROM tbl_customerDetails ORDER BY customerID ASC";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                if (rs.getInt("customerID") == customerID) {
                    customerID++;
                } else {
                    break;
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error generating customer ID: " + e.getMessage(), e);
        }
        return customerID;
    }

    public boolean saveCustomer(int customerID, String fullName, String contactNumber, int age) {
        if (fullName == null || fullName.trim().isEmpty() ||
            contactNumber == null || contactNumber.trim().isEmpty() ||
            age < 0 || age > 120) {
            LOGGER.warning("Invalid customer data provided");
            return false;
        }

        String registrationDate = LocalDate.now().toString();
        String sql = "INSERT INTO tbl_customerDetails (accountDateCreated, customerID, customerFullName, customerContactNumber, customerAge) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, registrationDate);
            pstmt.setInt(2, customerID);
            pstmt.setString(3, fullName.trim());
            pstmt.setString(4, contactNumber.trim());
            pstmt.setInt(5, age);
            pstmt.executeUpdate();

            // Create customer record
            String recordSql = "INSERT INTO tbl_customerRecords (customerID, membershipType, freebiesCount) VALUES (?, 'Regular', 0)";
            try (PreparedStatement recordPstmt = conn.prepareStatement(recordSql)) {
                recordPstmt.setInt(1, customerID);
                recordPstmt.executeUpdate();
            }

            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving customer: " + e.getMessage(), e);
            return false;
        }
    }

    public int findCustomerByID(int loginID) {
        if (loginID < 10000 || loginID > 99999) {
            return -1;
        }

        String sql = "SELECT customerID, customerFullName FROM tbl_customerDetails WHERE customerID = ? AND accountStatus = 'ACTIVE'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, loginID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String formattedLDT = LocalDateTime.now()
                            .format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

                    String updateSql = "UPDATE tbl_customerDetails SET dateTimeIn = ? WHERE customerID = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, formattedLDT);
                        updateStmt.setInt(2, loginID);
                        updateStmt.executeUpdate();
                    }

                    LOGGER.info("Customer " + loginID + " logged in successfully");
                    return rs.getInt("customerID");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding customer: " + e.getMessage(), e);
        }
        return -1;
    }

    public int findCustomerByDetails(String forgotFullName, String forgotContactNumber) {
        if (forgotFullName == null || forgotFullName.trim().isEmpty() ||
            forgotContactNumber == null || forgotContactNumber.trim().isEmpty()) {
            return -1;
        }

        String sql = "SELECT customerID FROM tbl_customerDetails WHERE LOWER(customerFullName) = LOWER(?) AND customerContactNumber = ? AND accountStatus = 'ACTIVE'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, forgotFullName.trim());
            pstmt.setString(2, forgotContactNumber.trim());

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customerID");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error finding customer by details: " + e.getMessage(), e);
        }
        return -1;
    }

    public boolean trackLogOut(int loginID) {
        String formattedLDT = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
        String updateSql = "UPDATE tbl_customerDetails SET dateTimeOut = ? WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSql)) {

            pstmt.setString(1, formattedLDT);
            pstmt.setInt(2, loginID);
            int updated = pstmt.executeUpdate();

            if (updated > 0) {
                LOGGER.info("Customer " + loginID + " logged out successfully");
                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error tracking logout: " + e.getMessage(), e);
        }
        return false;
    }

    public String getCustomerName(int customerID) {
        String sql = "SELECT customerFullName FROM tbl_customerDetails WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("customerFullName");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting customer name: " + e.getMessage(), e);
        }
        return "Unknown";
    }

    public int getCustomerAge(int customerID) {
        String sql = "SELECT customerAge FROM tbl_customerDetails WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customerAge");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting customer age: " + e.getMessage(), e);
        }
        return 0;
    }

    // ============ MEMBERSHIP METHODS ============

    public boolean updateMembershipType(int customerID, String membershipType) {
        if (!"Regular".equals(membershipType) && !"VIP".equals(membershipType) && !"Premium".equals(membershipType)) {
            LOGGER.warning("Invalid membership type: " + membershipType);
            return false;
        }

        String sql = "UPDATE tbl_customerRecords SET membershipType = ?, freebiesCount = ? WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, membershipType);
            pstmt.setInt(2, "VIP".equals(membershipType) ? 1 : 0);
            pstmt.setInt(3, customerID);

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating membership: " + e.getMessage(), e);
            return false;
        }
    }

    public String getMembershipType(int customerID) {
        String sql = "SELECT membershipType FROM tbl_customerRecords WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String type = rs.getString("membershipType");
                    return type != null ? type : "Regular";
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting membership type: " + e.getMessage(), e);
        }
        return "Regular";
    }

    public int getFreebiesCount(int customerID) {
        String sql = "SELECT freebiesCount FROM tbl_customerRecords WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("freebiesCount");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting freebies count: " + e.getMessage(), e);
        }
        return 0;
    }

    public boolean useFreebies(int customerID) {
        String sql = "UPDATE tbl_customerRecords SET freebiesCount = CASE WHEN freebiesCount > 0 THEN freebiesCount - 1 ELSE 0 END WHERE customerID = ? AND freebiesCount > 0";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error using freebies: " + e.getMessage(), e);
            return false;
        }
    }

    // ============ POINTS METHODS ============

    public boolean loyaltyPoints(int customerID, int totalPoints) {
        if (totalPoints < 0) {
            LOGGER.warning("Cannot add negative points");
            return false;
        }

        String sql = "UPDATE tbl_customerRecords SET customerPoints = COALESCE(customerPoints, 0) + ? WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, totalPoints);
            pstmt.setInt(2, customerID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding loyalty points: " + e.getMessage(), e);
            return false;
        }
    }

    public int getPoints(int customerID) {
        String sql = "SELECT customerPoints FROM tbl_customerRecords WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("customerPoints");
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting points: " + e.getMessage(), e);
        }
        return 0;
    }

    public boolean deductPoints(int customerID, int points) {
        if (points < 0) {
            LOGGER.warning("Cannot deduct negative points");
            return false;
        }

        // Pre-check: ensure customer has enough points
        int currentPoints = getPoints(customerID);
        if (currentPoints < points) {
            LOGGER.warning("Insufficient points for customer " + customerID + ": " + currentPoints + " < " + points);
            return false;
        }

        String sql = "UPDATE tbl_customerRecords SET customerPoints = customerPoints - ? WHERE customerID = ? AND customerPoints >= ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, points);
            pstmt.setInt(2, customerID);
            pstmt.setInt(3, points);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error deducting points: " + e.getMessage(), e);
            return false;
        }
    }

    // ============ TICKET METHODS ============

    public boolean saveTicketRecord(int customerID, String ticketName, int ticketAge, String ticketType, double ticketPrice) {
        if (ticketName == null || ticketName.trim().isEmpty() || ticketAge < 0 || ticketAge > 120) {
            LOGGER.warning("Invalid ticket data");
            return false;
        }

        String sql = "INSERT INTO tbl_ticketRecords (customerID, dateBought, ticketAge, ticketName, ticketType, ticketPrice) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.setInt(3, ticketAge);
            pstmt.setString(4, ticketName.trim());
            pstmt.setString(5, ticketType);
            pstmt.setDouble(6, ticketPrice);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving ticket record: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateTicketSummary(int customerID, double price) {
        String sql = "UPDATE tbl_customerRecords SET ticketBought = COALESCE(ticketBought, 0) + 1, totalCost = COALESCE(totalCost, 0) + ? WHERE customerID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setDouble(1, price);
            pstmt.setInt(2, customerID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating ticket summary: " + e.getMessage(), e);
            return false;
        }
    }

    public String[][] getTransactions(int customerID) {
        String sql = "SELECT dateBought, ticketName, ticketAge, appointmentDate, paymentStatus, ticketPrice FROM tbl_ticketRecords WHERE customerID = ? ORDER BY dateBought DESC";

        List<String[]> transactions = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    transactions.add(new String[]{
                        rs.getString("dateBought"),
                        rs.getString("ticketName"),
                        String.valueOf(rs.getInt("ticketAge")),
                        rs.getString("appointmentDate"),
                        rs.getString("paymentStatus"),
                        String.format("%.2f", rs.getDouble("ticketPrice"))
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting transactions: " + e.getMessage(), e);
        }

        return transactions.isEmpty() ? null : transactions.toArray(new String[0][]);
    }

    // ============ QUEUE METHODS ============

    public int getQueuePosition(String membership) {
        if ("VIP".equalsIgnoreCase(membership)) return 0;

        String sql = "SELECT COUNT(*) as count FROM tbl_queue WHERE membershipType = 'REGULAR' AND status = 'WAITING'";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) return rs.getInt("count") + 1;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting queue position: " + e.getMessage(), e);
        }
        return 1;
    }

    public boolean saveQueueEntry(int customerID, String className, String type, int position, String status) {
        // First, remove any existing queue entry for this customer
        String deleteSql = "DELETE FROM tbl_queue WHERE customerID = ? AND status = 'WAITING'";
        String insertSql = "INSERT INTO tbl_queue (customerID, className, membershipType, queuePosition, status, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                deleteStmt.setInt(1, customerID);
                deleteStmt.executeUpdate();

                insertStmt.setInt(1, customerID);
                insertStmt.setString(2, className);
                insertStmt.setString(3, type);
                insertStmt.setInt(4, position);
                insertStmt.setString(5, status);
                insertStmt.setString(6, timestamp);
                insertStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving queue entry: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateQueueStatus(int customerID, String status) {
        String sql = "UPDATE tbl_queue SET status = ? WHERE customerID = ? AND status IN ('WAITING', 'READY')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, customerID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating queue status: " + e.getMessage(), e);
            return false;
        }
    }

    public String[] getQueueStatus(int customerID) {
        String sql = "SELECT className, status, queuePosition FROM tbl_queue WHERE customerID = ? ORDER BY queueID DESC LIMIT 1";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("className"),
                        rs.getString("status"),
                        String.valueOf(rs.getInt("queuePosition"))
                    };
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting queue status: " + e.getMessage(), e);
        }
        return null;
    }

    // ============ APPOINTMENT METHODS ============

    public String[][] getAppointments(int customerID) {
        String sql = "SELECT appointmentDate, paymentStatus FROM tbl_ticketRecords WHERE customerID = ? AND appointmentDate IS NOT NULL ORDER BY appointmentDate";

        List<String[]> appointments = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new String[]{
                        rs.getString("appointmentDate"),
                        rs.getString("paymentStatus")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting appointments: " + e.getMessage(), e);
        }

        return appointments.isEmpty() ? null : appointments.toArray(new String[0][]);
    }

    public boolean saveAppointmentRecord(int customerID, String appointmentDate, String status) {
        if (appointmentDate == null || appointmentDate.trim().isEmpty()) {
            LOGGER.warning("Invalid appointment date");
            return false;
        }

        String sql = "UPDATE tbl_ticketRecords SET appointmentDate = ?, paymentStatus = ? WHERE customerID = ? AND appointmentDate IS NULL";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, appointmentDate);
            pstmt.setString(2, status);
            pstmt.setInt(3, customerID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving appointment: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean cancelAppointment(int customerID, String appointmentDate) {
        String sql = "UPDATE tbl_ticketRecords SET paymentStatus = 'CANCELLED' WHERE customerID = ? AND appointmentDate = ? AND paymentStatus != 'CANCELLED'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            pstmt.setString(2, appointmentDate);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cancelling appointment: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean moveAppointment(int customerID, String oldDate, String newDate) {
        String sql = "UPDATE tbl_ticketRecords SET appointmentDate = ? WHERE customerID = ? AND appointmentDate = ? AND paymentStatus != 'CANCELLED'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newDate);
            pstmt.setInt(2, customerID);
            pstmt.setString(3, oldDate);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error moving appointment: " + e.getMessage(), e);
            return false;
        }
    }

    // ============ ROOM MANAGEMENT METHODS ============

    public String[][] getAllRooms() {
        String sql = "SELECT roomNumber, roomType, capacity, pricePerNight, status FROM tbl_rooms ORDER BY roomNumber";

        List<String[]> rooms = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(new String[]{
                    String.valueOf(rs.getInt("roomNumber")),
                    rs.getString("roomType"),
                    String.valueOf(rs.getInt("capacity")),
                    String.format("%.2f", rs.getDouble("pricePerNight")),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all rooms: " + e.getMessage(), e);
        }

        return rooms.isEmpty() ? null : rooms.toArray(new String[0][]);
    }

    public String[][] getAvailableRooms() {
        String sql = "SELECT roomNumber, roomType, capacity, pricePerNight FROM tbl_rooms WHERE status = 'AVAILABLE' ORDER BY roomNumber";

        List<String[]> rooms = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                rooms.add(new String[]{
                    String.valueOf(rs.getInt("roomNumber")),
                    rs.getString("roomType"),
                    String.valueOf(rs.getInt("capacity")),
                    String.format("%.2f", rs.getDouble("pricePerNight"))
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting available rooms: " + e.getMessage(), e);
        }

        return rooms.isEmpty() ? null : rooms.toArray(new String[0][]);
    }

    public String[][] getAvailableRoomsByType(String roomType) {
        String sql = "SELECT roomNumber, roomType, capacity, pricePerNight FROM tbl_rooms WHERE status = 'AVAILABLE' AND roomType = ? ORDER BY roomNumber";

        List<String[]> rooms = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomType);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(new String[]{
                        String.valueOf(rs.getInt("roomNumber")),
                        rs.getString("roomType"),
                        String.valueOf(rs.getInt("capacity")),
                        String.format("%.2f", rs.getDouble("pricePerNight"))
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting rooms by type: " + e.getMessage(), e);
        }

        return rooms.isEmpty() ? null : rooms.toArray(new String[0][]);
    }

    public boolean bookRoom(int roomNumber, int customerID, String checkInDate, String checkOutDate, int guests) {
        String updateSql = "UPDATE tbl_rooms SET status = 'OCCUPIED', currentGuestID = ?, checkInDate = ?, checkOutDate = ?, guestCount = ? WHERE roomNumber = ? AND status = 'AVAILABLE'";
        String bookingSql = "INSERT INTO tbl_roomBookings (roomNumber, customerID, checkInDate, checkOutDate, guestCount, bookingStatus) VALUES (?, ?, ?, ?, ?, 'CONFIRMED')";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                 PreparedStatement bookingStmt = conn.prepareStatement(bookingSql)) {

                updateStmt.setInt(1, customerID);
                updateStmt.setString(2, checkInDate);
                updateStmt.setString(3, checkOutDate);
                updateStmt.setInt(4, guests);
                updateStmt.setInt(5, roomNumber);
                int updated = updateStmt.executeUpdate();

                if (updated == 0) {
                    conn.rollback();
                    return false;
                }

                bookingStmt.setInt(1, roomNumber);
                bookingStmt.setInt(2, customerID);
                bookingStmt.setString(3, checkInDate);
                bookingStmt.setString(4, checkOutDate);
                bookingStmt.setInt(5, guests);
                bookingStmt.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error booking room: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean checkOutRoom(int roomNumber) {
        String updateSql = "UPDATE tbl_rooms SET status = 'CLEANING', currentGuestID = NULL, checkInDate = NULL, checkOutDate = NULL, guestCount = 0 WHERE roomNumber = ?";
        String bookingSql = "UPDATE tbl_roomBookings SET bookingStatus = 'CHECKED_OUT', actualCheckOut = ? WHERE roomNumber = ? AND bookingStatus = 'CONFIRMED'";

        try (Connection conn = getConnection()) {
            try (PreparedStatement updateStmt = conn.prepareStatement(updateSql);
                 PreparedStatement bookingStmt = conn.prepareStatement(bookingSql)) {

                updateStmt.setInt(1, roomNumber);
                updateStmt.executeUpdate();

                bookingStmt.setString(1, LocalDate.now().toString());
                bookingStmt.setInt(2, roomNumber);
                bookingStmt.executeUpdate();

                return true;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking out room: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean setRoomCleaned(int roomNumber) {
        String sql = "UPDATE tbl_rooms SET status = 'AVAILABLE' WHERE roomNumber = ? AND status = 'CLEANING'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error setting room cleaned: " + e.getMessage(), e);
            return false;
        }
    }

    public String[] getRoomDetails(int roomNumber) {
        String sql = "SELECT * FROM tbl_rooms WHERE roomNumber = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        String.valueOf(rs.getInt("roomNumber")),
                        rs.getString("roomType"),
                        String.valueOf(rs.getInt("capacity")),
                        String.format("%.2f", rs.getDouble("pricePerNight")),
                        rs.getString("status"),
                        rs.getString("currentGuestID") != null ? String.valueOf(rs.getInt("currentGuestID")) : "None"
                    };
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting room details: " + e.getMessage(), e);
        }
        return null;
    }

    // ============ HOUSEKEEPING METHODS ============

    public boolean assignCleaningTask(int roomNumber, String staffName, String priority) {
        String sql = "INSERT INTO tbl_housekeeping (roomNumber, staffName, taskDate, priority, status) VALUES (?, ?, ?, ?, 'PENDING')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setString(2, staffName);
            pstmt.setString(3, LocalDate.now().toString());
            pstmt.setString(4, priority);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning cleaning task: " + e.getMessage(), e);
            return false;
        }
    }

    public String[][] getCleaningTasks(String status) {
        String sql = "SELECT h.*, r.roomType FROM tbl_housekeeping h JOIN tbl_rooms r ON h.roomNumber = r.roomNumber WHERE h.status = ? ORDER BY CASE h.priority WHEN 'URGENT' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END, h.taskDate";

        List<String[]> tasks = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    tasks.add(new String[]{
                        String.valueOf(rs.getInt("taskID")),
                        String.valueOf(rs.getInt("roomNumber")),
                        rs.getString("roomType"),
                        rs.getString("staffName"),
                        rs.getString("taskDate"),
                        rs.getString("priority")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting cleaning tasks: " + e.getMessage(), e);
        }

        return tasks.isEmpty() ? null : tasks.toArray(new String[0][]);
    }

    public boolean completeCleaningTask(int taskID, String notes) {
        String sql = "UPDATE tbl_housekeeping SET status = 'COMPLETED', completionDate = ?, notes = ? WHERE taskID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDate.now().toString());
            pstmt.setString(2, notes);
            pstmt.setInt(3, taskID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error completing cleaning task: " + e.getMessage(), e);
            return false;
        }
    }

    // ============ MAINTENANCE METHODS ============

    public boolean reportMaintenanceIssue(String facilityName, String issueType, String description, String reportedBy) {
        String sql = "INSERT INTO tbl_maintenance (facilityName, issueType, description, reportedBy, reportDate, status) VALUES (?, ?, ?, ?, ?, 'PENDING')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, facilityName);
            pstmt.setString(2, issueType);
            pstmt.setString(3, description);
            pstmt.setString(4, reportedBy);
            pstmt.setString(5, LocalDate.now().toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reporting maintenance issue: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean reportMaintenanceIssueWithSeverity(String facilityName, String issueType, String description, String reportedBy, String severity) {
        String sql = "INSERT INTO tbl_maintenance (facilityName, issueType, description, reportedBy, reportDate, status, severity) VALUES (?, ?, ?, ?, ?, 'PENDING', ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, facilityName);
            pstmt.setString(2, issueType);
            pstmt.setString(3, description);
            pstmt.setString(4, reportedBy);
            pstmt.setString(5, LocalDate.now().toString());
            pstmt.setString(6, severity);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error reporting maintenance issue with severity: " + e.getMessage(), e);
            return false;
        }
    }

    public String[][] getMaintenanceIssues(String status) {
        String sql = "SELECT * FROM tbl_maintenance WHERE status = ? ORDER BY reportDate";

        List<String[]> issues = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    issues.add(new String[]{
                        String.valueOf(rs.getInt("issueID")),
                        rs.getString("facilityName"),
                        rs.getString("issueType"),
                        rs.getString("description"),
                        rs.getString("reportedBy"),
                        rs.getString("reportDate"),
                        rs.getString("assignedTo") != null ? rs.getString("assignedTo") : "Not Assigned",
                        rs.getString("status"),
                        rs.getString("severity")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting maintenance issues: " + e.getMessage(), e);
        }

        return issues.isEmpty() ? null : issues.toArray(new String[0][]);
    }

    public String[][] getMaintenanceBySeverity(String severity) {
        String sql = "SELECT * FROM tbl_maintenance WHERE severity = ? AND status != 'COMPLETED' ORDER BY reportDate";

        List<String[]> issues = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, severity);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    issues.add(new String[]{
                        String.valueOf(rs.getInt("issueID")),
                        rs.getString("facilityName"),
                        rs.getString("issueType"),
                        rs.getString("description"),
                        rs.getString("severity"),
                        rs.getString("reportedBy"),
                        rs.getString("reportDate"),
                        rs.getString("assignedTo") != null ? rs.getString("assignedTo") : "Not Assigned",
                        rs.getString("status")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting maintenance by severity: " + e.getMessage(), e);
        }

        return issues.isEmpty() ? null : issues.toArray(new String[0][]);
    }

    public boolean assignMaintenance(int issueID, String assignedTo) {
        String sql = "UPDATE tbl_maintenance SET assignedTo = ?, status = 'IN_PROGRESS' WHERE issueID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, assignedTo);
            pstmt.setInt(2, issueID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error assigning maintenance: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean completeMaintenance(int issueID, String resolution) {
        String sql = "UPDATE tbl_maintenance SET status = 'COMPLETED', resolution = ?, completionDate = ? WHERE issueID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, resolution);
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.setInt(3, issueID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error completing maintenance: " + e.getMessage(), e);
            return false;
        }
    }

    // ============ FACILITY METHODS ============

    public boolean addFacility(String facilityName, String facilityType, String location, String operatingHours) {
        String sql = "INSERT INTO tbl_facilities (facilityName, facilityType, location, operatingHours, status) VALUES (?, ?, ?, ?, 'OPERATIONAL')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, facilityName);
            pstmt.setString(2, facilityType);
            pstmt.setString(3, location);
            pstmt.setString(4, operatingHours);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error adding facility: " + e.getMessage(), e);
            return false;
        }
    }

    public String[][] getAllFacilities() {
        String sql = "SELECT * FROM tbl_facilities ORDER BY facilityName";

        List<String[]> facilities = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                facilities.add(new String[]{
                    String.valueOf(rs.getInt("facilityID")),
                    rs.getString("facilityName"),
                    rs.getString("facilityType"),
                    rs.getString("location"),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all facilities: " + e.getMessage(), e);
        }

        return facilities.isEmpty() ? null : facilities.toArray(new String[0][]);
    }

    public boolean updateFacilityStatus(int facilityID, String status) {
        String sql = "UPDATE tbl_facilities SET status = ? WHERE facilityID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, facilityID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating facility status: " + e.getMessage(), e);
            return false;
        }
    }

    // ============ PREDICTIVE MAINTENANCE METHODS ============

    public boolean logFacilityUsage(int facilityID) {
        String sql = "UPDATE tbl_facilities SET totalCycles = COALESCE(totalCycles, 0) + 1 WHERE facilityID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error logging facility usage: " + e.getMessage(), e);
            return false;
        }
    }

    public int getTotalCycles(int facilityID) {
        String sql = "SELECT totalCycles FROM tbl_facilities WHERE facilityID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("totalCycles");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total cycles: " + e.getMessage(), e);
        }
        return 0;
    }

    public int getCycleThreshold(int facilityID) {
        String sql = "SELECT cycleThreshold FROM tbl_facilities WHERE facilityID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, facilityID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("cycleThreshold");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting cycle threshold: " + e.getMessage(), e);
        }
        return 1000;
    }

    public boolean schedulePredictiveMaintenance(int facilityID) {
        String sql = "UPDATE tbl_facilities SET nextScheduledMaintenance = ?, status = 'SCHEDULED_MAINTENANCE' WHERE facilityID = ?";
        String nextDate = LocalDate.now().plusDays(7).toString();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nextDate);
            pstmt.setInt(2, facilityID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error scheduling maintenance: " + e.getMessage(), e);
            return false;
        }
    }

    public String[][] getFacilitiesNeedingMaintenance() {
        String sql = "SELECT facilityID, facilityName, totalCycles, cycleThreshold, nextScheduledMaintenance FROM tbl_facilities WHERE totalCycles >= cycleThreshold OR (nextScheduledMaintenance <= ? AND nextScheduledMaintenance IS NOT NULL)";

        List<String[]> facilities = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDate.now().toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    facilities.add(new String[]{
                        String.valueOf(rs.getInt("facilityID")),
                        rs.getString("facilityName"),
                        String.valueOf(rs.getInt("totalCycles")),
                        String.valueOf(rs.getInt("cycleThreshold")),
                        rs.getString("nextScheduledMaintenance")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting facilities needing maintenance: " + e.getMessage(), e);
        }

        return facilities.isEmpty() ? null : facilities.toArray(new String[0][]);
    }

    // ============ CUSTOMER ROOM BOOKING METHODS ============

    public boolean cancelRoomBooking(int customerID, int roomNumber, String checkInDate) {
        // Update room status back to AVAILABLE
        String updateRoomSql = "UPDATE tbl_rooms SET status = 'AVAILABLE', currentGuestID = NULL, checkInDate = NULL, checkOutDate = NULL, guestCount = 0 WHERE roomNumber = ? AND currentGuestID = ?";
        // Update booking status to CANCELLED
        String updateBookingSql = "UPDATE tbl_roomBookings SET bookingStatus = 'CANCELLED' WHERE customerID = ? AND roomNumber = ? AND checkInDate = ? AND bookingStatus = 'CONFIRMED'";

        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement roomStmt = conn.prepareStatement(updateRoomSql);
                 PreparedStatement bookingStmt = conn.prepareStatement(updateBookingSql)) {

                roomStmt.setInt(1, roomNumber);
                roomStmt.setInt(2, customerID);
                roomStmt.executeUpdate();

                bookingStmt.setInt(1, customerID);
                bookingStmt.setInt(2, roomNumber);
                bookingStmt.setString(3, checkInDate);
                int updated = bookingStmt.executeUpdate();

                if (updated > 0) {
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error cancelling room booking: " + e.getMessage(), e);
            return false;
        }
    }

    public String[][] getCustomerBookings(int customerID) {
        String sql = "SELECT b.*, r.roomType FROM tbl_roomBookings b JOIN tbl_rooms r ON b.roomNumber = r.roomNumber WHERE b.customerID = ? ORDER BY b.checkInDate";

        List<String[]> bookings = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bookings.add(new String[]{
                        String.valueOf(rs.getInt("bookingID")),
                        String.valueOf(rs.getInt("roomNumber")),
                        rs.getString("roomType"),
                        rs.getString("checkInDate"),
                        rs.getString("checkOutDate"),
                        rs.getString("bookingStatus")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting customer bookings: " + e.getMessage(), e);
        }

        return bookings.isEmpty() ? null : bookings.toArray(new String[0][]);
    }

    // ============ SMART ROOM ASSIGNMENT METHODS ============

    public int[] getGuestRoomHistory(int customerID) {
        String sql = "SELECT DISTINCT roomNumber FROM tbl_roomBookings WHERE customerID = ? ORDER BY checkInDate DESC LIMIT 5";

        List<Integer> rooms = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    rooms.add(rs.getInt("roomNumber"));
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting guest room history: " + e.getMessage(), e);
        }

        return rooms.stream().mapToInt(Integer::intValue).toArray();
    }

    public int getTotalRoomBookings() {
        String sql = "SELECT COUNT(*) FROM tbl_roomBookings";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting total room bookings: " + e.getMessage(), e);
        }
        return 0;
    }

    public String[][] getTopRoomTypes() {
        String sql = "SELECT r.roomType, COUNT(*) as count FROM tbl_roomBookings b JOIN tbl_rooms r ON b.roomNumber = r.roomNumber GROUP BY r.roomType ORDER BY count DESC";

        List<String[]> types = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                types.add(new String[]{
                    rs.getString("roomType"),
                    String.valueOf(rs.getInt("count"))
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting top room types: " + e.getMessage(), e);
        }

        return types.isEmpty() ? null : types.toArray(new String[0][]);
    }

    public int getCurrentGuestRoom(int customerID) {
        String sql = "SELECT roomNumber FROM tbl_rooms WHERE currentGuestID = ? AND status = 'OCCUPIED'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("roomNumber");
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting current guest room: " + e.getMessage(), e);
        }
        return -1;
    }

    // ============ ROOM CONTROL SYSTEM (IoT) METHODS ============

    public boolean updateRoomTemperature(int roomNumber, double temperature) {
        // Using INSERT with ON CONFLICT (UPSERT pattern for SQLite)
        String sql = "INSERT INTO tbl_roomStatus (roomNumber, temperature) VALUES (?, ?) ON CONFLICT(roomNumber) DO UPDATE SET temperature = excluded.temperature";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setDouble(2, Math.max(16, Math.min(30, temperature))); // Clamp between 16-30
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room temperature: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateRoomLights(int roomNumber, boolean lightsOn) {
        String sql = "INSERT INTO tbl_roomStatus (roomNumber, lightsOn) VALUES (?, ?) ON CONFLICT(roomNumber) DO UPDATE SET lightsOn = excluded.lightsOn";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setBoolean(2, lightsOn);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room lights: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean updateRoomDND(int roomNumber, boolean dndStatus) {
        String sql = "INSERT INTO tbl_roomStatus (roomNumber, dndStatus) VALUES (?, ?) ON CONFLICT(roomNumber) DO UPDATE SET dndStatus = excluded.dndStatus";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setBoolean(2, dndStatus);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error updating room DND: " + e.getMessage(), e);
            return false;
        }
    }

    public RoomStatus getRoomStatus(int roomNumber) {
        String sql = "SELECT * FROM tbl_roomStatus WHERE roomNumber = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new RoomStatus(
                            rs.getDouble("temperature"),
                            rs.getBoolean("lightsOn"),
                            rs.getBoolean("dndStatus"),
                            rs.getString("lastGuestRequest"),
                            rs.getString("requestTime")
                    );
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting room status: " + e.getMessage(), e);
        }
        return new RoomStatus(22.0, true, false, null, null);
    }

    public String[][] getAllRoomStatuses() {
        String sql = "SELECT rs.*, r.status as roomStatus FROM tbl_roomStatus rs JOIN tbl_rooms r ON rs.roomNumber = r.roomNumber ORDER BY rs.roomNumber";

        List<String[]> statuses = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                statuses.add(new String[]{
                    String.valueOf(rs.getInt("roomNumber")),
                    String.valueOf(rs.getDouble("temperature")),
                    String.valueOf(rs.getBoolean("lightsOn")),
                    String.valueOf(rs.getBoolean("dndStatus")),
                    rs.getString("lastGuestRequest"),
                    rs.getString("roomStatus")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting all room statuses: " + e.getMessage(), e);
        }

        return statuses.isEmpty() ? null : statuses.toArray(new String[0][]);
    }

    public boolean createGuestHousekeepingRequest(int roomNumber, int customerID, String when, String notes) {
        String sql = "INSERT INTO tbl_roomStatus (roomNumber, lastGuestRequest, requestTime) VALUES (?, ?, ?) ON CONFLICT(roomNumber) DO UPDATE SET lastGuestRequest = excluded.lastGuestRequest, requestTime = excluded.requestTime";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setString(2, "Housekeeping: " + when + " - " + notes);
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.executeUpdate();

            // Also create a housekeeping task
            assignCleaningTask(roomNumber, "Guest Request", "MEDIUM");
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating housekeeping request: " + e.getMessage(), e);
            return false;
        }
    }

    public boolean createTowelRequest(int roomNumber, int customerID, int towelCount) {
        String sql = "INSERT INTO tbl_roomStatus (roomNumber, lastGuestRequest, requestTime) VALUES (?, ?, ?) ON CONFLICT(roomNumber) DO UPDATE SET lastGuestRequest = excluded.lastGuestRequest, requestTime = excluded.requestTime";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setString(2, "Towels: " + towelCount + " requested");
            pstmt.setString(3, LocalDateTime.now().toString());
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error creating towel request: " + e.getMessage(), e);
            return false;
        }
    }

    // ============ LOST & FOUND METHODS ============

    public int saveFoundItem(int roomNumber, String dateFound, String category, String description, String foundBy, String storageLocation) {
        String sql = "INSERT INTO tbl_lostFound (roomNumber, dateFound, itemCategory, itemDescription, foundBy, storageLocation, status) VALUES (?, ?, ?, ?, ?, ?, 'FOUND')";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, roomNumber);
            pstmt.setString(2, dateFound);
            pstmt.setString(3, category);
            pstmt.setString(4, description);
            pstmt.setString(5, foundBy);
            pstmt.setString(6, storageLocation);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error saving found item: " + e.getMessage(), e);
        }
        return -1;
    }

    public String[] getLostItemDetails(int itemID) {
        String sql = "SELECT * FROM tbl_lostFound WHERE itemID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemID);
            try (ResultSet rs = pstmt.executeQuery()) {
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
            LOGGER.log(Level.SEVERE, "Error getting lost item details: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean claimItem(int itemID, int customerID, String claimantName) {
        String sql = "UPDATE tbl_lostFound SET status = 'CLAIMED', claimedBy = ?, claimDate = ?, claimantName = ? WHERE itemID = ? AND status = 'FOUND'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, customerID);
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.setString(3, claimantName);
            pstmt.setInt(4, itemID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error claiming item: " + e.getMessage(), e);
            return false;
        }
    }

    public String[][] getUnclaimedItems() {
        String sql = "SELECT * FROM tbl_lostFound WHERE status = 'FOUND' ORDER BY dateFound";

        List<String[]> items = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(new String[]{
                    String.valueOf(rs.getInt("itemID")),
                    String.valueOf(rs.getInt("roomNumber")),
                    rs.getString("itemCategory"),
                    rs.getString("itemDescription"),
                    rs.getString("dateFound"),
                    rs.getString("status"),
                    rs.getString("storageLocation")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting unclaimed items: " + e.getMessage(), e);
        }

        return items.isEmpty() ? null : items.toArray(new String[0][]);
    }

    public String[][] getClaimedItems() {
        String sql = "SELECT itemID, itemCategory, itemDescription, claimedBy, claimDate, claimantName FROM tbl_lostFound WHERE status = 'CLAIMED' ORDER BY claimDate DESC";

        List<String[]> items = new ArrayList<>();

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(new String[]{
                    String.valueOf(rs.getInt("itemID")),
                    rs.getString("itemCategory"),
                    rs.getString("itemDescription"),
                    String.valueOf(rs.getInt("claimedBy")),
                    rs.getString("claimDate")
                });
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting claimed items: " + e.getMessage(), e);
        }

        return items.isEmpty() ? null : items.toArray(new String[0][]);
    }

    public String[][] getItemsForDisposal() {
        String sql = "SELECT * FROM tbl_lostFound WHERE status = 'FOUND' AND dateFound <= ?";

        List<String[]> items = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, LocalDate.now().minusDays(30).toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    items.add(new String[]{
                        String.valueOf(rs.getInt("itemID")),
                        rs.getString("itemCategory"),
                        rs.getString("itemDescription"),
                        rs.getString("dateFound")
                    });
                }
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error getting items for disposal: " + e.getMessage(), e);
        }

        return items.isEmpty() ? null : items.toArray(new String[0][]);
    }

    public boolean markItemDisposed(int itemID, String reason) {
        String sql = "UPDATE tbl_lostFound SET status = 'DISPOSED', disposalReason = ?, disposalDate = ? WHERE itemID = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, reason);
            pstmt.setString(2, LocalDate.now().toString());
            pstmt.setInt(3, itemID);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error marking item disposed: " + e.getMessage(), e);
            return false;
        }
    }

    public String[][] searchLostItemsByRoom(int roomNumber) {
        String sql = "SELECT * FROM tbl_lostFound WHERE roomNumber = ? AND status = 'FOUND'";
        return executeLostFoundSearch(sql, roomNumber);
    }

    public String[][] searchLostItemsByDate(String fromDate, String toDate) {
        String sql = "SELECT * FROM tbl_lostFound WHERE dateFound BETWEEN ? AND ? AND status = 'FOUND'";

        List<String[]> items = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, fromDate);
            pstmt.setString(2, toDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractLostFoundResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching lost items by date: " + e.getMessage(), e);
        }
        return null;
    }

    public String[][] searchLostItemsByCategory(String category) {
        String sql = "SELECT * FROM tbl_lostFound WHERE itemCategory = ? AND status = 'FOUND'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractLostFoundResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching lost items by category: " + e.getMessage(), e);
        }
        return null;
    }

    public String[][] searchLostItemsByKeyword(String keyword) {
        String sql = "SELECT * FROM tbl_lostFound WHERE itemDescription LIKE ? AND status = 'FOUND'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractLostFoundResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching lost items by keyword: " + e.getMessage(), e);
        }
        return null;
    }

    public String[][] searchLostItemsByRooms(int[] roomNumbers) {
        if (roomNumbers == null || roomNumbers.length == 0) return null;

        StringBuilder placeholders = new StringBuilder();
        for (int i = 0; i < roomNumbers.length; i++) {
            if (i > 0) placeholders.append(",");
            placeholders.append("?");
        }

        String sql = "SELECT * FROM tbl_lostFound WHERE roomNumber IN (" + placeholders + ") AND status = 'FOUND'";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < roomNumbers.length; i++) {
                pstmt.setInt(i + 1, roomNumbers[i]);
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractLostFoundResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error searching lost items by rooms: " + e.getMessage(), e);
        }
        return null;
    }

    private String[][] executeLostFoundSearch(String sql, int param) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, param);
            try (ResultSet rs = pstmt.executeQuery()) {
                return extractLostFoundResults(rs);
            }
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error executing lost found search: " + e.getMessage(), e);
        }
        return null;
    }

    private String[][] extractLostFoundResults(ResultSet rs) throws SQLException {
        List<String[]> items = new ArrayList<>();

        while (rs.next()) {
            items.add(new String[]{
                String.valueOf(rs.getInt("itemID")),
                String.valueOf(rs.getInt("roomNumber")),
                rs.getString("itemCategory"),
                rs.getString("itemDescription"),
                rs.getString("dateFound"),
                rs.getString("status"),
                rs.getString("storageLocation")
            });
        }

        return items.isEmpty() ? null : items.toArray(new String[0][]);
    }

    // ============ DATA CLASSES ============

    /**
     * RoomStatus - Plain data class for room IoT status
     * Independent of any UI/controller class - pure data transfer object
     */
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

    // ============ BUILDER CLASS ============

    public static class RepositoryBuilder {
        private String path;

        public RepositoryBuilder setDatabasePath() {
            // Use relative path for cross-platform compatibility
            String userHome = System.getProperty("user.home");
            this.path = "jdbc:sqlite:" + userHome + "/theme_park_resort.db";
            return this;
        }

        public Repository build() {
            if (path == null) throw new IllegalStateException("Database path not set!");
            return new Repository(path);
        }
    }
}
