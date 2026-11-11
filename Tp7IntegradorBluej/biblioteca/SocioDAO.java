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

        final int[] filasSocio = new int[1];

        // La baja borra primero el historial de prestamos del socio dentro de
        // una transaccion: sin eso la FK de Prestamo rechaza el DELETE.
        // La consola ya bloquea la baja si el socio tiene prestamos activos.
        this.getConexionBD().enTransaccion(new TrabajoBD() {
            public void ejecutar(Connection p_conexion) throws SQLException {

                String borrarPrestamos =
                    "DELETE FROM Prestamo WHERE dniSocio = ?";
                String borrarSocio =
                    "DELETE FROM Socio WHERE dniSocio = ?";

                try (PreparedStatement sentenciaPrestamos =
                         p_conexion.prepareStatement(borrarPrestamos);
                     PreparedStatement sentenciaSocio =
                         p_conexion.prepareStatement(borrarSocio)) {

                    sentenciaPrestamos.setInt(1, p_socio.getDniSocio());
                    sentenciaPrestamos.executeUpdate();

                    sentenciaSocio.setInt(1, p_socio.getDniSocio());
                    filasSocio[0] = sentenciaSocio.executeUpdate();
                }
            }
        });

        return filasSocio[0];
    }

    /**
     * Persiste los dias de prestamo actuales del socio (el premio de los
     * docentes responsables es el unico update de datos que tiene el sistema).
     *
     * @return la cantidad de filas afectadas (0 si el DNI no existe en la base)
     */
    public int actualizarDiasPrestamo(Socio p_socio) throws SQLException {

        String consulta = "UPDATE Socio SET diasPrestamo = ? WHERE dniSocio = ?";

        try (Connection conexion = this.getConexionBD().conectar();
             PreparedStatement sentencia = conexion.prepareStatement(consulta)) {

            sentencia.setInt(1, p_socio.getDiasPrestamo());
            sentencia.setInt(2, p_socio.getDniSocio());

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