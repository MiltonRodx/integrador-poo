package biblioteca.swing;

import biblioteca.Docente;
import biblioteca.Estudiante;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Calcula los numeros del panel de estadisticas a partir de las consultas.
 * Vencido se evalua con las ReglasDePlazo (consistente con la pantalla de
 * prestamos del mismo dia).
 */
public class ServicioEstadisticas {

    private final ConsultasBiblioteca consultas;
    private final ReglasDePlazo reglasDePlazo;

    public ServicioEstadisticas(ConsultasBiblioteca p_consultas,
                                ReglasDePlazo p_reglasDePlazo) {
        this.consultas = p_consultas;
        this.reglasDePlazo = p_reglasDePlazo;
    }

    public EstadisticasBiblioteca calcular() {
        int docentes = 0;
        int estudiantes = 0;
        for (var unSocio : this.consultas.listarSocios()) {
            if (unSocio instanceof Docente) docentes++;
            if (unSocio instanceof Estudiante) estudiantes++;
        }

        int activos = 0;
        int vencidos = 0;
        Calendar hoy = hoyMedianoche();
        for (var unPrestamo : this.consultas.listarPrestamosActivos()) {
            activos++;
            if (unPrestamo.vencido(hoy)) {
                vencidos++;
            }
        }

        int disponibles = 0;
        for (var unLibro : this.consultas.listarLibros()) {
            if (!unLibro.prestado()) disponibles++;
        }

        return new EstadisticasBiblioteca(
            this.consultas.listarLibros().size(),
            disponibles,
            this.consultas.listarSocios().size(),
            docentes,
            estudiantes,
            activos,
            vencidos);
    }

    /** Hoy, normalizado a medianoche (misma regla que el dominio). */
    private GregorianCalendar hoyMedianoche() {
        GregorianCalendar fecha = new GregorianCalendar();
        fecha.set(GregorianCalendar.HOUR_OF_DAY, 0);
        fecha.set(GregorianCalendar.MINUTE, 0);
        fecha.set(GregorianCalendar.SECOND, 0);
        fecha.set(GregorianCalendar.MILLISECOND, 0);
        return fecha;
    }
}
