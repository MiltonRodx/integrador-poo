package biblioteca;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SocioDAO {
    private ConexionBD conexionBD;

    public SocioDAO(ConexionBD p_conexionBD){
        this.setConexionBD(p_conexionBD);
    }

    private void setConexionBD(ConexionBD p_conexionBD){
        this.conexionBD = p_conexionBD;
    }

    public ConexionBD getConexionBD(){
        return this.conexionBD;
    }

    public void agregar(Socio p_socio) throws SQLException{

        Connection conexion = this.getConexionBD().conectar();

        String tipo = "";
        String area = null;
        String carrera = null;
        
        if(p_socio instanceof Docente){

            Docente unDocente = (Docente) p_socio;

            tipo = "Docente";
            area = unDocente.getArea();

        } else if(p_socio instanceof Estudiante){

            Estudiante unEstudiante = (Estudiante) p_socio;

            tipo = "Estudiante";
            carrera = unEstudiante.getCarrera();
        }

        String consulta = "INSERT INTO Socio " +
                          "(dniSocio, nombre, diasPrestamo, tipo, area, carrera) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement sentencia = conexion.prepareStatement(consulta);

        sentencia.setInt(1, p_socio.getDniSocio());
        sentencia.setString(2, p_socio.getNombre());
        sentencia.setInt(3, p_socio.getDiasPrestamo());
        sentencia.setString(4, tipo);
        sentencia.setString(5, area);
        sentencia.setString(6, carrera);

        sentencia.executeUpdate();

        sentencia.close();
        conexion.close();
    }
    
    public int eliminar(Socio p_socio) throws SQLException {

        String consulta = "DELETE FROM Socio WHERE dniSocio = ?";

        // try-with-resources: cierra la sentencia y la conexion aunque falle
        try (Connection conexion = this.getConexionBD().conectar();
             PreparedStatement sentencia = conexion.prepareStatement(consulta)) {

            sentencia.setInt(1, p_socio.getDniSocio());

            return sentencia.executeUpdate();
        }
    }

    public ArrayList<Socio> obtenerTodos() throws SQLException{

        ArrayList<Socio> socios = new ArrayList<Socio>();

        Connection conexion = this.getConexionBD().conectar();

        PreparedStatement sentencia = conexion.prepareStatement(
            "SELECT * FROM Socio"
        );

        ResultSet resultado = sentencia.executeQuery();

        while(resultado.next()){

            int dni = resultado.getInt("dniSocio");
            String nombre = resultado.getString("nombre");
            int diasPrestamo = resultado.getInt("diasPrestamo");
            String tipo = resultado.getString("tipo");

            if(tipo.equals("Docente")){

                Docente unDocente = new Docente(
                    dni,
                    nombre,
                    diasPrestamo,
                    resultado.getString("area")
                );

                socios.add(unDocente);

            } else if(tipo.equals("Estudiante")){

                Estudiante unEstudiante = new Estudiante(
                    dni,
                    nombre,
                    resultado.getString("carrera")
                );

                socios.add(unEstudiante);
            }
        }

        resultado.close();
        sentencia.close();
        conexion.close();

        return socios;
    }
}