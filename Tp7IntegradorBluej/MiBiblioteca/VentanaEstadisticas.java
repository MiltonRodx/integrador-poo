
package MiBiblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Calendar;
import java.sql.SQLException;

public class VentanaEstadisticas extends JPanel
{
    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;
    private VentanaPrincipal ventanaPrincipal;

    private JPanel panelGraficos;

    private final Color azulOscuro = new Color(15, 30, 60);
    private final Color azul = new Color(45, 90, 160);
    private final Color azulClaro = new Color(120, 165, 220);
    private final Color fondo = new Color(242, 246, 251);

    public VentanaEstadisticas(
        Biblioteca p_biblioteca,
        BibliotecaDAO p_bibliotecaDAO,
        VentanaPrincipal p_ventanaPrincipal)
    {
        this.biblioteca = p_biblioteca;
        this.bibliotecaDAO = p_bibliotecaDAO;
        this.ventanaPrincipal = p_ventanaPrincipal;

        this.setLayout(new BorderLayout());
        this.setBackground(fondo);

        this.crearInterfaz();
        this.actualizarEstadisticas();
    }

    private void crearInterfaz()
    {
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(fondo);
        panelPrincipal.setBorder(new EmptyBorder(25, 30, 25, 30));

        JPanel panelTitulo = new JPanel();
        panelTitulo.setLayout(new BoxLayout(panelTitulo, BoxLayout.Y_AXIS));
        panelTitulo.setBackground(fondo);

        JLabel titulo = new JLabel("Estadisticas de la biblioteca");
        titulo.setFont(new Font("Arial", Font.BOLD, 25));
        titulo.setForeground(azulOscuro);
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel("Un resumen de nuestra biblioteca");
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(90, 100, 120));
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelTitulo.add(titulo);
        panelTitulo.add(Box.createVerticalStrut(8));
        panelTitulo.add(subtitulo);

        panelPrincipal.add(panelTitulo, BorderLayout.NORTH);

        panelGraficos = new JPanel(new GridLayout(2, 2, 20, 20));
        panelGraficos.setBackground(fondo);

        panelPrincipal.add(panelGraficos, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 5));
        panelBotones.setBackground(fondo);

        JButton botonActualizar = new JButton("Actualizar estadisticas");
        JButton botonInforme = new JButton("Mostrar informe completo");

        configurarBoton(botonActualizar);
        configurarBoton(botonInforme);

        botonActualizar.addActionListener(evento -> {
            this.actualizarEstadisticas();
        });

        botonInforme.addActionListener(evento -> {
            VentanaInforme informe = new VentanaInforme(
                biblioteca,
                bibliotecaDAO,
                ventanaPrincipal
            );

            ventanaPrincipal.mostrarContenido(informe);
        });

        panelBotones.add(botonActualizar);
        panelBotones.add(botonInforme);

        panelPrincipal.add(panelBotones, BorderLayout.SOUTH);

        this.add(panelPrincipal, BorderLayout.CENTER);
    }

    private void configurarBoton(JButton boton)
    {
        boton.setFont(new Font("Arial", Font.BOLD, 13));
        boton.setBackground(azulOscuro);
        boton.setForeground(Color.WHITE);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(12, 20, 12, 20));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    public void actualizarEstadisticas()
    {
        panelGraficos.removeAll();

        int docentes = 0;
        int estudiantes = 0;

        for (Socio socio : biblioteca.getSocios())
        {
            if (socio instanceof Docente)
            {
                docentes++;
            }
            else if (socio instanceof Estudiante)
            {
                estudiantes++;
            }
        }

        int librosPrestados = 0;
        int librosDisponibles = 0;

        HashMap<String, Integer> cantidadPorTitulo = new HashMap<>();

        for (Libro libro : biblioteca.getLibros())
        {
            if (libro.prestado())
            {
                librosPrestados++;
            }
            else
            {
                librosDisponibles++;
            }

            String titulo = libro.getTitulo();

            if (cantidadPorTitulo.containsKey(titulo))
            {
                cantidadPorTitulo.put(
                    titulo,
                    cantidadPorTitulo.get(titulo) + 1
                );
            }
            else
            {
                cantidadPorTitulo.put(titulo, 1);
            }
        }

        int activos = 0;
        int vencidos = 0;
        int devueltos = 0;

        Calendar hoy = Calendar.getInstance();

        try
        {
            for (Socio socio : biblioteca.getSocios())
            {
                for (Prestamo prestamo : socio.getPrestamos())
                {
                    if (prestamo.estaDevuelto())
                    {
                        devueltos++;
                    }
                    else
                    {
                        Calendar fechaVencimiento =
                            (Calendar) prestamo.getFechaRetiro().clone();

                        int diasPrestamo =
                            bibliotecaDAO.getPrestamoDAO()
                                .obtenerDiasPrestamo(prestamo);

                        fechaVencimiento.add(
                            Calendar.DAY_OF_MONTH,
                            diasPrestamo
                        );

                        if (hoy.after(fechaVencimiento))
                        {
                            vencidos++;
                        }
                        else
                        {
                            activos++;
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            JOptionPane.showMessageDialog(
                this,
                "No se pudieron cargar los datos de los prestamos.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }

        ArrayList<String> titulos = new ArrayList<>();
        ArrayList<Integer> cantidades = new ArrayList<>();

        for (String titulo : cantidadPorTitulo.keySet())
        {
            titulos.add(titulo);
            cantidades.add(cantidadPorTitulo.get(titulo));
        }

        panelGraficos.add(
            new PanelGrafico(
                "Socios",
                "Cantidad de socios registrados",
                new String[]{"Docentes", "Estudiantes"},
                new int[]{docentes, estudiantes},
                new Color[]{azulOscuro, azulClaro},
                docentes + estudiantes
            )
        );

        panelGraficos.add(
            new PanelGrafico(
                "Libros",
                "Estado de los ejemplares",
                new String[]{"Prestados", "Disponibles"},
                new int[]{librosPrestados, librosDisponibles},
                new Color[]{azul, azulClaro},
                librosPrestados + librosDisponibles
            )
        );

        String[] nombresTitulos = titulos.toArray(new String[0]);

        int[] cantidadesTitulos = new int[cantidades.size()];

        for (int i = 0; i < cantidades.size(); i++)
        {
            cantidadesTitulos[i] = cantidades.get(i);
        }

        Color[] coloresTitulos = new Color[]{
            azulOscuro,
            azul,
            azulClaro,
            new Color(80, 120, 180),
            new Color(160, 190, 225),
            new Color(35, 65, 110)
        };

        Color[] coloresFinales = new Color[cantidadesTitulos.length];

        for (int i = 0; i < coloresFinales.length; i++)
        {
            coloresFinales[i] =
                coloresTitulos[i % coloresTitulos.length];
        }

        panelGraficos.add(
            new PanelGrafico(
                "Titulos diferentes",
                "Cantidad de ejemplares por titulo",
                nombresTitulos,
                cantidadesTitulos,
                coloresFinales,
                cantidadPorTitulo.size()
            )
        );

        panelGraficos.add(
            new PanelGrafico(
                "Prestamos",
                "Estado de los prestamos registrados",
                new String[]{"Activos", "Vencidos", "Devueltos"},
                new int[]{activos, vencidos, devueltos},
                new Color[]{azulOscuro, azulClaro, azul},
                activos + vencidos + devueltos
            )
        );

        panelGraficos.revalidate();
        panelGraficos.repaint();
    }

    private class PanelGrafico extends JPanel
    {
        private String titulo;
        private String descripcion;
        private GraficoCircular grafico;

        public PanelGrafico(
            String p_titulo,
            String p_descripcion,
            String[] nombres,
            int[] valores,
            Color[] colores,
            int total)
        {
            this.titulo = p_titulo;
            this.descripcion = p_descripcion;

            this.setLayout(new BorderLayout(5, 5));
            this.setBackground(Color.WHITE);
            this.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(220, 228, 240)
                ),
                new EmptyBorder(12, 12, 12, 12)
            ));

            JLabel etiquetaTitulo = new JLabel(titulo);
            etiquetaTitulo.setFont(new Font("Arial", Font.BOLD, 17));
            etiquetaTitulo.setForeground(azulOscuro);
            etiquetaTitulo.setHorizontalAlignment(SwingConstants.CENTER);

            JLabel etiquetaDescripcion = new JLabel(descripcion);
            etiquetaDescripcion.setFont(new Font("Arial", Font.PLAIN, 11));
            etiquetaDescripcion.setForeground(Color.GRAY);
            etiquetaDescripcion.setHorizontalAlignment(SwingConstants.CENTER);

            JPanel encabezado = new JPanel(new GridLayout(2, 1, 2, 2));
            encabezado.setBackground(Color.WHITE);
            encabezado.add(etiquetaTitulo);
            encabezado.add(etiquetaDescripcion);

            this.add(encabezado, BorderLayout.NORTH);

            grafico = new GraficoCircular(
                nombres,
                valores,
                colores,
                total
            );

            this.add(grafico, BorderLayout.CENTER);
        }
    }

    private class GraficoCircular extends JPanel
    {
        private String[] nombres;
        private int[] valores;
        private Color[] colores;
        private int total;
        private double progreso;

        public GraficoCircular(
            String[] p_nombres,
            int[] p_valores,
            Color[] p_colores,
            int p_total)
        {
            this.nombres = p_nombres;
            this.valores = p_valores;
            this.colores = p_colores;
            this.total = p_total;
            this.progreso = 0;

            this.setBackground(Color.WHITE);

            Timer animacion = new Timer(15, evento -> {
                progreso += 0.035;

                if (progreso >= 1)
                {
                    progreso = 1;
                    ((Timer) evento.getSource()).stop();
                }

                repaint();
            });

            animacion.start();
        }

        @Override
        protected void paintComponent(Graphics g)
        {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();

            g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
            );

            int ancho = getWidth();
            int alto = getHeight();

            int diametro = Math.min(125, alto - 20);

            int x = (ancho - diametro) / 2;
            int y = 5;

            int inicio = 90;

            if (total > 0)
            {
                for (int i = 0; i < valores.length; i++)
                {
                    if (valores[i] > 0)
                    {
                        int angulo = (int) Math.round(
                            360.0 * valores[i] / total * progreso
                        );

                        g2.setColor(colores[i]);

                        g2.fill(new Arc2D.Double(
                            x,
                            y,
                            diametro,
                            diametro,
                            inicio,
                            -angulo,
                            Arc2D.PIE
                        ));

                        inicio -= angulo;
                    }
                }
            }
            else
            {
                g2.setColor(new Color(225, 232, 242));
                g2.fillOval(x, y, diametro, diametro);
            }

            int centro = diametro / 2;

            g2.setColor(Color.WHITE);
            g2.fillOval(
                x + centro / 2,
                y + centro / 2,
                centro,
                centro
            );

            g2.setColor(azulOscuro);
            g2.setFont(new Font("Arial", Font.BOLD, 20));

            String textoTotal = String.valueOf(total);
            FontMetrics medidas = g2.getFontMetrics();

            g2.drawString(
                textoTotal,
                x + (diametro - medidas.stringWidth(textoTotal)) / 2,
                y + centro + 7
            );

            int inicioLeyenda = y + diametro + 13;
            int anchoLeyenda = Math.max(100, ancho - 20);

            g2.setFont(new Font("Arial", Font.PLAIN, 10));

            for (int i = 0; i < nombres.length; i++)
            {
                int columna = i % 2;
                int fila = i / 2;

                int posicionX = 10 + columna * (anchoLeyenda / 2);
                int posicionY = inicioLeyenda + fila * 17;

                g2.setColor(colores[i]);
                g2.fillOval(posicionX, posicionY - 8, 9, 9);

                g2.setColor(azulOscuro);

                String texto = nombres[i] + ": " + valores[i];

                g2.drawString(
                    texto,
                    posicionX + 13,
                    posicionY
                );
            }

            g2.dispose();
        }
    }
}
