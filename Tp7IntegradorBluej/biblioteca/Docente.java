package biblioteca;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Socio docente: docente de la FaCENA vinculado a un area.
 *
 * Nace con 5 dias de prestamo y es el unico tipo que participa del premio
 * por responsabilidad: cuando {@link #esResponsable()} informa que nunca
 * debio un libro, se le adicionan dias con {@link #cambiarDiasDePrestamos(int)}.
 * Su area de especializacion se persiste en la columna area de la tabla
 * socio (los estudiantes la tienen en NULL).
 */
public class Docente extends Socio {
    private String area;
   
    /**
     * Crea un docente reconstruido desde la base, con historial incluido.
     *
     * @param p_dniSocio D.N.I. del docente
     * @param p_nombre nombre y apellido
     * @param p_diasPrestamo dias de prestamo persistidos (base mas premios)
     * @param p_prestamos historial de prestamos ya vinculado
     * @param p_area area de especializacion
     */
    public Docente(int p_dniSocio, String p_nombre, int p_diasPrestamo,
            ArrayList<Prestamo> p_prestamos, String p_area) {
        super(p_dniSocio, p_nombre, p_diasPrestamo, p_prestamos);
        this.setArea(p_area);
    }
   
    /**
     * Crea un docente nuevo con ficha de prestamos vacia.
     *
     * @param p_dniSocio D.N.I. del docente
     * @param p_nombre nombre y apellido
     * @param p_diasPrestamo dias de prestamo iniciales (5 por consigna)
     * @param p_area area de especializacion
     */
    public Docente(int p_dniSocio, String p_nombre, int p_diasPrestamo, String p_area) {
        super(p_dniSocio, p_nombre, p_diasPrestamo);
        this.setArea(p_area);
    }
   
    /** Establece el area de especializacion del docente. */
    private void setArea(String p_area){ this.area = p_area; }

    /** @return el area de especializacion del docente. */
    public String getArea(){ return this.area; }

    /**
     * Informa si el docente "nunca tuvo ni tiene" un prestamo vencido.
     *
     * Los prestamos activos se evaluan contra HOY y los ya devueltos contra
     * su propia fecha de devolucion (devolver el mismo dia del vencimiento
     * cuenta como a tiempo, consigna). Basta UN solo vencido, presente o
     * pasado, para dejar de ser responsable.
     *
     * @return true si ninguno de sus prestamos estuvo vencido
     */
    public boolean esResponsable() {
        // "Nunca tuvo ni tiene un préstamo vencido":
        //   - lo que sigue en su poder se evalúa contra HOY;
        //   - lo ya devuelto se evalúa contra su propia fecha de devolución
        //     (devolver el mismo día del vencimiento cuenta como a tiempo, consigna).
        // La evaluación es a nivel día: la fecha evaluada se normaliza a medianoche.
        for (Prestamo unPrestamo : this.getPrestamos()) {
            Calendar fechaEvaluar = (unPrestamo.getFechaDevolucion() != null) // expresion ternaria.
                    ? (Calendar) unPrestamo.getFechaDevolucion().clone() // si existe fecha devolucion, la clono
                    : Calendar.getInstance();   // si no existe obtengo la fecha del sistema.
            // limpio la fecha en terminos no relacionados a dias mes y año.
            fechaEvaluar.set(Calendar.HOUR_OF_DAY, 0);
            fechaEvaluar.set(Calendar.MINUTE, 0);
            fechaEvaluar.set(Calendar.SECOND, 0);
            fechaEvaluar.set(Calendar.MILLISECOND, 0);

            if (unPrestamo.vencido(fechaEvaluar)) return false; // para cada prestamo si esta vencido alguno, no es responsable
        }
        
        return true;
    }
   
    // - cambiarDiasDePrestamos(int): adiciona días de préstamo al docente.
    /**
     * Premio a la responsabilidad: ADICIONA dias de prestamo (no reemplaza).
     *
     * @param p_dias dias adicionales para sus proximos retiros
     */
    public void cambiarDiasDePrestamos(int p_dias) {
        this.setDiasPrestamo(this.getDiasPrestamo() + p_dias);
    }
    
    /**
     * Regla del docente: sin tope de libros, solo el bloqueo comun por
     * vencidos que aporta la clase padre.
     *
     * @return true si no tiene prestamos vencidos activos
     */
    public boolean puedePedir(){
        return super.puedePedir();
    }

    /** @return "Docente" (etiqueta de los listados y del discriminador de la BD). */
    public String soyDeLaClase(){
        return "Docente";
    }
    
} 
