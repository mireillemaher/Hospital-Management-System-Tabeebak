import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryDBHandler {
    private Connection conn;

    public InventoryDBHandler(Connection conn) {
        this.conn = conn;
    }

    public void addItem(InventoryItem item) throws SQLException {
        String sql = "INSERT INTO inventory (item_id, name, quantity, unit_price, category) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemId());
            pstmt.setString(2, item.getName());
            pstmt.setInt(3, item.getQuantity());
            pstmt.setDouble(4, item.getUnitPrice());
            pstmt.setString(5, item.getCategory());
            pstmt.executeUpdate();
        }
        if (item.getQuantity() < 0) {
            throw new SQLException("Quantity cannot be negative");
        }
    }


    public void deleteItem(String itemId) throws SQLException {
        String sql = "DELETE FROM inventory WHERE item_id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
        }
    }



    public List<InventoryItem> getAllInventory() throws SQLException {
        List<InventoryItem> inventory = new ArrayList<>();
        String sql = "SELECT * FROM inventory ORDER BY name";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                inventory.add(new InventoryItem(
                        rs.getString("item_id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getString("category")
                ));
            }
        }
        return inventory;
    }
}