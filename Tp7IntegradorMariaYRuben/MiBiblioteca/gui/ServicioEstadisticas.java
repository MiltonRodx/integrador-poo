package MiBiblioteca.gui;

/** Calcula los numeros del panel de estadisticas a partir de las consultas. */
public class ServicioEstadisticas {

    private final ConsultasBiblioteca consultas;

    public ServicioEstadisticas(ConsultasBiblioteca p_consultas) {
        this.consultas = p_consultas;
    }

    public EstadisticasBiblioteca calcular() {
        return new EstadisticasBiblioteca(
            this.consultas.listarLibros().size(),
            this.consultas.listarSocios().size(),
            this.consultas.listarPrestamosActivos().size());
    }
}
