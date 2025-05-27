import org.junit.jupiter.api.*;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

class PatientLoginControllerTest {
    private PatientLoginController controller;
    private final String TEST_PATIENT_ID = "TEST_12345";
    private final String TEST_PASSWORD = "1234";
    private final String INVALID_PASSWORD = "00000";

    @BeforeEach
    void setUp() throws SQLException {
        controller = new PatientLoginController();
        insertTestPatient(TEST_PATIENT_ID, TEST_PASSWORD);
    }

    @AfterEach
    void tearDown() throws SQLException {
        deleteTestPatient(TEST_PATIENT_ID);
    }


    @Test
    @DisplayName("1. Valid credentials → true")
    void authenticatePatient_ValidCredentials_ReturnsTrue() {
        boolean result = controller.authenticatePatient(TEST_PATIENT_ID, TEST_PASSWORD);
        assertTrue(result);
    }
    @Test
    @DisplayName("2. Invalid password → false")
    void authenticatePatient_InvalidPassword_ReturnsFalse() {
        boolean result = controller.authenticatePatient(TEST_PATIENT_ID, INVALID_PASSWORD);
        assertFalse(result);
    }
    @Test
    @DisplayName("3. Non-existent ID → false")
    void authenticatePatient_NonExistentId_ReturnsFalse() {
        boolean result = controller.authenticatePatient("NON_EXISTENT_ID", TEST_PASSWORD);
        assertFalse(result);
    }
    @Test
    @DisplayName("4. Existing test patient → Patient object")
    void getPatientFromDatabase_ExistingId_ReturnsPatient() {
        Patient result = controller.getPatientFromDatabase(TEST_PATIENT_ID);
        assertNotNull(result);
        assertEquals(TEST_PATIENT_ID, result.getPatientId());
    }
    @Test
    @DisplayName("5. Non-existent patient → null")
    void getPatientFromDatabase_NonExistentId_ReturnsNull() {
        Patient result = controller.getPatientFromDatabase("NON_EXISTENT_ID");
        assertNull(result);
    }

    private void insertTestPatient(String id, String password) throws SQLException {
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO patients (patient_id, first_name, last_name, password, dob, gender, contact_number, email, address) " +
                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
             )) {
            stmt.setString(1, id);
            stmt.setString(2, "Test");
            stmt.setString(3, "Patient");
            stmt.setString(4, password);
            stmt.setString(5, "2000-01-01"); // dob (required)
            stmt.setString(6, "Male");          // gender (if required)
            stmt.setString(7, "1234567890"); // contact_number (if required)
            stmt.setString(8, "test@test.com"); // email (if required)
            stmt.setString(9, "Test Address");  // address (if required)
            stmt.executeUpdate();
        }
    }
    private void deleteTestPatient(String id) throws SQLException {
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM patients WHERE patient_id = ?"
             )) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }
}