package biblioteca.gui;

import atlantafx.base.theme.PrimerDark;
import atlantafx.base.theme.PrimerLight;
import biblioteca.SesionBiblioteca;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * Punto de entrada de la GUI.
 *
 * - Ventana unica con TabPane de 5 pestañas: Socios / Libros / Prestamos /
 *   Informes / Estadisticas.
 * - Tema AtlantaFX "Primer claro" (o "Primer oscuro" con el toggle): se aplica
 *   en init(), ANTES de construir UI.
 * - SesionBiblioteca: abre/carga la base una vez y la comparte con los
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
     * Conecta el toggle claro/oscuro de la barra superior.
     *
     * Al activarlo cambia el tema AtlantaFX (PrimerLight <-> PrimerDark en
     * runtime) y marca la raiz con la clase "dark" para los retoques de
     * tema.css. El CSS del toggle vive en tema.css.
     */
    private void armoToggleTema(Scene p_escena) {
        ToggleButton toggle = (ToggleButton) p_escena.getRoot().lookup("#toggleTema");
        if (toggle == null) return;   // sin toggle en el FXML, queda el tema fijo.

        toggle.selectedProperty().addListener((p_obs, p_antes, p_ahora) -> {
            if (p_ahora) {
                p_escena.getRoot().getStyleClass().add("dark");
                toggle.setText("\u2600 Claro");
                Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
            } else {
                p_escena.getRoot().getStyleClass().remove("dark");
                toggle.setText("\uD83C\uDF19 Oscuro");
                Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());
            }
        });
    }

    /**
     * Arma y muestra la ventana unica, previa al splash.
     *
     * Muestra el Splash (libro 3D que se abre, ~1.5 s o click para saltarlo) y
     * pase el control a la ventana principal recien al terminar la animacion.
     */
    @Override
    public void start(Stage p_stage) throws Exception {
        new Splash(() -> {
            try {
                this.armaVentana(p_stage);
            } catch (Exception e) {
                throw new RuntimeException("Error al armar la ventana: " + e.getMessage(), e);
            }
        }).mostrar();
    }

    /**
     * Prepara el contenido de la ventana unica.
     *
     * Abre la sesion de datos en la base compartida con la consola, carga
     * MainWindow.fxml con un controllerFactory que inyecta ESA sesion en
     * los 4 controllers, y fija tamano (1024x640), icono y titulo.
     */
    private void armaVentana(Stage p_stage) throws Exception {
        this.sesion = new SesionBiblioteca("biblioteca.db", CLAVE_INSTITUCION);

        FXMLLoader cargador = new FXMLLoader(App.class.getResource("MainWindow.fxml"));
        // Servicios compartidos (aporte de la compañera): consultas de lectura
        // para todos, y el panel de estadisticas vive solo de ellas.
        ConsultasBiblioteca consultas = new ConsultasBiblioteca(this.sesion);
        ConversorDeFechas conversorDeFechas = new ConversorDeFechas();
        ReglasDePlazo reglasDePlazo = new ReglasDePlazo(conversorDeFechas);
        ServicioEstadisticas servicioEstadisticas =
            new ServicioEstadisticas(consultas, reglasDePlazo);
        cargador.setControllerFactory(tipo -> {
            if (tipo == SociosController.class)    return new SociosController(this.sesion);
            if (tipo == LibrosController.class)    return new LibrosController(this.sesion);
            if (tipo == PrestamosController.class) return new PrestamosController(this.sesion, consultas);
            if (tipo == InformesController.class)  return new InformesController(this.sesion);
            if (tipo == PanelEstadisticasController.class) {
                return new PanelEstadisticasController(servicioEstadisticas, consultas);
            }
            throw new IllegalStateException("Controller inesperado: " + tipo);
        });
        Parent raiz = cargador.load();

        Scene escena = new Scene(raiz, 1024, 640);
        escena.getStylesheets().add(App.class.getResource("tema.css").toExternalForm());
        this.armoToggleTema(escena);
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
