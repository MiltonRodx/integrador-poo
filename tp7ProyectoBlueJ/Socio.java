
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

public abstract class Socio {
    
    private int dniSocio;
    private String nombre;
    private int diasPrestamos;
    private ArrayList <Prestamo> prestamos;
    
    public Socio(int p_dniSocio, String p_nombre, int p_diasPrestamos){
        this.setDniSocio(p_dniSocio);
        this.setNombre(p_nombre);
        this.setDiasPrestamos(p_diasPrestamos);
        this.setPrestamos(new ArrayList<Prestamo>());
    }

    public Socio(int p_dniSocio, String p_nombre, int p_diasPrestamos, ArrayList<Prestamo> p_prestamos){
        this.setDniSocio(p_dniSocio);
        this.setNombre(p_nombre);
        this.setDiasPrestamos(p_diasPrestamos);
        this.setPrestamos(p_prestamos);
    }
    
    // Accessors
    private void setDniSocio(int p_dniSocio){ this.dniSocio = p_dniSocio; }
    private void setNombre(String p_nombre){ this.nombre = p_nombre; }
    // setter de dias prestamos necesario usar en modificador de acceso 'protected'
    protected void setDiasPrestamos(int p_diasPrestamos){ this.diasPrestamos = p_diasPrestamos; }
    private void setPrestamos(ArrayList<Prestamo> p_prestamo){ this.prestamos = p_prestamo; }

    public int getDniSocio(){ return this.dniSocio; }
    public String getNombre(){ return this.nombre; }
    public int getDiasPrestamos(){ return this.diasPrestamos; }
    public ArrayList<Prestamo> getPrestamos(){ return this.prestamos; }

    public boolean agregarPrestamo(Prestamo p_prestamo) {
        if (p_prestamo == null)
            throw new IllegalArgumentException("El prestamo no puede ser nulo.");
            
        return this.getPrestamos().add(p_prestamo);
    }

    public boolean quitarPrestamo(Prestamo p_prestamos){ return this.getPrestamos().remove(p_prestamos); }

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
   
    public String toString (){
        return "DNI" + this.getDniSocio() + "\t||" + this.getNombre() + 
                "( " + this.soyDeLaClase() + " ) " + 
                "Libros Prestados : " + this.cantLibrosPrestados();   
    }

    public boolean puedePedir(){
        Calendar fechaActual = new GregorianCalendar();
    
        for(Prestamo unPrestamo : this.getPrestamos()){
            if (unPrestamo.getFechaDevolucion() == null &&
               unPrestamo.vencido(fechaActual)){
                return false;
            }
        }
    
        return true;
    }

    public abstract String soyDeLaClase();
}
