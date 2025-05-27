import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class InventoryManager {
    private final Connection connection;

    public InventoryManager() throws SQLException {
        this.connection = DatabaseHelper.connect();
    }

    public void addItem(InventoryItem item) throws SQLException {
        String sql = "INSERT INTO inventory (item_id, name, quantity,unit_price, category) "
                + "VALUES (?, ?, ?, ?,?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getItemId());
            pstmt.setString(2, item.getName());
            pstmt.setInt(3, item.getQuantity());
//            pstmt.setDate(4, item.getExpiryDate() != null ?
//                    Date.valueOf(item.getExpiryDate()) : null);
            pstmt.setDouble(5, item.getUnitPrice());
            pstmt.setString(6, item.getCategory());
            pstmt.executeUpdate();
        }
    }

    public InventoryItem getItemById(String itemId) throws SQLException {
        String sql = "SELECT * FROM inventory WHERE item_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new InventoryItem(
                        rs.getString("item_id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getString("category")
                );
            }
        }
        throw new IllegalArgumentException("Item not found");
    }

    public void consumeItem(String itemId, int quantity) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity - ? WHERE item_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, itemId);
            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new IllegalArgumentException("Item not found or insufficient quantity");
            }
        }
    }

    public void restockItem(String itemId, int quantity) throws SQLException {
        String sql = "UPDATE inventory SET quantity = quantity + ? WHERE item_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, quantity);
            pstmt.setString(2, itemId);
            pstmt.executeUpdate();
        }
    }

    public void updateItem(InventoryItem item) throws SQLException {
        String sql = "UPDATE inventory SET name = ?, quantity = ?," +
                "unit_price = ?, category = ? WHERE item_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, item.getName());
            pstmt.setInt(2, item.getQuantity());
//            pstmt.setDate(3, item.getExpiryDate() != null ?
//                    Date.valueOf(item.getExpiryDate()) : null);
            pstmt.setDouble(4, item.getUnitPrice());
            pstmt.setString(5, item.getCategory());
            pstmt.setString(6, item.getItemId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Updating item failed, no rows affected.");
            }
        }
    }

    public List<InventoryItem> getAllInventory() throws SQLException {
        List<InventoryItem> items = new ArrayList<>();
        String sql = "SELECT * FROM inventory";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(new InventoryItem(
                        rs.getString("item_id"),
                        rs.getString("name"),
                        rs.getInt("quantity"),
                        rs.getDouble("unit_price"),
                        rs.getString("category")
                ));
            }
        }
        return items;
    }

    public void close() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}