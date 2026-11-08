
package MiBiblioteca;

import javax.swing.*;
import java.awt.*;

public class PantallaCarga extends JFrame
{
    public PantallaCarga(Runnable alFinalizar)
    {
        this.setTitle("Gestion De Bibloteca");
        this.setSize(450, 250);
        this.setLocationRelativeTo(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(15, 30, 60));
        panel.setBorder(
            BorderFactory.createEmptyBorder(35, 25, 30, 25)
        );

        JLabel titulo = new JLabel("Gestion De Bibloteca - UNNE");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(Color.WHITE);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel mensaje = new JLabel("Iniciando...");
        mensaje.setFont(new Font("Arial", Font.PLAIN, 17));
        mensaje.setForeground(new Color(210, 225, 245));
        mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

        JProgressBar progreso = new JProgressBar();
        progreso.setIndeterminate(true);
        progreso.setForeground(new Color(100, 155, 225));
        progreso.setBackground(Color.WHITE);
        progreso.setMaximumSize(new Dimension(300, 12));

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(mensaje);
        panel.add(Box.createVerticalStrut(30));
        panel.add(progreso);

        this.add(panel);
        this.setVisible(true);

        Timer temporizador = new Timer(5000, evento -> {
            this.dispose();
            alFinalizar.run();
        });

        temporizador.setRepeats(false);
        temporizador.start();
    }
}
