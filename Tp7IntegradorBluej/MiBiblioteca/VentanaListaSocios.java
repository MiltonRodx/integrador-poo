
package MiBiblioteca;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class VentanaListaSocios extends JPanel {
    private Biblioteca biblioteca;
    private VentanaPrincipal ventanaPrincipal;

    private JTextField campoBuscar;
    private JPanel panelSocios;
    private JLabel etiquetaCantidad;
    private JLabel etiquetaDocentes;
    private JLabel etiquetaEstudiantes;

    public VentanaListaSocios(
        Biblioteca p_biblioteca,
        VentanaPrincipal p_ventanaPrincipal) {
        this.biblioteca = p_biblioteca;
        this.ventanaPrincipal = p_ventanaPrincipal;

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        this.crearInterfaz();
    }

    private void crearInterfaz() {
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        JLabel titulo = new JLabel("Lista de socios");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(25, 45, 85));

        JLabel descripcion = new JLabel(
            "Consulta y busca los socios registrados en nuestra biblioteca."
        );
        descripcion.setFont(new Font("Arial", Font.PLAIN, 15));
        descripcion.setForeground(new Color(100, 110, 125));

        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(
            new BoxLayout(panelSuperior, BoxLayout.Y_AXIS)
        );
        panelSuperior.setBackground(Color.WHITE);

        panelSuperior.add(titulo);
        panelSuperior.add(Box.createVerticalStrut(8));
        panelSuperior.add(descripcion);
        panelSuperior.add(Box.createVerticalStrut(20));

        JLabel etiquetaBuscar = new JLabel(
            "Buscar por nombre o D.N.I."
        );
        etiquetaBuscar.setFont(new Font("Arial", Font.BOLD, 14));
        etiquetaBuscar.setForeground(new Color(25, 45, 85));

        campoBuscar = new JTextField();
        campoBuscar.setFont(new Font("Arial", Font.PLAIN, 15));
        campoBuscar.setPreferredSize(new Dimension(400, 40));
        campoBuscar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
            )
        );

        etiquetaCantidad = new JLabel();
        etiquetaCantidad.setFont(new Font("Arial", Font.BOLD, 14));
        etiquetaCantidad.setForeground(new Color(65, 100, 155));

        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 10));
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.add(etiquetaBuscar, BorderLayout.NORTH);
        panelBusqueda.add(campoBuscar, BorderLayout.CENTER);
        panelBusqueda.add(etiquetaCantidad, BorderLayout.SOUTH);

        panelSuperior.add(panelBusqueda);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        panelSocios = new JPanel();
        panelSocios.setLayout(
            new BoxLayout(panelSocios, BoxLayout.Y_AXIS)
        );
        panelSocios.setBackground(Color.WHITE);

        JScrollPane desplazamiento = new JScrollPane(panelSocios);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
        desplazamiento.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        panelPrincipal.add(desplazamiento, BorderLayout.CENTER);

        etiquetaDocentes = new JLabel();
        etiquetaEstudiantes = new JLabel();

        etiquetaDocentes.setFont(new Font("Arial", Font.BOLD, 14));
        etiquetaEstudiantes.setFont(new Font("Arial", Font.BOLD, 14));

        etiquetaDocentes.setForeground(new Color(25, 45, 85));
        etiquetaEstudiantes.setForeground(new Color(25, 45, 85));

        JPanel panelResumen = new JPanel(new GridLayout(1, 2, 15, 0));
        panelResumen.setBackground(Color.WHITE);
        panelResumen.setBorder(
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        );

        panelResumen.add(etiquetaDocentes);
        panelResumen.add(etiquetaEstudiantes);

        BotonRedondeado botonVolver =
            new BotonRedondeado("Volver");

        botonVolver.addActionListener(
            evento -> ventanaPrincipal.mostrarSocios()
        );

        JPanel panelInferior = new JPanel(new BorderLayout(10, 10));
        panelInferior.setBackground(Color.WHITE);
        panelInferior.add(panelResumen, BorderLayout.CENTER);

        JPanel panelBoton = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );
        panelBoton.setBackground(Color.WHITE);
        panelBoton.add(botonVolver);

        panelInferior.add(panelBoton, BorderLayout.SOUTH);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        this.add(panelPrincipal, BorderLayout.CENTER);

        campoBuscar.getDocument().addDocumentListener(
            new DocumentListener()
            {
                public void insertUpdate(DocumentEvent evento)
                {
                    actualizarLista();
                }

                public void removeUpdate(DocumentEvent evento)
                {
                    actualizarLista();
                }

                public void changedUpdate(DocumentEvent evento)
                {
                    actualizarLista();
                }
            }
        );

        actualizarLista();
    }

    private void actualizarLista()
    {
        panelSocios.removeAll();

        String busqueda = campoBuscar.getText().trim().toLowerCase();

        String lista = biblioteca.listaDeSocios();

        String[] lineas = lista.split("\\R");

        int cantidadEncontrada = 0;

        for(String linea : lineas)
        {
            if(linea.contains("D.N.I.:"))
            {
                if(linea.toLowerCase().contains(busqueda))
                {
                    panelSocios.add(crearTarjeta(linea));
                    panelSocios.add(Box.createVerticalStrut(12));
                    cantidadEncontrada++;
                }
            }
        }

        etiquetaCantidad.setText(
            "Socios encontrados: " + cantidadEncontrada
        );

        if(cantidadEncontrada == 0)
        {
            JLabel sinResultados = new JLabel(
                "No se encontraron socios con esa busqueda."
            );

            sinResultados.setFont(new Font("Arial", Font.PLAIN, 16));
            sinResultados.setForeground(new Color(100, 110, 125));

            sinResultados.setBorder(
                BorderFactory.createEmptyBorder(25, 10, 25, 10)
            );

            panelSocios.add(sinResultados);
        }

        etiquetaDocentes.setText(
            "Docentes registrados: " +
            biblioteca.cantidadDeSociosPorTipo("Docente")
        );

        etiquetaEstudiantes.setText(
            "Estudiantes registrados: " +
            biblioteca.cantidadDeSociosPorTipo("Estudiante")
        );

        panelSocios.revalidate();
        panelSocios.repaint();
    }

    private JPanel crearTarjeta(String p_datos)
    {
        JPanel tarjeta = new JPanel(new BorderLayout());
        tarjeta.setBackground(Color.WHITE);

        tarjeta.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)
            )
        );

        tarjeta.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 90)
        );

        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel informacion = new JLabel(p_datos);
        informacion.setFont(new Font("Arial", Font.PLAIN, 15));
        informacion.setForeground(new Color(45, 55, 75));

        tarjeta.add(informacion, BorderLayout.CENTER);

        return tarjeta;
    }
}
