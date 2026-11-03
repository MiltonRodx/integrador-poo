package biblioteca.persistencia;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import biblioteca.Libro;
import biblioteca.Prestamo;
import biblioteca.Socio;

/**
 * CRUD sobre la tabla prestamo. Un Prestamo referencia dos agregados:
 * socio (dni natural) y libro (id surrogate). Al reconstruir desde la BD
 * los prestamos se "pegan" de vuelta a las fichas del libro y del socio,
 * tal como hace Biblioteca.prestarLibro en vivo.
 */
public class PrestamoDAO {

    private final ConexionBD conexion;

    public PrestamoDAO(ConexionBD p_conexion) {
        this.conexion = p_conexion;
    }

    // -------------------------
    // CREATE
    // -------------------------
    public void insert(Prestamo p_prestamo) {
        String sql = "INSERT INTO prestamo (socio_dni, libro_id, fecha_retiro, fecha_devolucion)"
                + " VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql,
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p_prestamo.getSocio().getDniSocio());
            ps.setInt(2, p_prestamo.getLibro().getId());
            ps.setLong(3, p_prestamo.getFechaRetiro().getTimeInMillis());
            if (p_prestamo.getFechaDevolucion() != null) {
                ps.setLong(4, p_prestamo.getFechaDevolucion().getTimeInMillis());
            } else {
                ps.setNull(4, java.sql.Types.INTEGER);
            }
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    p_prestamo.setId(claves.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo insertar el prestamo", e);
        }
    }

    // -------------------------
    // READ (solo filas crudas)
    // -------------------------
    public ArrayList<RegistroPrestamo> findAllRegistros() {
        String sql = "SELECT * FROM prestamo ORDER BY id";
        ArrayList<RegistroPrestamo> registros = new ArrayList<>();
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                registros.add(new RegistroPrestamo(rs.getInt("id"), rs.getInt("socio_dni"),
                        rs.getInt("libro_id"), rs.getLong("fecha_retiro"),
                        rs.getObject("fecha_devolucion") == null ? null : rs.getLong("fecha_devolucion")));
            }
        } catch (SQLException e) {
            throw new DAOException("No se pudo listar los prestamos", e);
        }
        return registros;
    }

    /**
     * Reconstruye los prestamos y los enlaza con los objetos ya cargados:
     * agregando el prestamo a la ficha del libro (getPrestamos) y a la del socio.
     *
     * @param p_libros libros ya cargados (necesita id persistente)
     * @param p_socios socios ya cargados (dni natural)
     */
    public ArrayList<Prestamo> findAll(ArrayList<Libro> p_libros, ArrayList<Socio> p_socios) {
        ArrayList<Prestamo> prestamos = new ArrayList<>();

        for (RegistroPrestamo registro : this.findAllRegistros()) {
            Libro libro = this.buscarLibro(p_libros, registro.libroId);
            Socio socio = this.buscarSocio(p_socios, registro.socioDni);
            if (libro == null || socio == null) {
                throw new DAOException("Registro de prestamo huerfano: "
                        + "socio_dni=" + registro.socioDni + " libro_id=" + registro.libroId);
            }

            Prestamo prestamo = new Prestamo(fechaDesde(registro.fechaRetiro), socio, libro);
            prestamo.setId(registro.id);
            if (registro.fechaDevolucion != null) {
                prestamo.registrarFechaDevolucion(fechaDesde(registro.fechaDevolucion));
            }

            // Repone la relacion de agregacion: ficha del libro y del socio.
            libro.agregarPrestamo(prestamo);
            socio.agregarPrestamo(prestamo);
            prestamos.add(prestamo);
        }
        return prestamos;
    }

    // -------------------------
    // UPDATE
    // -------------------------
    public void update(Prestamo p_prestamo) {
        // Un prestamo solo cambia en su fecha de devolucion (y a futuro sus claves son inmutables).
        String sql = "UPDATE prestamo SET fecha_devolucion = ? WHERE id = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            if (p_prestamo.getFechaDevolucion() != null) {
                ps.setLong(1, p_prestamo.getFechaDevolucion().getTimeInMillis());
            } else {
                ps.setNull(1, java.sql.Types.INTEGER);
            }
            ps.setInt(2, p_prestamo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo actualizar el prestamo id=" + p_prestamo.getId(), e);
        }
    }

    // -------------------------
    // DELETE
    // -------------------------
    public void delete(Prestamo p_prestamo) {
        String sql = "DELETE FROM prestamo WHERE id = ?";
        try (PreparedStatement ps = this.conexion.get().prepareStatement(sql)) {
            ps.setInt(1, p_prestamo.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DAOException("No se pudo eliminar el prestamo id=" + p_prestamo.getId(), e);
        }
    }

    public void deleteAll() {
        try (Statement stmt = this.conexion.get().createStatement()) {
            stmt.executeUpdate("DELETE FROM prestamo");
        } catch (SQLException e) {
            throw new DAOException("No se pudo vaciar la tabla prestamo", e);
        }
    }

    // -------------------------
    // Helpers internos
    // -------------------------
    private Libro buscarLibro(ArrayList<Libro> p_libros, int p_id) {
        for (Libro unLibro : p_libros) {
            if (unLibro.getId() == p_id) {
                return unLibro;
            }
        }
        return null;
    }

    private Socio buscarSocio(ArrayList<Socio> p_socios, int p_dni) {
        for (Socio unSocio : p_socios) {
            if (unSocio.getDniSocio() == p_dni) {
                return unSocio;
            }
        }
        return null;
    }

    /** Epoch millis -> Calendar a medianoche (mismo criterio de dia completo del dominio). */
    private Calendar fechaDesde(long p_epochMillis) {
        Calendar fecha = new GregorianCalendar();
        fecha.setTimeInMillis(p_epochMillis);
        fecha.set(Calendar.HOUR_OF_DAY, 0);
        fecha.set(Calendar.MINUTE, 0);
        fecha.set(Calendar.SECOND, 0);
        fecha.set(Calendar.MILLISECOND, 0);
        return fecha;
    }

    /**
     * Renglon crudo de la tabla prestamo: permite leer sin resolver
     * las referencias todavia (evita N+1 y duplicar el mapeo de socio/libro).
     */
    public record RegistroPrestamo(int id, int socioDni, int libroId,
                long fechaRetiro, Long fechaDevolucion) {
    }
}
