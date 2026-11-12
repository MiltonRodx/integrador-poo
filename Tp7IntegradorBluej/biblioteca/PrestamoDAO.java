package biblioteca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * =====================================================================
 * CLASE: PrestamoDAO (Data Access Object)
 * =====================================================================
 * Su propósito es centralizar todas las operaciones de base de datos 
 * relacionadas con la entidad "Prestamo" (Insertar, Consultar, Actualizar).
 * Separa la lógica de negocio de la lógica de persistencia.
 */
public class PrestamoDAO {
    
    // Atributos principales de la clase:
    private ConexionBD conexionBD; // Administra las conexiones a la Base de Datos y las transacciones.
    private LibroDAO libroDAO;     // Se usa para obtener el ID numérico de los libros.

    /**
     * Constructor: Recibe las dependencias necesarias para funcionar 
     * (Inyección de dependencias) y las asigna usando los setters privados.
     */
    public PrestamoDAO(ConexionBD p_conexionBD, LibroDAO p_libroDAO) {
        this.setConexionBD(p_conexionBD);
        this.setLibroDAO(p_libroDAO);
    }
    
    private void setConexionBD(ConexionBD p_conexionBD) { 
        this.conexionBD = p_conexionBD; 
    }

    private void setLibroDAO(LibroDAO p_libroDAO) { 
        this.libroDAO = p_libroDAO; 
    }

    public ConexionBD getConexionBD() { 
        return this.conexionBD; 
    }

    public LibroDAO getLibroDAO() { 
        return this.libroDAO; 
    }

    /**
     * =====================================================================
     * MÉTODO: agregar (con días de préstamo) - OPERACIÓN TRANSACCIONAL
     * =====================================================================
     * Para exponer: "Este método garantiza que el alta de un préstamo sea 
     * segura mediante una transacción. Usa la interfaz TrabajoBD para delegar 
     * la tarea a ConexionBD, la cual desactiva el autocommit."
     */
    public void agregar(Prestamo p_prestamo, int p_diasPrestamo)
        throws SQLException {
        
        // Llamamos al gestor de transacciones de ConexionBD pasándole 
        // una clase anónima que implementa la interfaz TrabajoBD.
        this.getConexionBD().enTransaccion(new TrabajoBD() {
            public void ejecutar(Connection p_conexion) throws SQLException {
                // Aquí se ejecuta el trabajo real inyectándole la conexión viva
                insertar(p_prestamo, p_diasPrestamo, p_conexion);
            }
        });
    }

    /**
     * =====================================================================
     * MÉTODO PRIVADO: insertar (El trabajo real de base de datos)
     * =====================================================================
     * Para exponer: "Este método hace el INSERT físico. Recibe la conexión 
     * de la transacción activa. MUY IMPORTANTE: No cierra la conexión, 
     * porque de eso se encarga exclusivamente ConexionBD.enTransaccion()."
     */
    private void insertar(Prestamo p_prestamo, int p_diasPrestamo,
                          Connection p_conexion)  throws SQLException {
        
        // Las bases de datos requieren las fechas en un formato estricto (yyyy-MM-dd)
        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

        String fechaRetiro = formato.format(
            p_prestamo.getFechaRetiro().getTime()
        );

        String fechaDevolucion = null;

        // Si el préstamo ya nació devuelto, formateamos esa fecha; sino queda en null
        if(p_prestamo.estaDevuelto()) {
            fechaDevolucion = formato.format(
                p_prestamo.getFechaDevolucion().getTime()
            );
        }

        // Definimos la consulta SQL utilizando placeholders (?) para evitar Inyección SQL
        String consulta = "INSERT INTO Prestamo " +
            "(fechaRetiro, fechaDevolucion, dniSocio, idLibro, diasPrestamo) " +
            "VALUES (?, ?, ?, ?, ?)";

        // PreparedStatement compila la consulta de forma segura
        PreparedStatement sentencia = p_conexion.prepareStatement(consulta);

        // Llenamos los signos de interrogación (?) ordenadamente
        sentencia.setString(1, fechaRetiro);
        sentencia.setString(2, fechaDevolucion);
        sentencia.setInt(3, p_prestamo.getSocio().getDniSocio());
        sentencia.setInt(4,
            this.getLibroDAO().obtenerIdentificador(p_prestamo.getLibro()));
        sentencia.setInt(5, p_diasPrestamo);

        // Ejecutamos la consulta (queda en memoria temporal de la transacción)
        sentencia.executeUpdate();
        
        // Cerramos la sentencia para liberar recursos (NUNCA cerramos la conexión aquí)
        sentencia.close();
    }

    /**
     * =====================================================================
     * MÉTODO: obtenerDiasPrestamo (Consulta de lectura)
     * =====================================================================
     * Para exponer: "Como esto es solo una consulta de lectura (SELECT), 
     * no requiere una transacción compleja. Abre su propia conexión directa, 
     * consulta y cierra todo al finalizar."
     */
    public int obtenerDiasPrestamo(Prestamo p_prestamo)
        throws SQLException {
        
        // Abre conexión directa (con autocommit en true por defecto)
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

        // Ejecutamos la consulta y obtenemos el resultado en un ResultSet
        ResultSet resultado = sentencia.executeQuery();

        int dias = p_prestamo.getSocio().getDiasPrestamo();

        // Si hay resultados, extraemos el valor numérico
        if(resultado.next())
        {
            dias = resultado.getInt("diasPrestamo");

            if(resultado.wasNull() || dias <= 0)
            {
                dias = p_prestamo.getSocio().getDiasPrestamo();
            }
        }

        // Cerramos recursos y conexión de lectura de forma ordenada
        resultado.close();
        sentencia.close();
        conexion.close();

        return dias;
    }

    /**
     * =====================================================================
     * MÉTODO: actualizarDevolucion (Wrapper transaccional)
     * =====================================================================
     * Para exponer: "Gestiona la actualización de la devolución usando 
     * otra vez la transacción. Si el UPDATE devuelve 0 filas, significa 
     * que el préstamo no existía como activo, y la app debe enterarse."
     */
    public int actualizarDevolucion(Prestamo p_prestamo)
        throws SQLException
    {
        final int[] filasActualizadas = new int[1];

        // Usamos la misma estructura de la interfaz TrabajoBD para la devolución
        this.getConexionBD().enTransaccion(new TrabajoBD()
        {
            public void ejecutar(Connection p_conexion) throws SQLException {
                filasActualizadas[0] =
                    actualizarDevolucion(p_prestamo, p_conexion);
            }
        });

        return filasActualizadas[0];
    }

    /**
     * =====================================================================
     * MÉTODO PRIVADO: actualizarDevolucion (El UPDATE real)
     * =====================================================================
     * Para exponer: "Ejecuta el UPDATE en la base de datos usando la conexión 
     * transaccional provista, asegurando que la fecha de devolución se registre 
     * solo si estaba en NULL (activo)."
     */
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

    /**
     * =====================================================================
     * MÉTODO: cargarPrestamos (Reconstrucción de objetos desde la BD)
     * =====================================================================
     * Para exponer: "Este método lee todos los registros de la tabla Prestamo 
     * al iniciar el sistema, y usando las listas de socios y libros ya cargados, 
     * vuelve a armar los objetos en memoria relacionándolos correctamente."
     */
    public void cargarPrestamos(ArrayList<Socio> socios,
                                ArrayList<Libro> libros)
        throws SQLException, ParseException
    {
        Connection conexion = this.getConexionBD().conectar();

        PreparedStatement sentencia =
            conexion.prepareStatement("SELECT * FROM Prestamo");

        ResultSet resultado = sentencia.executeQuery();

        SimpleDateFormat formato = new SimpleDateFormat("yyyy-MM-dd");

        // Recorremos cada fila obtenida de la base de datos
        while(resultado.next()) {
            int dni = resultado.getInt("dniSocio");
            int idLibro = resultado.getInt("idLibro");

            Socio unSocio = null;
            Libro unLibro = null;

            // Buscamos el socio correspondiente en la lista pasada por parámetro
            for(Socio socio : socios) {
                if(socio.getDniSocio() == dni)
                    unSocio = socio;
            }

            // Buscamos el libro correspondiente en la lista pasada por parámetro
            for(Libro libro : libros) {
                if (this.getLibroDAO().obtenerIdentificador(libro) == idLibro) {
                    unLibro = libro;
                }
            }

            // Si encontramos ambos objetos en memoria, reconstruimos el Préstamo
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

                // Instanciamos el objeto Prestamo
                Prestamo unPrestamo = new Prestamo(
                    fechaRetiro,
                    unSocio,
                    unLibro
                );

                if(fechaGuardada != null)
                {
                    unPrestamo.registrarFechaDevolucion(fechaDevolucion);
                }

                // Vinculamos bidireccionalmente el préstamo con el socio y el libro
                unSocio.agregarPrestamo(unPrestamo);
                unLibro.agregarPrestamo(unPrestamo);
            }
        }
        
        // Cerramos todo al terminar de leer
        resultado.close();
        sentencia.close();
        conexion.close();
    }
    
    /**
     * =====================================================================
     * MÉTODO: agregar (Sobrecargado)
     * =====================================================================
     * Para exponer: "Es una sobrecarga del método agregar. Permite dar de alta 
     * un préstamo buscando automáticamente los días que le corresponden al socio, 
     * delegando finalmente en el método principal de arriba."
     */
    public void agregar(Prestamo p_prestamo) throws SQLException {
        this.agregar(
            p_prestamo,
            p_prestamo.getSocio().getDiasPrestamo()
        );
    }
}