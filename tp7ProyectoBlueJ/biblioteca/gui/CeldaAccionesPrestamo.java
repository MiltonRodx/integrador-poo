package biblioteca.gui;

import biblioteca.Prestamo;
import java.util.function.Consumer;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;

/** Celda con el menu "..." de cada fila de la tabla de prestamos. */
public class CeldaAccionesPrestamo extends TableCell<Prestamo, Prestamo> {

    private final MenuButton menuDeAcciones = new MenuButton("⋯");

    public CeldaAccionesPrestamo(Consumer<Prestamo> p_alRegistrarDevolucion) {
        MenuItem opcionDevolver = new MenuItem("Registrar devolución");
        opcionDevolver.setOnAction(p_evento -> p_alRegistrarDevolucion.accept(getItem()));
        this.menuDeAcciones.getItems().add(opcionDevolver);
        this.menuDeAcciones.getStyleClass().add("boton-acciones");
    }

    @Override
    protected void updateItem(Prestamo p_prestamo, boolean p_vacia) {
        super.updateItem(p_prestamo, p_vacia);
        setGraphic(p_vacia || p_prestamo == null ? null : this.menuDeAcciones);
    }
}
