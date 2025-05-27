import org.junit.jupiter.api.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BillingDBHandlerTest {
    private Connection conn;
    private BillingDBHandler dbHandler;
    private static final String TEST_PATIENT_ID = "TEST_PAT_001";
    private static final String TEST_PATIENT_NAME = "Test Patient";
    private static final String TEST_DESCRIPTION = "Test Bill";
    private static final String TEST_SERVICE_NAME = "Test Service";
    private static final String TEST_ITEM_NAME = "Test Item";
    private static final double TEST_SERVICE_FEE = 100.0;
    private static final int TEST_QUANTITY = 2;
    private static final double TEST_UNIT_PRICE = 50.0;
    private static final double TEST_PAYMENT_AMOUNT = 75.0;

    @BeforeEach
    void setUp() throws SQLException {
        conn = DatabaseHelper.connect();
        dbHandler = new BillingDBHandler(conn);
        clearTestData();
        // Setup test patient
        try (PreparedStatement pstmt = conn.prepareStatement(
                "INSERT INTO patients (patient_id, first_name,last_name,dob,gender,contact_number,email,address,password) VALUES (?,?,?,?,?,?,?,?,?)")) {
            pstmt.setString(1, TEST_PATIENT_ID);
            pstmt.setString(2, TEST_PATIENT_NAME);
            pstmt.setString(3, "Magued");
            pstmt.setString(4, "2004-04-09");
            pstmt.setString(5, "Male");
            pstmt.setString(6, "01282000932");
            pstmt.setString(7, "martin@gmail.com");
            pstmt.setString(8, "El rehab");
            pstmt.setString(9, "1234");
            pstmt.executeUpdate();
        }
    }

    @AfterEach
    void tearDown() throws SQLException {
        clearTestData();
        conn.close();
    }

    private void clearTestData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM payments WHERE bill_id IN (SELECT bill_id FROM bills WHERE patient_id LIKE 'TEST_%')");
            stmt.execute("DELETE FROM bill_items WHERE bill_id IN (SELECT bill_id FROM bills WHERE patient_id LIKE 'TEST_%')");
            stmt.execute("DELETE FROM bills WHERE patient_id LIKE 'TEST_%'");
            stmt.execute("DELETE FROM patients WHERE patient_id LIKE 'TEST_%'");
        }
    }

    // ===== POSITIVE TESTS =====
    @Test
    @DisplayName("1. Create bill → bill exists in database")
    void createBill_ShouldCreateBill() throws SQLException {
        int billId = dbHandler.createBill(TEST_PATIENT_ID, TEST_PATIENT_NAME, TEST_DESCRIPTION);
        assertTrue(billExists(billId));
    }

    @Test
    @DisplayName("2. Add service to bill → service item added and total updated")
    void addServiceToBill_ShouldAddService() throws SQLException {
        int billId = createTestBill();
        dbHandler.addServiceToBill(billId, TEST_SERVICE_NAME, TEST_SERVICE_FEE);

        BillingDBHandler.Bill bill = dbHandler.getBill(billId);
        assertEquals(1, bill.getItems().size());
        assertEquals(TEST_SERVICE_FEE, bill.getTotalAmount());
        assertEquals(TEST_SERVICE_NAME, bill.getItems().get(0).getDescription());
    }

    @Test
    @DisplayName("3. Add inventory item to bill → item added and total updated")
    void addInventoryItemToBill_ShouldAddItem() throws SQLException {
        int billId = createTestBill();
        dbHandler.addInventoryItemToBill(billId, TEST_ITEM_NAME, TEST_QUANTITY, TEST_UNIT_PRICE);

        BillingDBHandler.Bill bill = dbHandler.getBill(billId);
        assertEquals(1, bill.getItems().size());
        assertEquals(TEST_QUANTITY * TEST_UNIT_PRICE, bill.getTotalAmount());
        assertEquals(TEST_ITEM_NAME, bill.getItems().get(0).getDescription());
    }



    @Test
    @DisplayName("5. Get bill → returns correct bill with items")
    void getBill_ShouldReturnBill() throws SQLException {
        int billId = createTestBill();
        dbHandler.addServiceToBill(billId, TEST_SERVICE_NAME, TEST_SERVICE_FEE);

        BillingDBHandler.Bill bill = dbHandler.getBill(billId);
        assertEquals(billId, bill.getBillId());
        assertEquals(TEST_PATIENT_ID, bill.getPatientId());
        assertEquals(1, bill.getItems().size());
    }

    @Test
    @DisplayName("6. Get patient bills → returns all patient bills")
    void getPatientBills_ShouldReturnAllBills() throws SQLException {
        int billId1 = createTestBill();
        int billId2 = dbHandler.createBill(TEST_PATIENT_ID, TEST_PATIENT_NAME, "Test Bill 2");

        List<BillingDBHandler.Bill> bills = dbHandler.getPatientBills(TEST_PATIENT_ID);
        assertEquals(2, bills.size());
        assertTrue(bills.stream().anyMatch(b -> b.getBillId() == billId1));
        assertTrue(bills.stream().anyMatch(b -> b.getBillId() == billId2));
    }

    // ===== NEGATIVE TESTS =====
    @Test
    @DisplayName("7. Create bill with null patient ID → throws IllegalArgumentException")
    void createBill_NullPatientId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.createBill(null, TEST_PATIENT_NAME, TEST_DESCRIPTION);
        });
    }

    @Test
    @DisplayName("8. Create bill with empty patient ID → throws IllegalArgumentException")
    void createBill_EmptyPatientId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.createBill(" ", TEST_PATIENT_NAME, TEST_DESCRIPTION);
        });
    }

    @Test
    @DisplayName("9. Create bill with null patient name → throws IllegalArgumentException")
    void createBill_NullPatientName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.createBill(TEST_PATIENT_ID, null, TEST_DESCRIPTION);
        });
    }

    @Test
    @DisplayName("10. Create bill with empty description → throws IllegalArgumentException")
    void createBill_EmptyDescription_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.createBill(TEST_PATIENT_ID, TEST_PATIENT_NAME, " ");
        });
    }

    @Test
    @DisplayName("11. Create bill with non-existent patient → throws SQLException")
    void createBill_NonExistentPatient_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            dbHandler.createBill("NON_EXISTENT", TEST_PATIENT_NAME, TEST_DESCRIPTION);
        });
    }

    @Test
    @DisplayName("12. Add service with invalid bill ID → throws IllegalArgumentException")
    void addServiceToBill_InvalidBillId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.addServiceToBill(0, TEST_SERVICE_NAME, TEST_SERVICE_FEE);
        });
    }

    @Test
    @DisplayName("13. Add service with null service name → throws IllegalArgumentException")
    void addServiceToBill_NullServiceName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.addServiceToBill(1, null, TEST_SERVICE_FEE);
        });
    }

    @Test
    @DisplayName("14. Add service with negative fee → throws IllegalArgumentException")
    void addServiceToBill_NegativeFee_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.addServiceToBill(1, TEST_SERVICE_NAME, -1.0);
        });
    }

    @Test
    @DisplayName("15. Add service to non-existent bill → throws SQLException")
    void addServiceToBill_NonExistentBill_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            dbHandler.addServiceToBill(999, TEST_SERVICE_NAME, TEST_SERVICE_FEE);
        });
    }

    @Test
    @DisplayName("16. Add inventory item with invalid bill ID → throws IllegalArgumentException")
    void addInventoryItemToBill_InvalidBillId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.addInventoryItemToBill(0, TEST_ITEM_NAME, TEST_QUANTITY, TEST_UNIT_PRICE);
        });
    }

    @Test
    @DisplayName("17. Add inventory item with null item name → throws IllegalArgumentException")
    void addInventoryItemToBill_NullItemName_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.addInventoryItemToBill(1, null, TEST_QUANTITY, TEST_UNIT_PRICE);
        });
    }

    @Test
    @DisplayName("18. Add inventory item with negative quantity → throws IllegalArgumentException")
    void addInventoryItemToBill_NegativeQuantity_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.addInventoryItemToBill(1, TEST_ITEM_NAME, -1, TEST_UNIT_PRICE);
        });
    }

    @Test
    @DisplayName("19. Add inventory item with negative unit price → throws IllegalArgumentException")
    void addInventoryItemToBill_NegativeUnitPrice_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.addInventoryItemToBill(1, TEST_ITEM_NAME, TEST_QUANTITY, -1.0);
        });
    }

    @Test
    @DisplayName("20. Add inventory item to non-existent bill → throws SQLException")
    void addInventoryItemToBill_NonExistentBill_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            dbHandler.addInventoryItemToBill(999, TEST_ITEM_NAME, TEST_QUANTITY, TEST_UNIT_PRICE);
        });
    }

    @Test
    @DisplayName("21. Make payment with invalid bill ID → throws IllegalArgumentException")
    void makePayment_InvalidBillId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.makePayment(0, TEST_PAYMENT_AMOUNT, "CASH");
        });
    }

    @Test
    @DisplayName("22. Make payment with negative amount → throws IllegalArgumentException")
    void makePayment_NegativeAmount_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.makePayment(1, -1.0, "CASH");
        });
    }

    @Test
    @DisplayName("23. Make payment with null payment method → throws IllegalArgumentException")
    void makePayment_NullPaymentMethod_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.makePayment(1, TEST_PAYMENT_AMOUNT, null);
        });
    }

    @Test
    @DisplayName("24. Make payment with invalid payment method → throws IllegalArgumentException")
    void makePayment_InvalidPaymentMethod_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.makePayment(1, TEST_PAYMENT_AMOUNT, "CHECK");
        });
    }

    @Test
    @DisplayName("25. Make payment on non-existent bill → throws SQLException")
    void makePayment_NonExistentBill_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            dbHandler.makePayment(999, TEST_PAYMENT_AMOUNT, "CASH");
        });
    }

    @Test
    @DisplayName("26. Get bill with invalid bill ID → throws IllegalArgumentException")
    void getBill_InvalidBillId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.getBill(0);
        });
    }

    @Test
    @DisplayName("27. Get bill with non-existent bill ID → throws SQLException")
    void getBill_NonExistentBill_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            dbHandler.getBill(999);
        });
    }

    @Test
    @DisplayName("28. Get patient bills with null patient ID → throws IllegalArgumentException")
    void getPatientBills_NullPatientId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.getPatientBills(null);
        });
    }

    @Test
    @DisplayName("29. Get patient bills with empty patient ID → throws IllegalArgumentException")
    void getPatientBills_EmptyPatientId_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            dbHandler.getPatientBills(" ");
        });
    }

    @Test
    @DisplayName("4. Make payment → updates bill status correctly")
    void makePayment_ShouldUpdateStatus() throws SQLException {
        int billId = createTestBill();
        dbHandler.addServiceToBill(billId, TEST_SERVICE_NAME, TEST_SERVICE_FEE);

        // Initial status should be Pending
        BillingDBHandler.Bill bill = dbHandler.getBill(billId);
        assertEquals("Pending", bill.getStatus());
    }

    @Test
    @DisplayName("30. Make full payment in one transaction → status changes directly to Paid")
    void makePayment_FullPayment_StatusChangesToPaid() throws SQLException {
        int billId = createTestBill();
        dbHandler.addServiceToBill(billId, TEST_SERVICE_NAME, TEST_SERVICE_FEE);

        // Make full payment in one go
        dbHandler.makePayment(billId, TEST_SERVICE_FEE, "CASH");

        BillingDBHandler.Bill bill = dbHandler.getBill(billId);
        assertEquals(TEST_SERVICE_FEE, bill.getAmountPaid());
        assertEquals("Paid", bill.getStatus());
    }

    @Test
    @DisplayName("31. Make overpayment → status changes to Paid and records full amount")
    void makePayment_Overpayment_StatusChangesToPaid() throws SQLException {
        int billId = createTestBill();
        dbHandler.addServiceToBill(billId, TEST_SERVICE_NAME, TEST_SERVICE_FEE);

        // Pay more than the total amount
        double overpaymentAmount = TEST_SERVICE_FEE + 50.0;
        dbHandler.makePayment(billId, overpaymentAmount, "CARD");

        BillingDBHandler.Bill bill = dbHandler.getBill(billId);
        assertEquals(overpaymentAmount, bill.getAmountPaid());
        assertEquals("Paid", bill.getStatus());
    }

    @Test
    @DisplayName("33. Multiple payments accumulate correctly")
    void makePayment_MultiplePayments_AccumulateCorrectly() throws SQLException {
        int billId = createTestBill();
        dbHandler.addServiceToBill(billId, TEST_SERVICE_NAME, TEST_SERVICE_FEE);

        // Make three partial payments
        dbHandler.makePayment(billId, 40.0, "CASH");
        dbHandler.makePayment(billId, 35.0, "CARD");
        dbHandler.makePayment(billId, 25.0, "CASH");

        BillingDBHandler.Bill bill = dbHandler.getBill(billId);
        assertEquals(100.0, bill.getAmountPaid());
        assertEquals("Paid", bill.getStatus());

        // Verify all payments were recorded
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT COUNT(*) FROM payments WHERE bill_id = ?")) {
            pstmt.setInt(1, billId);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next());
            assertEquals(3, rs.getInt(1));
        }
    }

    // ===== HELPER METHODS =====
    private int createTestBill() throws SQLException {
        return dbHandler.createBill(TEST_PATIENT_ID, TEST_PATIENT_NAME, TEST_DESCRIPTION);
    }

    private boolean billExists(int billId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM bills WHERE bill_id = ?")) {
            pstmt.setInt(1, billId);
            return pstmt.executeQuery().next();
        }
    }

    public int createBillWithoutGeneratedKey() throws SQLException {
        String query = "INSERT INTO bills (patient_id, patient_name, descript) VALUES (?, ?, ?)";

        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, "FAIL_TEST");
            stmt.setString(2, "Test Fail");
            stmt.setString(3, "Should fail");

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (!rs.next()) {
                    throw new SQLException("No generated keys returned");
                }
                return rs.getInt(1);
            }
        }
    }

}