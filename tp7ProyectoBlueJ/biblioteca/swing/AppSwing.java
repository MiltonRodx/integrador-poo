package biblioteca.swing;

import biblioteca.SesionBiblioteca;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la GUI Swing.
 *
 * - Ventana unica con JTabbedPane de 5 pestañas: Socios / Libros / Prestamos /
 *   Informes / Estadisticas.
 * - Tema: Nimbus nativo del JDK + paleta propia de Tema.java (equivalente del
 *   AtlantaFX Primer Light/Dark, con toggle claro/oscuro en la barra superior).
 * - SesionBiblioteca: abre/carga la base en un hilo aparte (SwingWorker) y se
 *   comparte con todos los paneles, mientras el splash despista al usuario.
 * - Al cerrar la ventana se libera la conexion SQLite en forma ordenada.
 *
 * Ventaja sobre la version JavaFX: main() directo, SIN Launcher, SIN
 * module-path y SIN jars externos (solo el sqlite-jdbc).
 *
 * Lanzamiento CLI (Linux):   ./gui.sh
 * Lanzamiento CLI (Windows): gui.bat
 * Desde BlueJ: click derecho en AppSwing -> void main(String[] args).
 */
public class AppSwing {

    /** Clave institucional compartida con el modo consola (misma fila en biblioteca.db). */
    public static final String CLAVE_INSTITUCION = "Biblioteca FaCENA - UNNE";

    private SesionBiblioteca sesion;

    /** Entrada principal: instala el tema y delega en el EDT. */
    public static void main(String[] args) {
        Tema.instalar();
        SwingUtilities.invokeLater(AppSwing::arrancar);
    }

    /** Secuencia completa: splash -> carga de la base -> ventana principal. */
    static void arrancar() {
        new SplashSwing(() -> {
            javax.swing.SwingWorker<SesionBiblioteca, Void> cargador =
                new javax.swing.SwingWorker<SesionBiblioteca, Void>() {
                    @Override protected SesionBiblioteca doInBackground() {
                        return new SesionBiblioteca("biblioteca.db", CLAVE_INSTITUCION);
                    }
                    @Override protected void done() {
                        try {
                            new VentanaSwing(get()).mostrar();
                        } catch (Exception e) {
                            throw new RuntimeException("Error al armar la ventana: "
                                + e.getMessage(), e);
                        }
                    }
                };
            cargador.execute();
        }).mostrar();
    }
}
