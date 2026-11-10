
package biblioteca;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.util.ArrayList;

public class VentanaListaLibros extends JPanel
{
    private Biblioteca biblioteca;
    private VentanaCatalogo ventanaCatalogo;

    private JTextField campoBuscar;
    private JPanel panelLibros;
    private JLabel etiquetaCantidad;

    public VentanaListaLibros(
        Biblioteca p_biblioteca,
        VentanaCatalogo p_ventanaCatalogo)
    {
        this.biblioteca = p_biblioteca;
        this.ventanaCatalogo = p_ventanaCatalogo;

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        this.crearInterfaz();
    }

    private void crearInterfaz()
    {
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(
            BorderFactory.createEmptyBorder(30, 35, 30, 35)
        );

        JLabel titulo = new JLabel("Biblioteca de libros");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(15, 30, 60));

        JLabel descripcion = new JLabel(
            "Explora nuestro catalogo y encuentra tu proxima lectura."
        );
        descripcion.setFont(new Font("Arial", Font.PLAIN, 15));
        descripcion.setForeground(new Color(100, 110, 125));

        JPanel panelEncabezado = new JPanel(new GridLayout(2, 1, 5, 5));
        panelEncabezado.setBackground(Color.WHITE);
        panelEncabezado.add(titulo);
        panelEncabezado.add(descripcion);

        campoBuscar = new JTextField();
        campoBuscar.setFont(new Font("Arial", Font.PLAIN, 16));
        campoBuscar.setPreferredSize(new Dimension(300, 42));
        campoBuscar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 230)),
                BorderFactory.createEmptyBorder(5, 12, 5, 12)
            )
        );

        JLabel etiquetaBuscar = new JLabel("Buscar por titulo:");
        etiquetaBuscar.setFont(new Font("Arial", Font.BOLD, 14));
        etiquetaBuscar.setForeground(new Color(45, 60, 80));

        etiquetaCantidad = new JLabel();
        etiquetaCantidad.setFont(new Font("Arial", Font.PLAIN, 13));
        etiquetaCantidad.setForeground(new Color(100, 110, 125));

        JPanel panelBusqueda = new JPanel(new BorderLayout(10, 10));
        panelBusqueda.setBackground(Color.WHITE);
        panelBusqueda.add(etiquetaBuscar, BorderLayout.NORTH);
        panelBusqueda.add(campoBuscar, BorderLayout.CENTER);
        panelBusqueda.add(etiquetaCantidad, BorderLayout.SOUTH);

        panelLibros = new JPanel();
        panelLibros.setLayout(new BoxLayout(panelLibros, BoxLayout.Y_AXIS));
        panelLibros.setBackground(Color.WHITE);

        JScrollPane desplazamiento = new JScrollPane(panelLibros);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
        desplazamiento.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        BotonRedondeado botonVolver =
            new BotonRedondeado("Volver al catalogo");

        JPanel panelInferior = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        panelInferior.setBackground(Color.WHITE);
        panelInferior.add(botonVolver);

        JPanel panelSuperior = new JPanel(new BorderLayout(15, 15));
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.add(panelEncabezado, BorderLayout.NORTH);
        panelSuperior.add(panelBusqueda, BorderLayout.CENTER);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(desplazamiento, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        this.add(panelPrincipal, BorderLayout.CENTER);

        campoBuscar.getDocument().addDocumentListener(
            new DocumentListener()
            {
                @Override
                public void insertUpdate(DocumentEvent evento)
                {
                    actualizarLista();
                }

                @Override
                public void removeUpdate(DocumentEvent evento)
                {
                    actualizarLista();
                }

                @Override
                public void changedUpdate(DocumentEvent evento)
                {
                    actualizarLista();
                }
            }
        );

        botonVolver.addActionListener(
            evento -> ventanaCatalogo.mostrarCatalogo()
        );

        actualizarLista();
    }

    private void actualizarLista()
    {
        panelLibros.removeAll();

        String busqueda = campoBuscar.getText().toLowerCase();

        ArrayList<Libro> libros = biblioteca.getLibros();

        int cantidadEncontrada = 0;

        for(Libro unLibro : libros)
        {
            String titulo = unLibro.getTitulo().toLowerCase();

            if(titulo.contains(busqueda))
            {
                panelLibros.add(crearTarjeta(unLibro));
                panelLibros.add(Box.createVerticalStrut(12));

                cantidadEncontrada++;
            }
        }

        etiquetaCantidad.setText(
            "Libros encontrados: " + cantidadEncontrada
        );

        if(cantidadEncontrada == 0)
        {
            JLabel mensaje = new JLabel(
                "No se encontraron libros con ese titulo."
            );

            mensaje.setFont(new Font("Arial", Font.PLAIN, 16));
            mensaje.setForeground(new Color(100, 110, 125));
            mensaje.setAlignmentX(Component.CENTER_ALIGNMENT);

            panelLibros.add(Box.createVerticalStrut(40));
            panelLibros.add(mensaje);
        }

        panelLibros.revalidate();
        panelLibros.repaint();
    }

    private JPanel crearTarjeta(Libro p_libro)
    {
        JPanel tarjeta = new JPanel(new BorderLayout(15, 10));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235)),
                BorderFactory.createEmptyBorder(18, 22, 18, 22)
            )
        );

        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        tarjeta.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel titulo = new JLabel(p_libro.getTitulo());
        titulo.setFont(new Font("Arial", Font.BOLD, 19));
        titulo.setForeground(new Color(15, 30, 60));

        JLabel editorial = new JLabel(
            "Editorial: " + p_libro.getEditorial()
        );
        editorial.setFont(new Font("Arial", Font.PLAIN, 14));
        editorial.setForeground(new Color(85, 95, 110));

        JLabel edicion = new JLabel(
            "Edicion: " + p_libro.getEdicion()
            + "     |     Anio: " + p_libro.getAnio()
        );
        edicion.setFont(new Font("Arial", Font.PLAIN, 14));
        edicion.setForeground(new Color(85, 95, 110));

        JPanel panelDatos = new JPanel(new GridLayout(3, 1, 5, 5));
        panelDatos.setBackground(Color.WHITE);
        panelDatos.add(titulo);
        panelDatos.add(editorial);
        panelDatos.add(edicion);

        JLabel icono = new JLabel("LIBRO");
        icono.setFont(new Font("Arial", Font.BOLD, 13));
        icono.setForeground(new Color(49, 79, 125));
        icono.setHorizontalAlignment(SwingConstants.CENTER);
        icono.setPreferredSize(new Dimension(75, 60));

        tarjeta.add(icono, BorderLayout.WEST);
        tarjeta.add(panelDatos, BorderLayout.CENTER);

        return tarjeta;
    }
}
