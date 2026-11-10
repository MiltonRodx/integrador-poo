
package biblioteca;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import java.awt.*;

public class GestorTemas {
    // Atributos
    private int temaActual = 0;

    private Color colorMenu;
    private Color colorFondo;
    private Color colorEncabezado;
    private Color colorTexto;
    private Color colorTarjeta;
    private Color colorTextoSecundario;
    private Color colorBoton;
    private Color colorBotonHover;
    private Color colorBotonPresionado;

    // Constructor
    public GestorTemas() {
        this.cargarColores();
    }
    
    // Setters
    private void setTemaActual(int cant) { this.temaActual = cant; }
    public int getTemaActual() { return this.temaActual; }
    
    // Getters
    public Color getColorMenu() { return this.colorMenu; }
    public Color getColorFondo() { return this.colorFondo; }
    public Color getColorEncabezado() { return this.colorEncabezado; }
    public Color getColorTexto() { return this.colorTexto; }
    public Color getColorTarjeta() { return this.colorTarjeta; }
    public Color getColorTextoSecundario() { return this.colorTextoSecundario; }
    

    public void cambiarTema() {
        this.setTemaActual(this.getTemaActual() + 1);

        if(temaActual > 3) {
            temaActual = 0;
        }

        this.cargarColores();
    }

    private void cargarColores() {
        
        switch(temaActual) {
            // Azul
            case 0:
                colorMenu = new Color(15, 30, 60);
                colorFondo = new Color(242, 246, 251);
                colorEncabezado = Color.WHITE;
                colorTexto = new Color(15, 30, 60);
                colorTarjeta = Color.WHITE;
                colorTextoSecundario = new Color(100, 110, 125);

                colorBoton = new Color(25, 45, 85);
                colorBotonHover = new Color(65, 100, 155);
                colorBotonPresionado = new Color(15, 30, 60);
                break;

            // Oscuro
            case 1:
                colorMenu = new Color(37, 41, 54);
                colorFondo = new Color(23, 26, 33);
                colorEncabezado = new Color(37, 41, 54);
                colorTexto = new Color(240, 240, 240);
                colorTarjeta = new Color(48, 53, 65);
                colorTextoSecundario = new Color(190, 195, 205);

                colorBoton = new Color(65, 75, 100);
                colorBotonHover = new Color(90, 105, 140);
                colorBotonPresionado = new Color(35, 40, 55);
                break;

            // Rosa
            case 2:
                colorMenu = new Color(173, 102, 136);
                colorFondo = new Color(255, 240, 245);
                colorEncabezado = new Color(255, 249, 252);
                colorTexto = new Color(135, 65, 95);
                colorTarjeta = Color.WHITE;
                colorTextoSecundario = new Color(145, 105, 125);

                colorBoton = new Color(173, 102, 136);
                colorBotonHover = new Color(205, 130, 165);
                colorBotonPresionado = new Color(135, 65, 95);
                break;

            // Rojo
            default:
                colorMenu = new Color(143, 48, 61);
                colorFondo = new Color(249, 233, 234);
                colorEncabezado = new Color(255, 248, 248);
                colorTexto = new Color(118, 43, 54);
                colorTarjeta = Color.WHITE;
                colorTextoSecundario = new Color(145, 95, 100);

                colorBoton = new Color(143, 48, 61);
                colorBotonHover = new Color(180, 70, 85);
                colorBotonPresionado = new Color(105, 30, 42);
                break;
        }
    }

    

    
    public void aplicarTema(Container contenedor)
    {
        Component[] componentes = contenedor.getComponents();

        for(Component componente : componentes)
        {
            if(componente instanceof BotonRedondeado)
            {
                BotonRedondeado boton = (BotonRedondeado) componente;

                boton.cambiarColores(
                    colorBoton,
                    colorBotonHover,
                    colorBotonPresionado
                );

                boton.setForeground(Color.WHITE);
            }
            else if(componente instanceof JButton)
            {
                componente.setBackground(colorBoton);
                componente.setForeground(Color.WHITE);
            }
            else if(componente instanceof JLabel)
            {
                JLabel etiqueta = (JLabel) componente;

                if(!Color.WHITE.equals(etiqueta.getForeground()))
                {
                    etiqueta.setForeground(colorTexto);
                }
            }
            else if(componente instanceof JTextField
                || componente instanceof JTextArea
                || componente instanceof JComboBox
                || componente instanceof JSpinner)
            {
                componente.setBackground(colorTarjeta);
                componente.setForeground(colorTexto);
            }
            else if(componente instanceof JTable)
            {
                JTable tabla = (JTable) componente;

                tabla.setBackground(colorTarjeta);
                tabla.setForeground(colorTexto);
                tabla.setGridColor(colorTextoSecundario);

                JTableHeader encabezado = tabla.getTableHeader();

                if(encabezado != null)
                {
                    encabezado.setBackground(colorMenu);
                    encabezado.setForeground(Color.WHITE);
                }
            }
            else if(componente instanceof JPanel)
            {
                JPanel panel = (JPanel) componente;

                if(esColorClaro(panel.getBackground()))
                {
                    panel.setBackground(colorTarjeta);
                }
                else
                {
                    panel.setBackground(colorFondo);
                }
            }
            else if(componente instanceof JScrollPane)
            {
                JScrollPane barras = (JScrollPane) componente;

                barras.setBackground(colorFondo);
                barras.getViewport().setBackground(colorFondo);
            }

            if(componente instanceof JComponent)
            {
                // Los bordes con titulo usan un color propio: en los temas
                // oscuros quedaba el titulo oscuro sobre fondo oscuro
                if(((JComponent) componente).getBorder()
                    instanceof TitledBorder)
                {
                    TitledBorder borde = (TitledBorder)
                        ((JComponent) componente).getBorder();

                    borde.setTitleColor(colorTexto);
                }
            }

            if(componente instanceof Container)
            {
                aplicarTema((Container) componente);
            }
        }

        contenedor.revalidate();
        contenedor.repaint();
    }

    private boolean esColorClaro(Color color)
    {
        double luminosidad =
            (color.getRed() * 0.299)
            + (color.getGreen() * 0.587)
            + (color.getBlue() * 0.114);

        return luminosidad > 220;
    }
    
    public Color getColorBotonHover() { return colorBotonHover; }

    public Color getColorBotonPresionado() { return colorBotonPresionado; }
}
