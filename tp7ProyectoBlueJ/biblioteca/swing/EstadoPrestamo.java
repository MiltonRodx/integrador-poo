package biblioteca.swing;

/** Estado visible de un prestamo, con el texto y la clase CSS de su etiqueta. */
public enum EstadoPrestamo {

    EN_PLAZO("En plazo", "estado-en-plazo"),
    POR_VENCER("Por vencer", "estado-por-vencer"),
    VENCIDO("Vencido", "estado-vencido"),
    DEVUELTO("Devuelto", "estado-devuelto");

    private final String etiqueta;
    private final String claseCss;

    EstadoPrestamo(String p_etiqueta, String p_claseCss) {
        this.etiqueta = p_etiqueta;
        this.claseCss = p_claseCss;
    }

    public String getEtiqueta() {
        return this.etiqueta;
    }

    public String getClaseCss() {
        return this.claseCss;
    }
}
