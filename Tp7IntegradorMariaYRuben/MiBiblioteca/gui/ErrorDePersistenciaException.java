package MiBiblioteca.gui;

/** Fallo al leer/escribir en la base; envuelve la SQLException original. */
public class ErrorDePersistenciaException extends RuntimeException {

    public ErrorDePersistenciaException(String p_mensaje, Throwable p_causa) {
        super(p_mensaje, p_causa);
    }
}
