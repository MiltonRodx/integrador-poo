package MiBiblioteca.gui;

import MiBiblioteca.BibliotecaDAO;
import MiBiblioteca.ConexionBD;
import MiBiblioteca.Libro;
import MiBiblioteca.Socio;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Dueña del estado en memoria (socios y libros con sus prestamos) y de la
 * conexion a la base. Avisa a los suscriptores cuando algo cambia, asi las
 * pantallas se refrescan sin conocerse entre si.
 */
public class SesionBiblioteca {

    private final ConexionBD conexionBD;
    private final BibliotecaDAO bibliotecaDAO;
    private final ArrayList<Socio> socios;
    private final ArrayList<Libro> libros;
    private final ArrayList<Runnable> suscriptores = new ArrayList<>();

    public SesionBiblioteca(String p_rutaBaseDatos) throws SQLException, ParseException {
        this.conexionBD = new ConexionBD(p_rutaBaseDatos);
        this.bibliotecaDAO = new BibliotecaDAO(this.conexionBD);
        this.bibliotecaDAO.crearTablas();

        this.libros = this.bibliotecaDAO.getLibroDAO().obtenerTodos();
        this.socios = this.bibliotecaDAO.getSocioDAO().obtenerTodos();
        this.bibliotecaDAO.getPrestamoDAO().cargarPrestamos(this.socios, this.libros);
    }

    public List<Socio> getSocios() {
        return Collections.unmodifiableList(this.socios);
    }

    public List<Libro> getLibros() {
        return Collections.unmodifiableList(this.libros);
    }

    public BibliotecaDAO getBibliotecaDAO() {
        return this.bibliotecaDAO;
    }

    public void suscribir(Runnable p_accionAlCambiar) {
        this.suscriptores.add(p_accionAlCambiar);
    }

    public void notificarCambio() {
        for (Runnable unSuscriptor : this.suscriptores) {
            unSuscriptor.run();
        }
    }

    public void cerrar() {
        try {
            this.conexionBD.cerrar();
        } catch (SQLException e) {
            System.err.println("No se pudo cerrar la base: " + e.getMessage());
        }
    }
}
