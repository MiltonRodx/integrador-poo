package biblioteca.gui;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.GregorianCalendar;

/** Traduce entre Calendar (modelo) y LocalDate (pantalla) y da formato de texto. */
public class ConversorDeFechas {

    private final DateTimeFormatter formatoCorto = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public LocalDate aLocalDate(Calendar p_calendario) {
        return p_calendario.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /** Devuelve la fecha como Calendar a medianoche (misma regla del dominio). */
    public Calendar aCalendar(LocalDate p_fecha) {
        return GregorianCalendar.from(p_fecha.atStartOfDay(ZoneId.systemDefault()));
    }

    public String aTextoCorto(LocalDate p_fecha) {
        return p_fecha.format(this.formatoCorto);
    }
}
