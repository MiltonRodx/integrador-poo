package MiBiblioteca.gui;

import MiBiblioteca.Prestamo;

/** Una fila de la tabla de prestamos, ya lista para mostrarse. */
public class FilaPrestamo {

    private final Prestamo prestamo;
    private final String nombreSocio;
    private final String tituloLibro;
    private final String fechaRetiroTexto;
    private final String fechaVencimientoTexto;
    private final EstadoPrestamo estado;

    public FilaPrestamo(Prestamo p_prestamo, String p_nombreSocio, String p_tituloLibro,
                        String p_fechaRetiroTexto, String p_fechaVencimientoTexto,
                        EstadoPrestamo p_estado) {
        this.prestamo = p_prestamo;
        this.nombreSocio = p_nombreSocio;
        this.tituloLibro = p_tituloLibro;
        this.fechaRetiroTexto = p_fechaRetiroTexto;
        this.fechaVencimientoTexto = p_fechaVencimientoTexto;
        this.estado = p_estado;
    }

    public Prestamo getPrestamo() { return this.prestamo; }
    public String getNombreSocio() { return this.nombreSocio; }
    public String getTituloLibro() { return this.tituloLibro; }
    public String getFechaRetiroTexto() { return this.fechaRetiroTexto; }
    public String getFechaVencimientoTexto() { return this.fechaVencimientoTexto; }
    public EstadoPrestamo getEstado() { return this.estado; }
}
