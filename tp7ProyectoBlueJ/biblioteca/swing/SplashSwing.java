package biblioteca.swing;

import java.awt.AlphaComposite;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferStrategy;
import javax.swing.JDialog;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Splash de arranque (versión Swing del Splash JavaFX).
 *
 * - JDialog SIN borde ni fondo: solo se ve la "tarjeta" azul con el libro.
 * - Render ACTIVO con Canvas + BufferStrategy (doble buffer que no depende del
 *   RepaintManager de Swing): es lo que evita el parpadeo/glitch de las
 *   ventanas translúcidas en X11 cuando se repintan 30 veces por segundo.
 * - Animación del libro 2D con Timer (la versión JavaFX usaba perspectiva 3D
 *   real; en Swing se dura con rotación alrededor del lomo: misma idea visual
 *   con la técnica que puede el toolkit).
 * - Duración ~2.5 s; click en cualquier parte para saltar.
 * - Al terminar entrega el control a la ventana principal (Runnable alListo).
 */
public class SplashSwing {

    private static final int ANCHO = 460;
    private static final int ALTO = 300;

    private final JDialog ventana = new JDialog((java.awt.Frame) null, false);
    private final Runnable alListo;

    private Canvas lienzo;
    private Pintor pintor;
    private Timer linea;
    private boolean pasandoEscena;
    private long inicioAnimacion;

    public SplashSwing(Runnable p_alListo) { this.alListo = p_alListo; }

    /** Construye la tarjeta, arma la animación y muestra el splash. */
    public void mostrar() {
        this.pintor = new Pintor();

        this.lienzo = new Canvas();
        this.lienzo.setIgnoreRepaint(true);   // el buffer strategy manda, no Swing
        this.lienzo.setPreferredSize(new Dimension(ANCHO, ALTO));

        this.ventana.setUndecorated(true);
        this.ventana.setBackground(new Color(0, 0, 0, 0));   // tarjeta "flotando"
        this.ventana.setAlwaysOnTop(true);                   // nada la tapa durante la animación
        this.ventana.getContentPane().add(this.lienzo);
        this.ventana.pack();
        this.ventana.setShape(new java.awt.geom.RoundRectangle2D.Double(
            0, 0, ANCHO, ALTO, 22, 22));
        this.ventana.setLocationRelativeTo(null);
        this.ventana.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        this.ventana.addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { SplashSwing.this.terminar(); }
        });
        this.ventana.setVisible(true);
        this.lienzo.createBufferStrategy(2);

        this.inicioAnimacion = System.currentTimeMillis();
        líneaDeTiempo();
        this.linea.start();
    }

    /**
     * Timer de 33ms (~30fps) que avanza la animación: la tapa se abre entre
     * 250ms y 1450ms (con cubic ease-out, igual que el Timeline JavaFX) y los
     * títulos aparecen entre 1350ms y 1900ms. En 2200ms termina.
     *
     * Cada tick dibuja DIRECTO en el back-buffer y muestra con bs.show(): sin
     * pasar por el pipeline de repaint de Swing (que es lo que parpadeaba).
     */
    private void líneaDeTiempo() {
        this.linea = new Timer(33, e -> {
            long ms = System.currentTimeMillis() - this.inicioAnimacion;
            dibuja(Math.min(ms, 2200));
            if (ms >= 2200) {
                this.linea.stop();
                this.terminar();
            }
        });
    }

    /** Vuelca un frame del splash al back-buffer y lo muestra. */
    private void dibuja(long p_ms) {
        BufferStrategy buffer = this.lienzo.getBufferStrategy();
        if (buffer == null) return;
        do {
            do {
                Graphics2D g2 = (Graphics2D) buffer.getDrawGraphics();
                this.pintor.pinta(g2, p_ms);
                g2.dispose();
            } while (buffer.contentsRestored());
            buffer.show();
        } while (buffer.contentsLost());
    }

    /** Fundido de salida y pase de control a la ventana principal. */
    private void terminar() {
        if (this.pasandoEscena || this.pintor == null) return;
        this.pasandoEscena = true;
        if (this.linea != null && this.linea.isRunning()) this.linea.stop();

        Timer salida = new Timer(16, null);
        long desde = System.currentTimeMillis();
        salida.addActionListener(e -> {
            long t = System.currentTimeMillis() - desde;
            if (t >= 300) {
                salida.stop();
                this.ventana.dispose();
                SwingUtilities.invokeLater(this.alListo);
                return;
            }
            this.pintor.setAlfa(1f - t / 300f);
            dibuja(this.pintor.tiempo());
        });
        salida.start();
    }

    // =====================================================================
    // Pintor del splash: mismo dibujo que pinta la tarjeta de la versión
    // JavaFX (fondo azul degradado + libro + títulos), frame a frame.
    // =====================================================================
    static class Pintor {

        private long tiempo = 0;    // progreso 0..2.2s
        private float alfa = 1f;    // alfa global del fundido de salida

        void setAlfa(float p_alfa) { this.alfa = Math.max(0f, Math.min(1f, p_alfa)); }
        long tiempo() { return this.tiempo; }

        void pinta(Graphics2D g2, long p_ms) {
            this.tiempo = p_ms;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

            // ---- fade de ENTRADA de la tarjeta: primeros 120ms ----
            float entrada = Math.min(1f, p_ms / 120f);
            g2.setComposite(AlphaComposite.SrcOver.derive(this.alfa * entrada));
            g2.setPaint(new java.awt.GradientPaint(0, 0, Tema.AZUL_OSCURO, 0, ALTO,
                Tema.AZUL_MEDIO));
            g2.fill(new java.awt.geom.RoundRectangle2D.Double(0, 0, ANCHO, ALTO, 22, 22));

            this.pintaLibro(g2);
            this.pintaTitulos(g2);
        }

        /** El libro: hojas abanicadas + tapa trasera + tapa frontal que gira. */
        private void pintaLibro(Graphics2D g2) {
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
            int centroX = ANCHO / 2;
            int arribaY = 44;
            int anchoTapa = 104;
            int altoTapa = 132;

            double rotado = this.tiempo < 250 ? 0
                : suaviza(Math.min(1f, (this.tiempo - 250f) / 1200f)) * -105;

            g2.translate(centroX, arribaY + altoTapa / 2.0);

            // Toda la proyección del giro 3D sobre el eje del lomo se reduce a
            // una escala horizontal cos(angulo) alrededor de x=0: la tapa "se
            // achata" al girar y "sale por el otro lado" al pasar los 90°.
            //
            // hojas interiores (leve abanico, dan grosor)
            g2.setPaint(Tema.CREMA_HOJA);
            for (int i = 0; i < 3; i++) {
                double s = Math.cos(Math.toRadians(8 + 7 * (2 - i)));
                g2.fillRoundRect(2, -altoTapa / 2 + 6,
                    Math.max(1, (int) Math.round((anchoTapa - 6) * s)), altoTapa - 12, 8, 8);
            }
            // tapa trasera, fija abierta 24° (la proyeccion la achata un poco)
            g2.setPaint(Tema.CUERO_TAPA2);
            double sTrasera = Math.cos(Math.toRadians(24));
            g2.fillRoundRect(0, -altoTapa / 2,
                Math.max(1, (int) Math.round(anchoTapa * sTrasera)), altoTapa, 10, 10);
            // tapa frontal: de s=1 (cerrada) a s=cos(-105)=-0.26 (volteada)
            double sFrontal = Math.cos(Math.toRadians(rotado));
            double anchoFrontal = anchoTapa * sFrontal;
            g2.setPaint(sFrontal >= 0 ? Tema.CUERO_TAPA1 : Tema.CUERO_TAPA2);
            g2.fillRoundRect((int) Math.round(Math.min(0, anchoFrontal)),
                -altoTapa / 2,
                Math.max(1, (int) Math.round(Math.abs(anchoFrontal))), altoTapa, 10, 10);
            // cerradura dorada junto al borde libre de la tapa (desaparece al
            // cruzar la mitad del giro: "se soltó el broche")
            if (sFrontal >= 0.35) {
                int bordeLibre = (int) Math.round(anchoFrontal);
                g2.setPaint(Tema.DORADO);
                g2.fillRoundRect(bordeLibre - 34, altoTapa / 2 - 22, 18, 12, 4, 4);
            }

            g2.translate(-centroX, -(arribaY + altoTapa / 2.0));
        }

        /** Títulos: aparecen con leve subida entre 1350ms y 1900ms. */
        private void pintaTitulos(Graphics2D g2) {
            float progresoTitulos = this.tiempo < 1350 ? 0f
                : suaviza(Math.min(1f, (this.tiempo - 1350f) / 550f));
            if (progresoTitulos <= 0) return;
            g2.setComposite(AlphaComposite.SrcOver.derive(this.alfa * progresoTitulos));

            Font tituloFont = new Font(Font.SANS_SERIF, Font.BOLD, 26);
            Font subFont = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
            g2.setFont(tituloFont);
            String titulo = "Gestión de Biblioteca";
            int anchoT = g2.getFontMetrics(tituloFont).stringWidth(titulo);
            int yTitulo = ALTO - 44 + (int) (12 * (1 - progresoTitulos));
            g2.setPaint(Tema.CREMA_TEXTO);
            g2.drawString(titulo, (ANCHO - anchoT) / 2, yTitulo);

            g2.setFont(subFont);
            String sub = "FaCENA · UNNE · Equipo 49";
            int anchoS = g2.getFontMetrics(subFont).stringWidth(sub);
            g2.setPaint(Tema.AZUL_SUBTIT);
            g2.drawString(sub, (ANCHO - anchoS) / 2, ALTO - 20);
        }

        /** Ease-out cúbico (equivalente del Interpolator.EASE_OUT del Timeline). */
        private float suaviza(float p_x) {
            return 1f - (float) Math.pow(1f - p_x, 3.0);
        }
    }
}
