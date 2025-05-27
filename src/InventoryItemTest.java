import javafx.beans.property.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
public class InventoryItemTest {
    private InventoryItem item;
    @BeforeEach
    void setUp() {
        item = new InventoryItem("A1", "Test Item", 10, 5.0, "General");
    }
    @Nested
    @DisplayName("Constructor Tests")
    public class ConstructorTests {
        @Test
        @DisplayName("First Constructor Test")

        public void firstConstructorTest() {
            InventoryItem sample = new InventoryItem("A1", "Aspirin", 50, 35.0, "Pain Killer");
            assertEquals("A1", sample.getItemId());
            assertEquals("Aspirin", sample.getName());
            assertEquals(50, sample.getQuantity());
            assertEquals(35.0, sample.getUnitPrice());
            assertEquals("Pain Killer", sample.getCategory());
        }

        @Test
        @DisplayName("Second Constructor Test")
        public void secondConstructorTest()
        {
            InventoryItem sample= new InventoryItem("A1", "Aspirin", "Pain Killer", 50, 35.0);
            assertEquals("A1", sample.getItemId());
            assertEquals("Aspirin", sample.getName());
            assertEquals(50, sample.getQuantity());
            assertEquals(35.0, sample.getUnitPrice());
        }
        @Test
        @DisplayName("Third Constructor Test")
        public void thirdConstructorTest()
        {
            InventoryItem sample = new InventoryItem(
                    "A1", "Aspirin", 50, 35.0, "Pain Killer",
                    new SimpleStringProperty("A35"),
                    new SimpleStringProperty("Panadol"),
                    new SimpleIntegerProperty(30),
                    new SimpleDoubleProperty(20.0),
                    new SimpleStringProperty("Pain Killer")
            );
            assertEquals("A35", sample.getItemId());
            assertEquals("Panadol", sample.getName());
            assertEquals(30, sample.getQuantity());
            assertEquals(20.0, sample.getUnitPrice());
            assertEquals("Pain Killer", sample.getCategory());

        }

    }
    @Nested
    public class businessMethodsTests{
        @Test
        @DisplayName("Consume reduces quantity correctly")
        public void consumeValidAmount() {
            item.consume(3);
            assertEquals(7, item.getQuantity());
        }

        @Test
        @DisplayName("Consume throws exception for 0 amount")
        public void consumeZeroThrows() {
            assertThrows(IllegalArgumentException.class, () -> item.consume(0));
        }

        @Test
        @DisplayName("Consume throws exception for too large amount")
        public void consumeTooMuchThrows() {
            assertThrows(IllegalStateException.class, () -> item.consume(20));
        }
        @Test
        @DisplayName("Restock increases quantity")
        public void restockValidAmount() {
            item.restock(5);
            assertEquals(15, item.getQuantity());
        }

        @Test
        @DisplayName("Restock throws exception for negative amount")
        public void restockNegativeThrows() {
            assertThrows(IllegalArgumentException.class, () -> item.restock(-10));
        }



    }
    }
