import org.junit.jupiter.api.*;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;


import static org.junit.jupiter.api.Assertions.*;

public class PatientManagerTest {

    private static Connection conn;
    private static PatientManager patientManager;

    @BeforeAll
    public static void setup() throws SQLException {
        conn = DatabaseHelper.connect();
        patientManager = new PatientManager(conn);
    }

    @AfterAll
    public static void teardown() throws SQLException {
        if (conn != null && !conn.isClosed()) {
            conn.close();
        }
    }

    @Test
    public void testRegisterAndCheckPatientExists() throws SQLException {
        Patient testPatient = new Patient(
                "P100", "John", "Doe", "1990-01-01", "Male",
                "1234567890", "john.doe@example.com", "123 Street", "password123"
        );

        // Clean up in case the test patient already exists
        conn.prepareStatement("DELETE FROM patients WHERE patient_id = 'P100'").executeUpdate();

        patientManager.registerPatient(testPatient);
        assertTrue(patientManager.patientExists("P100"));
    }

    @Test
    public void testGetPatient() throws SQLException {
        Patient patient = patientManager.getPatient("P100");
        assertNotNull(patient);
        assertEquals("John", patient.getFirstName());
    }
}
