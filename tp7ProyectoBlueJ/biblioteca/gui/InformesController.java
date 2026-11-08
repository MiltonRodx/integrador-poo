package biblioteca.gui;

import biblioteca.SesionBiblioteca;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.text.Font;

/**
 * Pestaña Informes: los listados de la consigna con sus FORMATOS EXACTOS
 * (formato1/2/3) en un TextArea monoespaciado, titulos sin repetir, prestamos
 * vencidos y contadores por tipo de socio.
 */
public class InformesController {

    private final SesionBiblioteca sesion;

    @FXML private TextArea areaInforme;
    @FXML private Label lblEstudiantes;
    @FXML private Label lblDocentes;
    @FXML private Label lblVencidos;

    /** Sesion compartida (misma base que consola), inyectada por App.start(). */
    public InformesController(SesionBiblioteca p_sesion) { this.sesion = p_sesion; }

    /**
     * Cableado JavaFX automatico: letra monoespaciada para respetar las
     * columnas de los formatos de la consigna, suscripcion al estado de la
     * sesion y primer refresco de contadores.
     */
    @FXML
    private void initialize() {
        // monoespaciada: los formatos de la consigna son de consola (columnas alineadas)
        this.areaInforme.setFont(Font.font("Monospaced", 13));
        this.sesion.suscribir(this::actualizar);
        actualizar();
    }

    /** Contadores vivos (se refrescan con cada cambio del estado). */
    public void actualizar() {
        biblioteca.Biblioteca b = this.sesion.getBiblioteca();
        this.lblEstudiantes.setText(String.valueOf(b.cantidadDeSociosPorTipo("Estudiante")));
        this.lblDocentes.setText(String.valueOf(b.cantidadDeSociosPorTipo("Docente")));
        this.lblVencidos.setText(String.valueOf(b.prestamosVencidos().size()));
    }

    /** Informe formato1: socios con totales por tipo. */
    @FXML private void informeSocios()      { mostrar(this.sesion.getBiblioteca().listaDeSocios()); }

    /** Informe formato2: ejemplares con estado de prestamo. */
    @FXML private void informeLibros()      { mostrar(this.sesion.getBiblioteca().listaDeLibros()); }

    /** Titulos sin repetir (los ejemplares si pueden duplicarse). */
    @FXML private void informeTitulos()     { mostrar(this.sesion.getBiblioteca().listaDeTitulos()); }

    /** Informe formato3: docentes responsables de hoy. */
    @FXML private void informeResponsables(){ mostrar(this.sesion.getBiblioteca().listaDeDocentesResponsables()); }

    /** Informe de vencidos activos: encabezado con la cantidad + cada prestamo. */
    @FXML
    private void informeVencidos() {
        StringBuilder informe = new StringBuilder("Prestamos vencidos (sin devolver) a la fecha: ")
            .append(this.sesion.getBiblioteca().prestamosVencidos().size()).append("\n\n");
        for (Object unPrestamo : this.sesion.getBiblioteca().prestamosVencidos()) {
            informe.append("--------------------------------------------------\n")
                .append(unPrestamo).append("\n");
        }
        mostrar(informe.toString());
    }

    /** Volca el informe elegido en el area de texto central. */
    private void mostrar(String p_texto) {
        this.areaInforme.setText(p_texto);
    }
}
