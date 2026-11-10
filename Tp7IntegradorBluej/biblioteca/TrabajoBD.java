package biblioteca;

import java.sql.Connection;
import java.sql.SQLException;

// Un trabajo que se ejecuta dentro de una transaccion.
//
// La implementacion recibe la Connection y NO debe cerrarla ni tocar
// el autoCommit: ConexionBD.enTransaccion() se encarga de hacer el
// commit al final (o el rollback si algo explota a mitad).
public interface TrabajoBD {
    void ejecutar(Connection p_conexion) throws SQLException;
}
