import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PatientManager {
    private Connection conn;

    public PatientManager(Connection conn) {
        this.conn = conn;
    }
    public boolean patientExists(String patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients WHERE patient_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        }
    }
    public Patient getPatient(String patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Patient(
                        rs.getString("patient_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("dob"),
                        rs.getString("gender"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("password")
                );
            }
        }
        return null; // Patient not found
    }

    public void registerPatient(Patient patient) throws SQLException {
        String sql = "INSERT INTO patients (patient_id, first_name, last_name, dob, gender, contact_number, email, address, password) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patient.getId());
            pstmt.setString(2, patient.getFirstName());
            pstmt.setString(3, patient.getLastName());
            pstmt.setString(4, patient.getDob());
            pstmt.setString(5, patient.getGender());
            pstmt.setString(6, patient.getContactNumber());
            pstmt.setString(7, patient.getEmail());
            pstmt.setString(8, patient.getAddress());
            pstmt.setString(9, patient.getPassword());
            pstmt.executeUpdate();
        }
    }
}