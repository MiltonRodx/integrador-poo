package biblioteca.gui;

import biblioteca.Estudiante;
import biblioteca.Libro;
import biblioteca.Prestamo;
import biblioteca.SesionBiblioteca;
import biblioteca.Socio;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Operaciones que MODIFICAN prestamos: registrar un retiro y una devolucion.
 *
 * Puerto de la idea central de la compañera: la PERSISTENCIA MANDA. La
 * memoria se cambia junto con la base y, si el guardado falla, se deshace el
 * cambio en memoria (aqui via sesion.recargar(), que relee la base y deja el
 * estado coherente) para que nunca existan estados desincronizados.
 *
 * Las reglas de negocio siguen siendo del dominio: este servicio solo las
 * traduce a mensajes precisos para la GUI (mismo orden de revisiones que
 * Biblioteca.prestarLibro()).
 */
public class ServicioPrestamos {

    private final SesionBiblioteca sesionBiblioteca;
    private final ConversorDeFechas conversorDeFechas;
    private final ReglasDePlazo reglasDePlazo;

    public ServicioPrestamos(SesionBiblioteca p_sesionBiblioteca,
                             ConversorDeFechas p_conversorDeFechas,
                             ReglasDePlazo p_reglasDePlazo) {
        this.sesionBiblioteca = p_sesionBiblioteca;
        this.conversorDeFechas = p_conversorDeFechas;
        this.reglasDePlazo = p_reglasDePlazo;
    }

    public LocalDate calcularFechaDeVencimiento(Socio p_socio, LocalDate p_fechaRetiro) {
        return this.reglasDePlazo.calcularFechaDeVencimiento(p_fechaRetiro, p_socio);
    }

    /**
     * Registra un retiro. Rechaza (PrestamoRechazadoException) con el motivo
     * exacto: libro ya prestado, prestamos vencidos, tope de 3 del estudiante.
     */
    public void registrarPrestamo(Socio p_socio, Libro p_libro, LocalDate p_fechaRetiro)
            throws PrestamoRechazadoException {
        if (p_socio == null || p_libro == null || p_fechaRetiro == null) {
            throw new PrestamoRechazadoException("Faltan datos del prestamo.");
        }
        if (p_libro.prestado()) {
            throw new PrestamoRechazadoException(
                "El libro \"" + p_libro.getTitulo() + "\" ya está prestado.");
        }
        if (!p_socio.puedePedir()) {
            if (p_socio instanceof Estudiante && p_socio.cantLibrosPrestados() >= 3) {
                throw new PrestamoRechazadoException(p_socio.getNombre()
                    + " ya tiene 3 libros en su poder (tope para estudiantes).");
            }
            throw new PrestamoRechazadoException(p_socio.getNombre()
                + " tiene préstamos vencidos: no puede pedir hasta devolverlos.");
        }

        Prestamo nuevoPrestamo = new Prestamo(
            this.conversorDeFechas.aCalendar(p_fechaRetiro), p_socio, p_libro);
        p_socio.agregarPrestamo(nuevoPrestamo);
        p_libro.agregarPrestamo(nuevoPrestamo);
        try {
            this.sesionBiblioteca.guardar();
        } catch (RuntimeException e) {
            this.sesionBiblioteca.recargar();   // deshago el cambio en memoria
            throw new ErrorDePersistenciaException("No se pudo guardar el préstamo.", e);
        }
        this.sesionBiblioteca.publicar();
    }

    /** Registra la devolucion de HOY del prestamo indicado (si sigue activo). */
    public void registrarDevolucion(Prestamo p_prestamo) {
        if (p_prestamo == null || p_prestamo.getFechaDevolucion() != null) {
            return;
        }
        p_prestamo.registrarFechaDevolucion(this.hoyMedianoche());
        try {
            this.sesionBiblioteca.guardar();
        } catch (RuntimeException e) {
            this.sesionBiblioteca.recargar();   // deshago el cambio en memoria
            throw new ErrorDePersistenciaException("No se pudo guardar la devolución.", e);
        }
        this.sesionBiblioteca.publicar();
    }

    /** Fecha de vencimiento del prestamo (retiro + dias del socio). */
    public LocalDate vencimientoDe(Prestamo p_prestamo) {
        return this.reglasDePlazo.calcularFechaDeVencimiento(p_prestamo);
    }

    /** Clasificación visible del prestamo a la fecha de HOY. */
    public EstadoPrestamo estadoDe(Prestamo p_prestamo) {
        return this.reglasDePlazo.clasificar(p_prestamo, LocalDate.now());
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
