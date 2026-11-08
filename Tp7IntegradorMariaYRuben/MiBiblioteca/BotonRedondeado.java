
package MiBiblioteca;

import javax.swing.*;
import java.awt.*;

public class BotonRedondeado extends JButton
{
    private Color colorNormal;
    private Color colorHover;
    private Color colorPresionado;

    public BotonRedondeado(String p_texto)
    {
        super(p_texto);

        colorNormal = new Color(25, 45, 85);
        colorHover = new Color(65, 100, 155);
        colorPresionado = new Color(15, 30, 60);

        this.setFont(new Font("Arial", Font.BOLD, 15));
        this.setForeground(Color.WHITE);
        this.setBackground(colorNormal);
        this.setFocusPainted(false);
        this.setBorderPainted(false);
        this.setContentAreaFilled(false);
        this.setOpaque(false);
        this.setRolloverEnabled(true);
        this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        this.setMargin(new Insets(10, 15, 10, 15));
    }

    public void cambiarColores(Color p_normal, Color p_hover, Color p_presionado)
    {
        colorNormal = p_normal;
        colorHover = p_hover;
        colorPresionado = p_presionado;

        this.setBackground(colorNormal);
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics grafico)
    {
        Graphics2D dibujo = (Graphics2D) grafico.create();

        dibujo.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        );

        if(this.getModel().isPressed())
        {
            dibujo.setColor(colorPresionado);
        }
        else if(this.getModel().isRollover())
        {
            dibujo.setColor(colorHover);
        }
        else
        {
            dibujo.setColor(colorNormal);
        }

        dibujo.fillRoundRect(
            0, 0, getWidth(), getHeight(), 25, 25
        );

        dibujo.dispose();

        super.paintComponent(grafico);
    }

    @Override
    protected void paintBorder(Graphics grafico)
    {
        // No dibujamos bordes cuadrados
    }
}
