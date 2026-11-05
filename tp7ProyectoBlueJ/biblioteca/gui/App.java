package biblioteca.gui;

import atlantafx.base.theme.PrimerLight;
import biblioteca.SesionBiblioteca;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Punto de entrada de la GUI.
 *
 * - Ventana unica con TabPane de 4 pestañas: Socios / Libros / Prestamos / Informes.
 * - Tema AtlantaFX "Primer claro": se aplica en init(), ANTES de construir UI.
 * - SesionBiblioteca: abre/carga la base una vez y la comparte con los 4
 *   controllers (creados por el controllerFactory del FXMLLoader).
 * - stop(): al cerrar la ventana se libera la conexion SQLite.
 *
 * Lanzamiento CLI (Linux):   ./gui.sh
 * Lanzamiento CLI (Windows): gui.bat
 * Desde BlueJ: click derecho en Launcher -> void main(String[]) (con los jars
 * cargados en Preferencias -> Librerias).
 */
public class App extends Application {

    /** Clave institucional compartida con el modo consola (misma fila en biblioteca.db). */
    public static final String CLAVE_INSTITUCION = "Biblioteca FaCENA - UNNE";

    private SesionBiblioteca sesion;

    /**
     * Preparacion JavaFX: fija el tema ANTES de construir la UI.

     * Sin este paso los controles no heredarian el estilo AtlantaFX
     * (Primer claro), que se define al arrancar la escena.
     */
    @Override
    public void init() {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
    }

    /**
     * Arma y muestra la ventana unica.
     *
     * Abre la sesion de datos en la base compartida con la consola, carga
     * MainWindow.fxml con un controllerFactory que inyecta ESA sesion en
     * los 4 controllers, y fija tamano (1024x640), icono y titulo.
     */
    @Override
    public void start(Stage p_stage) throws Exception {
        this.sesion = new SesionBiblioteca("biblioteca.db", CLAVE_INSTITUCION);

        FXMLLoader cargador = new FXMLLoader(App.class.getResource("MainWindow.fxml"));
        cargador.setControllerFactory(tipo -> {
            if (tipo == SociosController.class)    return new SociosController(this.sesion);
            if (tipo == LibrosController.class)    return new LibrosController(this.sesion);
            if (tipo == PrestamosController.class) return new PrestamosController(this.sesion);
            if (tipo == InformesController.class)  return new InformesController(this.sesion);
            throw new IllegalStateException("Controller inesperado: " + tipo);
        });
        Parent raiz = cargador.load();

        Scene escena = new Scene(raiz, 1024, 640);
        p_stage.getIcons().add(new Image(App.class.getResourceAsStream("icono.png")));
        p_stage.setTitle("Biblioteca FaCENA - UNNE");
        p_stage.setScene(escena);
        p_stage.show();
    }

    /** Cierre de la ventana: libera la conexion SQLite en forma ordenada. */
    @Override
    public void stop() {
        if (this.sesion != null) this.sesion.cerrar();
    }

    /**
     * Entrada JavaFX "pura" (solo valida si se arranca por module-path).
     *
     * En este proyecto la entrada real es {@link Launcher},
     * que delega aca sin exigir module-path.
     *
     * @param args argumentos estandar de Application.launch
     */
    public static void main(String[] args) {
        launch(args);
    }
}
