package biblioteca.gui;

import biblioteca.Libro;
import biblioteca.SesionBiblioteca;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * Pestaña Libros: alta de ejemplares (los titulos pueden repetir: son
 * multiples ejemplares) y baja (bloqueada si el libro esta prestado).
 * Cada operacion de escritura persiste con sesion.guardar().
 */
public class LibrosController {

    private final SesionBiblioteca sesion;

    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> colTitulo;
    @FXML private TableColumn<Libro, Integer> colEdicion;
    @FXML private TableColumn<Libro, String> colEditorial;
    @FXML private TableColumn<Libro, Integer> colAnio;
    @FXML private TableColumn<Libro, String> colPrestado;

    /** Sesion compartida (misma base que consola), inyectada por App.start(). */
    public LibrosController(SesionBiblioteca p_sesion) { this.sesion = p_sesion; }

    /**
     * Cableado JavaFX automatico: bind de columnas (con ReadOnlyObjectWrapper)
     * + suscripcion al estado de la sesion + primer recarga de la tabla.
     */
    @FXML
    private void initialize() {
        colTitulo.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getTitulo()));
        colEdicion.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getEdicion()));
        colEditorial.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getEditorial()));
        colAnio.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getAnio()));
        colPrestado.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            dato.getValue().prestado() ? "Si" : "No"));

        this.sesion.suscribir(this::recargar);
        recargar();
    }

    /** Recarga la tabla desde el estado actual de la biblioteca. */
    public void recargar() {
        this.tablaLibros.getItems().setAll(this.sesion.getBiblioteca().getLibros());
    }

    /** Alta de libro: dialogo de 4 campos + validacion de enteros. */
    @FXML
    private void altaLibro() {
        Optional<String[]> valores = Dialogos.formulario(ventana(), "Alta de libro",
            "Completá los datos del ejemplar",
            "Titulo", "Nro. de edicion", "Editorial", "Anio de edicion");
        if (valores.isEmpty()) return;   // cancelado

        try {
            int edicion = Integer.parseInt(valores.get()[1]);
            int anio = Integer.parseInt(valores.get()[3]);
            String titulo = valores.get()[0];
            String editorial = valores.get()[2];

            if (titulo.isEmpty()) { Dialogos.error("El titulo no puede quedar vacio."); return; }
            if (editorial.isEmpty()) { Dialogos.error("La editorial no puede quedar vacia."); return; }
            if (edicion <= 0) { Dialogos.error("La edicion debe ser 1 o mayor."); return; }
            if (anio <= 0) { Dialogos.error("El anio debe ser mayor a cero."); return; }

            this.sesion.getBiblioteca().nuevoLibro(titulo, edicion, editorial, anio);
            this.sesion.guardar();
            this.sesion.publicar();
        } catch (NumberFormatException sinNumero) {
            Dialogos.error("Edicion y anio deben ser numeros enteros.");
        }
    }

    /** Baja del libro seleccionado; FK de prestamos: sin historial asociado, DELETE real en BD. */
    @FXML
    private void bajaLibro() {
        Libro elegido = this.tablaLibros.getSelectionModel().getSelectedItem();
        if (elegido == null) { Dialogos.aviso("Seleccione un libro en la tabla."); return; }
        // --- NUEVO: confirmacion destructiva ---
        if (!Dialogos.confirm("¿Desea dar de baja el libro " + elegido.getTitulo() + "?")) {
            return; // usuario cancelo la baja
        }
        if (!elegido.getPrestamos().isEmpty()) {
            // Igual que el menu de consola: la FK prestamo -> libro impide borrar con historial.
            Dialogos.aviso("\"" + elegido.getTitulo() + "\" (edicion " + elegido.getEdicion()
                + ") no puede darse de baja: tiene " + elegido.getPrestamos().size()
                + " prestamo(s) asociados (FK).");
            return;
        }
        try {
            this.sesion.getDao().getLibroDAO().delete(elegido);   // DELETE real en la BD
            this.sesion.getBiblioteca().quitarLibro(elegido);     // coherencia memoria <-> BD
        } catch (biblioteca.persistencia.DAOException e) {
            Dialogos.error("La base impide la baja.\n" + e.getMessage());
            return;
        }
        this.sesion.guardar();
        this.sesion.publicar();
    }

    /** Ventana dueña de los dialogos. */
    private javafx.stage.Window ventana() {
        return this.tablaLibros.getScene() != null ? this.tablaLibros.getScene().getWindow() : null;
    }
}
