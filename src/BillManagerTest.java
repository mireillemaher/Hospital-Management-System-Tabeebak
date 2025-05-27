import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BillManagerTest {
    private static Connection conn;
    private static BillManager billManager;
    private static final String TEST_PATIENT_ID = "PAT_001";
    private static final String TEST_PATIENT_NAME = "John Doe";
    private static final String TEST_DESCRIPTION = "Consultation fee";
    private static int testBillId;

    @BeforeAll
    static void setUpBeforeClass() throws SQLException {
        conn = DatabaseHelper.connect();
    }

    @AfterAll
    static void tearDownAfterClass() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @BeforeEach
    void setUp() throws SQLException {
        billManager = new BillManager(conn);
        clearTestData();
        // Setup complete test patient with all required fields
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO patients (patient_id, first_name, last_name, dob, gender, contact_number, email, password) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
            pstmt.setString(1, TEST_PATIENT_ID);
            pstmt.setString(2, "John");
            pstmt.setString(3, "Doe");
            pstmt.setDate(4, Date.valueOf(LocalDate.of(1990, 1, 1)));
            pstmt.setString(5, "Male");
            pstmt.setString(6, "123-456-7890");
            pstmt.setString(7, "john.doe@example.com");
            pstmt.setString(8, "securepassword");
            pstmt.executeUpdate();
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        clearTestData();
    }

    private static void clearTestData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM bill_items");
            stmt.execute("DELETE FROM bills");
            stmt.execute("DELETE FROM patients WHERE patient_id = '" + TEST_PATIENT_ID + "'");
        }
    }

    @Test
    @DisplayName("1. Create bill → bill exists in database")
    void createBill_ShouldCreateBill() throws SQLException {
        Billing bill = billManager.createBill(TEST_PATIENT_ID, TEST_PATIENT_NAME, TEST_DESCRIPTION);
        assertNotNull(bill, "Bill should not be null");

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM bills WHERE patient_id = ? AND patient_name = ? AND descript = ?")) {
            pstmt.setString(1, TEST_PATIENT_ID);
            pstmt.setString(2, TEST_PATIENT_NAME);
            pstmt.setString(3, TEST_DESCRIPTION);
            assertTrue(pstmt.executeQuery().next(), "Bill should exist in database");
        }
    }


    @Test
    @DisplayName("3. Update bill status → status updated in database")
    void updateBillStatus_ShouldUpdateStatus() throws SQLException {
        Billing bill = billManager.createBill(TEST_PATIENT_ID, TEST_PATIENT_NAME, TEST_DESCRIPTION);
        testBillId = getLastInsertedBillId();

        billManager.updateBillStatus(testBillId, "Paid");

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT status FROM bills WHERE bill_id = ?")) {
            pstmt.setInt(1, testBillId);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("Paid", rs.getString("status"));
        }
    }


    @Test
    @DisplayName("8. Create bill with invalid patient → throws SQLException")
    void createBill_InvalidPatient_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            billManager.createBill("INVALID_PATIENT", "Invalid", "Test");
        });
    }

    @Test
    @DisplayName("9. Add bill item to non-existent bill → throws SQLException")
    void addBillItem_NonExistentBill_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            billManager.addBillItem(9999, "Test", 10.0);
        });
    }

    @Test
    @DisplayName("10. Get non-existent bill → returns null")
    void getBill_NonExistentBill_ReturnsNull() throws SQLException {
        Billing bill = billManager.getBill(9999);
        assertNull(bill, "Should return null for non-existent bill");
    }

    @Test
    @DisplayName("11. Get bills for non-existent patient → returns empty list")
    void getBillsByPatient_NonExistentPatient_ReturnsEmptyList() throws SQLException {
        List<Billing> bills = billManager.getBillsByPatient("NON_EXISTENT");
        assertTrue(bills.isEmpty(), "Should return empty list for non-existent patient");
    }


    @Test
    @DisplayName("13. Create bill with empty descript → succeeds")
    void createBill_EmptyDescription_Succeeds() throws SQLException {
        Billing bill = billManager.createBill(TEST_PATIENT_ID, TEST_PATIENT_NAME, "");
        assertNotNull(bill, "Bill should be created with empty descript");

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM bills WHERE descript = ''")) {
            assertTrue(pstmt.executeQuery().next(), "Bill with empty descript should exist");
        }
    }


    private int getLastInsertedBillId() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Could not get last inserted bill ID");
    }
}