
package biblioteca;

import javax.swing.*;
import java.awt.*;

public class UtilidadesInterfaz
{
    // Crea un campo con su titulo
    public static JPanel crearCampo(String p_titulo, JTextField p_campo)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel etiqueta = new JLabel(p_titulo);
        etiqueta.setFont(new Font("Arial", Font.BOLD, 13));
        etiqueta.setForeground(new Color(25, 45, 85));

        p_campo.setFont(new Font("Arial", Font.PLAIN, 13));
        p_campo.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 35)
        );

        panel.add(etiqueta);
        panel.add(Box.createVerticalStrut(5));
        panel.add(p_campo);

        return panel;
    }

    // Crea una cabecera con titulo y subtitulo
    public static JPanel crearCabecera(String p_titulo, String p_subtitulo)
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);

        JLabel titulo = new JLabel(p_titulo);
        titulo.setFont(new Font("Arial", Font.BOLD, 27));
        titulo.setForeground(new Color(25, 45, 85));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel(p_subtitulo);
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitulo.setForeground(Color.GRAY);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(5));
        panel.add(subtitulo);

        return panel;
    }

    // Muestra un mensaje de error
    public static void mostrarMensajeError(Component p_componente, String p_mensaje)
    {
        JOptionPane.showMessageDialog(
            p_componente,
            p_mensaje,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }

    // Muestra un mensaje de exito
    public static void mostrarMensajeExito(Component p_componente, String p_mensaje)
    {
        JOptionPane.showMessageDialog(
            p_componente,
            p_mensaje,
            "Operacion realizada",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}
