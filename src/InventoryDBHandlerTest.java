import org.junit.jupiter.api.*;
import java.sql.*;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InventoryDBHandlerTest {
    private Connection conn;
    private InventoryDBHandler dbHandler;
    private final String TEST_ITEM_ID = "TEST_001";
    private final String TEST_ITEM_NAME = "Test Item";
    private final int TEST_QUANTITY = 10;
    private final double TEST_PRICE = 9.99;
    private final String TEST_CATEGORY = "Test";

    @BeforeEach
    void setUp() throws SQLException {
        conn = DatabaseHelper.connect();
        dbHandler = new InventoryDBHandler(conn);
        clearTestData();
    }

    @AfterEach
    void tearDown() throws SQLException {
        clearTestData();
        conn.close();
    }

    private void clearTestData() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM inventory WHERE item_id LIKE 'TEST_%'");
        }
    }

    // ===== POSITIVE TESTS =====
    @Test
    @DisplayName("1. Add item → item exists in database")
    void addItem_ShouldAddToDatabase() throws SQLException {
        InventoryItem testItem = createTestItem();
        dbHandler.addItem(testItem);
        assertItemExists(TEST_ITEM_ID);
    }
    @Test
    @DisplayName("2. Delete item → item removed from database")
    void deleteItem_ShouldRemoveFromDatabase() throws SQLException {
        addTestItem(TEST_ITEM_ID);
        dbHandler.deleteItem(TEST_ITEM_ID);
        assertItemNotExists(TEST_ITEM_ID);
    }
    @Test
    @DisplayName("3. Get all inventory → returns all items")
    void getAllInventory_ShouldReturnAllItems() throws SQLException {
        addTestItem("TEST_001");
        addTestItem("TEST_002");
        List<InventoryItem> inventory = dbHandler.getAllInventory();
        assertTrue(inventory.size() >= 2);
        assertTrue(containsItem(inventory, "TEST_001"));
        assertTrue(containsItem(inventory, "TEST_002"));
    }
    // ===== NEGATIVE TESTS =====
    @Test
    @DisplayName("4. Add duplicate item → throws SQLException")
    void addItem_DuplicateId_ThrowsException() throws SQLException {
        addTestItem(TEST_ITEM_ID);
        InventoryItem duplicateItem = createTestItem();
        assertThrows(SQLException.class, () -> {
            dbHandler.addItem(duplicateItem);
        });
    }
    @Test
    @DisplayName("5. Add item with null name → throws SQLException")
    void addItem_NullName_ThrowsException() {
        InventoryItem item = new InventoryItem(
                "TEST_NULL", null, TEST_QUANTITY, TEST_PRICE, TEST_CATEGORY);
        assertThrows(SQLException.class, () -> {
            dbHandler.addItem(item);
        });
    }
    @Test
    @DisplayName("6. Add item with negative quantity → throws SQLException")
    void addItem_NegativeQuantity_ThrowsException() {
        InventoryItem item = new InventoryItem(
                "TEST_NEG", "Negative Qty", -1, TEST_PRICE, TEST_CATEGORY);
        assertThrows(SQLException.class, () -> {
            dbHandler.addItem(item);
        });
    }
    @Test
    @DisplayName("7. Delete non-existent item → no exception")
    void deleteItem_NonExistent_NoException() {
        assertDoesNotThrow(() -> {
            dbHandler.deleteItem("NON_EXISTENT");
        });
    }

    @Test
    @DisplayName("8. Get empty inventory → returns empty list")
    void getAllInventory_Empty_ReturnsEmptyList() throws SQLException {
        List<InventoryItem> inventory = dbHandler.getAllInventory();
        assertTrue(inventory.isEmpty());
    }

    @Test
    @DisplayName("9. Add item with zero quantity → succeeds")
    void addItem_ZeroQuantity_Success() {
        InventoryItem item = new InventoryItem("TEST_ZERO", "Zero Qty", 0, 1.99, "Test");
        assertDoesNotThrow(() -> dbHandler.addItem(item));
    }

    @Test
    @DisplayName("10. Add item with special characters → succeeds")
    void addItem_SpecialChars_Success() throws SQLException {
        InventoryItem item = new InventoryItem("TEST_SPEC", "Item@123#", 1, 1.99, "Test");
        dbHandler.addItem(item);
        assertItemExists("TEST_SPEC");
    }

    // ===== HELPER METHODS =====
    private InventoryItem createTestItem() {
        return new InventoryItem(
                TEST_ITEM_ID, TEST_ITEM_NAME, TEST_QUANTITY, TEST_PRICE, TEST_CATEGORY);
    }

    private void addTestItem(String itemId) throws SQLException {
        dbHandler.addItem(new InventoryItem(
                itemId, "Test Item", 1, 1.99, "Test"));
    }

    private void assertItemExists(String itemId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM inventory WHERE item_id = ?")) {
            pstmt.setString(1, itemId);
            assertTrue(pstmt.executeQuery().next());
        }
    }

    private void assertItemNotExists(String itemId) throws SQLException {
        try (PreparedStatement pstmt = conn.prepareStatement(
                "SELECT 1 FROM inventory WHERE item_id = ?")) {
            pstmt.setString(1, itemId);
            assertFalse(pstmt.executeQuery().next());
        }
    }

    private boolean containsItem(List<InventoryItem> items, String itemId) {
        return items.stream().anyMatch(i -> i.getItemId().equals(itemId));
    }
}