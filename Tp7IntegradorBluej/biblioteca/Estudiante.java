package biblioteca;

import java.util.ArrayList;

/**
 * Socio estudiante: persona inscripta en una carrera de la FaCENA.
 *
 * Nace con 20 dias de prestamo (la consigna define el vencimiento "20 dias
 * despues del retiro") y su regla particular es el TOPE de 3 libros en su
 * poder, ademas del bloqueo comun por prestamos vencidos. Su carrera se
 * persiste en la columna carrera de la tabla socio (los docentes la tienen
 * en NULL).
 */
public class Estudiante extends Socio {
    private String carrera;
    
    /**
     * Crea un estudiante reconstruido desde la base, con historial incluido.
     *
     * @param p_dniSocio D.N.I. del estudiante
     * @param p_nombre nombre y apellido
     * @param p_diasPrestamo dias de prestamo persistidos
     * @param p_carrera carrera que cursa
     * @param p_prestamos historial de prestamos ya vinculado
     */
    public Estudiante(int p_dniSocio, String p_nombre, String p_carrera,
            ArrayList<Prestamo> p_prestamos) { // con coleccion
       super(p_dniSocio, p_nombre, 20, p_prestamos);
       this.setCarrera(p_carrera);
    }
    
    /**
     * Crea un estudiante nuevo con ficha de prestamos vacia.
     *
     * @param p_dniSocio D.N.I. del estudiante
     * @param p_nombre nombre y apellido
     * @param p_diasPrestamo dias de prestamo iniciales (20 por consigna)
     * @param p_carrera carrera que cursa
     */
    public Estudiante(int p_dniSocio, String p_nombre, String p_carrera) {
        super(p_dniSocio, p_nombre, 20);
        this.setCarrera(p_carrera);
    }
   
    // Accessors

    /** Establece la carrera que cursa el estudiante. */
    private void setCarrera(String p_carrera){ this.carrera = p_carrera; }

    /** @return la carrera que cursa el estudiante. */
    public String getCarrera(){ return this.carrera; }
    
    /**
     * Regla del estudiante: solo puede pedir si NO llego al tope de 3 libros
     * en su poder, ademas del bloqueo comun por vencidos.
     *
     * Con "<=" se podia cerrar el ciclo llevandose un 4.º libro; por eso se
     * exige "< 3" y recien despues se delega en la regla de la clase padre.
     *
     * @return true si no tiene 3 libros en mano y no tiene vencidos
     */
    public boolean puedePedir(){
        // +puedePedir(): devuelve true si el Estudiante no tiene ningún préstamo vencido
        // y si no tiene más de 3 libros prestados (en su poder).
        // "<= 3" dejaría pedir un 4.º libro; la consigna quiere como tope 3 en su poder.
        return this.cantLibrosPrestados() < 3 && super.puedePedir();
    }
    
    /** @return "Estudiante" (etiqueta de los listados y del discriminador de la BD). */
    public String soyDeLaClase() { return "Estudiante";  }
}
