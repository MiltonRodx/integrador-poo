package MiBiblioteca.gui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.util.Callback;

/**
 * Cambia la pantalla central. Cada FXML se carga una sola vez y se
 * reutiliza (asi un controller no se suscribe dos veces a la sesion).
 */
public class NavegadorPantallas {

    private final StackPane contenedorDePantalla;
    private final Callback<Class<?>, Object> fabricaDeControladores;
    private final HashMap<String, Parent> pantallasCargadas = new HashMap<>();

    public NavegadorPantallas(StackPane p_contenedorDePantalla,
                              Callback<Class<?>, Object> p_fabricaDeControladores) {
        this.contenedorDePantalla = p_contenedorDePantalla;
        this.fabricaDeControladores = p_fabricaDeControladores;
    }

    public void mostrar(String p_nombreArchivoFxml) {
        Parent pantalla = this.pantallasCargadas.computeIfAbsent(
            p_nombreArchivoFxml, this::cargar);
        this.contenedorDePantalla.getChildren().setAll(pantalla);
    }

    private Parent cargar(String p_nombreArchivoFxml) {
        FXMLLoader cargador = new FXMLLoader(getClass().getResource(p_nombreArchivoFxml));
        cargador.setControllerFactory(this.fabricaDeControladores);
        try {
            return cargador.load();
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo cargar " + p_nombreArchivoFxml, e);
        }
    }
}
