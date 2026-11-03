package biblioteca.gui;

import biblioteca.Estudiante;
import biblioteca.Libro;
import biblioteca.Prestamo;
import biblioteca.SesionBiblioteca;
import biblioteca.Socio;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.util.StringConverter;

/**
 * Pestaña Préstamos: registro de retiros (combo socio + combo de libros
 * disponibles + fecha de retiro), devolución del prestamo seleccionado y
 * tabla con filtro Activos / Vencidos / Todos.
 *
 * Si el prestamo se rechaza se muestra un dialogo con el MOTIVO, replicando
 * en orden las mismas revisiones que hace Biblioteca.prestarLibro().
 * Cada operacion de escritura persiste con sesion.guardar().
 */
public class PrestamosController {

    private final SesionBiblioteca sesion;

    @FXML private ComboBox<Socio> comboSocios;
    @FXML private ComboBox<Libro> comboLibros;
    @FXML private DatePicker fechaRetiro;
    @FXML private TableView<Prestamo> tablaPrestamos;
    @FXML private TableColumn<Prestamo, String> colPSocio;
    @FXML private TableColumn<Prestamo, String> colPLibro;
    @FXML private TableColumn<Prestamo, String> colPRetiro;
    @FXML private TableColumn<Prestamo, String> colPVence;
    @FXML private TableColumn<Prestamo, String> colPDevolucion;
    @FXML private TableColumn<Prestamo, String> colPEstado;
    @FXML private javafx.scene.control.RadioButton radioActivos;
    @FXML private javafx.scene.control.RadioButton radioVencidos;
    @FXML private javafx.scene.control.RadioButton radioTodos;

    public PrestamosController(SesionBiblioteca p_sesion) { this.sesion = p_sesion; }

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
            formato(dato.getValue().getFechaRetiro())));
        colPVence.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            formato(vence(dato.getValue()))));
        colPDevolucion.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            dato.getValue().getFechaDevolucion() == null ? "-"
                : formato(dato.getValue().getFechaDevolucion())));
        colPEstado.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(
            estado(dato.getValue())));

        // por defecto: retiro de hoy (la fecha viaja a medianoche)
        this.fechaRetiro.setValue(LocalDate.now());

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
            boolean vencido = activo && unPrestamo.vencido(hoyMedianoche());
            if ((this.radioTodos != null && this.radioTodos.isSelected())
                    || (this.radioActivos.isSelected() && activo)
                    || (this.radioVencidos.isSelected() && vencido)) {
                visibles.add(unPrestamo);
            }
        }
        this.tablaPrestamos.getItems().setAll(visibles);
    }

    /** Registra el retiro con las validaciones de la consigna. */
    @FXML
    private void prestar() {
        Socio socio = this.comboSocios.getValue();
        Libro libro = this.comboLibros.getValue();
        LocalDate fecha = this.fechaRetiro.getValue();

        if (socio == null || libro == null || fecha == null) {
            Dialogos.aviso("Elija socio, libro disponible y fecha de retiro.");
            return;
        }

        String motivo = motivoDeRechazo(socio, libro);
        if (motivo != null) {
            Dialogos.error("No se puede registrar el prestamo.\n\n" + motivo);
            return;
        }

        Calendar retiro = new GregorianCalendar(fecha.getYear(), fecha.getMonthValue() - 1,
            fecha.getDayOfMonth());   // medianoche del dia elegido
        boolean prestado = this.sesion.getBiblioteca().prestarLibro(retiro, socio, libro);
        if (!prestado) { // no deberia ocurrir: el motivo ya fue revisado
            Dialogos.error("El prestamo fue rechazado por el modelo.");
            return;
        }
        this.sesion.guardar();
        this.sesion.publicar();
    }

    /**
     * Motivo de rechazo (null = se puede prestar). Mismo ORDEN de revisiones
     * que Biblioteca.prestarLibro(): libro prestado primero, luego puedePedir().
     */
    private String motivoDeRechazo(Socio p_socio, Libro p_libro) {
        if (p_libro.prestado()) {
            return "El libro \"" + p_libro.getTitulo() + "\" ya esta prestado.";
        }
        if (!p_socio.puedePedir()) {
            if (p_socio instanceof Estudiante && p_socio.cantLibrosPrestados() >= 3) {
                return p_socio.getNombre() + " ya tiene 3 libros en su poder "
                    + "(tope para estudiantes).";
            }
            return p_socio.getNombre() + " tiene prestamos vencidos: no puede pedir "
                + "hasta devolverlos.";
        }
        return null;   // puede pedir
    }

    /** Devuelve el prestamo seleccionado (fecha de devolucion = hoy, por el modelo). */
    @FXML
    private void devolver() {
        Prestamo elegido = this.tablaPrestamos.getSelectionModel().getSelectedItem();
        if (elegido == null) { Dialogos.aviso("Seleccione un prestamo en la tabla."); return; }
        if (elegido.getFechaDevolucion() != null) {
            Dialogos.aviso("Ese prestamo ya fue devuelto el "
                + formato(elegido.getFechaDevolucion()) + ".");
            return;
        }
        try {
            this.sesion.getBiblioteca().devolverLibro(elegido.getLibro());
            this.sesion.guardar();
            this.sesion.publicar();
        } catch (biblioteca.LibroNoPrestadoException e) {
            Dialogos.error(e.getMessage());
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

    private ArrayList<Libro> librosDisponibles() {
        ArrayList<Libro> disponibles = new ArrayList<>();
        for (Libro unLibro : this.sesion.getBiblioteca().getLibros()) {
            if (!unLibro.prestado()) disponibles.add(unLibro);
        }
        return disponibles;
    }

    /** Fecha de vencimiento = retiro + dias de prestamo del socio. */
    private Calendar vence(Prestamo p_prestamo) {
        Calendar vence = (Calendar) p_prestamo.getFechaRetiro().clone();
        vence.add(Calendar.DAY_OF_MONTH, p_prestamo.getSocio().getDiasPrestamos());
        return vence;
    }

    /** Estado para la tabla: Devuelto / VENCIDO / A tiempo (evaluacion a nivel dia). */
    private String estado(Prestamo p_prestamo) {
        if (p_prestamo.getFechaDevolucion() != null) return "Devuelto";
        return p_prestamo.vencido(hoyMedianoche()) ? "VENCIDO" : "A tiempo";
    }

    /** Hoy, normalizado a medianoche (misma regla que el modelo). */
    private GregorianCalendar hoyMedianoche() {
        GregorianCalendar fecha = new GregorianCalendar();
        fecha.set(GregorianCalendar.HOUR_OF_DAY, 0);
        fecha.set(GregorianCalendar.MINUTE, 0);
        fecha.set(GregorianCalendar.SECOND, 0);
        fecha.set(GregorianCalendar.MILLISECOND, 0);
        return fecha;
    }

    /** Formato de la consigna para fechas: anio/mes/dia. */
    private static String formato(Calendar p_fecha) {
        if (p_fecha == null) return "-";
        return p_fecha.get(Calendar.YEAR) + "/"
            + (p_fecha.get(Calendar.MONTH) + 1) + "/"
            + p_fecha.get(Calendar.DAY_OF_MONTH);
    }

    private StringConverter<Socio> converterSocio() {
        return new StringConverter<>() {
            @Override public String toString(Socio socio) {
                return socio == null ? "" : socio.getNombre() + " (" + socio.soyDeLaClase() + ")";
            }
            @Override public Socio fromString(String texto) { return comboSocios.getValue(); }
        };
    }

    private StringConverter<Libro> converterLibro() {
        return new StringConverter<>() {
            @Override public String toString(Libro libro) {
                return libro == null ? "" : libro.getTitulo() + " (edicion " + libro.getEdicion() + ")";
            }
            @Override public Libro fromString(String texto) { return comboLibros.getValue(); }
        };
    }
}
