package biblioteca.gui;

import biblioteca.Prestamo;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;

/** Celda que dibuja el estado del prestamo como una etiqueta de color suave. */
public class CeldaEstadoPrestamo extends TableCell<Prestamo, EstadoPrestamo> {

    private final Label etiquetaDeEstado = new Label();

    @Override
    protected void updateItem(EstadoPrestamo p_estado, boolean p_vacia) {
        super.updateItem(p_estado, p_vacia);
        if (p_vacia || p_estado == null) {
            setGraphic(null);
            return;
        }
        this.etiquetaDeEstado.setText(p_estado.getEtiqueta());
        this.etiquetaDeEstado.getStyleClass().setAll("pildora-estado", p_estado.getClaseCss());
        setGraphic(this.etiquetaDeEstado);
    }
}
