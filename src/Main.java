import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("main_view.fxml"));
        primaryStage.setTitle("Hospital Management System");
        primaryStage.setScene(new Scene(root));
        primaryStage.show();
        primaryStage.setMaximized(true);  // Or set to your preferred full-screen mode

    }


    public static void main(String[] args) {
        launch(args);
    }
}
