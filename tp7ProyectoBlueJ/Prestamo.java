 

import java.util.Calendar;
import java.util.GregorianCalendar;

public class Prestamo
{
    private Calendar fechaRetiro;
    private Calendar fechaDevolucion;
    private Socio socio;
    private Libro libro;

    public Prestamo(Calendar p_fechaRetiro, Socio p_socio, Libro p_libro){
        this.setFechaRetiro(p_fechaRetiro);
        this.setFechaDevolucion(null);
        this.setSocio(p_socio);
        this.setLibro(p_libro);
    }

    private void setFechaRetiro(Calendar p_fechaRetiro){
        this.fechaRetiro = p_fechaRetiro;
    }

    private void setFechaDevolucion(Calendar p_fechaDevolucion){
        this.fechaDevolucion = p_fechaDevolucion;
    }

    private void setSocio(Socio p_socio){
        this.socio = p_socio;
    }

    private void setLibro(Libro p_libro){
        this.libro = p_libro;
    }

    public Calendar getFechaRetiro(){
        return this.fechaRetiro;
    }

    public Calendar getFechaDevolucion(){
        return this.fechaDevolucion;
    }

    public Socio getSocio(){
        return this.socio;
    }

    public Libro getLibro(){
        return this.libro;
    }

   public void registrarFechaDevolucion(Calendar p_fecha){
        if (p_fecha == null){
            throw new IllegalArgumentException("La fecha de devolucion no puede ser nula");
        }
        this.setFechaDevolucion(p_fecha);
    }
 
    /* Vencido si la fecha pasada es mayor a: fecha de retiro + dias de prestamo del socio */
    public boolean vencido(Calendar p_fecha){
        int dia = this.getFechaRetiro().get(Calendar.DAY_OF_MONTH);
        int mes = this.getFechaRetiro().get(Calendar.MONTH) + 1 ;
        int anio = this.getFechaRetiro().get(Calendar.YEAR);
        //Instancie una variable temporal de gregorian calendar
        //y puso como parametro los dias el mes y el año
        GregorianCalendar vencimiento = new GregorianCalendar(anio, mes, dia);
        //despues lo que hice fue agregar a la VT y compare el dia actual con 
        //la cantidad de dias de prestamos que tiene mi socio
        vencimiento.add(Calendar.DAY_OF_MONTH, this.getSocio().getDiasPrestamos());
         // y aca verifico despues si es posterior a mi vencimiento o no 
        return p_fecha.after(vencimiento);
    }
    
    
 
    /*To String es largo porque yo decidi dar formato aca
       lo pense en vencimiento tambien pero como debia hacerlo en dos metodos
       de registrar fecha y vencimiento iban a ser muy largos ahi los codigos
       y repetitivos*/
    public String toString(){
    int diaRetiro = this.getFechaRetiro().get(Calendar.DAY_OF_MONTH);
    int mesRetiro = this.getFechaRetiro().get(Calendar.MONTH) + 1; 
    int anioRetiro = this.getFechaRetiro().get(Calendar.YEAR);
    // para que me devuelva una cadena con el dia mes y año 
    String devolucion = "Sin devolver";
    if (this.getFechaDevolucion() != null){
        int dia = this.getFechaDevolucion().get(Calendar.DAY_OF_MONTH);
        int mes = this.getFechaDevolucion().get(Calendar.MONTH) + 1;
        int anio = this.getFechaDevolucion().get(Calendar.YEAR);
        devolucion = anio + "/" + mes + "/" + dia;
    }

    return "Retiro: " + anioRetiro + "/" + mesRetiro + "/" + diaRetiro +
           " - Devolucion: " + devolucion +
           "\nLibro: " + this.getLibro().getTitulo() +
           "\nSocio: " + this.getSocio().getNombre();
    }
}
