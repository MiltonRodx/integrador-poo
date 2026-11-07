package biblioteca.swing;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;
import biblioteca.SesionBiblioteca;

/**
 * Ventana principal: JTabbedPane de 5 pestañas + barra superior con el toggle
 * claro/oscuro (equivalente de la barra del MainWindow.fxml).
 *
 * Cada pestaña es un panel propio (PanelSocios, PanelLibros...) que se
 * suscribe a la sesion para refrescarse ante cualquier cambio del estado.
 * Al cerrar la ventana se libera la conexion SQLite.
 */
public class VentanaSwing extends JFrame {

    private final SesionBiblioteca sesion;

    /** Recibe la sesion ya abierta (AppSwing la carga en el worker). */
    public VentanaSwing(SesionBiblioteca p_sesion) {
        super("Biblioteca FaCENA - UNNE");
        this.sesion = p_sesion;
    }

    /** Arma el contenido, el toggle de tema y muestra la ventana. */
    public void mostrar() {
        this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        this.addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                VentanaSwing.this.sesion.cerrar();   // cierre ordenado de la base
                System.exit(0);
            }
        });

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setName("raiz");

        // ---- Barra superior: toggle claro/oscuro a la derecha ----
        JToggleButton toggleTema = new JToggleButton("🌙 Oscuro", false);
        toggleTema.setName("toggleTema");
        toggleTema.setToolTipText("Cambia el tema de la interfaz");
        toggleTema.addActionListener(e -> {
            boolean oscuro = toggleTema.isSelected();
            Tema.cambiarTema(oscuro);
            toggleTema.setText(oscuro ? "☀ Claro" : "🌙 Oscuro");
        });
        JPanel barra = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        barra.setOpaque(false);
        barra.add(toggleTema);
        raiz.add(barra, BorderLayout.NORTH);

        // ---- Pestañas ----
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
        pestañas.addTab("Socios", new PanelSocios(this.sesion));
        pestañas.addTab("Libros", new PanelLibros(this.sesion));
        pestañas.addTab("Préstamos", new PanelPrestamos(this.sesion,
            new ConsultasBiblioteca(this.sesion)));
        pestañas.addTab("Informes", new PanelInformes(this.sesion));
        ConsultasBiblioteca consultasH = new ConsultasBiblioteca(this.sesion);
        pestañas.addTab("Estadísticas", new PanelEstadisticasSwing(
            new ServicioEstadisticas(consultasH,
                new ReglasDePlazo(new ConversorDeFechas())),
            consultasH, this.sesion));
        raiz.add(pestañas, BorderLayout.CENTER);

        this.setContentPane(raiz);
        this.setSize(1024, 640);
        this.setPreferredSize(new java.awt.Dimension(1024, 640));
        this.setIconImage(icono());
        Tema.registrarRaiz(this);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }

    /** Busca un recurso en varios caminos (el primero que exista). */
    private static java.io.InputStream primero(String... p_caminos) {
        for (String camino : p_caminos) {
            java.io.InputStream flujo = VentanaSwing.class.getResourceAsStream(camino);
            if (flujo != null) return flujo;
        }
        return null;
    }

    /** Icono de la ventana (el mismo PNG que usaba la version JavaFX). */
    private static BufferedImage icono() {
        try (java.io.InputStream flujo = VentanaSwing.class
                .getResourceAsStream("/biblioteca/swing/icono.png")) {
            if (flujo != null) {
                return javax.imageio.ImageIO.read(flujo);
            }
        } catch (Exception e) {
            // sin icono no pasa nada: la imagen es decorativa
        }
        return new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    }

    /** Panel chico para los espacios no construidos todavia (fase en curso). */
    private static class Placeholder extends JPanel {
        Placeholder(String p_mensaje) {
            super(new GridLayout(1, 1));
            JLabel etiqueta = new JLabel(p_mensaje);
            etiqueta.setHorizontalAlignment(SwingConstants.CENTER);
            etiqueta.setForeground(Tema.colorTextoSuave());
            etiqueta.setName("fijo-" + etiqueta.getText());
            this.add(etiqueta);
            this.setName("tarjeta");
        }
    }
}
