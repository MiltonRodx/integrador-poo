package MiBiblioteca.gui;

import MiBiblioteca.Libro;
import MiBiblioteca.Prestamo;
import MiBiblioteca.Socio;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Operaciones que MODIFICAN prestamos: registrar un retiro, registrar una
 * devolucion y armar las filas de la tabla. Persiste primero en la base y
 * recien despues actualiza la memoria, asi no quedan desincronizadas.
 */
public class ServicioPrestamos {

    private final SesionBiblioteca sesionBiblioteca;
    private final ConsultasBiblioteca consultas;
    private final ConversorDeFechas conversorDeFechas;
    private final ReglasDePlazo reglasDePlazo;

    public ServicioPrestamos(SesionBiblioteca p_sesionBiblioteca, ConsultasBiblioteca p_consultas,
                             ConversorDeFechas p_conversorDeFechas, ReglasDePlazo p_reglasDePlazo) {
        this.sesionBiblioteca = p_sesionBiblioteca;
        this.consultas = p_consultas;
        this.conversorDeFechas = p_conversorDeFechas;
        this.reglasDePlazo = p_reglasDePlazo;
    }

    public LocalDate calcularFechaDeVencimiento(Socio p_socio, LocalDate p_fechaRetiro) {
        return this.reglasDePlazo.calcularFechaDeVencimiento(p_fechaRetiro, p_socio);
    }

    public void registrarPrestamo(Socio p_socio, Libro p_libro, LocalDate p_fechaRetiro)
            throws PrestamoRechazadoException {
        if (p_libro.prestado()) {
            throw new PrestamoRechazadoException(
                "El libro \"" + p_libro.getTitulo() + "\" ya está prestado.");
        }
        if (!p_socio.puedePedir()) {
            throw new PrestamoRechazadoException(
                p_socio.getNombre() + " no puede pedir libros: tiene préstamos vencidos "
                + "o alcanzó su tope de libros en su poder.");
        }

        Prestamo nuevoPrestamo = new Prestamo(
            this.conversorDeFechas.aCalendar(p_fechaRetiro), null, p_socio, p_libro);
        try {
            this.sesionBiblioteca.getBibliotecaDAO().getPrestamoDAO().agregar(nuevoPrestamo);
        } catch (SQLException e) {
            throw new ErrorDePersistenciaException("No se pudo guardar el préstamo.", e);
        }
        p_socio.agregarPrestamo(nuevoPrestamo);
        p_libro.agregarPrestamo(nuevoPrestamo);
        this.sesionBiblioteca.notificarCambio();
    }

    public void registrarDevolucion(Prestamo p_prestamo) {
        if (p_prestamo.estaDevuelto()) {
            return;
        }
        p_prestamo.registrarFechaDevolucion(Calendar.getInstance());
        try {
            this.sesionBiblioteca.getBibliotecaDAO().getPrestamoDAO().actualizarDevolucion(p_prestamo);
        } catch (SQLException e) {
            p_prestamo.registrarFechaDevolucion(null);   // deshago el cambio en memoria
            throw new ErrorDePersistenciaException("No se pudo guardar la devolución.", e);
        }
        this.sesionBiblioteca.notificarCambio();
    }

    public List<FilaPrestamo> listarFilasDePrestamosActivos() {
        LocalDate hoy = LocalDate.now();
        ArrayList<FilaPrestamo> filas = new ArrayList<>();
        for (Prestamo unPrestamo : this.consultas.listarPrestamosActivos()) {
            filas.add(new FilaPrestamo(
                unPrestamo,
                unPrestamo.getSocio().getNombre(),
                unPrestamo.getLibro().getTitulo(),
                this.conversorDeFechas.aTextoCorto(
                    this.conversorDeFechas.aLocalDate(unPrestamo.getFechaRetiro())),
                this.conversorDeFechas.aTextoCorto(
                    this.reglasDePlazo.calcularFechaDeVencimiento(unPrestamo)),
                this.reglasDePlazo.clasificar(unPrestamo, hoy)));
        }
        return filas;
    }
}
