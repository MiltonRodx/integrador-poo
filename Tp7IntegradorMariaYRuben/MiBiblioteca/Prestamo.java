package MiBiblioteca;

import java.util.Calendar;
import java.text.SimpleDateFormat;

public class Prestamo
{
    private Calendar fechaRetiro;
    private Calendar fechaDevolucion;
    private Socio socio;
    private Libro libro;

    // Al crearse solo tiene fecha de retiro: la devolución queda en null
    public Prestamo(Calendar p_fechaRetiro,  Calendar p_fechaDevolucion, Socio p_socio, Libro p_libro){
        this.setFechaRetiro(p_fechaRetiro);
        this.setFechaDevolucion(p_fechaDevolucion);
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
        this.setFechaDevolucion(p_fecha);
    }

    public boolean estaDevuelto(){
        return this.getFechaDevolucion() != null;
    }

    // Vencimiento = fecha de retiro + días de préstamo del socio
    public boolean vencido(Calendar p_fecha){
        Calendar vencimiento = (Calendar) this.getFechaRetiro().clone();
        vencimiento.add(Calendar.DAY_OF_MONTH, this.getSocio().getDiasPrestamo());
        return p_fecha.after(vencimiento);
    }

    // Sirve para saber si el docente es responsable:
    // si ya se devolvió, se mira si la devolución fue tarde; si no, si hoy está vencido.
    public boolean estuvoVencido(){
        if(this.estaDevuelto()){
            return this.vencido(this.getFechaDevolucion());
        }
        return this.vencido(Calendar.getInstance());
    }

    public String toString(){
        SimpleDateFormat formato = new SimpleDateFormat("yyyy/MM/dd");
        
        
        Calendar fechaVencimiento = (Calendar) this.getFechaRetiro().clone();
        fechaVencimiento.add(Calendar.DAY_OF_MONTH, this.getSocio().getDiasPrestamo());
        
        
        String devolucion = "Sin devolver";
        if(this.estaDevuelto()){
            devolucion = formato.format(this.getFechaDevolucion().getTime());
        }
        
       
        return "Retiro: " + formato.format(this.getFechaRetiro().getTime()) +
               " - Vence: " + formato.format(fechaVencimiento.getTime()) +
               " - Devolución: " + devolucion +
               "\nLibro: " + this.getLibro().getTitulo() +
               "\nSocio: " + this.getSocio().getNombre();
    }
    }

