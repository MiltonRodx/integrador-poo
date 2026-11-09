
package MiBiblioteca;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// Splash de 5 segundos, saltable: con un click se corta la espera y
// arranca la GUI en el momento. La barra queda igual.
public class PantallaCarga extends JFrame {
    private Timer temporizador;
    private boolean salteado;

    private final Runnable alFinalizar;

    public PantallaCarga(Runnable p_alFinalizar) {
        this.alFinalizar = p_alFinalizar;

        this.salteado = false;

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

        // Aviso para que se sepa que el splash se puede saltear
        JLabel sugerencia = new JLabel("Hace click para continuar");
        sugerencia.setFont(new Font("Arial", Font.ITALIC, 12));
        sugerencia.setForeground(new Color(130, 165, 210));
        sugerencia.setAlignmentX(Component.CENTER_ALIGNMENT);
        sugerencia.setBorder(
            BorderFactory.createEmptyBorder(18, 0, 0, 0)
        );

        panel.add(titulo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(mensaje);
        panel.add(Box.createVerticalStrut(30));
        panel.add(progreso);
        panel.add(sugerencia);

        this.add(panel);
        this.escuchar(this);
        this.setVisible(true);

        temporizador = new Timer(5000, evento -> { this.saltar(); });

        temporizador.setRepeats(false);
        temporizador.start();
    }

    // Cierra el splash y abre la GUI; si ya se despidio, no vuelve a pasar
    private void saltar() {
        if(this.salteado) {
            return;
        }

        this.salteado = true;
        this.temporizador.stop();
        this.dispose();
        this.alFinalizar.run();
    }

    // Escuchar el click (o tecla) en TODOS los componentes del splash,
    // asi se salta aunque se clique sobre el titulo o la barra
    private void escuchar(Container p_contenedor) {
        p_contenedor.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent evento) {
                saltar();
            }
        });

        for(Component componente : p_contenedor.getComponents())
        {
            componente.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent evento) {
                    saltar();
                }
            });

            if(componente instanceof Container)
            {
                this.escuchar((Container) componente);
            }
        }
    }
}
