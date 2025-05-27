import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class PatientLoginController {
    @FXML private TextField patientIdField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String patientId = patientIdField.getText();
        String password = passwordField.getText();

        if (authenticatePatient(patientId, password)) {
            try {
                // Load the patient dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("patient_main_window.fxml"));
                Parent root = loader.load();

                // Get the controller and pass patient data
                PatientMainController controller = loader.getController();
                controller.initData(getPatientFromDatabase(patientId));

                // Get current stage and switch scene
                Stage stage = (Stage) patientIdField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Patient Dashboard - Tabeebak");
                stage.centerOnScreen();
                stage.setMaximized(true);  // Or set to your preferred full-screen mode

            } catch (IOException e) {
                showAlert("Navigation Error", "Failed to load patient dashboard: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Login Failed", "Invalid patient ID or password");
        }
    }
    @FXML
    private void handleSignUpClick(ActionEvent event) throws IOException {
        Parent signupRoot = FXMLLoader.load(getClass().getResource("patient_signup.fxml"));
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(signupRoot, 800, 600)); // Set consistent size
        stage.setTitle("Patient Sign Up");
    }

    boolean authenticatePatient(String patientId, String password) {
        String query = "SELECT password FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // In production: Use password hashing (e.g., BCrypt.checkpw)
                return password.equals(rs.getString("password"));
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error during authentication: " + e.getMessage());
        }
        return false;
    }

    Patient getPatientFromDatabase(String patientId) {
        String query = "SELECT * FROM patients WHERE patient_id = ?";

        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, patientId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return new Patient(
                        rs.getString("patient_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("dob"),
                        rs.getString("gender"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            showAlert("Database Error", "Failed to load patient data: " + e.getMessage());
        }
        return null;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}