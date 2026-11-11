package biblioteca;
import java.util.ArrayList;

/**
 * Ejemplar fisico de un titulo en la biblioteca.
 *
 * Varios ejemplares pueden compartir titulo/editorial (por eso los
 * listados de "titulos sin repetir" usan distinct()). Cada Libro lleva su
 * ficha de prestamos (historico completo, activos y devueltos).
 *
 * La identidad persistente (id de la tabla libro) no se guarda en el
 * objeto: la resuelve la capa DAO con su mapa libro -> idLibro, por eso
 * el dominio queda fiel al UML de la consigna.
 */
public class Libro {
    
    private String titulo;
    private int edicion;
    private String editorial;
    private int anio;
    private ArrayList<Prestamo> prestamos;
    
    /**
     * Crea un ejemplar con un historial de prestamos pre-existente.
     *
     * Lo usa la capa de persistencia para reconstruir el libro desde la
     * base con su ficha ya repuesta por PrestamoDAO.
     *
     * @param p_titulo titulo del libro
     * @param p_edicion numero de edicion
     * @param p_editorial editorial
     * @param p_anio anio de edicion
     * @param p_prestamos historial de prestamos ya vinculado
     */
    public Libro(String p_titulo, int p_edicion, String p_editorial, int p_anio , ArrayList<Prestamo> p_prestamos){
        this.setTitulo(p_titulo);
        this.setEdicion(p_edicion);
        this.setEditorial(p_editorial);
        this.setAnio(p_anio);
        this.setPrestamos(p_prestamos);
    }
    
    /**
     * Crea un ejemplar nuevo, con ficha de prestamos vacia.
     *
     * @param p_titulo titulo del libro
     * @param p_edicion numero de edicion
     * @param p_editorial editorial
     * @param p_anio anio de edicion
     */
    public Libro(String p_titulo, int p_edicion, String p_editorial, int p_anio){
        this.setTitulo(p_titulo);
        this.setEdicion(p_edicion);
        this.setEditorial(p_editorial);
        this.setAnio(p_anio);
        this.setPrestamos(new ArrayList<Prestamo>());
    }
    
    /** Establece la ficha de prestamos del ejemplar. */
    private void setPrestamos(ArrayList<Prestamo> p_prestamos) { this.prestamos = p_prestamos; }

    /** Establece el titulo del ejemplar. */
    private void setTitulo(String p_titulo) { this.titulo = p_titulo; }

    /** Establece el numero de edicion. */
    private void setEdicion(int p_edicion) { this.edicion = p_edicion; }

    /** Establece la editorial. */
    private void setEditorial(String p_editorial) { this.editorial = p_editorial; }

    /** Establece el anio de edicion. */
    private void setAnio(int p_anio) { this.anio = p_anio; }

    /** @return la ficha de prestamos del ejemplar (historico completo). */
    public ArrayList<Prestamo> getPrestamos() { return this.prestamos; }

    /** @return el titulo del ejemplar. */
    public String getTitulo() { return this.titulo; }

    /** @return el numero de edicion. */
    public int getEdicion() { return this.edicion; }

    /** @return la editorial. */
    public String getEditorial() { return this.editorial; }

    /** @return el anio de edicion. */
    public int getAnio() { return this.anio; }
    
    /**
     * Agrega un prestamo a la ficha del ejemplar (retiro o carga desde la BD).
     *
     * @param p_prestamo prestamo que se inscribe
     * @return true si quedo agregado a la coleccion
     */
    public boolean agregarPrestamo(Prestamo p_prestamo) { return this.getPrestamos().add(p_prestamo); }

    /**
     * Quita un prestamo de la ficha del ejemplar.
     *
     * @param p_prestamo prestamo a quitar
     * @return true si estaba en la ficha y fue removido
     */
    public boolean quitarPrestamo(Prestamo p_prestamo) { return this.getPrestamos().remove(p_prestamo); }
    
    // Metodos especificos
    /**
     * Indica si el ejemplar esta prestado hoy.
     *
     * @return true si la ficha contiene al menos un prestamo ACTIVO (sin fecha de devolucion)
     */
    public boolean prestado() {
        for (Prestamo prestamo : this.getPrestamos())
            if (prestamo.getFechaDevolucion() == null) return true;
            
        return false;
    }
    
    /**
     * Ultimo prestamo inscripto en la ficha del ejemplar.
     *
     * Es el registro sobre el que Biblioteca.devolverLibro() escribe la
     * fecha de devolucion (el prestamo ACTIVO sin devolver, por diseño).
     *
     * @return el prestamo mas reciente de la ficha
     */
    public Prestamo ultimoPrestamo(){ return this.getPrestamos().getLast(); }

    /**
     * Render del formato2 de la consigna: una linea por ejemplar.
     *
     * @return "Titulo: <<titulo>>" para el listado de libros
     */
    public String toString(){ return "Titulo: " + this.getTitulo(); }
}
