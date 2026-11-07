package biblioteca.gui;

/** No se pudo completar una operacion de escritura contra la base SQLite. */
public class ErrorDePersistenciaException extends RuntimeException {

    public ErrorDePersistenciaException(String p_mensaje, Throwable p_causa) {
        super(p_mensaje, p_causa);
    }
}
