package biblioteca;


import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Prestamo de un ejemplar a un socio, con sus fechas clave.
 *
 * La fecha de devolucion queda null mientras el prestamo siga ACTIVO. El
 * vencimiento se calcula como retiro + dias de prestamo del socio y se
 * evalua a nivel dia: un prestamo que vence HOY todavia no esta vencido.
 * El id de la tabla prestamo no se guarda en el objeto: la capa DAO
 * referencia cada prestamo por su clave natural (socio + libro + retiro).
 */
public class Prestamo {
    private Calendar fechaRetiro;
    private Calendar fechaDevolucion;
    private Socio socio;
    private Libro libro;

    /**
     * Crea el prestamo por RETIRO: socio, libro y fecha de retiro.
     *
     * La fecha de devolucion nace null (prestamo activo).
     *
     * @param p_fechaRetiro fecha de retiro (se normaliza a medianoche)
     * @param p_socio socio que se lleva el libro
     * @param p_libro ejemplar retirado
     */
    public Prestamo(Calendar p_fechaRetiro, Socio p_socio, Libro p_libro){
        this.setFechaRetiro(p_fechaRetiro);
        this.setFechaDevolucion(null);
        this.setSocio(p_socio);
        this.setLibro(p_libro);
    }

    /**
     * Establece la fecha de retiro normalizada a medianoche: la evaluacion
     * de vencidos es a nivel dia, la hora del dia no debe influir.
     */
    private void setFechaRetiro(Calendar p_fechaRetiro){
        Calendar retiro = (Calendar) p_fechaRetiro.clone();
        retiro.set(Calendar.HOUR_OF_DAY, 0);
        retiro.set(Calendar.MINUTE, 0);
        retiro.set(Calendar.SECOND, 0);
        retiro.set(Calendar.MILLISECOND, 0);
        this.fechaRetiro = retiro;
    }

    /** Establece la fecha de devolucion (null mientras el prestamo este activo). */
    private void setFechaDevolucion(Calendar p_fechaDevolucion){ this.fechaDevolucion = p_fechaDevolucion; }

    /** Establece el socio que retiro el libro. */
    private void setSocio(Socio p_socio){ this.socio = p_socio; }

    /** Establece el ejemplar retirado. */
    private void setLibro(Libro p_libro){ this.libro = p_libro; }

    /** @return la fecha de retiro del prestamo. */
    public Calendar getFechaRetiro(){ return this.fechaRetiro; }

    /** @return la fecha de devolucion, o null si sigue activo (sin devolver). */
    public Calendar getFechaDevolucion(){ return this.fechaDevolucion; }

    /** @return el socio que tiene el libro. */
    public Socio getSocio(){ return this.socio; }

    /** @return el ejemplar prestado. */
    public Libro getLibro(){ return this.libro; }

    /** @return true si ya se registro la fecha de devolucion; false si sigue activo. */
    public boolean estaDevuelto(){ return this.getFechaDevolucion() != null; }

    /**
     * Registra la devolucion del libro y cierra el prestamo.
     *
     * @param p_fecha fecha en que se devolvio (se normaliza a medianoche)
     * @throws IllegalArgumentException si la fecha es nula (sin fecha no hay devolucion)
     */
    public void registrarFechaDevolucion(Calendar p_fecha){
        if (p_fecha == null){
            throw new IllegalArgumentException("La fecha de devolucion no puede ser nula");
        }
        // Tambien a medianoche: devolver el dia del vencimiento sigue siendo a tiempo.
        Calendar devolucion = (Calendar) p_fecha.clone();
        devolucion.set(Calendar.HOUR_OF_DAY, 0);
        devolucion.set(Calendar.MINUTE, 0);
        devolucion.set(Calendar.SECOND, 0);
        devolucion.set(Calendar.MILLISECOND, 0);
        this.setFechaDevolucion(devolucion);
    }
 
    /**
     * Informa si el prestamo esta vencido RELATIVO a la fecha indicada.
     *
     * El vencimiento se calcula clonando la fecha de retiro y adicionando
     * los dias de prestamo del socio. after() ya descarta la igualdad, asi
     * que evaluar justo el dia del vencimiento da false (nivel dia).
     *
     * @param p_fecha fecha de referencia (debe venir normalizada a medianoche)
     * @return true si la fecha indicada supera el vencimiento
     */
    public boolean vencido(Calendar p_fecha){
        Calendar vencimiento = (Calendar) this.getFechaRetiro().clone();
        vencimiento.add(Calendar.DAY_OF_MONTH, this.getSocio().getDiasPrestamo());
        return p_fecha.after(vencimiento);
    }
 
    /*To String es largo porque yo decidi dar formato aca
       lo pense en vencimiento tambien pero como debia hacerlo en dos metodos
       de registrar fecha y vencimiento iban a ser muy largos ahi los codigos
       y repetitivos*/
    /**
     * Render del informe de prestamos: retiro, devolucion, libro y socio.
     *
     * Fechas en anio/mes/dia y "Sin devolver" para los activos. Vive aca y
     * no en vencimiento/registrarFecha porque concentrarlo en dos metodos
     * reproduciria la logica de fechas repetida (decision de diseño).
     *
     * @return texto multi-linea con el detalle del prestamo
     */
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
