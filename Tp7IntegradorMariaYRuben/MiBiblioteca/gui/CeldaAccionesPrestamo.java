package MiBiblioteca.gui;

import java.util.function.Consumer;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableCell;

/** Celda con el menu "..." de cada fila (por ahora: registrar devolucion). */
public class CeldaAccionesPrestamo extends TableCell<FilaPrestamo, FilaPrestamo> {

    private final MenuButton menuDeAcciones = new MenuButton("\u22EF");

    public CeldaAccionesPrestamo(Consumer<FilaPrestamo> p_alRegistrarDevolucion) {
        MenuItem opcionDevolver = new MenuItem("Registrar devolución");
        opcionDevolver.setOnAction(p_evento -> p_alRegistrarDevolucion.accept(getItem()));
        this.menuDeAcciones.getItems().add(opcionDevolver);
        this.menuDeAcciones.getStyleClass().add("boton-acciones");
    }

    @Override
    protected void updateItem(FilaPrestamo p_fila, boolean p_vacia) {
        super.updateItem(p_fila, p_vacia);
        setGraphic(p_vacia || p_fila == null ? null : this.menuDeAcciones);
    }
}
