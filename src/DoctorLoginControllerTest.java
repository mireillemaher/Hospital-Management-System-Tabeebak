import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.*;

class DoctorLoginControllerTest {
    private DoctorLoginController controller;
    private final String TEST_DOCTOR_ID = "TEST_1122"; // New ID for testing only
    private final String TEST_DOC_PASSWORD = "validpass";
    private final String INVALID_DOC_PASSWORD = "invalidpass";

    @BeforeEach
    void insertT() throws SQLException {
        controller = new DoctorLoginController();
        insertTestDoctor(TEST_DOCTOR_ID, TEST_DOC_PASSWORD); // Only inserts test data
    }

    @AfterEach
    void cleanUp() throws SQLException {
        deleteTestDoctor(TEST_DOCTOR_ID); // Only cleans up test data
    }

    @Test
    @DisplayName("Successful authentication with valid credentials")
    void authenticateDoctor_ValidCredentials_ReturnsTrue() {
        boolean result = controller.authenticateDoctor(TEST_DOCTOR_ID, TEST_DOC_PASSWORD);
        assertTrue(result);
    }

    @Test
    @DisplayName("Failed authentication with invalid password")
    void authenticateDoctor_InvalidPassword_ReturnsFalse() {
        boolean result = controller.authenticateDoctor(TEST_DOCTOR_ID, INVALID_DOC_PASSWORD);
        assertFalse(result);
    }

    @Test
    @DisplayName("Failed authentication with NULL password")
    void authenticateDoctor_NullPassword_ReturnsFalse() {
        boolean result = controller.authenticateDoctor(TEST_DOCTOR_ID, null);
        assertFalse(result);

    }

    @Test
    @DisplayName("Failed authentication with NULL ID")
    void authenticateDoctor_NullId_ReturnsFalse() {
        boolean result = controller.authenticateDoctor(null, TEST_DOC_PASSWORD);
        assertFalse(result);
    }

    @Test
    @DisplayName("Failed authentication with empty password")
    void authenticateDoctor_EmptyPassword_ReturnsFalse() {
        boolean result = controller.authenticateDoctor(TEST_DOCTOR_ID, "");
        assertFalse(result);
    }


    @Test
    @DisplayName("Failed authentication with non-existent doctor ID")
    void authenticateDoctor_NonExistentId_ReturnsFalse() {
        boolean result = controller.authenticateDoctor("NON_EXISTENT_ID", TEST_DOC_PASSWORD);
        assertFalse(result);
    }

    private void insertTestDoctor(String id, String password) throws SQLException {
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO doctors (DoctorID, first_name, last_name, Password, specialization, PhoneNumber) " +
                             "VALUES (?, ?, ?, ?, ?, ?)"
             )) {
            stmt.setString(1, id);
            stmt.setString(2, "Test");
            stmt.setString(3, "Doctor");
            stmt.setString(4, password);
            stmt.setString(5, "TestSpecialty");
            stmt.setString(6, "1234567890");
            stmt.executeUpdate();
        }
    }


    private void deleteTestDoctor(String id) throws SQLException {
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM doctors WHERE DoctorID = ?"
             )) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        }
    }
}

