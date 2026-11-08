package MiBiblioteca.gui;

import MiBiblioteca.Libro;
import MiBiblioteca.Socio;
import java.time.LocalDate;
import java.util.Optional;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

/**
 * Pantalla "Nuevo Préstamo": formulario de alta + tabla de prestamos activos.
 * Solo coordina la pantalla; las reglas y la persistencia viven en los servicios.
 */
public class PrestamosController {

    private final ServicioPrestamos servicioPrestamos;
    private final ConsultasBiblioteca consultas;
    private final ConversorDeFechas conversorDeFechas;
    private final CuadrosDeDialogo cuadrosDeDialogo;

    private Socio socioSeleccionado;

    @FXML private TextField campoDniSocio;
    @FXML private TextField campoNombreSocio;
    @FXML private ComboBox<Libro> comboLibrosDisponibles;
    @FXML private DatePicker selectorFechaPrestamo;
    @FXML private TextField campoFechaVencimiento;

    @FXML private TableView<FilaPrestamo> tablaPrestamosActivos;
    @FXML private TableColumn<FilaPrestamo, String> columnaSocio;
    @FXML private TableColumn<FilaPrestamo, String> columnaLibro;
    @FXML private TableColumn<FilaPrestamo, String> columnaFechaPrestamo;
    @FXML private TableColumn<FilaPrestamo, String> columnaVencimiento;
    @FXML private TableColumn<FilaPrestamo, EstadoPrestamo> columnaEstado;
    @FXML private TableColumn<FilaPrestamo, FilaPrestamo> columnaAcciones;

    public PrestamosController(ServicioPrestamos p_servicioPrestamos,
                               ConsultasBiblioteca p_consultas,
                               ConversorDeFechas p_conversorDeFechas,
                               CuadrosDeDialogo p_cuadrosDeDialogo) {
        this.servicioPrestamos = p_servicioPrestamos;
        this.consultas = p_consultas;
        this.conversorDeFechas = p_conversorDeFechas;
        this.cuadrosDeDialogo = p_cuadrosDeDialogo;
    }

    @FXML
    private void initialize() {
        this.configurarComboDeLibros();
        this.configurarColumnasDeLaTabla();

        this.selectorFechaPrestamo.setValue(LocalDate.now());
        this.selectorFechaPrestamo.valueProperty().addListener(
            (p_observable, p_anterior, p_nueva) -> this.actualizarFechaDeVencimiento());

        this.consultas.alCambiarLosDatos(this::recargarDatos);
        this.recargarDatos();
    }

    // ---------------- acciones del formulario ----------------

    @FXML
    private void buscarSocio() {
        try {
            int dni = Integer.parseInt(this.campoDniSocio.getText().trim());
            Optional<Socio> encontrado = this.consultas.buscarSocioPorDni(dni);
            if (encontrado.isEmpty()) {
                this.cuadrosDeDialogo.mostrarAviso("No hay ningún socio con D.N.I. " + dni + ".");
                this.olvidarSocio();
                return;
            }
            this.socioSeleccionado = encontrado.get();
            this.campoNombreSocio.setText(this.socioSeleccionado.getNombre());
            this.actualizarFechaDeVencimiento();
        } catch (NumberFormatException sinNumero) {
            this.cuadrosDeDialogo.mostrarError("Ingresá el D.N.I. como número, sin puntos.");
        }
    }

    @FXML
    private void registrarPrestamo() {
        Libro libroElegido = this.comboLibrosDisponibles.getValue();
        LocalDate fechaRetiro = this.selectorFechaPrestamo.getValue();
        if (this.socioSeleccionado == null || libroElegido == null || fechaRetiro == null) {
            this.cuadrosDeDialogo.mostrarAviso("Buscá un socio, elegí un libro y una fecha de préstamo.");
            return;
        }
        try {
            this.servicioPrestamos.registrarPrestamo(this.socioSeleccionado, libroElegido, fechaRetiro);
            this.cuadrosDeDialogo.mostrarInformacion("Préstamo registrado correctamente.");
            this.limpiarFormulario();
        } catch (PrestamoRechazadoException rechazo) {
            this.cuadrosDeDialogo.mostrarError(rechazo.getMessage());
        } catch (ErrorDePersistenciaException falloDeBase) {
            this.cuadrosDeDialogo.mostrarError(falloDeBase.getMessage());
        }
    }

    private void registrarDevolucion(FilaPrestamo p_fila) {
        try {
            this.servicioPrestamos.registrarDevolucion(p_fila.getPrestamo());
        } catch (ErrorDePersistenciaException falloDeBase) {
            this.cuadrosDeDialogo.mostrarError(falloDeBase.getMessage());
        }
    }

    // ---------------- estado de la pantalla ----------------

    private void actualizarFechaDeVencimiento() {
        LocalDate fechaRetiro = this.selectorFechaPrestamo.getValue();
        if (this.socioSeleccionado == null || fechaRetiro == null) {
            this.campoFechaVencimiento.clear();
            return;
        }
        LocalDate vencimiento = this.servicioPrestamos
            .calcularFechaDeVencimiento(this.socioSeleccionado, fechaRetiro);
        this.campoFechaVencimiento.setText(this.conversorDeFechas.aTextoCorto(vencimiento)
            + "   (" + this.socioSeleccionado.getDiasPrestamo() + " días)");
    }

    private void recargarDatos() {
        this.comboLibrosDisponibles.getItems().setAll(this.consultas.listarLibrosDisponibles());
        this.tablaPrestamosActivos.getItems().setAll(
            this.servicioPrestamos.listarFilasDePrestamosActivos());
    }

    private void olvidarSocio() {
        this.socioSeleccionado = null;
        this.campoNombreSocio.clear();
        this.campoFechaVencimiento.clear();
    }

    private void limpiarFormulario() {
        this.campoDniSocio.clear();
        this.olvidarSocio();
        this.comboLibrosDisponibles.getSelectionModel().clearSelection();
        this.selectorFechaPrestamo.setValue(LocalDate.now());
    }

    // ---------------- configuracion de controles ----------------

    private void configurarComboDeLibros() {
        this.comboLibrosDisponibles.setConverter(new StringConverter<>() {
            @Override
            public String toString(Libro p_libro) {
                return p_libro == null ? "" : p_libro.getTitulo() + " (edición " + p_libro.getEdicion() + ")";
            }

            @Override
            public Libro fromString(String p_texto) {
                return comboLibrosDisponibles.getValue();
            }
        });
    }

    private void configurarColumnasDeLaTabla() {
        this.columnaSocio.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getNombreSocio()));
        this.columnaLibro.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getTituloLibro()));
        this.columnaFechaPrestamo.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getFechaRetiroTexto()));
        this.columnaVencimiento.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getFechaVencimientoTexto()));
        this.columnaEstado.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue().getEstado()));
        this.columnaAcciones.setCellValueFactory(p_dato ->
            new ReadOnlyObjectWrapper<>(p_dato.getValue()));

        this.columnaEstado.setCellFactory(p_columna -> new CeldaEstadoPrestamo());
        this.columnaAcciones.setCellFactory(p_columna ->
            new CeldaAccionesPrestamo(this::registrarDevolucion));
    }
}
