package MiBiblioteca.gui;

/** Un prestamo no se puede registrar por una regla de negocio (el mensaje explica el motivo). */
public class PrestamoRechazadoException extends Exception {

    public PrestamoRechazadoException(String p_motivo) {
        super(p_motivo);
    }
}
