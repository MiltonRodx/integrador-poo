package MiBiblioteca.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.stage.Stage;
import javafx.scene.stage.StageStyle;

public class Launcher extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
        Parent root = loader.load();
        
        // Ventana personalizada estilo Windows moderno / Frameless
        primaryStage.initStyle(StageStyle.UNDECORATED);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Biblioteca FaCENA - UNNE");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
