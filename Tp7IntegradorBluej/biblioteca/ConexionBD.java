package biblioteca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBD {
    // Atributos
    private String rutaBaseDatos;
    private Connection conexion; // ACÁ GUARDAMOS LA CONEXIÓN

    // Constructor
    public ConexionBD(String p_rutaBaseDatos) {
        this.setRutaBaseDatos(p_rutaBaseDatos);
        this.conexion = null;
    }

    // Accessors
    private void setRutaBaseDatos(String p_rutaBaseDatos) { this.rutaBaseDatos = p_rutaBaseDatos; }

    public String getRutaBaseDatos() { return this.rutaBaseDatos; }
    
    private void setConexion(Connection p_conexion) { this.conexion = p_conexion; }
    
    public Connection getConexion() { return this.conexion; }
    
    // Metodos
    // Método para conectar (si ya está conectado, devuelve la misma)
    public Connection conectar() throws SQLException {
        if (this.conexion == null || this.conexion.isClosed()) {
            this.conexion = DriverManager.getConnection(
                "jdbc:sqlite:" + this.getRutaBaseDatos()
            );

            // Usamos try-with-resources para el Statement así se cierra solo
            try (Statement sentencia = this.conexion.createStatement()) {
                sentencia.execute("PRAGMA foreign_keys = ON");
            }
        }
        
        return this.getConexion();
    }

    public void cerrar() throws SQLException {
        if (this.getConexion() != null && !this.getConexion().isClosed()) {
            this.getConexion().close();
        }
    }

    // Ejecuta un trabajo dentro de una transaccion: todo o nada.
    // Si el trabajo termina bien se hace COMMIT; si algo explota a mitad,
    // se hace ROLLBACK y el error sube para que quien llamo decida que
    // mostrar. Al terminar (salga como salga) la conexion queda con el
    // autoCommit como estaba, lista para el resto de la app.
    public void enTransaccion(TrabajoBD p_trabajo) throws SQLException
    {
        Connection conexion = this.conectar();

        try {
            conexion.setAutoCommit(false);

            try {
                p_trabajo.ejecutar(conexion);
                conexion.commit();
            }
            catch (SQLException error) {
                conexion.rollback();
                throw error;
            }
        }
        finally {
            try {
                conexion.setAutoCommit(true);
            }
            catch (SQLException ignorado) {
                // Si ya no se puede reconfigurar la conexion, poco se puede
                // hacer aca: el error real ya esta subiendo al llamador.
            }
        }
    }
}