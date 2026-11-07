package biblioteca.gui;

/** El modelo rechazo el prestamo (libro prestado / vencidos / tope del socio). */
public class PrestamoRechazadoException extends Exception {

    public PrestamoRechazadoException(String p_mensaje) {
        super(p_mensaje);
    }
}
