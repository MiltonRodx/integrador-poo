
package biblioteca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

public class LibroDAO
{
    private ConexionBD conexionBD;
    private HashMap<Libro, Integer> identificadoresLibros;

    public LibroDAO(ConexionBD p_conexionBD){
        this.setConexionBD(p_conexionBD);
        this.setIdentificadoresLibros(new HashMap<Libro, Integer>());
    }

    private void setConexionBD(ConexionBD p_conexionBD){
        this.conexionBD = p_conexionBD;
    }

    private void setIdentificadoresLibros(HashMap<Libro, Integer> p_identificadoresLibros){
        this.identificadoresLibros = p_identificadoresLibros;
    }

    public ConexionBD getConexionBD(){
        return this.conexionBD;
    }

    public HashMap<Libro, Integer> getIdentificadoresLibros(){
        return this.identificadoresLibros;
    }

    public void agregar(Libro p_libro) throws SQLException {

        Connection conexion = this.getConexionBD().conectar();

        String consulta = "INSERT INTO Libro (titulo, edicion, editorial, anio) " +
                          "VALUES (?, ?, ?, ?)";

        PreparedStatement sentencia = conexion.prepareStatement(
            consulta, Statement.RETURN_GENERATED_KEYS
        );

        sentencia.setString(1, p_libro.getTitulo());
        sentencia.setInt(2, p_libro.getEdicion());
        sentencia.setString(3, p_libro.getEditorial());
        sentencia.setInt(4, p_libro.getAnio());

        sentencia.executeUpdate();

        ResultSet resultado = sentencia.getGeneratedKeys();

        if(resultado.next()){
            this.getIdentificadoresLibros().put(
                p_libro, resultado.getInt(1)
            );
        }

        resultado.close();
        sentencia.close();
        conexion.close();
    }

    public ArrayList<Libro> obtenerTodos() throws SQLException{
        /*Abre una conexión con SQLite, prepara una sentencia 
         * y ejecuta un SELECT * FROM Libro para obtener todos los registros de la tabla de libros. 
         * El resultado de esa consulta queda guardado en formato de filas en el objeto resultado (ResultSet).*/
        ArrayList<Libro> libros = new ArrayList<Libro>();

        Connection conexion = this.getConexionBD().conectar();
        // Statement: Es cualquier orden o instrucción completa que le envías al motor de la base de datos para que ejecute una acción.
        Statement sentencia = conexion.createStatement();

        ResultSet resultado = sentencia.executeQuery(
            "SELECT * FROM Libro"
        );

        while(resultado.next()){

            Libro unLibro = new Libro(
                resultado.getString("titulo"),
                resultado.getInt("edicion"),
                resultado.getString("editorial"),
                resultado.getInt("anio")
            );

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

    public int obtenerIdentificador(Libro p_libro){
        return this.getIdentificadoresLibros().get(p_libro);
    }

    public void eliminar(Libro p_libro) throws SQLException{

        Integer identificador = this.getIdentificadoresLibros().get(p_libro);

        if(identificador == null){
            throw new SQLException("No se encontro el identificador del libro.");
        }

        Connection conexion = this.getConexionBD().conectar();

        String consulta = "DELETE FROM Libro WHERE idLibro = ?";

        PreparedStatement sentencia = conexion.prepareStatement(consulta);

        sentencia.setInt(1, identificador);

        sentencia.executeUpdate();

        sentencia.close();
        conexion.close();

        this.getIdentificadoresLibros().remove(p_libro);
    }
}
