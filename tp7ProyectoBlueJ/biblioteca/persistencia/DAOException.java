package biblioteca.persistencia;

/**
 * Excepcion de la capa de persistencia.
 * Envuelve los errores de JDBC/SQL (checked) en una unchecked para no
 * ensuciar las firmas de los DAOs ni obligar a la GUI a operar con throws.
 */
public class DAOException extends RuntimeException {

    public DAOException(String p_message) {
        super(p_message);
    }

    public DAOException(String p_message, Throwable p_causa) {
        super(p_message, p_causa);
    }
}
