
public class LibroNoPrestadoException extends Exception {
    
    public LibroNoPrestadoException(String p_message) {
        super(p_message);
    }
    
    public LibroNoPrestadoException() {
        super("El libro no está prestado");
    }

}