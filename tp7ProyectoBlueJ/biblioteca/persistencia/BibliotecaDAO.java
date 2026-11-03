package biblioteca.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import biblioteca.Biblioteca;
import biblioteca.Libro;
import biblioteca.Prestamo;
import biblioteca.Socio;

/**
 * Fachada de persistencia del agregado raiz Biblioteca.
 *
 * - guardar(): sincroniza el grafo de objetos (biblioteca/libros/socios/prestamos)
 *   contra la BD. Inserta lo que no tiene id persistente y actualiza el resto.
 * - cargar(): reconstruye el grafo completo desde la BD, con las colecciones
 *   de prestamos repuestas en libro y socio (listo para operar el negocio).
 */
public class BibliotecaDAO {

    private final ConexionBD conexion;
    private final LibroDAO libroDAO;
    private final SocioDAO socioDAO;
    private final PrestamoDAO prestamoDAO;

    public BibliotecaDAO(ConexionBD p_conexion) {
        this.conexion = p_conexion;
        this.libroDAO = new LibroDAO(p_conexion);
        this.socioDAO = new SocioDAO(p_conexion);
        this.prestamoDAO = new PrestamoDAO(p_conexion);
    }

    public LibroDAO getLibroDAO() { return this.libroDAO; }

    public SocioDAO getSocioDAO() { return this.socioDAO; }

    public PrestamoDAO getPrestamoDAO() { return this.prestamoDAO; }

    // -------------------------------------------------------------------------
    // Sincronizacion (upsert por identidad persistente)
    // -------------------------------------------------------------------------
    public void guardar(Biblioteca p_biblioteca) {
        this.guardarBiblioteca(p_biblioteca);

        // 1) libros: los prestamos necesitan el id del libro, van primero.
        for (Libro unLibro : p_biblioteca.getLibros()) {
            if (unLibro.getId() == -1) {
                this.libroDAO.insert(unLibro);
            } else {
                this.libroDAO.update(unLibro);
            }
        }

        // 2) socios: los prestamos necesitan el dni.
        for (Socio unSocio : p_biblioteca.getSocios()) {
            if (this.socioDAO.findByDni(unSocio.getDniSocio()) == null) {
                this.socioDAO.insert(unSocio);
            } else {
                this.socioDAO.update(unSocio); // dias de prestamo pueden variar (premio docente)
            }
        }

        // 3) prestamos: al final, con socio y libro ya persistidos.
        for (Libro unLibro : p_biblioteca.getLibros()) {
            for (Prestamo unPrestamo : unLibro.getPrestamos()) {
                if (unPrestamo.getId() == -1) {
                    this.prestamoDAO.insert(unPrestamo);
                } else {
                    this.prestamoDAO.update(unPrestamo);
                }
            }
        }
    }

    // -------------------------------------------------------------------------
    // Reconstruccion del agregado
    // -------------------------------------------------------------------------
    public Biblioteca cargar(String p_nombre) {
        if (!this.existeBiblioteca(p_nombre)) {
            Biblioteca nueva = new Biblioteca(p_nombre);
            this.guardarBiblioteca(nueva);
            return nueva; // primera ejecucion: se puede operar y guardar normalmente
        }

        ArrayList<Libro> libros = this.libroDAO.findAll();
        ArrayList<Socio> socios = this.socioDAO.findAll();
        this.prestamoDAO.findAll(libros, socios); // repone prestamos en fichas de libro y socio

        Biblioteca biblioteca = new Biblioteca(p_nombre, libros, socios); // constructor con colecciones
        return biblioteca;
    }

    // -------------------------------------------------------------------------
    // Tabla biblioteca
    // -------------------------------------------------------------------------
    /** Crea el registro de la biblioteca si todavia no existe (idempotente). */
    public boolean existeBiblioteca(String p_nombre) {
        String sql = "SELECT nombre FROM biblioteca WHERE nombre = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setString(1, p_nombre);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo verificar la biblioteca " + p_nombre, e);
        }
    }

    private void guardarBiblioteca(Biblioteca p_biblioteca) {
        String sql = "INSERT OR IGNORE INTO biblioteca (nombre) VALUES (?)";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setString(1, p_biblioteca.getNombre());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo registrar la biblioteca " + p_biblioteca.getNombre(), e);
        }
    }
}
