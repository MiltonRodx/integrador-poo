package biblioteca.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import biblioteca.Libro;

/**
 * CRUD sobre la tabla libro. Nunca imprime: devuelve colecciones / objetos,
 * la GUI (o quien invoque) decide que mostrar.
 */
public class LibroDAO {

    private final ConexionBD conexion;

    public LibroDAO(ConexionBD p_conexion) {
        this.conexion = p_conexion;
    }

    // -------------------------
    // CREATE
    // -------------------------
    public void insert(Libro p_libro) {
        String sql = "INSERT INTO libro (titulo, edicion, editorial, anio) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p_libro.getTitulo());
            ps.setInt(2, p_libro.getEdicion());
            ps.setString(3, p_libro.getEditorial());
            ps.setInt(4, p_libro.getAnio());
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    p_libro.setId(claves.getInt(1)); // identidad persistente devuelta por SQLite
                }
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo insertar el libro: " + p_libro.getTitulo(), e);
        }
    }

    // -------------------------
    // READ
    // -------------------------
    public Libro findById(int p_id) {
        String sql = "SELECT * FROM libro WHERE id = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setInt(1, p_id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? this.mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el libro id=" + p_id, e);
        }
    }

    public ArrayList<Libro> findAll() {
        String sql = "SELECT * FROM libro ORDER BY id";
        ArrayList<Libro> libros = new ArrayList<>();
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                libros.add(this.mapear(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los libros", e);
        }
        return libros;
    }

    // -------------------------
    // UPDATE
    // -------------------------
    public void update(Libro p_libro) {
        String sql = "UPDATE libro SET titulo = ?, edicion = ?, editorial = ?, anio = ? WHERE id = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setString(1, p_libro.getTitulo());
            ps.setInt(2, p_libro.getEdicion());
            ps.setString(3, p_libro.getEditorial());
            ps.setInt(4, p_libro.getAnio());
            ps.setInt(5, p_libro.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar el libro id=" + p_libro.getId(), e);
        }
    }

    // -------------------------
    // DELETE
    // -------------------------
    public void delete(Libro p_libro) {
        String sql = "DELETE FROM libro WHERE id = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setInt(1, p_libro.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar el libro id=" + p_libro.getId(), e);
        }
    }

    public void deleteAll() {
        try (Statement stmt = this.conexion.get().createStatement()) {
            stmt.executeUpdate("DELETE FROM libro");
        } catch (SQLException e) {
            throw new DAOException("No se pudo vaciar la tabla libro", e);
        }
    }

    /** Traduce un renglon del ResultSet al objeto de dominio. */
    private Libro mapear(ResultSet p_rs) throws SQLException {
        Libro libro = new Libro(p_rs.getString("titulo"), p_rs.getInt("edicion"),
                p_rs.getString("editorial"), p_rs.getInt("anio"));
        libro.setId(p_rs.getInt("id"));
        return libro;
    }
}
