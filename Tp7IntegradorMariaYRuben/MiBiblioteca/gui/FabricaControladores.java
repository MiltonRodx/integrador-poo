package MiBiblioteca.gui;

import javafx.util.Callback;

/**
 * Unico lugar donde se arman los objetos y se les inyectan sus dependencias.
 * Los controllers no crean servicios: los reciben.
 */
public class FabricaControladores implements Callback<Class<?>, Object> {

    private final ConsultasBiblioteca consultas;
    private final ServicioPrestamos servicioPrestamos;
    private final ServicioEstadisticas servicioEstadisticas;
    private final ConversorDeFechas conversorDeFechas;
    private final CuadrosDeDialogo cuadrosDeDialogo;

    public FabricaControladores(SesionBiblioteca p_sesionBiblioteca) {
        this.conversorDeFechas = new ConversorDeFechas();
        this.cuadrosDeDialogo = new CuadrosDeDialogo();
        this.consultas = new ConsultasBiblioteca(p_sesionBiblioteca);
        ReglasDePlazo reglasDePlazo = new ReglasDePlazo(this.conversorDeFechas);
        this.servicioPrestamos = new ServicioPrestamos(
            p_sesionBiblioteca, this.consultas, this.conversorDeFechas, reglasDePlazo);
        this.servicioEstadisticas = new ServicioEstadisticas(this.consultas);
    }

    @Override
    public Object call(Class<?> p_tipoDeControlador) {
        if (p_tipoDeControlador == PrincipalController.class) {
            return new PrincipalController(this);
        }
        if (p_tipoDeControlador == PrestamosController.class) {
            return new PrestamosController(this.servicioPrestamos, this.consultas,
                this.conversorDeFechas, this.cuadrosDeDialogo);
        }
        if (p_tipoDeControlador == PanelEstadisticasController.class) {
            return new PanelEstadisticasController(this.servicioEstadisticas, this.consultas);
        }
        if (p_tipoDeControlador == SociosController.class) {
            return new SociosController(this.consultas);
        }
        if (p_tipoDeControlador == CatalogoController.class) {
            return new CatalogoController(this.consultas);
        }
        throw new IllegalStateException("Controlador inesperado: " + p_tipoDeControlador);
    }
}
