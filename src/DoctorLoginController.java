import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.*;

public class DoctorLoginController {
    @FXML private TextField doctorIdField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String doctorId = doctorIdField.getText();
        String password = passwordField.getText();

        if (authenticateDoctor(doctorId, password)) {
            try {
                // Load the doctor dashboard
                FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
                Parent root = loader.load();

                // Get the controller and pass doctor data
                DoctorDashboardController controller = loader.getController();
//                controller.initData(getDoctorFromDatabase(doctorId));

                // Get current stage and switch scene
                Stage stage = (Stage) doctorIdField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.setTitle("Doctor Dashboard - Tabeebak");
                stage.centerOnScreen();
                stage.setMaximized(true);  // Or set to your preferred full-screen mode

            } catch (IOException e) {
                showAlert("", "" + e.getMessage());
                e.printStackTrace();
            }
        } else {
            showAlert("Login Failed", "Invalid doctor ID or password");
        }
    }
/*
    public void initData(Doctor doctor) {
        try {
            if (doctor == null) {
                throw new IllegalArgumentException("Doctor data cannot be null");
            }
            // Initialize your UI components here
            System.out.println("Dashboard initialized with: " + doctor.getDoctorId());

        } catch (Exception e) {
            System.err.println("Dashboard initialization failed:");
            e.printStackTrace();
            throw e; // Re-throw to see in main error
        }
    }
    @FXML
    private void handleSuccessfulLogin(Doctor doctor, Connection connection) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("DoctorDashboard.fxml"));
            Parent root = loader.load();

            // Get controller and set data
            DoctorDashboardController controller = loader.getController();

            // Show the stage
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Doctor Dashboard");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to load dashboard");
        }
    }*/


    boolean authenticateDoctor(String doctorId, String password) {
        String query = "SELECT Password FROM doctors WHERE DoctorID = ?";

        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // In production: Use password hashing (e.g., BCrypt.checkpw)
                return rs.getString("Password").equals(password);

            }
        } catch (SQLException e) {
            showAlert("Database Error", "Error during authentication: " + e.getMessage());
        }
        return false;
    }


    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}