import com.sun.javafx.collections.ImmutableObservableList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.JFXPanel;
import javafx.scene.control.TableView;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

public class DoctorDashboardControllerTest {

    private DoctorDashboardController controller;
    private final String TestPatientID ="TEST17";
    private final String TestPatientFirst= "Amir";
    private final String TestPatientLast= "Karara";
    //private final String TestPatientName="Amir Karara";
    //private final double TestPatientfee = 123;
    //private final InventoryItem item = new InventoryItem("1","Item",3,350,"Medical");


    @BeforeAll
    static void initToolkit() {
        // Ensures JavaFX toolkit is initialized for tests
        new JFXPanel(); // Initializes JavaFX environment
    }

    @BeforeEach
    void setUp() throws SQLException {
        controller = new DoctorDashboardController();
        controller.availableSlotsTable = new TableView<>();
        controller.currentDoctorId = "_1";
        controller.medicalRecordsTable = null;
        controller.inventoryTable=new TableView<>();
        controller.inventoryData = FXCollections.observableArrayList();


        try {
            controller.connection = DatabaseHelper.connect(); // Use your actual DB connection method
        } catch (SQLException e) {
            fail("Database connection failed in test setup");
        }
    }

    @Test
    void loadAvailableSlots_ReturnsTrueOrFalse() {
        boolean result = controller.loadAvailableSlots();
        // Just check that the method returns a boolean value based on DB outcome
        assertTrue(result);
    }

    @Test
    void loadPatientsForDoctors_ReturnsTrueOrFalse(){
        boolean result = controller.loadPatientsForDoctor();
        assertTrue(result);
    }

    @Test
    void loadPatientAppointments_ReturnsTrueOrFalse(){
        boolean result = controller.loadPatientAppointments(TestPatientFirst, TestPatientLast);
        assertTrue(result);
    }

    @Test
    void addConsultationToBilling_ReturnsTrueOrFalse() throws SQLException {
        boolean result = controller.addConsultationToBilling("1","A B",400);
        assertTrue(result);
    }

    @Test
    void patientExists_ReturnsTrueIfPatientFound() throws SQLException {
        assertTrue(controller.patientExists("A", "B"));
    }

    /*@Test
    void loadMedicalRecords_DoesNotThrowException() {
        boolean result = controller.loadMedicalRecords();
        assertTrue(result);
    }*/

    @Test
    void loadInventory_ReturnsTrueOrFalse(){
        boolean result = controller.loadInventory();
        assertTrue(result);
    }

    @Test
    void getNextInventoryId_ReturnsNextId_WhenMaxIdExists() throws SQLException {
        insertTestInventoryItem("I1", "Mask", 10, 1.5, "PPE");

        String nextId = String.valueOf(controller.getNextInventoryId());

        // The expected next ID should be 2 if I1 is the only item in the database
        assertEquals(2, nextId);

        // Clean up after test
        deleteTestInventoryItem("I1");
    }

    @Test
    void getNextInventoryId_Returns1_WhenNoItemsExist() throws SQLException {
        deleteAllInventoryItems();

        String nextId = String.valueOf(controller.getNextInventoryId());

        // The expected next ID should be 1 if the inventory table is empty
        assertEquals(1, nextId);
    }

    private void insertTestInventoryItem(String itemId, String itemName, int quantity, double unitPrice, String category) throws SQLException {
        String sql = "INSERT INTO inventory (item_id, name, quantity, unit_price, category) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = controller.connection.prepareStatement(sql)) {
            pstmt.setString(1, itemId);
            pstmt.setString(2, itemName);
            pstmt.setInt(3, quantity);
            pstmt.setDouble(4, unitPrice);
            pstmt.setString(5, category);
            pstmt.executeUpdate();
        }
    }

    private void deleteTestInventoryItem(String itemId) throws SQLException {
        // Delete related rows in patient_items first
        String deletePatientItems = "DELETE FROM patient_items WHERE item_id = ?";
        try (PreparedStatement pstmt = controller.connection.prepareStatement(deletePatientItems)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
        }

        // Then delete from inventory
        String deleteInventory = "DELETE FROM inventory WHERE item_id = ?";
        try (PreparedStatement pstmt = controller.connection.prepareStatement(deleteInventory)) {
            pstmt.setString(1, itemId);
            pstmt.executeUpdate();
        }
    }


    private void deleteAllInventoryItems() throws SQLException {
        // First delete from patient_items to satisfy FK constraints
        String deletePatientItems = "DELETE FROM patient_items";
        try (PreparedStatement pstmt = controller.connection.prepareStatement(deletePatientItems)) {
            pstmt.executeUpdate();
        }

        // Then delete from inventory
        String deleteInventory = "DELETE FROM inventory";
        try (PreparedStatement pstmt = controller.connection.prepareStatement(deleteInventory)) {
            pstmt.executeUpdate();
        }
    }

    private void runAndWait(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            CountDownLatch latch = new CountDownLatch(1);
            Platform.runLater(() -> {
                try {
                    action.run();
                } finally {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }


    @Test
    void deleteItemCompletely_Test() throws SQLException {
        insertTestInventoryItem("I1", "Mask", 10, 1.5, "PPE");

        InventoryItem item = new InventoryItem("I1", "Mask", 10, 1.5, "PPE");

        runAndWait(() -> controller.removeItemFromTable(item));// FX Thread safe

        boolean result = controller.deleteItemCompletely(item);
        assertTrue(result);
    }

    @Test
    void ReduceQuantityByOne_Test() throws SQLException {
        insertTestInventoryItem("I2", "Glove", 5, 30, "PPE");

        InventoryItem item = new InventoryItem("I2", "Glove", 5, 30, "PPE");
        boolean result = controller.reduceQuantityByOne(item);
        assertTrue(result);
    }

    @Test
    void getOrCreateBill_DummyTest() {
        Patient dummyPatient = new Patient("TEST23", "Basem", "Samra");

        try {
            int result = controller.getOrCreateBill(controller.connection, dummyPatient);
            assertTrue(result > 0, "Bill ID should be greater than 0");
            fail("Expected SQLException due to null connection");
        } catch (SQLException e) {
            assertTrue(true); // Expected
        }
    }

    @Test
    void addItemToBill_Test() throws SQLException {
        InventoryItem item = new InventoryItem("I3", "Scrub", 5, 40, "PPE");
        boolean result = controller.addItemToBill(controller.connection, 1, item, 2,"Treatment");
        assertTrue(result);
    }

    @Test
    void updateInventory_Test() throws SQLException{
        InventoryItem item = new InventoryItem("I4", "Panadol", 5, 25, "PPE");
        boolean result = controller.updateInventory(controller.connection, item, 6);
        assertTrue(result);
    }

    @Test
    void updateBillTotal_Test() throws SQLException{
        boolean result = controller.updateBillTotal(controller.connection, 1);
        assertTrue(result);
    }

    @Test
    void askToGenerateBill_Test() {
        DoctorDashboardController controller = new DoctorDashboardController();

        // Use Platform.runLater to run the code on the JavaFX application thread
        Platform.runLater(() -> {
            try {
                controller.askToGenerateBill("TEST_PATIENT_ID");
                assertTrue(true);
            } catch (Exception e) {
                fail("Method threw an exception: " + e.getMessage());
            }
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void generateAndTestBill_Test() throws SQLException {
        // Use Platform.runLater to run the code on the FX thread
        Platform.runLater(() -> {
            boolean result = controller.generateAndSendBill("1");
            assertTrue(result);
        });

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void handleTest(){
        boolean result = controller.handleShowAppointedPatients();
        assertTrue(result);
    }

}

