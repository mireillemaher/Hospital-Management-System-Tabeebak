import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BillingDBHandler {
    private Connection conn;

    public BillingDBHandler(Connection conn) {
        this.conn = conn;
    }

    // Inner Bill class
    public class Bill {
        private int billId;
        private String patientId;
        private String patientName;
        private String description;
        private double totalAmount;
        private double amountPaid;
        private String status;
        private LocalDate createdDate;
        private List<BillItem> items;

        public Bill(int billId, String patientId, String patientName, String description,
                    double totalAmount, double amountPaid, String status, LocalDate createdDate) {
            this.billId = billId;
            this.patientId = patientId;
            this.patientName = patientName;
            this.description = description;
            this.totalAmount = totalAmount;
            this.amountPaid = amountPaid;
            this.status = status;
            this.createdDate = createdDate;
            this.items = new ArrayList<>();
        }

        // Getters
        public int getBillId() { return billId; }
        public String getPatientId() { return patientId; }
        public String getPatientName() { return patientName; }
        public String getDescription() { return description; }
        public double getTotalAmount() { return totalAmount; }
        public String getStatus() { return status; }
        public List<BillItem> getItems() { return items; }

        public void addItem(BillItem item) {
            items.add(item);
        }

        public double getAmount() {
            return amountPaid;
        }

        public double getAmountPaid() {
            return amountPaid;
        }

        public void setAmountPaid(double amountPaid) {
            this.amountPaid = amountPaid;
        }

        // Optionally, you might want this as well:
        public double getRemainingAmount() {
            return totalAmount - amountPaid;
        }
    }

    // Inner BillItem class
    public class BillItem {
        private String description;
        private double amount;
        private int quantity;
        private double unitPrice;

        public BillItem(String description, double amount, int quantity, double unitPrice) {
            this.description = description;
            this.amount = amount;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
        }

        // Getters
        public String getDescription() { return description; }
        public double getAmount() { return amount; }
        public int getQuantity() { return quantity; }
    }

    // Create a new bill with validation
    public int createBill(String patientId, String patientName, String description) throws SQLException {
        // Validate inputs
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }
        if (patientName == null || patientName.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient name cannot be null or empty");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Description cannot be null or empty");
        }

        // Verify patient exists
        if (!patientExists(patientId)) {
            throw new SQLException("Patient does not exist with ID: " + patientId);
        }

        String sql = "INSERT INTO bills (patient_id, patient_name, descript, total_amount, amount_paid, status) " +
                "VALUES (?, ?, ?, 0, 0, 'Pending')";

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, patientId);
            pstmt.setString(2, patientName);
            pstmt.setString(3, description);
            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            throw new SQLException("Failed to create bill - no ID generated");
        }
    }

    // Add a service to a bill with validation
    public void addServiceToBill(int billId, String serviceName, double serviceFee) throws SQLException {
        // Validate inputs
            if (billId <= 0) {
                throw new IllegalArgumentException("Invalid bill ID");
            }
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Service name cannot be null or empty");
        }
        if (serviceFee <= 0) {
            throw new IllegalArgumentException("Service fee must be positive");
        }

        conn.setAutoCommit(false);
        try {
            // Verify bill exists
            if (!billExists(billId)) {
                throw new SQLException("Bill does not exist with ID: " + billId);
            }

            // Add service item
            String itemSql = "INSERT INTO bill_items (bill_id, item_type, descript, quantity, unit_price, amount) " +
                    "VALUES (?, 'Service', ?, 1, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                pstmt.setInt(1, billId);
                pstmt.setString(2, serviceName);
                pstmt.setDouble(3, serviceFee);
                pstmt.setDouble(4, serviceFee);
                pstmt.executeUpdate();
            }

            // Update bill total
            String updateSql = "UPDATE bills SET total_amount = total_amount + ? WHERE bill_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, serviceFee);
                pstmt.setInt(2, billId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Add an inventory item to a bill with validation
    public void addInventoryItemToBill(int billId, String itemName, int quantity, double unitPrice) throws SQLException {
        // Validate inputs
        if (billId <= 0) {
            throw new IllegalArgumentException("Invalid bill ID");
        }
        if (itemName == null || itemName.trim().isEmpty()) {
            throw new IllegalArgumentException("Item name cannot be null or empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (unitPrice <= 0) {
            throw new IllegalArgumentException("Unit price must be positive");
        }

        conn.setAutoCommit(false);
        try {
            // Verify bill exists
            if (!billExists(billId)) {
                throw new SQLException("Bill does not exist with ID: " + billId);
            }

            double total = unitPrice * quantity;

            // Add inventory item
            String itemSql = "INSERT INTO bill_items (bill_id, item_type, descript, quantity, unit_price, amount) " +
                    "VALUES (?, 'Inventory', ?, ?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(itemSql)) {
                pstmt.setInt(1, billId);
                pstmt.setString(2, itemName);
                pstmt.setInt(3, quantity);
                pstmt.setDouble(4, unitPrice);
                pstmt.setDouble(5, total);
                pstmt.executeUpdate();
            }

            // Update bill total
            String updateSql = "UPDATE bills SET total_amount = total_amount + ? WHERE bill_id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(updateSql)) {
                pstmt.setDouble(1, total);
                pstmt.setInt(2, billId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Record a payment with validation
    public void makePayment(int billId, double amount, String paymentMethod) throws SQLException {
        // Validate inputs
        if (billId <= 0) {
            throw new IllegalArgumentException("Invalid bill ID");
        }
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (paymentMethod == null || paymentMethod.trim().isEmpty()) {
            throw new IllegalArgumentException("Payment method cannot be null or empty");
        }
        if (!paymentMethod.equals("CASH") && !paymentMethod.equals("CARD")) {
            throw new IllegalArgumentException("Invalid payment method. Must be 'CASH' or 'CARD'");
        }

        conn.setAutoCommit(false);
        try {
            // Verify bill exists
            if (!billExists(billId)) {
                throw new SQLException("Bill does not exist with ID: " + billId);
            }

            // Record payment
            String paymentSql = "INSERT INTO payments (bill_id, amount, payment_method) " +
                    "VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(paymentSql)) {
                pstmt.setInt(1, billId);
                pstmt.setDouble(2, amount);
                pstmt.setString(3, paymentMethod);
                pstmt.executeUpdate();
            }

            // Update bill status
            String billSql = "UPDATE bills SET amount_paid = amount_paid + ?, " +
                    "status = CASE WHEN amount_paid + ? >= total_amount THEN 'Paid' " +
                    "WHEN amount_paid + ? > 0 THEN 'Partial' ELSE 'Pending' END " +
                    "WHERE bill_id = ?";

            try (PreparedStatement pstmt = conn.prepareStatement(billSql)) {
                pstmt.setDouble(1, amount);
                pstmt.setDouble(2, amount);
                pstmt.setDouble(3, amount);
                pstmt.setInt(4, billId);
                pstmt.executeUpdate();
            }

            conn.commit();
        } catch (SQLException e) {
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // Get a bill by ID with validation
    public Bill getBill(int billId) throws SQLException {
        if (billId <= 0) {
            throw new IllegalArgumentException("Invalid bill ID");
        }

        String billSql = "SELECT * FROM bills WHERE bill_id = ?";
        String itemsSql = "SELECT * FROM bill_items WHERE bill_id = ?";

        Bill bill = null;

        // Get bill header
        try (PreparedStatement pstmt = conn.prepareStatement(billSql)) {
            pstmt.setInt(1, billId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                bill = new Bill(
                        rs.getInt("bill_id"),
                        rs.getString("patient_id"),
                        rs.getString("patient_name"),
                        rs.getString("descript"),
                        rs.getDouble("total_amount"),
                        rs.getDouble("amount_paid"),
                        rs.getString("status"),
                        rs.getDate("created_at").toLocalDate()
                );
            } else {
                throw new SQLException("Bill not found with ID: " + billId);
            }
        }

        // Get bill items
        try (PreparedStatement pstmt = conn.prepareStatement(itemsSql)) {
            pstmt.setInt(1, billId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                BillItem item = new BillItem(
                        rs.getString("descript"),
                        rs.getDouble("amount"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price")
                );
                bill.addItem(item);
            }
        }

        return bill;
    }

    // Get all bills for a patient with validation
    public List<Bill> getPatientBills(String patientId) throws SQLException {
        if (patientId == null || patientId.trim().isEmpty()) {
            throw new IllegalArgumentException("Patient ID cannot be null or empty");
        }

        List<Bill> bills = new ArrayList<>();
        String sql = "SELECT bill_id FROM bills WHERE patient_id = ? ORDER BY created_at DESC";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                try {
                    bills.add(getBill(rs.getInt("bill_id")));
                } catch (SQLException e) {
                    // Log error but continue with other bills
//                    System.err.println("Error loading bill " + rs.getInt("bill_id") + ": " + e.getMessage());
                }
            }
        }
        return bills;
    }

    // Helper method to check if patient exists
    private boolean patientExists(String patientId) throws SQLException {
        String sql = "SELECT 1 FROM patients WHERE patient_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Helper method to check if bill exists
    private boolean billExists(int billId) throws SQLException {
        String sql = "SELECT 1 FROM bills WHERE bill_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, billId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}