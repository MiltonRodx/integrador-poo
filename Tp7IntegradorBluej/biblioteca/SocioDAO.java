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

    private void setConexionBD(ConexionBD p_conexionBD){ this.conexionBD = p_conexionBD; }

    public ConexionBD getConexionBD(){ return this.conexionBD; }

    public void agregar(Socio p_socio) throws SQLException{

        Connection conexion = this.getConexionBD().conectar();

        String tipo = p_socio.soyDeLaClase();
        String area = null;
        String carrera = null;
        
        
        if (tipo.equals("Docente")){
            Docente unDocente = (Docente) p_socio;
            area = unDocente.getArea();
        }
        else if (tipo.equals("Estudiante")) {
            Estudiante unEstudiante = (Estudiante) p_socio;
            carrera = unEstudiante.getCarrera();
        }
    
        // Estructura de la consulta SQL (con placeholders '?').
        String consulta = "INSERT INTO Socio " +
                          "(dniSocio, nombre, diasPrestamo, tipo, area, carrera) " +
                          "VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement sentencia = conexion.prepareStatement(consulta);

        // Posicionar los valores en los '?' (placeholders) de la sentencia.
        sentencia.setInt(1, p_socio.getDniSocio());
        sentencia.setString(2, p_socio.getNombre());
        sentencia.setInt(3, p_socio.getDiasPrestamo());
        sentencia.setString(4, tipo);
        sentencia.setString(5, area);
        sentencia.setString(6, carrera);

        // Ejecución
        sentencia.executeUpdate();

        // Cierre de la sentencia y la conexión
        sentencia.close();
        conexion.close();
    }
    
    // Da de baja un socio y devuelve cuántas filas de Socio se borraron (0 si no existía)
    public int eliminar(Socio p_socio) throws SQLException {
    
        // Caja de un elemento para sacar el resultado de la clase anónima
        final int[] filasSocio = new int[1];
    
        // Todo corre en una transacción: o se borra todo o no se borra nada
        this.getConexionBD().enTransaccion(new TrabajoBD() {
            public void ejecutar(Connection p_conexion) throws SQLException {
    
                // Primero los préstamos, porque la FK de Prestamo apunta al socio
                String borrarPrestamos =
                    "DELETE FROM Prestamo WHERE dniSocio = ?";
                // Después el socio
                String borrarSocio =
                    "DELETE FROM Socio WHERE dniSocio = ?";
    
                // try-with-resources: cierra las sentencias solo al terminar
                try (PreparedStatement sentenciaPrestamos =
                         p_conexion.prepareStatement(borrarPrestamos);
                     PreparedStatement sentenciaSocio =
                         p_conexion.prepareStatement(borrarSocio)) {
    
                    // Borra el historial de préstamos del socio
                    sentenciaPrestamos.setInt(1, p_socio.getDniSocio());
                    sentenciaPrestamos.executeUpdate();
    
                    // Borra al socio y guarda cuántas filas afectó
                    sentenciaSocio.setInt(1, p_socio.getDniSocio());
                    filasSocio[0] = sentenciaSocio.executeUpdate();
                }
            }
        });
    
        // Devuelve el resultado guardado en la caja
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

        // ResultSet
        ResultSet resultado = sentencia.executeQuery();

        while(resultado.next()){

            int dni = resultado.getInt("dniSocio");
            String nombre = resultado.getString("nombre");
            int diasPrestamo = resultado.getInt("diasPrestamo");
            String tipo = resultado.getString("tipo");

            if (tipo.equals("Docente")){

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