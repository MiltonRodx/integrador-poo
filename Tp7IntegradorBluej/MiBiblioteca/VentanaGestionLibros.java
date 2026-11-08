package MiBiblioteca;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class VentanaGestionLibros extends JPanel
{
    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;
    private VentanaCatalogo ventanaCatalogo;

    private JTextField campoTitulo;
    private JTextField campoEdicion;
    private JTextField campoEditorial;
    private JTextField campoAnio;

    private JComboBox<String> comboLibros;
    private ArrayList<Libro> librosSeleccionados;

    public VentanaGestionLibros(
        Biblioteca p_biblioteca,
        BibliotecaDAO p_bibliotecaDAO,
        VentanaCatalogo p_ventanaCatalogo)
    {
        this.biblioteca = p_biblioteca;
        this.bibliotecaDAO = p_bibliotecaDAO;
        this.ventanaCatalogo = p_ventanaCatalogo;

        this.librosSeleccionados = new ArrayList<Libro>();

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        this.crearInterfaz();
    }

    private void crearInterfaz()
    {
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        JLabel titulo = new JLabel("Gestionar libros");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(25, 45, 85));

        JLabel descripcion = new JLabel(
            "Agrega nuevos libros o administra los ejemplares existentes."
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

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        JPanel panelAgregar = new JPanel(new BorderLayout(15, 15));
        panelAgregar.setBackground(Color.WHITE);
        panelAgregar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
            )
        );

        JLabel tituloAgregar = new JLabel("Agregar un nuevo libro");
        tituloAgregar.setFont(new Font("Arial", Font.BOLD, 21));
        tituloAgregar.setForeground(new Color(25, 45, 85));

        JPanel formulario = new JPanel(new GridLayout(4, 1, 10, 12));
        formulario.setBackground(Color.WHITE);

        campoTitulo = crearCampo("Titulo del libro");
        campoEdicion = crearCampo("Numero de edicion");
        campoEditorial = crearCampo("Editorial");
        campoAnio = crearCampo("Anio de publicacion");

        formulario.add(campoTitulo);
        formulario.add(campoEdicion);
        formulario.add(campoEditorial);
        formulario.add(campoAnio);

        BotonRedondeado botonAgregar =
            new BotonRedondeado("Registrar libro");

        botonAgregar.addActionListener(
            evento -> registrarLibro()
        );

        JPanel panelBotonAgregar = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        panelBotonAgregar.setBackground(Color.WHITE);
        panelBotonAgregar.add(botonAgregar);

        JPanel contenidoAgregar = new JPanel(new BorderLayout(10, 15));
        contenidoAgregar.setBackground(Color.WHITE);
        contenidoAgregar.add(tituloAgregar, BorderLayout.NORTH);
        contenidoAgregar.add(formulario, BorderLayout.CENTER);
        contenidoAgregar.add(panelBotonAgregar, BorderLayout.SOUTH);

        panelAgregar.add(contenidoAgregar, BorderLayout.CENTER);

        JPanel panelEliminar = new JPanel(new BorderLayout(15, 15));
        panelEliminar.setBackground(Color.WHITE);
        panelEliminar.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 235)),
                BorderFactory.createEmptyBorder(20, 30, 20, 30)
            )
        );

        JLabel tituloEliminar = new JLabel("Eliminar un libro");
        tituloEliminar.setFont(new Font("Arial", Font.BOLD, 20));
        tituloEliminar.setForeground(new Color(25, 45, 85));

        JLabel descripcionEliminar = new JLabel(
            "Selecciona el ejemplar que deseas quitar del catalogo."
        );
        descripcionEliminar.setFont(new Font("Arial", Font.PLAIN, 14));

        comboLibros = new JComboBox<String>();
        comboLibros.setFont(new Font("Arial", Font.PLAIN, 14));

        BotonRedondeado botonEliminar =
            new BotonRedondeado("Eliminar libro");

        botonEliminar.addActionListener(
            evento -> eliminarLibro()
        );

        JPanel panelSeleccion = new JPanel(new BorderLayout(10, 10));
        panelSeleccion.setBackground(Color.WHITE);
        panelSeleccion.add(comboLibros, BorderLayout.CENTER);
        panelSeleccion.add(botonEliminar, BorderLayout.EAST);

        JPanel contenidoEliminar = new JPanel(new BorderLayout(10, 12));
        contenidoEliminar.setBackground(Color.WHITE);
        contenidoEliminar.add(tituloEliminar, BorderLayout.NORTH);
        contenidoEliminar.add(descripcionEliminar, BorderLayout.CENTER);
        contenidoEliminar.add(panelSeleccion, BorderLayout.SOUTH);

        panelEliminar.add(contenidoEliminar, BorderLayout.CENTER);

        JPanel panelCentral = new JPanel();
        panelCentral.setLayout(
            new BoxLayout(panelCentral, BoxLayout.Y_AXIS)
        );
        panelCentral.setBackground(Color.WHITE);

        panelAgregar.setPreferredSize(new Dimension(900, 390));
        panelAgregar.setMinimumSize(new Dimension(700, 390));
        panelAgregar.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 390)
        );

        panelEliminar.setPreferredSize(new Dimension(900, 200));
        panelEliminar.setMinimumSize(new Dimension(700, 200));
        panelEliminar.setMaximumSize(
            new Dimension(Integer.MAX_VALUE, 200)
        );

        panelCentral.add(panelAgregar);
        panelCentral.add(Box.createVerticalStrut(20));
        panelCentral.add(panelEliminar);

        JScrollPane desplazamiento = new JScrollPane(panelCentral);
        desplazamiento.setBorder(BorderFactory.createEmptyBorder());
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);
        desplazamiento.setHorizontalScrollBarPolicy(
            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        );

        panelPrincipal.add(desplazamiento, BorderLayout.CENTER);

        BotonRedondeado botonVolver =
            new BotonRedondeado("Volver al catalogo");

        botonVolver.addActionListener(
            evento -> ventanaCatalogo.mostrarCatalogo()
        );

        JPanel panelInferior = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );
        panelInferior.setBackground(Color.WHITE);
        panelInferior.add(botonVolver);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        this.add(panelPrincipal, BorderLayout.CENTER);

        actualizarComboLibros();
    }

    private JTextField crearCampo(String p_texto)
    {
        JTextField campo = new JTextField();
        campo.setFont(new Font("Arial", Font.PLAIN, 15));
        campo.setBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 210, 225)),
                p_texto
            )
        );

        return campo;
    }

    private void registrarLibro()
    {
        if(campoTitulo.getText().trim().isEmpty() ||
           campoEdicion.getText().trim().isEmpty() ||
           campoEditorial.getText().trim().isEmpty() ||
           campoAnio.getText().trim().isEmpty())
        {
            JOptionPane.showMessageDialog(
                this,
                "Completa todos los campos para registrar el libro."
            );
            return;
        }

        try
        {
            int edicion = Integer.parseInt(campoEdicion.getText().trim());
            int anio = Integer.parseInt(campoAnio.getText().trim());

            biblioteca.nuevoLibro(
                campoTitulo.getText().trim(),
                edicion,
                campoEditorial.getText().trim(),
                anio
            );

            Libro nuevoLibro = biblioteca.getLibros().get(
                biblioteca.getLibros().size() - 1
            );

            bibliotecaDAO.getLibroDAO().agregar(nuevoLibro);

            campoTitulo.setText("");
            campoEdicion.setText("");
            campoEditorial.setText("");
            campoAnio.setText("");

            actualizarComboLibros();

            JOptionPane.showMessageDialog(
                this,
                "El libro se registro correctamente."
            );
        }
        catch(NumberFormatException excepcion)
        {
            JOptionPane.showMessageDialog(
                this,
                "La edicion y el anio deben ser numeros."
            );
        }
        catch(SQLException excepcion)
        {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo guardar el libro en la base de datos."
            );
        }
    }

    private void actualizarComboLibros()
    {
        comboLibros.removeAllItems();
        librosSeleccionados.clear();

        for(Libro libro : biblioteca.getLibros())
        {
            comboLibros.addItem(
                libro.getTitulo() +
                " | Edicion: " + libro.getEdicion() +
                " | " + libro.getEditorial() +
                " | " + libro.getAnio()
            );

            librosSeleccionados.add(libro);
        }

        if(librosSeleccionados.isEmpty())
        {
            comboLibros.addItem("No hay libros registrados");
        }
    }

    private void eliminarLibro()
    {
        int indice = comboLibros.getSelectedIndex();

        if(indice == -1 || librosSeleccionados.isEmpty())
        {
            JOptionPane.showMessageDialog(
                this,
                "No hay libros para eliminar."
            );
            return;
        }

        Libro libro = librosSeleccionados.get(indice);

        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Deseas eliminar el libro " + libro.getTitulo() + "?",
            "Confirmar eliminacion",
            JOptionPane.YES_NO_OPTION
        );

        if(respuesta == JOptionPane.YES_OPTION)
        {
            try
            {
                bibliotecaDAO.getLibroDAO().eliminar(libro);
                biblioteca.getLibros().remove(libro);

                actualizarComboLibros();

                JOptionPane.showMessageDialog(
                    this,
                    "El libro se elimino correctamente."
                );
            }
            catch(SQLException excepcion)
            {
                JOptionPane.showMessageDialog(
                    this,
                    "No se puede eliminar el libro. Puede estar relacionado con un prestamo."
                );
            }
        }
    }
}