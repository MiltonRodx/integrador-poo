package biblioteca.swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.table.JTableHeader;

/**
 * Paleta y estilo de la GUI Swing (equivalente nativo de tema.css + AtlantaFX).
 *
 * - Se instala Nimbus (incluido en el JDK, cero dependencias externas) y se
 *   retocan las claves UIManager para acercarse al look de la GUI JavaFX.
 * - Los colores de pildoras/chips/tarjetas viven aca como constantes, para que
 *   los renderers pinten igual que el CSS de la version JavaFX.
 * - aplicarTema(oscuro, raiz) recorre el arbol de componentes y reajusta
 *   fondos/textos al modo claro u oscuro (toggle de la ventana principal).
 */
public class Tema {

    /** Modo actual: false = claro (por defecto como en App.java JavaFX). */
    private static boolean modoOscuro = false;

    /** Componentes raiz que deben re-estilizarse al cambiar el tema. */
    private static final List<Component> raices = new ArrayList<>();

    // ------------------------
    // Paleta (modo claro)
    // ------------------------
    public static final Color AZUL_OSCURO   = new Color(0x16294a); // splash
    public static final Color AZUL_MEDIO    = new Color(0x203a66);
    public static final Color CUERO_TAPA1   = new Color(0xa63f32); // libro splash
    public static final Color CUERO_TAPA2   = new Color(0x7e2f26);
    public static final Color CREMA_HOJA    = new Color(0xefe6d4);
    public static final Color DORADO        = new Color(0xf0c987);
    public static final Color CREMA_TEXTO   = new Color(0xf5f2ea);
    public static final Color AZUL_SUBTIT   = new Color(0xa9c0e0);

    public static final Color PIE_DISPONIBLES  = new Color(0x0ea5e9);
    public static final Color PIE_EN_PRESTAMO  = new Color(0x22c55e);

    /** Colores de la píldora de estado (light/dark) por clase CSS de EstadoPrestamo: {fondo, texto}. */
    public static Color[] coloresDePildora(EstadoPrestamo p_estado, boolean p_oscuro) {
        switch (p_estado) {
            case EN_PLAZO:    return p_oscuro
                ? new Color[]{ alfa(22, 101, 52, 0.45f),   new Color(0xbbf7d0) }
                : new Color[]{ new Color(0xdcfce7),        new Color(0x166534) };
            case POR_VENCER:  return p_oscuro
                ? new Color[]{ alfa(133, 77, 14, 0.45f),   new Color(0xfef08a) }
                : new Color[]{ new Color(0xfef9c3),        new Color(0x854d0e) };
            case VENCIDO:     return p_oscuro
                ? new Color[]{ alfa(153, 27, 27, 0.50f),   new Color(0xfecaca) }
                : new Color[]{ new Color(0xfee2e2),        new Color(0x991b1b) };
            case DEVUELTO:    return p_oscuro
                ? new Color[]{ alfa(51, 65, 85, 0.75f),    new Color(0xcbd5e1) }
                : new Color[]{ new Color(0xe2e8f0),        new Color(0x334155) };
        }
        return new Color[]{ Color.GRAY, Color.WHITE };
    }

    /** Chip de las tarjetas de estadisticas (azul/verde/violeta/rojo). */
    public static final Color CHIP_AZUL    = new Color(0xdbeafe);
    public static final Color CHIP_VERDE   = new Color(0xdcfce7);
    public static final Color CHIP_VIOLETA = new Color(0xede9fe);
    public static final Color CHIP_ROJO    = new Color(0xfee2e2);
    /** Versiones atenuadas para el modo oscuro (el alfa esacala el color). */
    public static Color chipOscuro(Color p_claro) { return alfa(p_claro.getRed(),
        p_claro.getGreen(), p_claro.getBlue(), 0.38f); }

    /** Color rgba() equivalente al CSS: r,g,b con alfa 0..1. */
    public static Color alfa(int p_r, int p_g, int p_b, float p_alfa) {
        return new Color(p_r, p_g, p_b, Math.round(p_alfa * 255f));
    }

    // ------------------------
    // Paleta dinamica (claro/oscuro)
    // ------------------------
    public static boolean oscuro()      { return Tema.modoOscuro; }
    public static Color  colorFondo()   { return Tema.modoOscuro ? new Color(0x0d1117) : Color.WHITE; }
    public static Color  colorPanel()   { return Tema.modoOscuro ? new Color(0x161b22) : Color.WHITE; }
    public static Color  colorTexto()   { return Tema.modoOscuro ? new Color(0xe6edf3) : new Color(0x24292f); }
    public static Color  colorTextoSuave() { return Tema.modoOscuro ? new Color(0x8b949e) : new Color(0x57606a); }
    public static Color  colorBorde()   { return Tema.modoOscuro ? new Color(0x30363d) : new Color(0xd0d7de); }
    public static Color  colorAcento()  { return Tema.modoOscuro ? new Color(0x58a6ff) : new Color(0x0969da); }
    public static Color  colorTablaFila() { return Tema.modoOscuro ? new Color(0x161b22) : Color.WHITE; }
    public static Color  colorTablaSel(){ return Tema.modoOscuro ? new Color(0x1f3a5f) : new Color(0xdbeafe); }

    /** Instala Nimbus + retoques base. Se llama UNA vez al arrancar. */
    public static void instalar() {
        try {
            UIManager.setLookAndFeel(new javax.swing.plaf.nimbus.NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            // sin Nimbus se queda el L&F por defecto (versión degradada igual funciona)
        }
        // Retoques globales (equivalentes a los defaults de AtlantaFX):
        UIManager.put("control", Tema.colorFondo());
        UIManager.put("nimbusBase", Tema.colorFondo());
        UIManager.put("nimbusFocus", Tema.colorAcento());
        UIManager.put("nimbusSelectionBackground", Tema.colorAcento());
        UIManager.put("OptionPane.messageFont", new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        UIManager.put("OptionPane.buttonFont", new Font(Font.SANS_SERIF, Font.PLAIN, 12));
    }

    /** Registra una raiz (ventana) que se re-estiliza en cada cambio de tema. */
    public static void registrarRaiz(Component p_raiz) {
        Tema.raices.add(p_raiz);
        Tema.estilizar(p_raiz);
    }

    /** Cambia claro/oscuro re-estilizando todas las raices registradas. */
    public static void cambiarTema(boolean p_oscuro) {
        Tema.modoOscuro = p_oscuro;
        for (Component raiz : new ArrayList<>(Tema.raices)) {
            Tema.estilizar(raiz);
            raiz.revalidate();
            raiz.repaint();
        }
    }

    /** Recorre el arbol ajustando fondos y textos al modo vigente. */
    public static void estilizar(Component p_componente) {
        if (p_componente instanceof javax.swing.JRootPane) {
            javax.swing.JRootPane rootPane = (javax.swing.JRootPane) p_componente;
            rootPane.setBackground(Tema.colorFondo());
            if (rootPane.getContentPane() instanceof java.awt.Container) {
                Tema.estilizar(rootPane.getContentPane());
                return;
            }
        }
        if (p_componente instanceof JPanel) {
            JPanel panel = (JPanel) p_componente;
            // los paneles "tarjeta" se marcan con name y los respetamos
            if (!"tarjeta".equals(panel.getName())) {
                panel.setBackground(Tema.colorFondo());
            }
        }
        if (p_componente instanceof JLabel) {
            JLabel etiqueta = (JLabel) p_componente;
            String nombre = etiqueta.getName();
            if (nombre != null && nombre.startsWith("fijo-")) {
                return;   // labels con color propio (p.ej. contadores en negrita)
            }
            etiqueta.setForeground(Tema.colorTexto());
        }
        if (p_componente instanceof JButton) {
            // el fondo del boton Nimbus es claro en ambos modos: texto oscuro siempre
            ((JButton) p_componente).setForeground(new Color(0x24292f));
        }
        if (p_componente instanceof JScrollPane) {
            JScrollPane scroll = (JScrollPane) p_componente;
            scroll.getViewport().setBackground(Tema.colorFondo());
            scroll.setBackground(Tema.colorFondo());
        }
        if (p_componente instanceof JTable) {
            JTable tabla = (JTable) p_componente;
            tabla.setBackground(Tema.colorTablaFila());
            tabla.setForeground(Tema.colorTexto());
            tabla.setSelectionBackground(Tema.colorTablaSel());
            tabla.setSelectionForeground(Tema.colorTexto());
            tabla.setGridColor(Tema.colorBorde());
            JTableHeader cabecera = tabla.getTableHeader();
            if (cabecera != null) {
                cabecera.setBackground(Tema.colorFondo());
                cabecera.setForeground(Tema.colorTextoSuave());
                ((javax.swing.table.DefaultTableCellRenderer) cabecera
                    .getDefaultRenderer()).setOpaque(false);
            }
        }
        if (p_componente instanceof JTabbedPane) {
            JTabbedPane pestañas = (JTabbedPane) p_componente;
            pestañas.setBackground(Tema.colorFondo());
            pestañas.setForeground(Tema.colorTexto());
        }
        // para JScrollPane el contenido vive dentro del viewport:
        if (p_componente instanceof java.awt.Container) {
            for (Component hijo : ((java.awt.Container) p_componente).getComponents()) {
                Tema.estilizar(hijo);
            }
        }
    }
}
