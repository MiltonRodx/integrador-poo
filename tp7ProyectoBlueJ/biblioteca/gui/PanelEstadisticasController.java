package biblioteca.gui;

import java.time.LocalDate;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;

/**
 * Panel de estadisticas: 4 tarjetas (libros / socios / activos / vencidos) y
 * un grafico circular del catalogo. Se refresca solo ante cualquier cambio
 * del estado de la sesion.
 */
public class PanelEstadisticasController {

    private final ServicioEstadisticas servicioEstadisticas;
    private final ConsultasBiblioteca consultas;
    private final ConversorDeFechas conversorDeFechas;

    @FXML private Label etiquetaFecha;
    @FXML private Label etiquetaLibros;
    @FXML private Label etiquetaLibrosDetalle;
    @FXML private Label etiquetaSocios;
    @FXML private Label etiquetaSociosDetalle;
    @FXML private Label etiquetaActivos;
    @FXML private Label etiquetaActivosDetalle;
    @FXML private Label etiquetaVencidos;
    @FXML private Label etiquetaVencidosDetalle;
    @FXML private Label etiquetaDisponibles;
    @FXML private Label etiquetaEnPrestamo;
    @FXML private PieChart graficoCatalogo;

    public PanelEstadisticasController(ServicioEstadisticas p_servicioEstadisticas,
                                       ConsultasBiblioteca p_consultas) {
        this.servicioEstadisticas = p_servicioEstadisticas;
        this.consultas = p_consultas;
        this.conversorDeFechas = new ConversorDeFechas();
    }

    @FXML
    private void initialize() {
        // El pie por defecto anima cada refresh y "parpadea"; lo apagamos.
        this.graficoCatalogo.setAnimated(false);
        this.graficoCatalogo.setData(FXCollections.observableArrayList(
            new PieChart.Data("Disponibles", 0),
            new PieChart.Data("En préstamo", 0)));

        this.consultas.alCambiarLosDatos(this::refrescar);
        this.refrescar();
    }

    private void refrescar() {
        EstadisticasBiblioteca est = this.servicioEstadisticas.calcular();

        this.etiquetaFecha.setText("Estado al " + this.conversorDeFechas.aTextoCorto(LocalDate.now()));

        this.etiquetaLibros.setText(String.valueOf(est.totalLibros()));
        this.etiquetaLibrosDetalle.setText(est.librosDisponibles() + " disponibles");

        this.etiquetaSocios.setText(String.valueOf(est.totalSocios()));
        this.etiquetaSociosDetalle.setText(
            est.sociosDocentes() + " docentes, " + est.sociosEstudiantes() + " estudiantes");

        this.etiquetaActivos.setText(String.valueOf(est.prestamosActivos()));
        this.etiquetaActivosDetalle.setText("con préstamo vigente");

        this.etiquetaVencidos.setText(String.valueOf(est.prestamosVencidos()));
        this.etiquetaVencidosDetalle.setText(est.prestamosVencidos() > 0
            ? "requieren devolución" : "todo en orden");

        this.etiquetaDisponibles.setText("Disponibles: " + est.librosDisponibles());
        this.etiquetaEnPrestamo.setText("En préstamo: " + (est.totalLibros() - est.librosDisponibles()));

        this.graficoCatalogo.getData().get(0).setPieValue(est.librosDisponibles());
        this.graficoCatalogo.getData().get(1).setPieValue(est.totalLibros() - est.librosDisponibles());
    }
}
