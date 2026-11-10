
package biblioteca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

public class PrestamoDAO {
    private ConexionBD conexionBD;
    private LibroDAO libroDAO;

    public PrestamoDAO(ConexionBD p_conexionBD, LibroDAO p_libroDAO)
    {
        this.setConexionBD(p_conexionBD);
        this.setLibroDAO(p_libroDAO);
    }

    private void setConexionBD(ConexionBD p_conexionBD)
    {
        this.conexionBD = p_conexionBD;
    }

    private void setLibroDAO(LibroDAO p_libroDAO)
    {
        this.libroDAO = p_libroDAO;
    }

    public ConexionBD getConexionBD()
    {
        return this.conexionBD;
    }

    public LibroDAO getLibroDAO()
    {
        return this.libroDAO;
    }

    public void agregar(Prestamo p_prestamo, int p_diasPrestamo)
        throws SQLException
    {
        // La base manda: el alta se ejecuta dentro de una transaccion.
        // Si el insert falla (por ejemplo, el indice unico rechaza un
        // prestamo duplicado), la transaccion ya hizo rollback y el
        // error sube para que la GUI deshaga el cambio en memoria.
        this.getConexionBD().enTransaccion(new TrabajoBD()
        {
            public void ejecutar(Connection p_conexion) throws SQLException
            {
                insertar(p_prestamo, p_diasPrestamo, p_conexion);
            }
        });
    }

    // Insert de un prestamo con la conexion de la transaccion.
    // NO cierra la conexion: el manejo lo hace ConexionBD.enTransaccion().
    private void insertar(Prestamo p_prestamo, int p_diasPrestamo,
                          Connection p_conexion)
        throws SQLException
    {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

        String fechaRetiro = formato.format(
            p_prestamo.getFechaRetiro().getTime()
        );

        String fechaDevolucion = null;

        if(p_prestamo.estaDevuelto()) {
            fechaDevolucion = formato.format(
                p_prestamo.getFechaDevolucion().getTime()
            );
        }

        String consulta = "INSERT INTO Prestamo " +
            "(fechaRetiro, fechaDevolucion, dniSocio, idLibro, diasPrestamo) " +
            "VALUES (?, ?, ?, ?, ?)";

        PreparedStatement sentencia = p_conexion.prepareStatement(consulta);

        sentencia.setString(1, fechaRetiro);
        sentencia.setString(2, fechaDevolucion);
        sentencia.setInt(3, p_prestamo.getSocio().getDniSocio());
        sentencia.setInt(4,
            this.getLibroDAO().obtenerIdentificador(p_prestamo.getLibro()));
        sentencia.setInt(5, p_diasPrestamo);

        sentencia.executeUpdate();
        sentencia.close();
    }

    public int obtenerDiasPrestamo(Prestamo p_prestamo)
        throws SQLException {
        Connection conexion = this.getConexionBD().conectar();

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

        String fechaRetiro = formato.format(
            p_prestamo.getFechaRetiro().getTime()
        );

        String consulta = "SELECT diasPrestamo FROM Prestamo " +
            "WHERE dniSocio = ? AND idLibro = ? AND fechaRetiro = ? " +
            "ORDER BY idPrestamo DESC LIMIT 1";

        PreparedStatement sentencia = conexion.prepareStatement(consulta);

        sentencia.setInt(1, p_prestamo.getSocio().getDniSocio());
        sentencia.setInt(2,
            this.getLibroDAO().obtenerIdentificador(p_prestamo.getLibro()));
        sentencia.setString(3, fechaRetiro);

        ResultSet resultado = sentencia.executeQuery();

        int dias = p_prestamo.getSocio().getDiasPrestamo();

        if(resultado.next())
        {
            dias = resultado.getInt("diasPrestamo");

            if(resultado.wasNull() || dias <= 0)
            {
                dias = p_prestamo.getSocio().getDiasPrestamo();
            }
        }

        resultado.close();
        sentencia.close();
        conexion.close();

        return dias;
    }

    // Devuelve la cantidad de filas actualizada. Si devuelve 0 el prestamo
    // no existe en la base como activo (ya devuelto o inexistente): quien
    // llamo debe deshacer el cambio en memoria para no desincronizarse.
    public int actualizarDevolucion(Prestamo p_prestamo)
        throws SQLException
    {
        final int[] filasActualizadas = new int[1];

        // Transaccion tambien para la devolucion: si el UPDATE falla,
        // la base queda como estaba y el error sube al llamador.
        this.getConexionBD().enTransaccion(new TrabajoBD()
        {
            public void ejecutar(Connection p_conexion) throws SQLException {
                filasActualizadas[0] =
                    actualizarDevolucion(p_prestamo, p_conexion);
            }
        });

        return filasActualizadas[0];
    }

    // UPDATE de la fecha de devolucion con la conexion de la transaccion.
    // NO cierra la conexion: el manejo lo hace ConexionBD.enTransaccion().
    private int actualizarDevolucion(Prestamo p_prestamo, Connection p_conexion)
        throws SQLException
    {
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

        String fechaDevolucion = formato.format(
            p_prestamo.getFechaDevolucion().getTime()
        );

        String fechaRetiro = formato.format(
            p_prestamo.getFechaRetiro().getTime()
        );

        String consulta = "UPDATE Prestamo SET fechaDevolucion = ? " +
            "WHERE dniSocio = ? AND idLibro = ? " +
            "AND fechaRetiro = ? AND fechaDevolucion IS NULL";

        PreparedStatement sentencia = p_conexion.prepareStatement(consulta);

        sentencia.setString(1, fechaDevolucion);
        sentencia.setInt(2, p_prestamo.getSocio().getDniSocio());
        sentencia.setInt(3,
            this.getLibroDAO().obtenerIdentificador(p_prestamo.getLibro()));
        sentencia.setString(4, fechaRetiro);

        int filas = sentencia.executeUpdate();

        sentencia.close();

        return filas;
    }

    public void cargarPrestamos(ArrayList<Socio> socios,
                                ArrayList<Libro> libros)
        throws SQLException, ParseException
    {
        Connection conexion = this.getConexionBD().conectar();

        PreparedStatement sentencia =
            conexion.prepareStatement("SELECT * FROM Prestamo");

        ResultSet resultado = sentencia.executeQuery();

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

        while(resultado.next()) {
            int dni = resultado.getInt("dniSocio");
            int idLibro = resultado.getInt("idLibro");

            Socio unSocio = null;
            Libro unLibro = null;

            for(Socio socio : socios) {
                if(socio.getDniSocio() == dni)
                    unSocio = socio;
            }

            for(Libro libro : libros) {
                if (this.getLibroDAO().obtenerIdentificador(libro) == idLibro) {
                    unLibro = libro;
                }
            }

            if(unSocio != null && unLibro != null)
            {
                Calendar fechaRetiro = Calendar.getInstance();

                fechaRetiro.setTime(
                    formato.parse(resultado.getString("fechaRetiro"))
                );

                Calendar fechaDevolucion = null;

                String fechaGuardada =
                    resultado.getString("fechaDevolucion");

                if(fechaGuardada != null)
                {
                    fechaDevolucion = Calendar.getInstance();

                    fechaDevolucion.setTime(
                        formato.parse(fechaGuardada)
                    );
                }

                // La consigna: el prestamo nace solo con fecha de retiro
                Prestamo unPrestamo = new Prestamo(
                    fechaRetiro,
                    unSocio,
                    unLibro
                );

                if(fechaGuardada != null)
                {
                    unPrestamo.registrarFechaDevolucion(fechaDevolucion);
                }

                unSocio.agregarPrestamo(unPrestamo);
                unLibro.agregarPrestamo(unPrestamo);
            }
        }
        
        resultado.close();
        sentencia.close();
        conexion.close();
    }
    
    
    public void agregar(Prestamo p_prestamo) throws SQLException {
        this.agregar(
            p_prestamo,
            p_prestamo.getSocio().getDiasPrestamo()
        );
   }

}
