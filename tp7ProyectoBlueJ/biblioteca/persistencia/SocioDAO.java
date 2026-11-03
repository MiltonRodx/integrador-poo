package biblioteca.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import biblioteca.Docente;
import biblioteca.Estudiante;
import biblioteca.Socio;

/**
 * CRUD sobre la tabla socio. Docentes y estudiantes comparten tabla con un
 * discriminador (columna tipo): patron "tabla plana con flag de tipo".
 * Al leer, el tipo decide que subclase reconstruir.
 */
public class SocioDAO {

    private final ConexionBD conexion;

    public SocioDAO(ConexionBD p_conexion) {
        this.conexion = p_conexion;
    }

    // -------------------------
    // CREATE
    // -------------------------
    public void insert(Socio p_socio) {
        String sql = "INSERT INTO socio (dni, nombre, tipo, dias_prestamos, area, carrera)"
                + " VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setInt(1, p_socio.getDniSocio());
            ps.setString(2, p_socio.getNombre());
            ps.setString(3, p_socio.soyDeLaClase()); // discriminador
            ps.setInt(4, p_socio.getDiasPrestamos());
            ps.setString(5, (p_socio instanceof Docente) ? ((Docente) p_socio).getArea() : null);
            ps.setString(6, (p_socio instanceof Estudiante) ? ((Estudiante) p_socio).getCarrera() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo insertar el socio dni=" + p_socio.getDniSocio(), e);
        }
    }

    // -------------------------
    // READ
    // -------------------------
    public Socio findByDni(int p_dni) {
        String sql = "SELECT * FROM socio WHERE dni = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setInt(1, p_dni);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? this.mapear(rs) : null;
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo buscar el socio dni=" + p_dni, e);
        }
    }

    public ArrayList<Socio> findAll() {
        String sql = "SELECT * FROM socio ORDER BY dni";
        ArrayList<Socio> socios = new ArrayList<>();
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                socios.add(this.mapear(rs));
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los socios", e);
        }
        return socios;
    }

    // -------------------------
    // UPDATE
    // -------------------------
    public void update(Socio p_socio) {
        // dias_prestamos puede cambiar (premio al docente responsable), area/carrera no.
        String sql = "UPDATE socio SET nombre = ?, tipo = ?, dias_prestamos = ?, area = ?, carrera = ?"
                + " WHERE dni = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setString(1, p_socio.getNombre());
            ps.setString(2, p_socio.soyDeLaClase());
            ps.setInt(3, p_socio.getDiasPrestamos());
            ps.setString(4, (p_socio instanceof Docente) ? ((Docente) p_socio).getArea() : null);
            ps.setString(5, (p_socio instanceof Estudiante) ? ((Estudiante) p_socio).getCarrera() : null);
            ps.setInt(6, p_socio.getDniSocio());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar el socio dni=" + p_socio.getDniSocio(), e);
        }
    }

    // -------------------------
    // DELETE
    // -------------------------
    public void delete(Socio p_socio) {
        // FK prestamo.socio_dni bloquea el borrado si el socio tiene prestamos asociados.
        String sql = "DELETE FROM socio WHERE dni = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setInt(1, p_socio.getDniSocio());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar el socio dni=" + p_socio.getDniSocio(), e);
        }
    }

    public void deleteAll() {
        try (Statement stmt = this.conexion.get().createStatement()) {
            stmt.executeUpdate("DELETE FROM socio");
        } catch (SQLException e) {
            throw new DAOException("No se pudo vaciar la tabla socio", e);
        }
    }

    /** Reconstruye la subclase correspondiente segun el discriminador tipo. */
    private Socio mapear(ResultSet p_rs) throws SQLException {
        String tipo = p_rs.getString("tipo");
        int dni = p_rs.getInt("dni");
        String nombre = p_rs.getString("nombre");
        int dias = p_rs.getInt("dias_prestamos");

        if ("Docente".equalsIgnoreCase(tipo)) {
            return new Docente(dni, nombre, dias, p_rs.getString("area"));
        }
        if ("Estudiante".equalsIgnoreCase(tipo)) {
            return new Estudiante(dni, nombre, dias, p_rs.getString("carrera"));
        }
        throw new DAOException("Tipo de socio desconocido en la BD: " + tipo);
    }
}
