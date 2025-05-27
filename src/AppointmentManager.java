import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentManager {
    private static Connection conn;

    public AppointmentManager(Connection conn) {
        this.conn = conn;
    }

    // Add available slot
    public static void addAvailableSlot(String doctorId, String slotDate) throws SQLException {
        String sql = "INSERT INTO available_slots (DoctorID, slot_date) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            pstmt.setString(2, slotDate);
            pstmt.executeUpdate();
        }
    }

    // Get available slots for a specific doctor
    public List<AvailableSlot> getAvailableSlots(String doctorId) throws SQLException {
        List<AvailableSlot> slots = new ArrayList<>();
        String sql = "SELECT * FROM available_slots WHERE DoctorID = ? ORDER BY slot_date";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                slots.add(new AvailableSlot(
                        rs.getInt("slot_id"),
                        rs.getString("DoctorID"),
                        rs.getString("slot_date")
                ));
            }
        }
        return slots;
    }

    // Get all available slots (for all doctors)
    public List<AvailableSlot> getAllAvailableSlots() throws SQLException {
        List<AvailableSlot> slots = new ArrayList<>();
        String sql = "SELECT * FROM available_slots ORDER BY DoctorID, slot_date";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                slots.add(new AvailableSlot(
                        rs.getInt("slot_id"),
                        rs.getString("DoctorID"),
                        rs.getString("slot_date")
                ));
            }
        }
        return slots;
    }

    // Book an appointment
    public static void bookAppointment(String patientId, int slotId) throws SQLException {
        conn.setAutoCommit(false);

        try {
            // 1. Get slot details
            AvailableSlot slot = getSlotById(slotId);

            // 2. Insert into appointments
            String insertSql = "INSERT INTO appointments " +
                    "(patient_id, DoctorID, appointment_date, status) " +
                    "VALUES (?, ?, ?, 'Scheduled')";

            try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, patientId);
                pstmt.setString(2, slot.getDoctorId());
                pstmt.setString(3, slot.getSlotDate());
                pstmt.executeUpdate();

                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        System.out.println("Booked appointment ID: " + rs.getInt(1));
                    }
                }
            }

            // 3. Remove from available slots
            String deleteSql = "DELETE FROM available_slots WHERE slot_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {
                pstmt.setInt(1, slotId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Get all booked appointments
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY appointment_date";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("appointment_id");
                appointments.add(new Appointment(
                        id,
                        rs.getString("patient_id"),
                        rs.getString("DoctorID"),
                        rs.getString("appointment_date"),
                        rs.getString("status")
                ));
            }
        }
        return appointments;
    }

    // Cancel an appointment
    public boolean cancelAppointment(int appointmentId) throws SQLException {
        String sql = "UPDATE appointments SET status = 'Canceled' WHERE appointment_id = ?";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
        }
    }

    // Helper method to get slot by ID
    static AvailableSlot getSlotById(int slotId) throws SQLException {
        String sql = "SELECT * FROM available_slots WHERE slot_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, slotId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new AvailableSlot(
                        rs.getInt("slot_id"),
                        rs.getString("DoctorID"),
                        rs.getString("slot_date")
                );
            }
        }
        throw new SQLException("Slot not found with ID: " + slotId);
    }
}