import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class AppointmentManagerTest {
    private static Connection conn;
    private static AppointmentManager appointmentManager;
    private static final String TEST_DOCTOR_ID = "DOC_001";
    private static final String TEST_PATIENT_ID = "PAT_004";
    private static final String TEST_SLOT_DATE = "2023-12-15 10:00:00";
    private static final String TEST_SLOT_DATE2 = "2023-12-15 11:00:00";

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

    @BeforeAll
    static void setUp() throws SQLException {
        appointmentManager = new AppointmentManager(conn);
        clearTestData();
        // Setup test data
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("INSERT INTO doctors (DoctorID, first_name) VALUES ('" + TEST_DOCTOR_ID + "', 'Test Doctor')");
            stmt.execute("INSERT INTO patients (patient_id, first_name, last_name, dob, gender, contact_number, email, address, password) " +
                    "VALUES ('" + TEST_PATIENT_ID + "', 'Martin', 'Magued', '2004-09-04', 'Male', '01282000932', 'martin@gmail.com', 'El rehab', '1234')");        }
    }

    @AfterAll
    static void tearDown() throws SQLException {
        clearTestData();
    }

    private static void clearTestData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM appointments WHERE patient_id = '" + TEST_PATIENT_ID + "'");
            stmt.execute("DELETE FROM available_slots WHERE DoctorID = '" + TEST_DOCTOR_ID + "'");
            stmt.execute("DELETE FROM doctors WHERE DoctorID = '" + TEST_DOCTOR_ID + "'");
            stmt.execute("DELETE FROM patients WHERE patient_id = '" + TEST_PATIENT_ID + "'");
        }
    }

    // ===== POSITIVE TESTS =====

    @Test
    @DisplayName("1. Add available slot → slot exists in database")
    void addAvailableSlot_ShouldAddSlot() throws SQLException {
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM available_slots WHERE DoctorID = ? AND slot_date = ?")) {
            pstmt.setString(1, TEST_DOCTOR_ID);
            pstmt.setString(2, TEST_SLOT_DATE);
            assertTrue(pstmt.executeQuery().next(), "Slot should exist in database");
        }
    }

    @Test
    @DisplayName("2. Get available slots for doctor → returns correct slots")
    void getAvailableSlots_ShouldReturnDoctorSlots() throws SQLException {
        // Add test slots
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE2);

        List<AvailableSlot> slots = appointmentManager.getAvailableSlots(TEST_DOCTOR_ID);
        assertEquals(2, slots.size(), "Should return 2 slots for the doctor");
        assertEquals(TEST_SLOT_DATE, slots.get(0).getSlotDate());
        assertEquals(TEST_SLOT_DATE2, slots.get(1).getSlotDate());
    }

    @Test
    @DisplayName("3. Get all available slots → returns all slots")
    void getAllAvailableSlots_ShouldReturnAllSlots() throws SQLException {
        // Add test slots
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);
        AppointmentManager.addAvailableSlot("DOC_002", TEST_SLOT_DATE2);

        List<AvailableSlot> slots = appointmentManager.getAllAvailableSlots();
        assertTrue(slots.size() >= 2, "Should return all available slots");
        assertTrue(slots.stream().anyMatch(s -> s.getDoctorId().equals(TEST_DOCTOR_ID)));
        assertTrue(slots.stream().anyMatch(s -> s.getDoctorId().equals("DOC_002")));
    }

    @Test
    @DisplayName("4. Book appointment → creates appointment and removes slot")
    void bookAppointment_ShouldCreateAppointment() throws SQLException {
        // Add test slot
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);
        int slotId = getLastInsertedSlotId();

        // Book appointment
        AppointmentManager.bookAppointment(TEST_PATIENT_ID, slotId);

        // Verify appointment exists
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM appointments WHERE patient_id = ? AND DoctorID = ?")) {
            pstmt.setString(1, TEST_PATIENT_ID);
            pstmt.setString(2, TEST_DOCTOR_ID);
            assertTrue(pstmt.executeQuery().next(), "Appointment should exist");
        }

        // Verify slot removed
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM available_slots WHERE slot_id = ?")) {
            pstmt.setInt(1, slotId);
            assertFalse(pstmt.executeQuery().next(), "Slot should be removed");
        }
    }

    @Test
    @DisplayName("5. Get all appointments → returns all booked appointments")
    void getAllAppointments_ShouldReturnAllAppointments() throws SQLException {
        // Setup
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);
        int slotId = getLastInsertedSlotId();
        AppointmentManager.bookAppointment(TEST_PATIENT_ID, slotId);

        // Test
        List<Appointment> appointments = appointmentManager.getAllAppointments();

        // Debug
        System.out.println("Retrieved " + appointments.size() + " appointments:");
        for (Appointment a : appointments) {
            System.out.printf("ID: %d, Patient: %s, Doctor: %s, Status: %s%n",
                    a.getAppointmentId(),
                    a.getPatientId(),
                    a.getDoctorId(),
                    a.getStatus());
        }

        // Verification
        assertFalse(appointments.isEmpty(), "No appointments returned");
        assertTrue(appointments.stream()
                        .anyMatch(a -> TEST_PATIENT_ID.equals(a.getPatientId()) &&
                                TEST_DOCTOR_ID.equals(a.getDoctorId())),
                "Test appointment not found");
    }

    @Test
    @DisplayName("6. Cancel appointment → updates status to Canceled")
    void cancelAppointment_ShouldUpdateStatus() throws SQLException {
        // Add test slot and book appointment
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);
        int slotId = getLastInsertedSlotId();
        AppointmentManager.bookAppointment(TEST_PATIENT_ID, slotId);
        int appointmentId = getLastInsertedAppointmentId();

        boolean result = appointmentManager.cancelAppointment(appointmentId);
        assertTrue(result, "Cancel should return true for successful update");

        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT status FROM appointments WHERE appointment_id = ?")) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            assertTrue(rs.next());
            assertEquals("Canceled", rs.getString("status"));
        }
    }

    @Test
    @DisplayName("7. Get slot by ID → returns correct slot")
    void getSlotById_ShouldReturnCorrectSlot() throws SQLException {
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);
        int slotId = getLastInsertedSlotId();

        AvailableSlot slot = AppointmentManager.getSlotById(slotId);
        assertEquals(TEST_DOCTOR_ID, slot.getDoctorId());
        assertEquals(TEST_SLOT_DATE, slot.getSlotDate());
    }

    // ===== NEGATIVE TESTS =====

    @Test
    @DisplayName("8. Book appointment with invalid slot ID → throws SQLException")
    void bookAppointment_InvalidSlotId_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            AppointmentManager.bookAppointment(TEST_PATIENT_ID, 9999);
        });
    }

    @Test
    @DisplayName("9. Cancel non-existent appointment → returns false")
    void cancelAppointment_NonExistentAppointment_ReturnsFalse() throws SQLException {
        boolean result = appointmentManager.cancelAppointment(9999);
        assertFalse(result, "Should return false for non-existent appointment");
    }

    @Test
    @DisplayName("10. Get slot by invalid ID → throws SQLException")
    void getSlotById_InvalidId_ThrowsException() {
        assertThrows(SQLException.class, () -> {
            AppointmentManager.getSlotById(9999);
        });
    }

    @Test
    @DisplayName("11. Get available slots for non-existent doctor → returns empty list")
    void getAvailableSlots_NonExistentDoctor_ReturnsEmptyList() throws SQLException {
        List<AvailableSlot> slots = appointmentManager.getAvailableSlots("NON_EXISTENT");
        assertTrue(slots.isEmpty(), "Should return empty list for non-existent doctor");
    }

    // ===== EDGE CASE TESTS =====

    @Test
    @DisplayName("12. Book already booked slot → throws SQLException (slot no longer available)")
    void bookAppointment_AlreadyBookedSlot_ThrowsException() throws SQLException {
        // Add test slot and book it
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);
        int slotId = getLastInsertedSlotId();
        AppointmentManager.bookAppointment(TEST_PATIENT_ID, slotId);

        // Try to book same slot again
        assertThrows(SQLException.class, () -> {
            AppointmentManager.bookAppointment("PAT_002", slotId);
        });
    }

    @Test
    @DisplayName("13. Transaction rollback on booking failure → maintains consistency")
    void bookAppointment_Failure_RollsBackTransaction() throws SQLException {
        // Add test slot
        AppointmentManager.addAvailableSlot(TEST_DOCTOR_ID, TEST_SLOT_DATE);
        int slotId = getLastInsertedSlotId();

        // Try to book with invalid patient (should fail)
        assertThrows(SQLException.class, () -> AppointmentManager.bookAppointment("INVALID_PATIENT", slotId));

        // Verify slot still exists
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM available_slots WHERE slot_id = ?")) {
            pstmt.setInt(1, slotId);
            assertTrue(pstmt.executeQuery().next(), "Slot should still exist after failed booking");
        }

        // Verify no appointment created
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM appointments WHERE DoctorID = ? AND time_slot = ?")) {
            pstmt.setString(1, TEST_DOCTOR_ID);
            pstmt.setString(2, TEST_SLOT_DATE);
            assertFalse(pstmt.executeQuery().next(), "No appointment should be created");
        }
    }

    // ===== HELPER METHODS =====

    private int getLastInsertedSlotId() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID()")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Could not get last inserted slot ID");
    }

    private int getLastInsertedAppointmentId() throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                     "SELECT appointment_id FROM appointments WHERE patient_id = '" +
                             TEST_PATIENT_ID + "' ORDER BY appointment_id DESC LIMIT 1")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Could not get last inserted appointment ID");
    }
}