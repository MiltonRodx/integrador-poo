package MiBiblioteca.gui;

import MiBiblioteca.Prestamo;
import MiBiblioteca.Socio;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Todo lo relacionado con plazos: fecha de vencimiento (retiro + dias del
 * socio) y clasificacion En plazo / Por vencer / Vencido / Devuelto.
 */
public class ReglasDePlazo {

    private final ConversorDeFechas conversorDeFechas;
    private final int diasDeAvisoPorVencer = 2;

    public ReglasDePlazo(ConversorDeFechas p_conversorDeFechas) {
        this.conversorDeFechas = p_conversorDeFechas;
    }

    public LocalDate calcularFechaDeVencimiento(LocalDate p_fechaRetiro, Socio p_socio) {
        return p_fechaRetiro.plusDays(p_socio.getDiasPrestamo());
    }

    public LocalDate calcularFechaDeVencimiento(Prestamo p_prestamo) {
        LocalDate fechaRetiro = this.conversorDeFechas.aLocalDate(p_prestamo.getFechaRetiro());
        return this.calcularFechaDeVencimiento(fechaRetiro, p_prestamo.getSocio());
    }

    public EstadoPrestamo clasificar(Prestamo p_prestamo, LocalDate p_hoy) {
        if (p_prestamo.estaDevuelto()) {
            return EstadoPrestamo.DEVUELTO;
        }
        LocalDate vencimiento = this.calcularFechaDeVencimiento(p_prestamo);
        if (p_hoy.isAfter(vencimiento)) {
            return EstadoPrestamo.VENCIDO;
        }
        if (ChronoUnit.DAYS.between(p_hoy, vencimiento) <= this.diasDeAvisoPorVencer) {
            return EstadoPrestamo.POR_VENCER;
        }
        return EstadoPrestamo.EN_PLAZO;
    }
}
