package MiBiblioteca.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    @FXML private TabPane mainTabPane;
    @FXML private Tab tabSocios;
    @FXML private Tab tabLibros;
    @FXML private Tab tabPrestamos;
    @FXML private Tab tabInformes;

    // Tarjetas de inicio / Dashboard
    @FXML private VBox cardLibros;
    @FXML private VBox cardSocios;
    @FXML private VBox cardPrestamos;

    // Control de ventana personalizada
    @FXML private Button btnMinimize;
    @FXML private Button btnMaximize;
    @FXML private Button btnClose;

    private double xOffset = 0;
    private double yOffset = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Navegación rápida desde los cuadros interactivos del Dashboard a las Solapas
        if (cardLibros != null) {
            cardLibros.setOnMouseClicked(event -> mainTabPane.getSelectionModel().select(tabLibros));
        }
        if (cardSocios != null) {
            cardSocios.setOnMouseClicked(event -> mainTabPane.getSelectionModel().select(tabSocios));
        }
        if (cardPrestamos != null) {
            cardPrestamos.setOnMouseClicked(event -> mainTabPane.getSelectionModel().select(tabPrestamos));
        }
    }

    // Funciones de control de la ventana (Minimizar, Maximizar, Cerrar)
    @FXML
    private void handleMinimize() {
        Stage stage = (Stage) btnMinimize.getScene().getWindow();
        stage.setIconified(true);
    }

    @FXML
    private void handleMaximize() {
        Stage stage = (Stage) btnMaximize.getScene().getWindow();
        stage.setMaximized(!stage.isMaximized());
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) btnClose.getScene().getWindow();
        stage.close();
    }
}
