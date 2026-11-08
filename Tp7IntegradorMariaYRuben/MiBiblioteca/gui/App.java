package MiBiblioteca.gui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Responsabilidad unica: arrancar JavaFX, abrir la sesion de datos y
 * mostrar la ventana principal. No contiene logica de negocio ni de pantallas.
 */
public class App extends Application {

    private SesionBiblioteca sesionBiblioteca;

    @Override
    public void init() {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    @Override
    public void start(Stage p_ventana) throws Exception {
        this.sesionBiblioteca = new SesionBiblioteca("biblioteca.db");

        FXMLLoader cargador = new FXMLLoader(App.class.getResource("Principal.fxml"));
        cargador.setControllerFactory(new FabricaControladores(this.sesionBiblioteca));
        Parent raiz = cargador.load();

        Scene escena = new Scene(raiz, 1180, 740);
        escena.getStylesheets().add(App.class.getResource("tema.css").toExternalForm());

        p_ventana.setTitle("Biblioteca Capibara");
        p_ventana.setScene(escena);
        p_ventana.show();
    }

    @Override
    public void stop() {
        if (this.sesionBiblioteca != null) {
            this.sesionBiblioteca.cerrar();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
