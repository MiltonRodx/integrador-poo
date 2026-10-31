import java.util.ArrayList;
import java.util.Calendar;

public class Docente extends Socio {
    private String area;
   
    public Docente(int p_dniSocio, String p_nombre, int p_diasPrestamos,
            ArrayList<Prestamo> p_prestamos, String p_area) {
        super(p_dniSocio, p_nombre, p_diasPrestamos, p_prestamos);
        this.setArea(p_area);
    }
   
    public Docente(int p_dniSocio, String p_nombre, int p_diasPrestamos, String p_area) {
        super(p_dniSocio, p_nombre, p_diasPrestamos);
        this.setArea(p_area);
    }
   
    private void setArea(String p_area){ this.area = p_area; }
   
    public String getArea(){ return this.area; }

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
    public void cambiarDiasDePrestamos(int p_dias) {
        this.setDiasPrestamos(this.getDiasPrestamos() + p_dias);
    }
    
    public boolean puedePedir(){
        return super.puedePedir();
    }
    
    public String soyDeLaClase(){
        return "Docente";
    }
    
} 
