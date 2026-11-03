package biblioteca.gui;

import biblioteca.Docente;
import biblioteca.Estudiante;
import biblioteca.SesionBiblioteca;
import biblioteca.Socio;
import java.util.Optional;
import javafx.collections.FXCollections; // no? remove if unused
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyObjectWrapper;

/**
 * Pestaña Socios: tabla completa, altas de docente/estudiante, busqueda por
 * D.N.I., baja (solo sin libros en poder) y premio de dias (solo docentes).
 * Cada operacion de escritura persiste con sesion.guardar().
 */
public class SociosController {

    private final SesionBiblioteca sesion;

    @FXML private TableView<Socio> tablaSocios;
    @FXML private TableColumn<Socio, Integer> colDni;
    @FXML private TableColumn<Socio, String> colNombre;
    @FXML private TableColumn<Socio, String> colTipo;
    @FXML private TableColumn<Socio, Integer> colPrestados;
    @FXML private TableColumn<Socio, Integer> colDias;
    @FXML private TextField campoBusqueda;

    public SociosController(SesionBiblioteca p_sesion) { this.sesion = p_sesion; }

    @FXML
    private void initialize() {
        // Cell value factories tipadas (las beans del TP no son Property, se usa wrapper)
        colDni.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getDniSocio()));
        colNombre.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getNombre()));
        colTipo.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().soyDeLaClase()));
        colPrestados.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().cantLibrosPrestados()));
        colDias.setCellValueFactory(dato -> new ReadOnlyObjectWrapper<>(dato.getValue().getDiasPrestamos()));

        this.sesion.suscribir(this::recargar);
        recargar();
    }

    /** Recarga la tabla desde el estado actual de la biblioteca. */
    public void recargar() {
        this.tablaSocios.getItems().setAll(this.sesion.getBiblioteca().getSocios());
    }

    @FXML private void altaDocente()    { alta("docente"); }
    @FXML private void altaEstudiante() { alta("estudiante"); }

    /** Alta de socio: dialogo de 3 campos + validaciones (dni numerico, unico, nombre vacio). */
    private void alta(String p_tipo) {
        boolean esDocente = p_tipo.equals("docente");
        Optional<String[]> valores = Dialogos.formulario(
            ventana(), "Alta de " + p_tipo, "Completá los datos del " + p_tipo,
            "D.N.I.", "Nombre y apellido", esDocente ? "Area" : "Carrera");
        if (valores.isEmpty()) return;   // el usuario cancelo

        try {
            int dni = Integer.parseInt(valores.get()[0]);
            String nombre = valores.get()[1];
            String extra = valores.get()[2];

            if (dni <= 0) { Dialogos.error("El D.N.I. debe ser un numero entero positivo."); return; }
            if (nombre.isEmpty()) { Dialogos.error("El nombre no puede quedar vacio."); return; }
            if (extra.isEmpty()) { Dialogos.error("El campo " + (esDocente ? "Area" : "Carrera") + " no puede quedar vacio."); return; }
            if (this.sesion.getBiblioteca().buscarSocio(dni) != null) {
                Dialogos.aviso("Ya existe un socio con D.N.I. " + dni + ".");
                return;
            }

            if (esDocente) this.sesion.getBiblioteca().nuevoSocioDocente(dni, nombre, extra);
            else           this.sesion.getBiblioteca().nuevoSocioEstudiante(dni, nombre, extra);

            this.sesion.guardar();
            this.sesion.publicar();
        } catch (NumberFormatException sinNumero) {
            Dialogos.error("El D.N.I. debe ser un numero entero.");
        }
    }

    /** Busqueda por D.N.I.: selecciona (y muestra) la fila del socio indicado. */
    @FXML
    private void buscarPorDni() {
        try {
            int dni = Integer.parseInt(this.campoBusqueda.getText().trim());
            Socio encontrado = this.sesion.getBiblioteca().buscarSocio(dni);
            if (encontrado == null) { Dialogos.aviso("No hay socio con D.N.I. " + dni + "."); return; }
            this.tablaSocios.getSelectionModel().select(encontrado);
            this.tablaSocios.scrollTo(encontrado);
        } catch (NumberFormatException sinNumero) {
            Dialogos.error("Ingrese el D.N.I. como numero, sin puntos.");
        }
    }

    /** Baja del socio seleccionado; se bloquea si conserva libros prestados. */
    @FXML
    private void baja() {
        Socio elegido = this.tablaSocios.getSelectionModel().getSelectedItem();
        if (elegido == null) { Dialogos.aviso("Seleccione un socio en la tabla."); return; }
        if (elegido.cantLibrosPrestados() > 0) {
            Dialogos.aviso(elegido.getNombre() + " no puede darse de baja: tiene "
                + elegido.cantLibrosPrestados() + " libro(s) en su poder.");
            return;
        }
        this.sesion.getBiblioteca().quitarSocio(elegido);
        this.sesion.guardar();
        this.sesion.publicar();
    }

    /** Premio de responsabilidad: adiciona dias de prestamo a un DOCENTE. */
    @FXML
    private void premiar() {
        Socio elegido = this.tablaSocios.getSelectionModel().getSelectedItem();
        if (!(elegido instanceof Docente)) {
            Dialogos.aviso("El premio de dias se aplica solo a docentes (seleccioné uno).");
            return;
        }
        Docente docente = (Docente) elegido;

        Optional<String[]> valores = Dialogos.formulario(ventana(), "Premio de responsabilidad",
            docente.getNombre() + " tiene " + docente.getDiasPrestamos()
                + " dias de prestamo. ¿Cuantos dias adiciona?",
            "Dias a adicionar");
        if (valores.isEmpty()) return;

        try {
            int dias = Integer.parseInt(valores.get()[0]);
            if (dias <= 0) { Dialogos.error("Ingrese una cantidad de dias mayor a cero."); return; }
            docente.cambiarDiasDePrestamos(dias);   // consigna: adiciona dias
            this.sesion.guardar();
            this.sesion.publicar();
        } catch (NumberFormatException sinNumero) {
            Dialogos.error("La cantidad de dias debe ser un numero entero.");
        }
    }

    /** Ventana dueña de los dialogos (la de la escena actual). */
    private javafx.stage.Window ventana() {
        return this.tablaSocios.getScene() != null ? this.tablaSocios.getScene().getWindow() : null;
    }
}
