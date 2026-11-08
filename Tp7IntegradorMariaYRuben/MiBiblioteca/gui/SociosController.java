package MiBiblioteca.gui;

import MiBiblioteca.Socio;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/** Pantalla Socios: listado de socios con sus libros en poder. */
public class SociosController {

    private final ConsultasBiblioteca consultas;

    @FXML private TableView<Socio> tablaSocios;
    @FXML private TableColumn<Socio, Integer> columnaDni;
    @FXML private TableColumn<Socio, String> columnaNombre;
    @FXML private TableColumn<Socio, String> columnaTipo;
    @FXML private TableColumn<Socio, Integer> columnaLibrosEnPoder;
    @FXML private TableColumn<Socio, Integer> columnaDiasDePrestamo;

    public SociosController(ConsultasBiblioteca p_consultas) {
        this.consultas = p_consultas;
    }

    @FXML
    private void initialize() {
        this.columnaDni.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getDniSocio()));
        this.columnaNombre.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getNombre()));
        this.columnaTipo.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().soyDeLaClase()));
        this.columnaLibrosEnPoder.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().cantLibrosPrestados()));
        this.columnaDiasDePrestamo.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getDiasPrestamo()));

        this.consultas.alCambiarLosDatos(this::recargarTabla);
        this.recargarTabla();
    }

    private void recargarTabla() {
        this.tablaSocios.getItems().setAll(this.consultas.listarSocios());
    }
}
