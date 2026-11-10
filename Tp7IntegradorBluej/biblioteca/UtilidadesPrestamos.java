package biblioteca;

import java.util.Calendar;
import java.sql.SQLException;

public class UtilidadesPrestamos
{
    public static Calendar calcularVencimiento(
        Prestamo p_prestamo,
        PrestamoDAO p_prestamoDAO) throws SQLException
    {
        int dias = p_prestamoDAO.obtenerDiasPrestamo(p_prestamo);

        Calendar vencimiento =
            (Calendar) p_prestamo.getFechaRetiro().clone();

        vencimiento.add(Calendar.DAY_OF_MONTH, dias);

        return vencimiento;
    }

    public static String formatearFecha(Calendar p_fecha)
    {
        int dia = p_fecha.get(Calendar.DAY_OF_MONTH);
        int mes = p_fecha.get(Calendar.MONTH) + 1;
        int anio = p_fecha.get(Calendar.YEAR);

        return String.format("%02d/%02d/%04d", dia, mes, anio);
    }
}