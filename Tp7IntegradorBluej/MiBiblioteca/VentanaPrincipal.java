
package MiBiblioteca;

import javax.swing.*;
import java.awt.*;

public class VentanaPrincipal extends JFrame
{
    private JPanel panelPrincipal;
    private JPanel panelMenu;
    private JPanel panelContenido;
    private JPanel panelSuperior;
    private JPanel panelTextos;

    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;

    private BotonRedondeado botonTema;

    private JLabel tituloSuperior;
    private JLabel subtituloSuperior;

    private GestorTemas gestorTemas;

    public VentanaPrincipal(Biblioteca p_biblioteca, BibliotecaDAO p_bibliotecaDAO)
    {
        this.biblioteca = p_biblioteca;
        this.bibliotecaDAO = p_bibliotecaDAO;

        gestorTemas = new GestorTemas();

        this.setTitle(biblioteca.getNombre());
        this.setSize(1300, 850);
        this.setMinimumSize(new Dimension(1000, 650));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);

        this.crearInterfaz();
    }

    // Punto de entrada de la GUI (el que usa BlueJ y el jar):
    // prepara la base, carga el estado y abre la ventana tras el splash.
    // El main de la consola del practico esta en GestionBiblioteca.main().
    public static void main(String[] args)
    {
        try
        {
            ConexionBD conexionBD = new ConexionBD("biblioteca.db");

            BibliotecaDAO bibliotecaDAO = new BibliotecaDAO(conexionBD);
            bibliotecaDAO.crearTablas();

            Biblioteca biblioteca =
                bibliotecaDAO.cargarBiblioteca("Biblioteca Gestion FACENA-UNNE");

            SwingUtilities.invokeLater(() -> {
                new PantallaCarga(() -> {
                    VentanaPrincipal ventana =
                        new VentanaPrincipal(biblioteca, bibliotecaDAO);

                    ventana.setVisible(true);
                });
            });
        }
        catch(Exception error)
        {
            System.out.println(
                "No se pudo iniciar la aplicacion: " + error.getMessage()
            );
        }
    }

    private void crearInterfaz()
    {
        panelPrincipal = new JPanel(new BorderLayout());
        panelPrincipal.setBackground(gestorTemas.getColorFondo());

        // Menu lateral
        panelMenu = new JPanel(new GridLayout(8, 1, 5, 12));
        panelMenu.setBackground(gestorTemas.getColorMenu());
        panelMenu.setPreferredSize(new Dimension(230, 0));
        panelMenu.setBorder(
            BorderFactory.createEmptyBorder(30, 15, 30, 15)
        );

        JLabel nombreMenu = new JLabel(
            biblioteca.getNombre(),
            SwingConstants.CENTER
        );

        nombreMenu.setForeground(Color.WHITE);
        nombreMenu.setFont(new Font("Arial", Font.BOLD, 17));

        BotonRedondeado botonInicio = new BotonRedondeado("Inicio");
        BotonRedondeado botonSocios = new BotonRedondeado("Socios");
        BotonRedondeado botonCatalogo = new BotonRedondeado("Catalogo");
        BotonRedondeado botonPrestamos = new BotonRedondeado("Prestamos");
        BotonRedondeado botonDevoluciones = new BotonRedondeado("Devoluciones");
        BotonRedondeado botonEstadisticas = new BotonRedondeado("Estadisticas");
        BotonRedondeado botonConsultas = new BotonRedondeado("Consultas");

        panelMenu.add(nombreMenu);
        panelMenu.add(botonInicio);
        panelMenu.add(botonSocios);
        panelMenu.add(botonCatalogo);
        panelMenu.add(botonPrestamos);
        panelMenu.add(botonDevoluciones);
        panelMenu.add(botonEstadisticas);
        panelMenu.add(botonConsultas);

        // Encabezado superior
        panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(gestorTemas.getColorEncabezado());
        panelSuperior.setPreferredSize(new Dimension(0, 85));
        panelSuperior.setBorder(
            BorderFactory.createEmptyBorder(15, 30, 15, 30)
        );

        tituloSuperior = new JLabel(biblioteca.getNombre());
        tituloSuperior.setFont(new Font("Arial", Font.BOLD, 24));
        tituloSuperior.setForeground(gestorTemas.getColorTexto());

        subtituloSuperior = new JLabel(
            "Cada lectura abre un mundo y enciende nuevas esperanzas"
        );

        subtituloSuperior.setFont(new Font("Arial", Font.PLAIN, 14));
        subtituloSuperior.setForeground(
            gestorTemas.getColorTextoSecundario()
        );

        panelTextos = new JPanel(new GridLayout(2, 1));
        panelTextos.setBackground(gestorTemas.getColorEncabezado());
        panelTextos.add(tituloSuperior);
        panelTextos.add(subtituloSuperior);

        panelSuperior.add(panelTextos, BorderLayout.WEST);

        // Boton para cambiar el tema
        botonTema = new BotonRedondeado("Tema");

        botonTema.addActionListener(
            evento -> cambiarTema()
        );

        // Contenido central
        panelContenido = new JPanel(new BorderLayout());
        panelContenido.setBackground(gestorTemas.getColorFondo());
        panelContenido.setBorder(
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        // Acciones del menu
        botonInicio.addActionListener(evento -> mostrarInicio());

        botonSocios.addActionListener(evento -> mostrarSocios());

        botonCatalogo.addActionListener(
            evento -> abrirCatalogo()
        );

        botonPrestamos.addActionListener(
            evento -> abrirPrestamos()
        );

        botonDevoluciones.addActionListener(
            evento -> abrirDevoluciones()
        );

        botonEstadisticas.addActionListener(
            evento -> abrirEstadisticas()
        );

        botonConsultas.addActionListener(
            evento -> abrirConsultas()
        );

        // Unimos los paneles
        panelPrincipal.add(panelMenu, BorderLayout.WEST);
        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);
        panelPrincipal.add(panelContenido, BorderLayout.CENTER);

        this.add(panelPrincipal);

        actualizarTema();
        mostrarInicio();
    }

    // Cambia entre los cuatro temas
    private void cambiarTema()
    {
        gestorTemas.cambiarTema();

        actualizarTema();

        mostrarInicio();
    }

    // Aplica el tema a la ventana principal
    private void actualizarTema()
    {
        panelPrincipal.setBackground(gestorTemas.getColorFondo());

        panelMenu.setBackground(gestorTemas.getColorMenu());

        panelSuperior.setBackground(
            gestorTemas.getColorEncabezado()
        );

        panelTextos.setBackground(
            gestorTemas.getColorEncabezado()
        );

        panelContenido.setBackground(
            gestorTemas.getColorFondo()
        );

        tituloSuperior.setForeground(
            gestorTemas.getColorTexto()
        );

        subtituloSuperior.setForeground(
            gestorTemas.getColorTextoSecundario()
        );

        botonTema.cambiarColores(
            gestorTemas.getColorMenu(),
            gestorTemas.getColorBotonHover(),
            gestorTemas.getColorBotonPresionado()
        );

        gestorTemas.aplicarTema(panelMenu);
        gestorTemas.aplicarTema(panelContenido);

        panelPrincipal.revalidate();
        panelPrincipal.repaint();
    }

    public void mostrarInicio()
    {
        panelContenido.removeAll();
        panelContenido.setLayout(new BorderLayout());
        panelContenido.setBackground(gestorTemas.getColorFondo());

        // El boton tema aparece solamente en Inicio
        panelSuperior.add(botonTema, BorderLayout.EAST);

        panelSuperior.revalidate();
        panelSuperior.repaint();

        JLabel bienvenida = new JLabel(
            "Bienvenido a la Biblioteca Gestion FACENA-UNNE",
            SwingConstants.CENTER
        );

        bienvenida.setFont(new Font("Arial", Font.BOLD, 25));
        bienvenida.setForeground(gestorTemas.getColorTexto());

        bienvenida.setBorder(
            BorderFactory.createEmptyBorder(35, 10, 15, 10)
        );

        

        panelContenido.add(bienvenida, BorderLayout.CENTER);
        

        gestorTemas.aplicarTema(panelContenido);

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    private void mostrarPantalla(String p_nombrePantalla)
    {
        panelContenido.removeAll();

        JLabel tituloPantalla = new JLabel(
            p_nombrePantalla,
            SwingConstants.CENTER
        );

        tituloPantalla.setFont(new Font("Arial", Font.BOLD, 28));
        tituloPantalla.setForeground(gestorTemas.getColorTexto());

        panelContenido.add(tituloPantalla, BorderLayout.CENTER);

        gestorTemas.aplicarTema(panelContenido);

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    // Metodo para cambiar el contenido sin modificar el menu ni el encabezado
    public void mostrarContenido(JPanel panel)
    {
        // El boton tema desaparece fuera de Inicio
        panelSuperior.remove(botonTema);
        panelSuperior.revalidate();
        panelSuperior.repaint();

        panelContenido.removeAll();

        panelContenido.add(panel, BorderLayout.CENTER);

        gestorTemas.aplicarTema(panelContenido);

        panelContenido.revalidate();
        panelContenido.repaint();
    }

    // Pantalla principal de socios
    public void mostrarSocios()
    {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setBackground(gestorTemas.getColorFondo());

        panel.setBorder(
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        JLabel titulo = new JLabel(
            "Gestion de socios",
            SwingConstants.CENTER
        );

        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(gestorTemas.getColorTexto());

        JLabel descripcion = new JLabel(
            "Selecciona una opcion para continuar.",
            SwingConstants.CENTER
        );

        descripcion.setFont(new Font("Arial", Font.PLAIN, 15));
        descripcion.setForeground(
            gestorTemas.getColorTextoSecundario()
        );

        JPanel encabezado = new JPanel(new GridLayout(2, 1, 5, 5));
        encabezado.setBackground(gestorTemas.getColorFondo());
        encabezado.add(titulo);
        encabezado.add(descripcion);

        JPanel tarjetas = new JPanel(new GridLayout(1, 3, 20, 20));
        tarjetas.setBackground(gestorTemas.getColorFondo());

        JPanel tarjetaDocentes = crearTarjetaSocios(
            "Docentes",
            "Registro y consulta de socios docentes."
        );

        JPanel tarjetaEstudiantes = crearTarjetaSocios(
            "Estudiantes",
            "Registro y consulta de socios estudiantes."
        );

        JPanel tarjetaLista = crearTarjetaSocios(
            "Lista de socios",
            "Consulta todos los socios registrados."
        );

        BotonRedondeado botonDocentes =
            new BotonRedondeado("Ingresar");

        BotonRedondeado botonEstudiantes =
            new BotonRedondeado("Ingresar");

        BotonRedondeado botonLista =
            new BotonRedondeado("Consultar lista");

        tarjetaDocentes.add(botonDocentes);
        tarjetaEstudiantes.add(botonEstudiantes);
        tarjetaLista.add(botonLista);

        tarjetas.add(tarjetaDocentes);
        tarjetas.add(tarjetaEstudiantes);
        tarjetas.add(tarjetaLista);

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(tarjetas, BorderLayout.CENTER);

        botonDocentes.addActionListener(
            evento -> abrirSociosDocentes()
        );

        botonEstudiantes.addActionListener(
            evento -> abrirSociosEstudiantes()
        );

        botonLista.addActionListener(
            evento -> mostrarListaSocios()
        );

        mostrarContenido(panel);
    }

    private JPanel crearTarjetaSocios(
        String p_titulo,
        String p_descripcion)
    {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(gestorTemas.getColorTarjeta());

        tarjeta.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235)),
                BorderFactory.createEmptyBorder(25, 15, 25, 15)
            )
        );

        JLabel titulo = new JLabel(p_titulo);
        titulo.setFont(new Font("Arial", Font.BOLD, 19));
        titulo.setForeground(gestorTemas.getColorTexto());
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descripcion = new JLabel(
            "<html><div style='text-align:center; width:180px;'>"
            + p_descripcion + "</div></html>"
        );

        descripcion.setFont(new Font("Arial", Font.PLAIN, 13));
        descripcion.setForeground(
            gestorTemas.getColorTextoSecundario()
        );

        descripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(Box.createVerticalGlue());
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(15));
        tarjeta.add(descripcion);
        tarjeta.add(Box.createVerticalStrut(25));
        tarjeta.add(Box.createVerticalGlue());

        return tarjeta;
    }

    private void abrirSociosDocentes()
    {
        VentanaSociosDocentes ventana =
            new VentanaSociosDocentes(
                biblioteca,
                bibliotecaDAO,
                this
            );

        mostrarContenido(ventana);
    }

    private void abrirSociosEstudiantes()
    {
        VentanaSociosEstudiantes ventana =
            new VentanaSociosEstudiantes(
                biblioteca,
                bibliotecaDAO,
                this
            );

        mostrarContenido(ventana);
    }

    private void mostrarListaSocios()
    {
        VentanaListaSocios ventana =
            new VentanaListaSocios(
                biblioteca,
                this
            );

        mostrarContenido(ventana);
    }

    private void abrirCatalogo()
    {
        VentanaCatalogo ventanaCatalogo =
            new VentanaCatalogo(biblioteca, bibliotecaDAO, this);

        mostrarContenido(ventanaCatalogo);
    }

    private void abrirPrestamos()
    {
        VentanaPrestamos ventanaPrestamos =
            new VentanaPrestamos(biblioteca, bibliotecaDAO, this);

        mostrarContenido(ventanaPrestamos);
    }

    private void abrirDevoluciones()
    {
        VentanaDevoluciones ventanaDevoluciones =
            new VentanaDevoluciones(biblioteca, bibliotecaDAO, this);

        mostrarContenido(ventanaDevoluciones);
    }

    public void abrirEstadisticas()
    {
        VentanaEstadisticas ventana =
            new VentanaEstadisticas(
                biblioteca,
                bibliotecaDAO,
                this
            );

        this.mostrarContenido(ventana);
    }

    public void abrirConsultas()
    {
        VentanaConsultas ventana =
            new VentanaConsultas(biblioteca, bibliotecaDAO, this);

        this.mostrarContenido(ventana);
    }
}
