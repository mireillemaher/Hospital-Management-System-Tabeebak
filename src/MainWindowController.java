import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;

public class MainWindowController {

    @FXML
    private Button patientButton;
    private Button doctorButton;

    @FXML
    private void handlePatientClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("patient_view.fxml"));
            Parent patientRoot = fxmlLoader.load();
            Stage stage = new Stage();
            stage.setTitle("Patient Menu");
            stage.setScene(new Scene(patientRoot));
            stage.show();

            // Optional: close current window
            Stage currentStage = (Stage) patientButton.getScene().getWindow();
            currentStage.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDoctorClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("doctor_view.fxml")
            );
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Doctor Login");
            stage.show();

            // Close current window if needed
            // ((Node)event.getSource()).getScene().getWindow().hide();

        } catch (IOException e) {
            System.err.println("Error loading doctor_view.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
