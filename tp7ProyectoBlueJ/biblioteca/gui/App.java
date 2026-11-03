package biblioteca.gui;

import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Punto de entrada de la GUI (esqueleto FJ1).
 *
 * - Ventana unica con TabPane de 4 pestañas: Socios / Libros / Prestamos / Informes.
 * - Tema AtlantaFX "Primer claro": se aplica en init(), ANTES de construir
 *   cualquier UI (init() corre antes de start()).
 * - Las vistas viven en .fxml junto a esta clase (paquete biblioteca.gui);
 *   los controllers de cada pestaña se conectan en la fase FJ2.
 *
 * Lanzamiento CLI (Linux):   ./gui.sh
 * Lanzamiento CLI (Windows): gui.bat
 * Desde BlueJ: click derecho en App -> void main(String[]) (con los jars
 * cargados en Preferencias -> Librerias).
 */
public class App extends Application {

    @Override
    public void init() {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    @Override
    public void start(Stage p_stage) throws Exception {
        FXMLLoader cargador = new FXMLLoader(App.class.getResource("MainWindow.fxml"));
        Parent raiz = cargador.load();

        Scene escena = new Scene(raiz, 1024, 640);
        p_stage.getIcons().add(new Image(App.class.getResourceAsStream("icono.png")));
        p_stage.setTitle("Biblioteca FaCENA - UNNE");
        p_stage.setScene(escena);
        p_stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
