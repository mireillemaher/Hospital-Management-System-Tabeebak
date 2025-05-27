import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.SQLException;

public class PatientSignupController {
    @FXML TextField firstNameField;
    @FXML TextField lastNameField;
    @FXML TextField patientIDField;
    @FXML TextField dobField;
    @FXML TextField contactNumberField;
    @FXML TextArea addressField;
    @FXML TextField emailField;
    @FXML PasswordField passwordField;
    @FXML RadioButton maleRadio;
    @FXML RadioButton femaleRadio;
    @FXML Button signUpButton;

    private PatientManager patientManager;

    public void initialize() {
        try {
            Connection conn = DatabaseHelper.connect();
            patientManager = new PatientManager(conn);
        } catch (SQLException e) {
            showAlert("Database Error", "Could not connect to database: " + e.getMessage());
        }

        // Set up gender toggle group
        ToggleGroup genderGroup = new ToggleGroup();
        maleRadio.setToggleGroup(genderGroup);
        femaleRadio.setToggleGroup(genderGroup);
        maleRadio.setSelected(true);
    }

    @FXML
    void handleSignUp() {
        try {
            if (!validateInputs()) return;

            if (patientManager.patientExists(patientIDField.getText())) {
                showAlert("Error", "Patient ID already exists");
                return;
            }

            Patient patient = new Patient(
                    patientIDField.getText(),
                    firstNameField.getText(),
                    lastNameField.getText(),
                    dobField.getText(),
                    maleRadio.isSelected() ? "Male" : "Female",
                    contactNumberField.getText(),
                    emailField.getText(),
                    addressField.getText(),
                    passwordField.getText()
            );

            patientManager.registerPatient(patient);
            showAlert("Success", "Patient registered successfully!");
            clearForm();

        } catch (SQLException e) {
            showAlert("Database Error", "Error saving patient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    boolean validateInputs() {
        if (firstNameField.getText().isEmpty() || lastNameField.getText().isEmpty() ||
                patientIDField.getText().isEmpty() || dobField.getText().isEmpty() ||
                contactNumberField.getText().isEmpty() || emailField.getText().isEmpty() ||
                passwordField.getText().isEmpty()) {
            showAlert("Validation Error", "Please fill in all required fields");
            return false;
        }
        return true;
    }

    void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        patientIDField.clear();
        dobField.clear();
        contactNumberField.clear();
        addressField.clear();
        emailField.clear();
        passwordField.clear();
        maleRadio.setSelected(true);
    }

    void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
