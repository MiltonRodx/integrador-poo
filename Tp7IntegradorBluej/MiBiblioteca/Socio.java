package MiBiblioteca;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Representa un socio de la biblioteca (clase abstracta).
 *
 * Guarda el D.N.I., el nombre, los dias de prestamo asignados y la ficha de
 * prestamos que tiene en su poder. Las reglas particulares de cada tipo
 * (tope de 3 libros, premio por responsabilidad) las definen las subclases
 * {@link Estudiante} y {@link Docente}; {@link #puedePedir()} aporta la
 * regla comun: nadie puede pedir teniendo un prestamo vencido.
 */
public abstract class Socio {
    
    // Atributos
    private int dniSocio;
    private String nombre;
    private int diasPrestamo;
    private ArrayList <Prestamo> prestamos;
    
    // Constructores
    /**
     * Crea un socio con su ficha de prestamos vacia.
     *
     * @param p_dniSocio D.N.I. (clave natural del socio, tambien de la BD)
     * @param p_nombre nombre y apellido
     * @param p_diasPrestamo duracion base de sus prestamos (la fija cada subclase)
     */
    public Socio(int p_dniSocio, String p_nombre, int p_diasPrestamo){
        this.setDniSocio(p_dniSocio);
        this.setNombre(p_nombre);
        this.setDiasPrestamo(p_diasPrestamo);
        this.setPrestamos(new ArrayList<Prestamo>());
    }

    /**
     * Crea un socio con un historial de prestamos pre-existente.
     *
     * Lo usa la capa de persistencia para reconstruir el socio desde la
     * base con sus prestamos ya asociados (fichas repuestas por PrestamoDAO).
     *
     * @param p_dniSocio D.N.I. (clave natural del socio)
     * @param p_nombre nombre y apellido
     * @param p_diasPrestamo duracion persistida de sus prestamos
     * @param p_prestamos historial de prestamos ya vinculado
     */
    public Socio(int p_dniSocio, String p_nombre, int p_diasPrestamo, ArrayList<Prestamo> p_prestamos){
        this.setDniSocio(p_dniSocio);
        this.setNombre(p_nombre);
        this.setDiasPrestamo(p_diasPrestamo);
        this.setPrestamos(p_prestamos);
    }
    
    
    // Accessors
    /** Establece el D.N.I. del socio (solo al construir: es su identidad). */
    private void setDniSocio(int p_dniSocio){ this.dniSocio = p_dniSocio; }

    /** Establece el nombre y apellido del socio. */
    private void setNombre(String p_nombre){ this.nombre = p_nombre; }

    // setter de dias prestamos necesario usar en modificador de acceso 'protected'
    /**
     * Establece los dias de prestamo; protected para que el premio de la
     * subclase Docente pueda sumarlos sin exponer el setter al publico.
     */
    protected void setDiasPrestamo(int p_diasPrestamo){ this.diasPrestamo = p_diasPrestamo; }

    /** Establece la coleccion de prestamos del socio. */
    private void setPrestamos(ArrayList<Prestamo> p_prestamo){ this.prestamos = p_prestamo; }

    /** @return el D.N.I. del socio (clave natural, tambien clave primaria en la BD). */
    public int getDniSocio(){ return this.dniSocio; }

    /** @return el nombre y apellido del socio. */
    public String getNombre(){ return this.nombre; }

    /** @return los dias de prestamo vigentes (base mas premios del docente). */
    public int getDiasPrestamo(){ return this.diasPrestamo; }

    /** @return el historial completo: prestamos activos y devueltos. */
    public ArrayList<Prestamo> getPrestamos(){ return this.prestamos; }

    /**
     * Inscribe un prestamo en la ficha del socio (retiro en vivo o carga
     * desde la base).
     *
     * @param p_prestamo prestamo que se registra
     * @return true si quedo agregado a la coleccion
     * @throws IllegalArgumentException si el prestamo es nulo
     */
    public boolean agregarPrestamo(Prestamo p_prestamo) {
        if (p_prestamo == null)
            throw new IllegalArgumentException("El prestamo no puede ser nulo.");
            
        return this.getPrestamos().add(p_prestamo);
    }

    /**
     * Saca un prestamo del historial del socio.
     *
     * @param p_prestamos prestamo a quitar
     * @return true si estaba en la coleccion y fue removido
     */
    public boolean quitarPrestamo(Prestamo p_prestamos){ return this.getPrestamos().remove(p_prestamos); }

    /**
     * Cuenta los libros ACTIVOS que el socio tiene en su poder.
     *
     * Los prestamos ya devueltos no cuentan: el tope de libros restringe lo
     * que esta hoy en mano, no todo el historial.
     *
     * @return cantidad de prestamos sin fecha de devolucion
     */
    public int cantLibrosPrestados(){
        return (int) this.getPrestamos().stream()
            .filter(p -> p.getFechaDevolucion() == null).count();
    }
    
    /*
        // funciona pero el de arriba queda mas fachero
        int cantidad = 0;
         
        for (Prestamo unPrestamo : this.getPrestamos()){
            if (unPrestamo.getFechaDevolucion() == null) cantidad++;
        }
        
        return cantidad;
    */
   
    /**
     * Render del formato1 de la consigna: D.N.I., nombre, tipo y libros en poder.
     *
     * El tipo sale de {@link #soyDeLaClase()}, lo que mantiene un unico
     * formato valido para todas las subclases (polimorfismo en los listados).
     *
     * @return linea de texto con los datos del socio
     */
    public String toString (){
        // Formato de la consigna: D.N.I.: <<dni>> || <<nombre y apellido>> (Tipo) || Libros Prestados: <<n>>
        return "D.N.I.: " + this.getDniSocio() + " || " + this.getNombre() +
                " (" + this.soyDeLaClase() + ")" +
                " || Libros Prestados: " + this.cantLibrosPrestados();
    }

    /**
     * Indica si el socio puede llevarse un libro hoy (regla comun).
     *
     * Devuelve false si tiene un prestamo vencido ACTIVO: el bloqueo por
     * morosidad es de la clase base y lo heredan todas las subclases. La
     * evaluacion es a nivel dia (hoy se normaliza a medianoche), asi lo
     * que vence HOY todavia esta a tiempo.
     *
     * @return true si no tiene prestamos vencidos activos
     */
    public boolean puedePedir(){
        // Evaluación a nivel día: la fecha actual se normaliza a medianoche,
        // así un préstamo que vence HOY todavía cuenta como a tiempo.
        Calendar fechaActual = new GregorianCalendar();
        fechaActual.set(Calendar.HOUR_OF_DAY, 0);
        fechaActual.set(Calendar.MINUTE, 0);
        fechaActual.set(Calendar.SECOND, 0);
        fechaActual.set(Calendar.MILLISECOND, 0);
    
        for(Prestamo unPrestamo : this.getPrestamos()){
            if (unPrestamo.getFechaDevolucion() == null &&
               unPrestamo.vencido(fechaActual)){
                return false;
            }
        }
    
        return true;
    }

    /**
     * Nombre del tipo concreto de socio.
     *
     * Ademas de mostrar "Docente"/"Estudiante" en los listados, ese literal
     * es la columna discriminador tipo de la tabla socio en la BD.
     *
     * @return el nombre de la clase concreta
     */
    public abstract String soyDeLaClase();
}
