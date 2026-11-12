package biblioteca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class LibroDAO {
    private ConexionBD conexionBD;
    /* HashMap para mantener en memoria una caché que asocia cada objeto Libro 
     * con su respectivo identificador numérico (idLibro) de la base de datos. */
    private HashMap<Libro, Integer> identificadoresLibros;

    /**
     * Constructor: Inicializa el DAO asociándolo a una gestora de conexión 
     * y creando el mapa para almacenar los identificadores de los libros.
     */
    public LibroDAO(ConexionBD p_conexionBD){
        this.setConexionBD(p_conexionBD);
        this.setIdentificadoresLibros(new HashMap<Libro, Integer>());
    }

    private void setConexionBD(ConexionBD p_conexionBD){ this.conexionBD = p_conexionBD; }

    private void setIdentificadoresLibros(HashMap<Libro, Integer> p_identificadoresLibros){
        this.identificadoresLibros = p_identificadoresLibros;
    }

    public ConexionBD getConexionBD(){ return this.conexionBD; }

    public HashMap<Libro, Integer> getIdentificadoresLibros(){
        return this.identificadoresLibros;
    }

    /**
     * Inserta un nuevo registro de Libro en la base de datos SQLite.
     * Utiliza un PreparedStatement con parámetros (?) para prevenir inyecciones SQL
     * y solicita que se devuelvan las claves generadas automáticamente (idLibro).
     */
    public void agregar(Libro p_libro) throws SQLException {

        Connection conexion = this.getConexionBD().conectar();

        String consulta = "INSERT INTO Libro (titulo, edicion, editorial, anio) " +
                          "VALUES (?, ?, ?, ?)";

        // PreparedStatement: Permite compilar la consulta SQL por adelantado y pasarle parámetros seguros.
        // Statement.RETURN_GENERATED_KEYS indica que queremos recuperar la clave primaria asignada por la BD.
        PreparedStatement sentencia = conexion.prepareStatement(
            consulta, Statement.RETURN_GENERATED_KEYS
        );

        // Asignación de valores a cada comodín (?) según su posición ordinal (1 a 4)
        sentencia.setString(1, p_libro.getTitulo());
        sentencia.setInt(2, p_libro.getEdicion());
        sentencia.setString(3, p_libro.getEditorial());
        sentencia.setInt(4, p_libro.getAnio());

        // Ejecuta la orden de inserción en la base de datos
        sentencia.executeUpdate();

        // Obtiene la clave primaria (idLibro) generada automáticamente por el autoincrementable de SQLite
        ResultSet resultado = sentencia.getGeneratedKeys();

        if(resultado.next()){
            // Guarda la relación entre el objeto Libro y su ID de base de datos en la caché local
            this.getIdentificadoresLibros().put(
                p_libro, resultado.getInt(1)
            );
        }

        // Cierre ordenado de recursos para evitar fugas de memoria o bloqueos en la base de datos
        resultado.close();
        sentencia.close();
        conexion.close();
    }

    /**
     * Abre una conexión con SQLite, prepara una sentencia 
     * y ejecuta un SELECT * FROM Libro para obtener todos los registros de la tabla de libros. 
     * El resultado de esa consulta queda guardado en formato de filas en el objeto resultado (ResultSet).
     */
    public ArrayList<Libro> obtenerTodos() throws SQLException{
        ArrayList<Libro> libros = new ArrayList<Libro>();

        Connection conexion = this.getConexionBD().conectar();
        // Statement: Es cualquier orden o instrucción completa que le envías al motor de la base de datos para que ejecute una acción.
        Statement sentencia = conexion.createStatement();

        ResultSet resultado = sentencia.executeQuery(
            "SELECT * FROM Libro"
        );

        // Recorre cada fila devuelta por la consulta SQL
        while(resultado.next()){

            Libro unLibro = new Libro(
                resultado.getString("titulo"),
                resultado.getInt("edicion"),
                resultado.getString("editorial"),
                resultado.getInt("anio")
            );

            // Mapea el libro recuperado con su ID correspondiente obtenido de la tabla
            this.getIdentificadoresLibros().put(
                unLibro, resultado.getInt("idLibro")
            );

            libros.add(unLibro);
        }

        resultado.close();
        sentencia.close();
        conexion.close();

        return libros;
    }

    /**
     * Consulta el mapa en memoria para devolver el identificador de base de datos 
     * asociado a una instancia específica de Libro.
     */
    public int obtenerIdentificador(Libro p_libro){
        return this.getIdentificadoresLibros().get(p_libro);
    }

    /**
     * Elimina un libro de la base de datos utilizando su identificador único (idLibro).
     * Verifica primero si el libro se encuentra registrado en el mapa local.
     */
    
    public void eliminar(Libro p_libro) throws SQLException {

        Integer identificador = this.getIdentificadoresLibros().get(p_libro);

        // Controla que el libro tenga un ID válido asignado antes de intentar borrarlo
        if(identificador == null){
            throw new SQLException("No se encontro el identificador del libro.");
        }

        Connection conexion = this.getConexionBD().conectar();

        String consulta = "DELETE FROM Libro WHERE idLibro = ?";

        PreparedStatement sentencia = conexion.prepareStatement(consulta);
        
        // Asigna el ID del libro al parámetro de la sentencia DELETE
        sentencia.setInt(1, identificador);

        // Ejecuta la baja en la base de datos
        sentencia.executeUpdate();

        sentencia.close();
        conexion.close();

        // Remueve la entrada correspondiente del HashMap local
        this.getIdentificadoresLibros().remove(p_libro);
    }
}