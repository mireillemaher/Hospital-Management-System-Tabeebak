import javafx.beans.property.*;
import java.util.Date;

public class InventoryItem {
    private final StringProperty itemId;
    private final StringProperty name;
    private final IntegerProperty quantity;
    private final DoubleProperty unitPrice;
    private final StringProperty category;

    // Main constructor with all fields
    public InventoryItem(String itemId, String name, int quantity, double unitPrice, String category) {
        this.itemId = new SimpleStringProperty(itemId);
        this.name = new SimpleStringProperty(name);
        this.quantity = new SimpleIntegerProperty(quantity);
        this.unitPrice = new SimpleDoubleProperty(unitPrice);
        this.category = new SimpleStringProperty(category);
    }

    // Additional constructor without expiryDate
    public InventoryItem(String itemId, String name, String category,
                         int quantity, double unitPrice) {
        this(itemId, name, quantity, unitPrice, category);
    }

    public InventoryItem(String itemId, String name, int quantity, double unitPrice, String category, StringProperty itemId1, StringProperty name1, IntegerProperty quantity1, DoubleProperty unitPrice1, StringProperty category1) {
        this.itemId = itemId1;
        this.name = name1;
        this.quantity = quantity1;
        this.unitPrice = unitPrice1;
        this.category = category1;
    }

    // Property getters
    public StringProperty itemIdProperty() { return itemId; }
    public StringProperty nameProperty() { return name; }
    public IntegerProperty quantityProperty() { return quantity; }
    public DoubleProperty unitPriceProperty() { return unitPrice; }
    public StringProperty categoryProperty() { return category; }

    // Standard getters
    public String getItemId() { return itemId.get(); }
    public String getName() { return name.get(); }
    public int getQuantity() { return quantity.get(); }
    public double getUnitPrice() { return unitPrice.get(); }
    public String getCategory() { return category.get(); }

    // Setters
    public void setItemId(String itemId) { this.itemId.set(itemId); }
    public void setName(String name) { this.name.set(name); }
    public void setQuantity(int quantity) { this.quantity.set(quantity); }
    public void setUnitPrice(double unitPrice) { this.unitPrice.set(unitPrice); }
    public void setCategory(String category) { this.category.set(category); }

    // Business methods

    public void consume(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        if (quantity.get() < amount) throw new IllegalStateException("Not enough stock");
        quantity.set(quantity.get() - amount);
    }

    public void restock(int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        quantity.set(quantity.get() + amount);
    }

    @Override
    public String toString() {
        return String.format("%s (%s) - Qty: %d, Price: $%.2f, Category: %s",
                name.get(), itemId.get(), quantity.get(), unitPrice.get(), category.get());
    }
}