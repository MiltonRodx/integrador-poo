package MiBiblioteca;

public class LibroNoPrestadoException extends Exception
{
    public LibroNoPrestadoException(String p_mensaje){
        super(p_mensaje);
    }
}
