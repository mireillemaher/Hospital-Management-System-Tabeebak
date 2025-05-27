import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillManager {
    private final Connection dbConnection;

    public BillManager(Connection conn) {
        this.dbConnection = conn;
    }

    public Billing createBill(String patientId, String patientName, String descript) throws SQLException {
        String sql = "INSERT INTO bills (patient_id, patient_name, total_amount, status, created_at, descript) " +
                "VALUES (?, ?, 0, 'Pending', CURRENT_DATE, ?)";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, patientId);
            pstmt.setString(2, patientName);
            pstmt.setString(3, descript);
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int billNumber = generatedKeys.getInt(1);
                    return new Billing(patientId, patientName, descript);
                }
            }
        }
        throw new SQLException("Failed to create bill");
    }

    public void addBillItem(int billNumber, String descript, double total_amount) throws SQLException {
        String sql = "INSERT INTO bill_items (bill_id, descript, total_amount) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, billNumber);
            pstmt.setString(2, descript);
            pstmt.setDouble(3, total_amount);
            pstmt.executeUpdate();
        }

        // Update the bill total
        sql = "UPDATE bills SET total_amount = total_amount + ? WHERE bill_id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setDouble(1, total_amount);
            pstmt.setInt(2, billNumber);
            pstmt.executeUpdate();
        }
    }

    public void finalizeBill(int billNumber) throws SQLException {
        String sql = "UPDATE bills SET status = 'Finalized' WHERE bill_id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, billNumber);
            pstmt.executeUpdate();
        }
    }

    public List<Billing> getBillsByPatient(String patientId) throws SQLException {
        String sql = "SELECT * FROM bills WHERE patient_id = ?";
        List<Billing> bills = new ArrayList<>();

        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Billing bill = new Billing(
                            rs.getString("patient_id"),
                            rs.getString("patient_name"),
                            rs.getString("descript"));
                    bill.setBillNumber(rs.getInt("bill_id"));
                    bill.setAmount(rs.getDouble("total_amount"));
                    bill.setStatus(rs.getString("status"));
                    bills.add(bill);

                    // Load bill items
                    loadBillItems(bill);
                }
            }
        }
        return bills;
    }

    public List<Billing> getAllBills() throws SQLException {
        String sql = "SELECT * FROM bills";
        List<Billing> bills = new ArrayList<>();

        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Billing bill = new Billing(
                        rs.getString("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("descript"));
                bill.setBillNumber(rs.getInt("bill_id"));
                bill.setAmount(rs.getDouble("total_amount"));
                bill.setStatus(rs.getString("status"));
                bills.add(bill);

                // Load bill items
                loadBillItems(bill);
            }
        }
        return bills;
    }

    public Billing getBill(int billNumber) throws SQLException {
        String sql = "SELECT * FROM bills WHERE bill_id = ?";

        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, billNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Billing bill = new Billing(
                            rs.getString("patient_id"),
                            rs.getString("patient_name"),
                            rs.getString("descript"));
                    bill.setBillNumber(rs.getInt("bill_id"));
                    bill.setAmount(rs.getDouble("total_amount"));
                    bill.setStatus(rs.getString("status"));

                    // Load bill items
                    loadBillItems(bill);
                    return bill;
                }
            }
        }
        return null;
    }

    public void updateBillStatus(int billNumber, String status) throws SQLException {
        String sql = "UPDATE bills SET status = ? WHERE bill_id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, billNumber);
            pstmt.executeUpdate();
        }
    }

    private void loadBillItems(Billing bill) throws SQLException {
        String sql = "SELECT descript, total_amount FROM bill_items WHERE bill_id = ?";
        try (PreparedStatement pstmt = dbConnection.prepareStatement(sql)) {
            pstmt.setInt(1, bill.getBillNumber());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    bill.addItem(new BillItem(
                            rs.getString("descript"),
                            rs.getDouble("total_amount")));
                }
            }
        }
    }
}