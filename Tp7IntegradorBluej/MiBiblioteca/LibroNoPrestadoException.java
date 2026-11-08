package MiBiblioteca;


/**
 * Excepcion de negocio (CHECKED) de la consigna: se lanza cuando se intenta
 * devolver un libro que no esta prestado, o cuando se pregunta quien tiene
 * un libro que se encuentra disponible en la biblioteca.
 *
 * CHECKED (hereda de Exception, no de RuntimeException) porque el enunciado
 * exige declararla y capturarla explicitamente: la captura queda en cada
 * invocador (menu de consola y GUI), que decide como presentarla.
 */
public class LibroNoPrestadoException extends Exception {

    /**
     * Crea la excepcion con un mensaje descriptivo a medida.
     *
     * @param p_message texto que explica la situacion de negocio
     */
    public LibroNoPrestadoException(String p_message) {
        super(p_message);
    }

    /**
     * Crea la excepcion con el mensaje por defecto de la consigna.
     */
    public LibroNoPrestadoException() {
        super("El libro no está prestado");
    }

}