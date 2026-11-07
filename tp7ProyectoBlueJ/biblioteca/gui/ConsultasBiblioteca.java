package biblioteca.gui;

import biblioteca.SesionBiblioteca;
import biblioteca.Libro;
import biblioteca.Prestamo;
import biblioteca.Socio;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lecturas de solo-consulta sobre la sesion. No modifica nada.
 *
 * Puerto del ConsultasBiblioteca de la compañera, adaptado a la
 * SesionBiblioteca de este proyecto (estado via getBiblioteca()).
 */
public class ConsultasBiblioteca {

    private final SesionBiblioteca sesionBiblioteca;

    public ConsultasBiblioteca(SesionBiblioteca p_sesionBiblioteca) {
        this.sesionBiblioteca = p_sesionBiblioteca;
    }

    public List<Socio> listarSocios() {
        return new ArrayList<Socio>(this.sesionBiblioteca.getBiblioteca().getSocios());
    }

    public List<Libro> listarLibros() {
        return new ArrayList<Libro>(this.sesionBiblioteca.getBiblioteca().getLibros());
    }

    public List<Libro> listarLibrosDisponibles() {
        ArrayList<Libro> disponibles = new ArrayList<>();
        for (Libro unLibro : this.listarLibros()) {
            if (!unLibro.prestado()) {
                disponibles.add(unLibro);
            }
        }
        return disponibles;
    }

    /** Prestamos sin devolver: el prestamo activo de cada ejemplar prestado. */
    public List<Prestamo> listarPrestamosActivos() {
        ArrayList<Prestamo> activos = new ArrayList<>();
        for (Libro unLibro : this.listarLibros()) {
            if (unLibro.prestado()) {
                activos.add(unLibro.ultimoPrestamo());
            }
        }
        return activos;
    }

    public Optional<Socio> buscarSocioPorDni(int p_dni) {
        for (Socio unSocio : this.listarSocios()) {
            if (unSocio.getDniSocio() == p_dni) {
                return Optional.of(unSocio);
            }
        }
        return Optional.empty();
    }

    /** La GUI se entera de cualquier cambio del estado (observable de la sesion). */
    public void alCambiarLosDatos(Runnable p_accion) {
        this.sesionBiblioteca.suscribir(p_accion);
    }
}
