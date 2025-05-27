import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;


import java.io.IOException;

public class PatientViewController {

    @FXML
    private void handleSignUpClick(ActionEvent event) throws IOException {
        Parent signupRoot = FXMLLoader.load(getClass().getResource("patient_signup.fxml"));
        Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(signupRoot, 800, 600)); // Set consistent size
        stage.setTitle("Patient Sign Up");
    }


    @FXML
    private void handleLoginButton(ActionEvent event) {
        try {
            // Load the login FXML file
            Parent root = FXMLLoader.load(getClass().getResource("patient_login.fxml"));

            // Get the current stage
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();

            // Create new scene and set it on the stage
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Patient Login");
            stage.show();
            stage.setMaximized(true);  // Or set to your preferred full-screen mode

        } catch (IOException e) {
            e.printStackTrace();
            // Show error message
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not load login page");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}