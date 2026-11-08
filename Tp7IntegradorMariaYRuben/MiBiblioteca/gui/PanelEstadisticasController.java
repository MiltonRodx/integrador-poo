package MiBiblioteca.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

/** Las tres tarjetas de estadisticas; se refrescan solas ante cualquier cambio. */
public class PanelEstadisticasController {

    private final ServicioEstadisticas servicioEstadisticas;
    private final ConsultasBiblioteca consultas;

    @FXML private Label etiquetaTotalLibros;
    @FXML private Label etiquetaTotalSocios;
    @FXML private Label etiquetaPrestamosActivos;

    public PanelEstadisticasController(ServicioEstadisticas p_servicioEstadisticas,
                                       ConsultasBiblioteca p_consultas) {
        this.servicioEstadisticas = p_servicioEstadisticas;
        this.consultas = p_consultas;
    }

    @FXML
    private void initialize() {
        this.consultas.alCambiarLosDatos(this::refrescar);
        this.refrescar();
    }

    private void refrescar() {
        EstadisticasBiblioteca estadisticas = this.servicioEstadisticas.calcular();
        this.etiquetaTotalLibros.setText(String.valueOf(estadisticas.totalLibros()));
        this.etiquetaTotalSocios.setText(String.valueOf(estadisticas.totalSocios()));
        this.etiquetaPrestamosActivos.setText(String.valueOf(estadisticas.prestamosActivos()));
    }
}
