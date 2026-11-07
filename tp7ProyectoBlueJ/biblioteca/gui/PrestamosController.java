package biblioteca.gui;

import biblioteca.Libro;
import biblioteca.Prestamo;
import biblioteca.SesionBiblioteca;
import biblioteca.Socio;
import java.time.LocalDate;
import java.util.ArrayList;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.StringConverter;

/**
 * Pestaña Préstamos: registro de retiros (combo socio + combo de libros
 * disponibles + fecha de retiro), tabla con filtro Activos / Vencidos /
 * Todos, busqueda en vivo y PILDORA de estado por fila (En plazo / Por
 * vencer / Vencido / Devuelto) con menu "..." para devolver.
 *
 * Las reglas de negocio se preguntan al modelo y la escritura pasa por
 * ServicioPrestamos (aporte de la compañera: persistencia con rollback).
 * Si el prestamo se rechaza se muestra un dialogo con el MOTIVO, en el mismo
 * orden de revisiones que Biblioteca.prestarLibro().
 */
public class PrestamosController {

    private final SesionBiblioteca sesion;
    private final ConversorDeFechas conversorDeFechas;
    private final ReglasDePlazo reglasDePlazo;
    private final ServicioPrestamos servicioPrestamos;

    @FXML private ComboBox<Socio> comboSocios;
    @FXML private ComboBox<Libro> comboLibros;
    @FXML private DatePicker fechaRetiro;
    @FXML private TableView<Prestamo> tablaPrestamos;
    @FXML private TableColumn<Prestamo, String> colPSocio;
    @FXML private TableColumn<Prestamo, String> colPLibro;
    @FXML private TableColumn<Prestamo, String> colPRetiro;
    @FXML private TableColumn<Prestamo, String> colPVence;
    @FXML private TableColumn<Prestamo, String> colPDevolucion;
    @FXML private TableColumn<Prestamo, EstadoPrestamo> colPEstado;
    @FXML private TableColumn<Prestamo, Prestamo> colPAcciones;
    @FXML private javafx.scene.control.RadioButton radioActivos;
    @FXML private javafx.scene.control.RadioButton radioVencidos;
    @FXML private javafx.scene.control.RadioButton radioTodos;
    @FXML private TextField campoFiltro;

    /** Búsqueda en vivo (por encima del filtro Activos/Vencidos/Todos). */
    private final ObservableList<Prestamo> fuentePrestamos = FXCollections.observableArrayList();
    private final FilteredList<Prestamo> filtradosPrestamos = new FilteredList<>(this.fuentePrestamos);

    /** Sesion compartida (misma base que consola) + consultas, inyectados por App. */
    public PrestamosController(SesionBiblioteca p_sesion, ConsultasBiblioteca p_consultas) {
        this.sesion = p_sesion;
        this.conversorDeFechas = new ConversorDeFechas();
        this.reglasDePlazo = new ReglasDePlazo(this.conversorDeFechas);
        this.servicioPrestamos = new ServicioPrestamos(p_sesion, this.conversorDeFechas,
            this.reglasDePlazo);
    }

    /**
     * Cableado JavaFX automatico: converters de los combos, bind de las
     * columnas (estado con pildora y menu de acciones por fila), fecha de
     * retiro en HOY y suscripcion al estado de la sesion.
     */
    @FXML
    private void initialize() {
        // El socio se muestra "nombre (tipo)"; el libro, "titulo (edicion N)"
        this.comboSocios.setConverter(converterSocio());
        this.comboLibros.setConverter(converterLibro());

        colPSocio.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            dato.getValue().getSocio().getNombre()));
        colPLibro.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            dato.getValue().getLibro().getTitulo()));
        colPRetiro.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            this.conversorDeFechas.aTextoCorto(
                this.conversorDeFechas.aLocalDate(dato.getValue().getFechaRetiro()))));
        colPVence.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            this.conversorDeFechas.aTextoCorto(
                this.reglasDePlazo.calcularFechaDeVencimiento(dato.getValue()))));
        colPDevolucion.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            dato.getValue().getFechaDevolucion() == null ? "-"
                : this.conversorDeFechas.aTextoCorto(this.conversorDeFechas.aLocalDate(
                    dato.getValue().getFechaDevolucion()))));

        // Estado: clasificacion del servicio, dibujada como pildora de color
        colPEstado.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            this.servicioPrestamos.estadoDe(dato.getValue())));
        colPEstado.setCellFactory(columna -> new CeldaEstadoPrestamo());

        // Acciones: menu "..." por fila (devolver directamente esa fila)
        colPAcciones.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue()));
        colPAcciones.setCellFactory(columna -> new CeldaAccionesPrestamo(this::devolverEl));

        // por defecto: retiro de hoy (la fecha viaja a medianoche)
        this.fechaRetiro.setValue(LocalDate.now());

        // Busqueda en vivo: mientras se escribe, la tabla se filtra sola
        // (se apila sobre el radio Activos/Vencidos/Todos).
        this.tablaPrestamos.setItems(this.filtradosPrestamos);
        this.campoFiltro.textProperty().addListener(
            (p_obs, p_viejo, p_nuevo) -> this.filtroPrestamos(p_nuevo));

        this.sesion.suscribir(this::recargar);
        recargar();
    }

    /** Recarga combos + tabla desde el estado actual, respetando el filtro elegido. */
    public void recargar() {
        this.comboSocios.getItems().setAll(this.sesion.getBiblioteca().getSocios());
        this.comboLibros.getItems().setAll(librosDisponibles());

        ArrayList<Prestamo> todos = todosLosPrestamos();
        ArrayList<Prestamo> visibles = new ArrayList<>();
        for (Prestamo unPrestamo : todos) {
            boolean activo = unPrestamo.getFechaDevolucion() == null;
            boolean vencido = this.servicioPrestamos.estadoDe(unPrestamo)
                == EstadoPrestamo.VENCIDO;
            if ((this.radioTodos != null && this.radioTodos.isSelected())
                    || (this.radioActivos.isSelected() && activo)
                    || (this.radioVencidos.isSelected() && vencido)) {
                visibles.add(unPrestamo);
            }
        }
        this.fuentePrestamos.setAll(visibles);
    }

    /** Predicado de busqueda en vivo: nombre del socio o titulo del libro. */
    private void filtroPrestamos(String p_texto) {
        String texto = p_texto.trim().toLowerCase();
        this.filtradosPrestamos.setPredicate(unPrestamo -> texto.isEmpty()
            || unPrestamo.getSocio().getNombre().toLowerCase().contains(texto)
            || unPrestamo.getLibro().getTitulo().toLowerCase().contains(texto));
    }

    /** Registra el retiro: valida el modelo y persiste via ServicioPrestamos. */
    @FXML
    private void prestar() {
        Socio socio = this.comboSocios.getValue();
        Libro libro = this.comboLibros.getValue();
        LocalDate fecha = this.fechaRetiro.getValue();

        if (socio == null || libro == null || fecha == null) {
            Dialogos.aviso("Elija socio, libro disponible y fecha de retiro.");
            return;
        }
        try {
            this.servicioPrestamos.registrarPrestamo(socio, libro, fecha);
        } catch (PrestamoRechazadoException e) {
            Dialogos.error("No se puede registrar el prestamo.\n\n" + e.getMessage());
        } catch (ErrorDePersistenciaException e) {
            Dialogos.error("Problema con la base de datos:\n\n" + e.getMessage());
        }
    }

    /** Devuelve el prestamo seleccionado en la tabla (boton "Devolver"). */
    @FXML
    private void devolver() {
        Prestamo elegido = this.tablaPrestamos.getSelectionModel().getSelectedItem();
        if (elegido == null) { Dialogos.aviso("Seleccione un prestamo en la tabla."); return; }
        devolverEl(elegido);
    }

    /** Devuelve UN prestamo dado (lo usa el menu "..." de cada fila). */
    private void devolverEl(Prestamo p_elegido) {
        if (p_elegido == null) return;
        if (p_elegido.getFechaDevolucion() != null) {
            Dialogos.aviso("Ese prestamo ya fue devuelto el "
                + this.conversorDeFechas.aTextoCorto(
                    this.conversorDeFechas.aLocalDate(p_elegido.getFechaDevolucion())) + ".");
            return;
        }
        try {
            this.servicioPrestamos.registrarDevolucion(p_elegido);
        } catch (ErrorDePersistenciaException e) {
            Dialogos.error("Problema con la base de datos:\n\n" + e.getMessage());
        }
    }

    // -------------------
    // Helpers
    // -------------------

    /** Todos los prestamos del estado actual (por libro, sin repetir). */
    private ArrayList<Prestamo> todosLosPrestamos() {
        ArrayList<Prestamo> todos = new ArrayList<>();
        for (Libro unLibro : this.sesion.getBiblioteca().getLibros()) {
            for (Prestamo unPrestamo : unLibro.getPrestamos()) {
                if (!todos.contains(unPrestamo)) todos.add(unPrestamo);
            }
        }
        return todos;
    }

    /** Ejemplares prestables hoy: los que NO tienen prestamo activo. */
    private ArrayList<Libro> librosDisponibles() {
        ArrayList<Libro> disponibles = new ArrayList<>();
        for (Libro unLibro : this.sesion.getBiblioteca().getLibros()) {
            if (!unLibro.prestado()) disponibles.add(unLibro);
        }
        return disponibles;
    }

    /** Visualiza el socio como "nombre (tipo)" en el ComboBox. */
    private StringConverter<Socio> converterSocio() {
        return new StringConverter<>() {
            @Override public String toString(Socio socio) {
                return socio == null ? "" : socio.getNombre() + " (" + socio.soyDeLaClase() + ")";
            }
            @Override public Socio fromString(String texto) { return comboSocios.getValue(); }
        };
    }

    /** Visualiza el ejemplar como "titulo (edicion N)" en el ComboBox. */
    private StringConverter<Libro> converterLibro() {
        return new StringConverter<>() {
            @Override public String toString(Libro libro) {
                return libro == null ? "" : libro.getTitulo() + " (edicion " + libro.getEdicion() + ")";
            }
            @Override public Libro fromString(String texto) { return comboLibros.getValue(); }
        };
    }
}
