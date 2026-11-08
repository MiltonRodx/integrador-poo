package MiBiblioteca.gui;

import MiBiblioteca.Libro;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Pantalla Catálogo: listado de ejemplares y si estan disponibles. */
public class CatalogoController {

    private final ConsultasBiblioteca consultas;

    @FXML private TableView<Libro> tablaLibros;
    @FXML private TableColumn<Libro, String> columnaTitulo;
    @FXML private TableColumn<Libro, Integer> columnaEdicion;
    @FXML private TableColumn<Libro, String> columnaEditorial;
    @FXML private TableColumn<Libro, Integer> columnaAnio;
    @FXML private TableColumn<Libro, String> columnaDisponibilidad;

    public CatalogoController(ConsultasBiblioteca p_consultas) {
        this.consultas = p_consultas;
    }

    @FXML
    private void initialize() {
        this.columnaTitulo.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getTitulo()));
        this.columnaEdicion.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getEdicion()));
        this.columnaEditorial.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getEditorial()));
        this.columnaAnio.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getAnio()));
        this.columnaDisponibilidad.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().prestado() ? "Prestado" : "Disponible"));

        this.consultas.alCambiarLosDatos(this::recargarTabla);
        this.recargarTabla();
    }

    private void recargarTabla() {
        this.tablaLibros.getItems().setAll(this.consultas.listarLibros());
    }
}
