import java.util.ArrayList;

public class Estudiante extends Socio {
    private String carrera;
    
    public Estudiante(int p_dniSocio, String p_nombre, int p_diasPrestamos, String p_carrera,
            ArrayList<Prestamo> p_prestamos) { // con coleccion
       super(p_dniSocio, p_nombre, p_diasPrestamos, p_prestamos);
       this.setCarrera(p_carrera);
    }
    
    public Estudiante(int p_dniSocio, String p_nombre, int p_diasPrestamos, String p_carrera) {
        super(p_dniSocio, p_nombre, p_diasPrestamos);
        this.setCarrera(p_carrera);
    }
   
    // Accessors
    private void setCarrera(String p_carrera){ this.carrera = p_carrera; }
    
    public String getCarrera(){ return this.carrera; }
    
    public boolean puedePedir(){
        // +puedePedir(): devuelve true si el Estudiante no tiene ningún préstamo vencido
        // y si no tiene más de 3 libros prestados (en su poder).
        return this.cantLibrosPrestados() <= 3 && super.puedePedir();
    }
    
    public String soyDeLaClase() { return "Estudiante";  }
}
