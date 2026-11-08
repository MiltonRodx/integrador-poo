package MiBiblioteca; 
import java.util.ArrayList;
import java.util.Calendar;
 
// Clase abstracta: un socio siempre es un Docente o un Estudiante
public abstract class Socio
{
    private int dniSocio;
    private String nombre;
    private int diasPrestamo;
    private ArrayList<Prestamo> prestamos;
 
    public Socio(int p_dniSocio, String p_nombre, int p_diasPrestamo){
        this.setDniSocio(p_dniSocio);
        this.setNombre(p_nombre);
        this.setDiasPrestamo(p_diasPrestamo);
        this.setPrestamos(new ArrayList<Prestamo>());
    }
 
     public Socio(int p_dniSocio, String p_nombre, int p_diasPrestamo, ArrayList<Prestamo> p_prestamos ){
        this.setDniSocio(p_dniSocio);
        this.setNombre(p_nombre);
        this.setDiasPrestamo(p_diasPrestamo);
        this.setPrestamos(p_prestamos);
    }
 
    private void setDniSocio(int p_dniSocio){
        this.dniSocio = p_dniSocio;
    }
 
    private void setNombre(String p_nombre){
        this.nombre = p_nombre;
    }
 
    // protected: Docente lo necesita para sumar días de préstamo
    protected void setDiasPrestamo(int p_diasPrestamo){
        this.diasPrestamo = p_diasPrestamo;
    }
 
    private void setPrestamos(ArrayList<Prestamo> p_prestamos){
        this.prestamos = p_prestamos;
    }
 
    public int getDniSocio(){
        return this.dniSocio;
    }
 
    public String getNombre(){
        return this.nombre;
    }
 
    public int getDiasPrestamo(){
        return this.diasPrestamo;
    }
 
    public ArrayList<Prestamo> getPrestamos(){
        return this.prestamos;
    }
 
    public void agregarPrestamo(Prestamo p_prestamo){
        this.getPrestamos().add(p_prestamo);
    }
 
    // Libros en su poder = préstamos sin fecha de devolución
    public int cantLibrosPrestados(){
        int cantidad = 0;
        for(Prestamo unPrestamo : this.getPrestamos()){
            if(!unPrestamo.estaDevuelto()){
                cantidad++;
            }
        }
        return cantidad;
    }
 
    
    public boolean puedePedir(){
        Calendar hoy = Calendar.getInstance();
        for(Prestamo unPrestamo : this.getPrestamos()){
            if(!unPrestamo.estaDevuelto() && unPrestamo.vencido(hoy)){
                return false;
            }
        }
        return true;
    }
 
    public String toString(){
        return "D.N.I.: " + this.getDniSocio() + " || " + this.getNombre() +
               " (" + this.soyDeLaClase() + ") || Libros Prestados: " + this.cantLibrosPrestados();
    }
 
    public abstract String soyDeLaClase();
}
 
