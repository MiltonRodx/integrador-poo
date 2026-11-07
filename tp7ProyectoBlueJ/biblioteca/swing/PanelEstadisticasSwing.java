package biblioteca.swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.time.LocalDate;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import biblioteca.Libro;
import biblioteca.SesionBiblioteca;
import biblioteca.Socio;

/**
 * Panel de estadisticas (Swing): 4 tarjetas (libros / socios / activos /
 * vencidos) y un grafico circular del catalogo. Se refresca solo ante
 * cualquier cambio del estado de la sesion.
 *
 * Equivalente del PanelEstadisticas.fxml + controller JavaFX: las tarjetas
 * son JPanels con borde redondeado y chip de color pintado a mano, y el
 * PieChart es un JComponent con fillArc (Swing no trae charts).
 */
public class PanelEstadisticasSwing extends JPanel {

    private final ServicioEstadisticas servicioEstadisticas;
    private final ConsultasBiblioteca consultas;
    private final ConversorDeFechas conversorDeFechas;
    private final SesionBiblioteca sesion;

    private final JLabel etiquetaFecha = new JLabel("estado al dia de hoy", JLabel.CENTER);
    private final Tarjeta tarjetaLibros =
        new Tarjeta("\uD83D\uDCDA", "Libros", Tema.CHIP_AZUL);
    private final Tarjeta tarjetaSocios =
        new Tarjeta("\uD83D\uDC65", "Socios", Tema.CHIP_VERDE);
    private final Tarjeta tarjetaActivos =
        new Tarjeta("\uD83D\uDCC5", "Activos", Tema.CHIP_VIOLETA);
    private final Tarjeta tarjetaVencidos =
        new Tarjeta("⚠️", "Vencidos", Tema.CHIP_ROJO);
    private final GraficoTorta grafico = new GraficoTorta();
    private final JLabel etiquetaDisponibles = new JLabel("Disponibles: -");
    private final JLabel etiquetaEnPrestamo = new JLabel("En préstamo: -");

    public PanelEstadisticasSwing(ServicioEstadisticas p_servicioEstadisticas,
                                  ConsultasBiblioteca p_consultas,
                                  SesionBiblioteca p_sesion) {
        super(new GridBagLayout());
        this.servicioEstadisticas = p_servicioEstadisticas;
        this.consultas = p_consultas;
        this.conversorDeFechas = new ConversorDeFechas();
        this.sesion = p_sesion;

        this.setOpaque(true);
        this.setBackground(Tema.colorFondo());

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.gridy = 0; c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(18, 18, 6, 18);

        // ---- encabezado: titulo + fecha de referencia ----
        JPanel encabezado = new JPanel(new GridBagLayout());
        encabezado.setOpaque(false);
        JLabel titulo = new JLabel("Resumen de la Biblioteca", JLabel.CENTER);
        titulo.setFont(titulo.getFont().deriveFont(Font.BOLD, 24f));
        titulo.setForeground(Tema.colorTexto());
        this.etiquetaFecha.setFont(this.etiquetaFecha.getFont().deriveFont(13f));
        this.etiquetaFecha.setForeground(Tema.colorTextoSuave());
        GridBagConstraints e = new GridBagConstraints();
        e.gridx = 0; e.weightx = 1;
        encabezado.add(titulo, e);
        e.gridy = 1;
        encabezado.add(this.etiquetaFecha, e);
        this.add(encabezado, c);

        // ---- fila de 4 tarjetas: libros / socios / activos / vencidos ----
        JPanel filaTarjetas = new JPanel(new java.awt.GridLayout(1, 4, 12, 12));
        filaTarjetas.setPreferredSize(new Dimension(0, 120));
        filaTarjetas.setMinimumSize(new Dimension(520, 120));
        filaTarjetas.setOpaque(false);
        filaTarjetas.add(this.tarjetaLibros);
        filaTarjetas.add(this.tarjetaSocios);
        filaTarjetas.add(this.tarjetaActivos);
        filaTarjetas.add(this.tarjetaVencidos);
        c.gridy = 1; c.insets = new Insets(6, 18, 6, 18);
        this.add(filaTarjetas, c);

        // ---- tarjeta del grafico: donut + leyenda de detalle ----
        JPanel cajaGrafico = new JPanel(new GridBagLayout());
        cajaGrafico.setOpaque(false);
        cajaGrafico.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(6, 18, 18, 18),
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.colorBorde(), 1, true),
                BorderFactory.createEmptyBorder(14, 14, 14, 14))));
        JLabel tituloDetalle = new JLabel("Detalle del catálogo");
        tituloDetalle.setFont(tituloDetalle.getFont().deriveFont(Font.BOLD, 14f));
        tituloDetalle.setForeground(Tema.colorTexto());
        this.etiquetaDisponibles.setForeground(Tema.colorTexto());
        this.etiquetaEnPrestamo.setForeground(Tema.colorTexto());
        JPanel leyenda = new JPanel(new GridBagLayout());
        leyenda.setOpaque(false);
        GridBagConstraints l = new GridBagConstraints();
        l.gridx = 0; l.gridy = 0; l.weightx = 1;
        l.anchor = GridBagConstraints.LINE_START;
        leyenda.add(tituloDetalle, l);
        l.gridy = 1; l.insets = new Insets(10, 0, 0, 0);
        leyenda.add(this.etiquetaDisponibles, l);
        l.gridy = 2; l.insets = new Insets(4, 0, 0, 0);
        leyenda.add(this.etiquetaEnPrestamo, l);
        GridBagConstraints gg = new GridBagConstraints();
        gg.gridx = 0; gg.weightx = 1; gg.weighty = 1;
        gg.fill = GridBagConstraints.BOTH;
        cajaGrafico.add(this.grafico, gg);
        gg.gridx = 1; gg.weightx = 0;
        gg.fill = GridBagConstraints.NONE;
        gg.anchor = GridBagConstraints.NORTHWEST;
        gg.insets = new Insets(0, 24, 0, 0);
        cajaGrafico.add(leyenda, gg);
        c.gridy = 2; c.weighty = 1; c.insets = new Insets(6, 18, 18, 18);
        c.fill = GridBagConstraints.BOTH;
        this.add(cajaGrafico, c);

        this.consultas.alCambiarLosDatos(this::refrescar);
        this.refrescar();
    }

    /** Recalcula los numeros y repinta el donut. */
    public void refrescar() {
        EstadisticasBiblioteca est = this.servicioEstadisticas.calcular();
        this.etiquetaFecha.setText("Estado al "
            + this.conversorDeFechas.aTextoCorto(LocalDate.now()));

        this.tarjetaLibros.setValores(String.valueOf(est.totalLibros()),
            est.librosDisponibles() + " disponibles");
        this.tarjetaSocios.setValores(String.valueOf(est.totalSocios()),
            est.sociosDocentes() + " docentes, "
                + est.sociosEstudiantes() + " estudiantes");
        this.tarjetaActivos.setValores(String.valueOf(est.prestamosActivos()),
            "con préstamo vigente");
        this.tarjetaVencidos.setValores(String.valueOf(est.prestamosVencidos()),
            est.prestamosVencidos() > 0 ? "requieren devolución" : "todo en orden");

        this.etiquetaDisponibles.setText("Disponibles: " + est.librosDisponibles());
        this.etiquetaEnPrestamo.setText("En préstamo: "
            + (est.totalLibros() - est.librosDisponibles()));

        this.grafico.setValores(est.librosDisponibles(),
            est.totalLibros() - est.librosDisponibles());
    }

    // ------------------------------------------------------------------
    // Ganchos para la prueba (sin ventanas modales)
    // ------------------------------------------------------------------
    /** Valores de las 4 tarjetas: {libros, socios, activos, vencidos}. */
    String[] valoresDeTarjetas() {
        return new String[]{ this.tarjetaLibros.valor.getText(),
            this.tarjetaSocios.valor.getText(),
            this.tarjetaActivos.valor.getText(),
            this.tarjetaVencidos.valor.getText() };
    }

    /** Leyenda del donut: {disponibles, en préstamo}. */
    String[] textosDeLeyenda() {
        return new String[]{ this.etiquetaDisponibles.getText(),
            this.etiquetaEnPrestamo.getText() };
    }

    /** Fecha de referencia de la cabecera. */
    String fechaDeReferencia() { return this.etiquetaFecha.getText(); }

    /** Presta vía el socio/libro para provocar un cambio del estado (prueba). */
    String prestaParaPrueba(Socio p_socio, Libro p_libro) {
        try {
            new ServicioPrestamos(this.sesion, this.conversorDeFechas,
                new ReglasDePlazo(this.conversorDeFechas))
                .registrarPrestamo(p_socio, p_libro, LocalDate.now());
            return null;
        } catch (PrestamoRechazadoException | RuntimeException e) {
            return e.getMessage();
        }
    }

    /** Valores del donut: {disponibles, en préstamo}. */
    int[] valoresDelPastel() {
        return new int[]{ this.grafico.disponibles, this.grafico.enPrestamo };
    }

    // ------------------------------------------------------------------
    // Tarjeta de estadistica (equivalente de .tarjeta-estadistica + chips)
    // ------------------------------------------------------------------
    private static class Tarjeta extends JPanel {

        private final String titulo;
        private final ChipChip chip;
        private final JLabel valor = new JLabel("-");
        private final JLabel detalle = new JLabel(" ");

        Tarjeta(String p_icono, String p_titulo, Color p_colorChip) {
            super(new GridBagLayout());
            this.titulo = p_titulo;
            this.setOpaque(false);
            this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Tema.colorBorde(), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
            this.setMinimumSize(new Dimension(130, 118));

            this.chip = new ChipChip(p_icono, p_colorChip);
            JLabel tituloLabel = new JLabel(p_titulo);
            tituloLabel.setFont(tituloLabel.getFont().deriveFont(11f));
            tituloLabel.setForeground(Tema.colorTextoSuave());
            this.valor.setFont(this.valor.getFont().deriveFont(Font.BOLD, 26f));
            this.valor.setForeground(Tema.colorTexto());
            this.detalle.setFont(this.detalle.getFont().deriveFont(10f));
            this.detalle.setForeground(Tema.colorTextoSuave());

            JPanel textos = new JPanel(new GridBagLayout());
            textos.setOpaque(false);
            GridBagConstraints t = new GridBagConstraints();
            t.gridx = 0; t.weightx = 1; t.fill = GridBagConstraints.HORIZONTAL;
            textos.add(tituloLabel, t);
            t.gridy = 1;
            textos.add(this.valor, t);
            t.gridy = 2;
            textos.add(this.detalle, t);

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.NORTHWEST;
            c.insets = new Insets(0, 0, 0, 12);
            this.add(this.chip, c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 0, 0, 0);
            this.add(textos, c);
        }

        void setValores(String p_valor, String p_detalle) {
            this.valor.setText(p_valor);
            this.detalle.setText(p_detalle);
        }
    }

    /** El chip cuadrado-redondeado con el icono (equivale de .chip-estadistica). */
    private static class ChipChip extends JLabel {

        private final Color colorChip;

        ChipChip(String p_icono, Color p_colorChip) {
            super(p_icono, JLabel.CENTER);
            this.colorChip = p_colorChip;
            this.setPreferredSize(new Dimension(46, 46));
            this.setMaximumSize(new Dimension(46, 46));
            this.setFont(this.getFont().deriveFont(20f));
            this.setForeground(Tema.colorTexto());
            this.setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Tema.oscuro() ? Tema.chipOscuro(this.colorChip) : this.colorChip);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 12, 12);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    // ------------------------------------------------------------------
    // El grafico circular (equivalente del PieChart) con Graphics2D
    // ------------------------------------------------------------------
    static class GraficoTorta extends JComponent {

        private int disponibles = 0;
        private int enPrestamo = 0;

        void setValores(int p_disponibles, int p_enPrestamo) {
            this.disponibles = p_disponibles;
            this.enPrestamo = p_enPrestamo;
            this.repaint();
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            int lado = Math.min(getWidth(), getHeight());
            int diametro = Math.max(20, lado - 24);
            int x = (getWidth() - diametro) / 2;
            int y = (getHeight() - diametro) / 2;
            int total = this.disponibles + this.enPrestamo;

            if (total > 0) {
                // Disponibles arranca arriba (como el PieChart de JavaFX)
                double gradosLibres = 360.0 * this.disponibles / total;
                g2.setPaint(Tema.PIE_DISPONIBLES);
                g2.fillArc(x, y, diametro, diametro, 90, -(int) Math.round(gradosLibres));
                g2.setPaint(Tema.PIE_EN_PRESTAMO);
                g2.fillArc(x, y, diametro, diametro,
                    90 - (int) Math.round(gradosLibres),
                    -360 + (int) Math.round(gradosLibres));
                // agujero del donut (mas limpio que el pastel plano)
                int agujero = diametro / 3;
                g2.setPaint(Tema.colorFondo());
                g2.fillArc(x + agujero / 2, y + agujero / 2, diametro - agujero,
                    diametro - agujero, 0, 360);
                // total al centro
                g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
                g2.setColor(Tema.colorTexto());
                String centro = String.valueOf(total);
                int textoW = g2.getFontMetrics().stringWidth(centro);
                g2.drawString(centro,
                    x + (diametro - textoW) / 2,
                    y + diametro / 2 + g2.getFontMetrics().getAscent() / 3);
            } else {
                g2.setColor(Tema.colorTextoSuave());
                g2.drawArc(x, y, diametro, diametro, 0, 360);
            }
            g2.dispose();
        }

        @Override public Dimension getPreferredSize() { return new Dimension(270, 210); }
    }
}
