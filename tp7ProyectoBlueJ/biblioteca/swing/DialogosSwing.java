package biblioteca.swing;

import java.awt.Component;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Optional;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

/**
 * Ventanitas de apoyo para la GUI Swing (equivalente del Dialogos JavaFX).
 *
 * - formulario(): dialogo generico modal de N campos de texto; devuelve los
 *   valores si el usuario confirma con OK, o vacio si cancela. La validacion
 *   de TIPO (enteros, etc.) queda en el panel que conoce su dominio.
 * - error()/aviso()/informacion(): alertas de una sola linea de invocacion.
 * - confirm(): pregunta destructiva para las bajas.
 */
public class DialogosSwing {

    /** Formulario generico: N etiquetas -> devuelve N Strings (trim) si OK. */
    public static Optional<String[]> formulario(Component p_dueno, String p_titulo,
            String p_cabecera, String... p_etiquetas) {
        Frame dueno = duenoDe(p_dueno);

        JPanel contenido = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 2, 8);
        c.anchor = GridBagConstraints.LINE_END;

        JTextField[] campos = new JTextField[p_etiquetas.length];
        for (int i = 0; i < p_etiquetas.length; i++) {
            campos[i] = new JTextField(22);
            c.gridx = 0; c.gridy = i; c.weightx = 0; c.fill = GridBagConstraints.NONE;
            contenido.add(new JLabel(p_etiquetas[i] + ": "), c);
            c.gridx = 1; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
            contenido.add(campos[i], c);
        }

        JLabel cabecera = new JLabel(p_cabecera);
        cabecera.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 4, 10));

        Object[] opciones = { "OK", "Cancelar" };
        JOptionPane avisador = new JOptionPane(new Object[]{ cabecera, contenido },
            JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null, opciones, opciones[1]);
        JDialog cartel = avisador.createDialog(dueno, p_titulo);
        cartel.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        // foco en el primer campo para escribir directo
        cartel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                campos[0].requestFocusInWindow();
            }
        });
        cartel.setLocationRelativeTo(p_dueno);
        cartel.setVisible(true);

        Optional<String[]> resultado = Optional.empty();
        if (avisador.getValue() != null && avisador.getValue().equals(opciones[0])) {
            resultado = Optional.of(valoresDe(campos));
        }
        cartel.dispose();
        return resultado;
    }

    /** Construye el array de valores a partir de los campos escritos. */
    private static String[] valoresDe(JTextField[] p_campos) {
        String[] valores = new String[p_campos.length];
        for (int i = 0; i < p_campos.length; i++) valores[i] = p_campos[i].getText().trim();
        return valores;
    }

    /** Alerta roja de error (ej. dato invalido en un formulario). */
    public static void error(Component p_dueno, String p_mensaje) {
        JOptionPane.showMessageDialog(p_dueno, p_mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Alerta de advertencia (ej. D.N.I. ya existente). */
    public static void aviso(Component p_dueno, String p_mensaje) {
        JOptionPane.showMessageDialog(p_dueno, p_mensaje, "Atencion", JOptionPane.WARNING_MESSAGE);
    }

    /** Alerta informativa (ej. operacion exitosa). */
    public static void informacion(Component p_dueno, String p_mensaje) {
        JOptionPane.showMessageDialog(p_dueno, p_mensaje, "Informacion",
            JOptionPane.INFORMATION_MESSAGE);
    }

    /** Pregunta destructiva (bajas): true solo con OK/Si. */
    public static boolean confirm(Component p_dueno, String p_mensaje) {
        return JOptionPane.showConfirmDialog(p_dueno, p_mensaje, "Confirmar",
            JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.OK_OPTION;
    }

    /** Frame dueño del dialogo (para hijos de panel, sube hasta la ventana). */
    private static Frame duenoDe(Component p_componente) {
        if (p_componente == null) return null;
        if (p_componente instanceof Frame) return (Frame) p_componente;
        return (Frame) javax.swing.SwingUtilities.getAncestorOfClass(Frame.class, p_componente);
    }
}
