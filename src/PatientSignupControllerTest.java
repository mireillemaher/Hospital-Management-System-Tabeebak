import javafx.embed.swing.JFXPanel;
import javafx.scene.control.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class PatientSignupControllerTest {
    private PatientSignupController controller;

    @BeforeAll
    static void initJFX() {
        // Initialize JavaFX toolkit
        new JFXPanel();
    }

    @BeforeEach
    void setUp() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        Platform.runLater(() -> {
            controller = new PatientSignupController();

            // Initialize JavaFX components
            controller.firstNameField = new TextField();
            controller.lastNameField = new TextField();
            controller.patientIDField = new TextField();
            controller.dobField = new TextField();
            controller.contactNumberField = new TextField();
            controller.addressField = new TextArea();
            controller.emailField = new TextField();
            controller.passwordField = new PasswordField();
            controller.maleRadio = new RadioButton("Male");
            controller.femaleRadio = new RadioButton("Female");

            // Set up toggle group
            ToggleGroup genderGroup = new ToggleGroup();
            controller.maleRadio.setToggleGroup(genderGroup);
            controller.femaleRadio.setToggleGroup(genderGroup);
            controller.maleRadio.setSelected(true);

            latch.countDown();
        });
        latch.await(5, TimeUnit.SECONDS);
    }

    private <T> T runOnFxThread(FxTestTask<T> task) throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final T[] result = (T[]) new Object[1];
        final Exception[] exception = new Exception[1];

        Platform.runLater(() -> {
            try {
                result[0] = task.run();
            } catch (Exception e) {
                exception[0] = e;
            } finally {
                latch.countDown();
            }
        });

        latch.await(5, TimeUnit.SECONDS);
        if (exception[0] != null) {
            throw exception[0];
        }
        return result[0];
    }

    // ===== validateInputs() Tests =====

    @Test
    void validateInputs_AllFieldsFilled_ReturnsTrue() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("John", "Doe", "PAT001", "1990-01-01",
                    "1234567890", "john@example.com", "password123", "123 Main St");
            return controller.validateInputs();
        });
        assertTrue(result);
    }

    @Test
    void validateInputs_MissingFirstName_ReturnsFalse() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("", "Doe", "PAT001", "1990-01-01",
                    "1234567890", "john@example.com", "password123", "123 Main St");
            return controller.validateInputs();
        });
        assertFalse(result);
    }

    @Test
    void validateInputs_MissingLastName_ReturnsFalse() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("John", "", "PAT001", "1990-01-01",
                    "1234567890", "john@example.com", "password123", "123 Main St");
            return controller.validateInputs();
        });
        assertFalse(result);
    }

    @Test
    void validateInputs_MissingPatientID_ReturnsFalse() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("John", "Doe", "", "1990-01-01",
                    "1234567890", "john@example.com", "password123", "123 Main St");
            return controller.validateInputs();
        });
        assertFalse(result);
    }

    @Test
    void validateInputs_MissingDOB_ReturnsFalse() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("John", "Doe", "PAT001", "",
                    "1234567890", "john@example.com", "password123", "123 Main St");
            return controller.validateInputs();
        });
        assertFalse(result);
    }

    @Test
    void validateInputs_MissingContactNumber_ReturnsFalse() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("John", "Doe", "PAT001", "1990-01-01",
                    "", "john@example.com", "password123", "123 Main St");
            return controller.validateInputs();
        });
        assertFalse(result);
    }

    @Test
    void validateInputs_MissingEmail_ReturnsFalse() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("John", "Doe", "PAT001", "1990-01-01",
                    "1234567890", "", "password123", "123 Main St");
            return controller.validateInputs();
        });
        assertFalse(result);
    }

    @Test
    void validateInputs_MissingPassword_ReturnsFalse() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("John", "Doe", "PAT001", "1990-01-01",
                    "1234567890", "john@example.com", "", "123 Main St");
            return controller.validateInputs();
        });
        assertFalse(result);
    }

    @Test
    void validateInputs_AddressEmpty_ReturnsTrue() throws Exception {
        Boolean result = runOnFxThread(() -> {
            setAllFields("John", "Doe", "PAT001", "1990-01-01",
                    "1234567890", "john@example.com", "password123", "");
            return controller.validateInputs();
        });
        assertTrue(result);
    }

    // ===== clearForm() Tests =====

    @Test
    void clearForm_ClearsAllFields() throws Exception {
        runOnFxThread(() -> {
            setAllFields("John", "Doe", "PAT001", "1990-01-01",
                    "1234567890", "123 Main St", "john@example.com", "password123");
            controller.femaleRadio.setSelected(true);
            controller.clearForm();
            return null;
        });

        runOnFxThread(() -> {
            assertAllFieldsCleared();
            return null;
        });
    }

    @Test
    void clearForm_WithSomeFieldsEmpty_ClearsAllFields() throws Exception {
        runOnFxThread(() -> {
            controller.firstNameField.setText("John");
            controller.lastNameField.setText("");
            controller.patientIDField.setText("PAT001");
            controller.dobField.setText("");
            controller.contactNumberField.setText("1234567890");
            controller.addressField.setText("");
            controller.emailField.setText("john@example.com");
            controller.passwordField.setText("");
            controller.femaleRadio.setSelected(true);

            controller.clearForm();
            return null;
        });

        runOnFxThread(() -> {
            assertAllFieldsCleared();
            return null;
        });
    }

    // Helper method to set all fields
    private void setAllFields(String firstName, String lastName, String patientId,
                              String dob, String contactNumber, String email,
                              String password, String address) {
        controller.firstNameField.setText(firstName);
        controller.lastNameField.setText(lastName);
        controller.patientIDField.setText(patientId);
        controller.dobField.setText(dob);
        controller.contactNumberField.setText(contactNumber);
        controller.emailField.setText(email);
        controller.passwordField.setText(password);
        controller.addressField.setText(address);
    }

    // Helper method to assert all fields are cleared
    private void assertAllFieldsCleared() {
        assertEquals("", controller.firstNameField.getText());
        assertEquals("", controller.lastNameField.getText());
        assertEquals("", controller.patientIDField.getText());
        assertEquals("", controller.dobField.getText());
        assertEquals("", controller.contactNumberField.getText());
        assertEquals("", controller.addressField.getText());
        assertEquals("", controller.emailField.getText());
        assertEquals("", controller.passwordField.getText());
        assertTrue(controller.maleRadio.isSelected());
        assertFalse(controller.femaleRadio.isSelected());
    }

    @FunctionalInterface
    private interface FxTestTask<V> {
        V run() throws Exception;
    }
}