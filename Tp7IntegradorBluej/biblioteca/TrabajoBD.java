package biblioteca;

import java.sql.Connection;
import java.sql.SQLException;

// Un trabajo que se ejecuta dentro de una transaccion.
//
// La implementacion recibe la Connection y NO debe cerrarla ni tocar
// el autoCommit: ConexionBD.enTransaccion() se encarga de hacer el
// commit al final (o el rollback si algo explota a mitad).
public interface TrabajoBD {
  /**
     * Ejecuta una tarea contra la base de datos dentro de una transacción.
     * 
     * Recibe la conexión ya configurada. No debe cerrar la conexión ni 
     * tocar el autoCommit, ya que el gestor se encarga del commit o del 
     * rollback si ocurre algún error.
     * 
     * @param p_conexion La conexión transaccional provista.
     * @throws SQLException Si ocurre un error en la base de datos.
     */
    void ejecutar(Connection p_conexion) throws SQLException;
}
