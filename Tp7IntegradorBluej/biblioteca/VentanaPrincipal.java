package biblioteca;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;
import java.util.ArrayList;

public class VentanaPrincipal extends JFrame {
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

    public VentanaPrincipal(Biblioteca p_biblioteca, BibliotecaDAO p_bibliotecaDAO) {
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
        catch (Exception error) {
            System.out.println("No se pudo iniciar la aplicacion: " + error.getMessage());
        }
    }

    private void crearInterfaz() {
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
        tituloSuperior.setHorizontalAlignment(SwingConstants.CENTER);

        subtituloSuperior = new JLabel(
            "Equipo: Los NullPointerException"
            //"Cada lectura abre un mundo y enciende nuevas esperanzas"
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

    // Tarjeta con todo centrado (titulo, descripcion y boton).
    // Es publica para poder reutilizarla desde VentanaCatalogo y otras pantallas.
    public JPanel crearTarjeta(
        String p_titulo,
        String p_descripcion,
        BotonRedondeado p_boton)
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

        JLabel titulo = new JLabel(p_titulo, SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 19));
        titulo.setForeground(gestorTemas.getColorTexto());
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descripcion = new JLabel(
            "<html><div style='text-align:center; width:180px;'>"
            + p_descripcion + "</div></html>",
            SwingConstants.CENTER
        );
        descripcion.setHorizontalAlignment(SwingConstants.CENTER);
        descripcion.setFont(new Font("Arial", Font.PLAIN, 13));
        descripcion.setForeground(gestorTemas.getColorTextoSecundario());
        descripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        // El boton no se estira y queda centrado
        p_boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        p_boton.setMaximumSize(p_boton.getPreferredSize());

        tarjeta.add(Box.createVerticalGlue());
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(15));
        tarjeta.add(descripcion);
        tarjeta.add(Box.createVerticalStrut(25));
        tarjeta.add(p_boton);
        tarjeta.add(Box.createVerticalGlue());

        return tarjeta;
    }

    /**
     * Igual que crearTarjeta(titulo, descripcion, boton), pero con un boton
     * secundario debajo del principal (por ejemplo "Premiar" en Docentes).
     */
    public JPanel crearTarjeta(
        String p_titulo,
        String p_descripcion,
        BotonRedondeado p_boton,
        BotonRedondeado p_botonSecundario)
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

        JLabel titulo = new JLabel(p_titulo, SwingConstants.CENTER);
        titulo.setFont(new Font("Arial", Font.BOLD, 19));
        titulo.setForeground(gestorTemas.getColorTexto());
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descripcion = new JLabel(
            "<html><div style='text-align:center; width:180px;'>"
            + p_descripcion + "</div></html>",
            SwingConstants.CENTER
        );
        descripcion.setHorizontalAlignment(SwingConstants.CENTER);
        descripcion.setFont(new Font("Arial", Font.PLAIN, 13));
        descripcion.setForeground(gestorTemas.getColorTextoSecundario());
        descripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Los botones no se estiran y quedan centrados, uno debajo del otro
        p_boton.setAlignmentX(Component.CENTER_ALIGNMENT);
        p_boton.setMaximumSize(p_boton.getPreferredSize());

        p_botonSecundario.setAlignmentX(Component.CENTER_ALIGNMENT);
        p_botonSecundario.setMaximumSize(p_botonSecundario.getPreferredSize());

        tarjeta.add(Box.createVerticalGlue());
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(15));
        tarjeta.add(descripcion);
        tarjeta.add(Box.createVerticalStrut(25));
        tarjeta.add(p_boton);
        tarjeta.add(Box.createVerticalStrut(10));
        tarjeta.add(p_botonSecundario);
        tarjeta.add(Box.createVerticalGlue());

        return tarjeta;
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

        // 4 tarjetas en una sola fila
        JPanel tarjetas = new JPanel(new GridLayout(1, 4, 20, 20));
        tarjetas.setBackground(gestorTemas.getColorFondo());

        BotonRedondeado botonDocentes =
            new BotonRedondeado("Ingresar");

        BotonRedondeado botonPremiar =
            new BotonRedondeado("Premiar");

        BotonRedondeado botonEstudiantes =
            new BotonRedondeado("Ingresar");

        BotonRedondeado botonLista =
            new BotonRedondeado("Consultar lista");

        BotonRedondeado botonEliminar =
            new BotonRedondeado("Eliminar");

        JPanel tarjetaDocentes = crearTarjeta(
            "Docentes",
            "Registro y consulta de socios docentes.",
            botonDocentes,
            botonPremiar
        );

        JPanel tarjetaEstudiantes = crearTarjeta(
            "Estudiantes",
            "Registro y consulta de socios estudiantes.",
            botonEstudiantes
        );

        JPanel tarjetaLista = crearTarjeta(
            "Lista de socios",
            "Consulta todos los socios registrados.",
            botonLista
        );

        JPanel tarjetaEliminar = crearTarjeta(
            "Eliminar socio",
            "Baja un socio solo si no tiene libros prestados.",
            botonEliminar
        );

        tarjetas.add(tarjetaDocentes);
        tarjetas.add(tarjetaEstudiantes);
        tarjetas.add(tarjetaLista);
        tarjetas.add(tarjetaEliminar);

        panel.add(encabezado, BorderLayout.NORTH);
        panel.add(tarjetas, BorderLayout.CENTER);

        botonDocentes.addActionListener(
            evento -> abrirSociosDocentes()
        );

        botonPremiar.addActionListener(
            evento -> premiarDocentes()
        );

        botonEstudiantes.addActionListener(
            evento -> abrirSociosEstudiantes()
        );

        botonLista.addActionListener(
            evento -> mostrarListaSocios()
        );

        botonEliminar.addActionListener(
            evento -> {
                // 1. Pedir DNI mediante dialogo
                String dniInput = JOptionPane.showInputDialog(
                    this,
                    "Ingrese el D.N.I. del socio a eliminar:",
                    "Eliminar socio",
                    JOptionPane.QUESTION_MESSAGE
                );
                if (dniInput == null) {
                    // Usuario presiono Cancelar
                    return;
                }
                int dni = 0;
                try {
                    dni = Integer.parseInt(dniInput.trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(
                        this,
                        "DNI invalido. Debe ser un numero.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                // 2. Buscar socio
                Socio socio = biblioteca.buscarSocio(dni);
                if (socio == null) {
                    JOptionPane.showMessageDialog(
                        this,
                        "No se encontro un socio con DNI: " + dniInput,
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                    return;
                }
                // 3. Verificar que NO tenga prestamos activos
                boolean tieneActivos = socio.getPrestamos().stream()
                        .anyMatch(p -> !p.estaDevuelto());
                if (tieneActivos) {
                    JOptionPane.showMessageDialog(
                        this,
                        "No se puede eliminar al socio " + socio.getNombre()
                            + ": tiene " + socio.cantLibrosPrestados()
                            + " libro(s) prestado(s) activo(s).",
                        "No se puede eliminar",
                        JOptionPane.WARNING_MESSAGE
                    );
                    return;
                }
                // 4. Eliminar socio
                boolean eliminado = biblioteca.quitarSocio(socio);
                if (eliminado) {
                    JOptionPane.showMessageDialog(
                        this,
                        "Socio eliminado correctamente: " + socio.getNombre(),
                        "Exito",
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    // Actualizar vista (volver a mostrar el menu de socios)
                    mostrarSocios();
                } else {
                    JOptionPane.showMessageDialog(
                        this,
                        "Error: no se pudo eliminar el socio.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    );
                }
            }
        );

        this.mostrarContenido(panel);
    }

    /**
     * Premio a los docentes responsables desde la tarjeta Docentes.
     *
     * Abre un dialogo con un desplegable para elegir a quien premiar
     * (un docente responsable, o todos de una vez) y la cantidad de dias
     * a agregar. Mismo criterio que el resto del sistema: primero el
     * modelo ({@link Docente#cambiarDiasDePrestamos(int)}); si la base
     * rechaza la persistencia, se deshace el cambio en memoria.
     */
    private void premiarDocentes()
    {
        ArrayList<Docente> responsables = biblioteca.docentesResponsables();

        if (responsables.isEmpty()) {
            JOptionPane.showMessageDialog(
                this,
                "No hay docentes responsables a quienes premiar.",
                "Premiar docentes",
                JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // Desplegable: la primera opcion premia a todos, las demas a uno solo
        JComboBox<String> selector = new JComboBox<String>();

        selector.addItem("(Todos los responsables)");

        for (Docente unDocente : responsables) {
            selector.addItem(
                unDocente.getNombre()
                + " (D.N.I. " + unDocente.getDniSocio() + ")"
                + " - tiene " + unDocente.getDiasPrestamo()
                + " dias de prestamo"
            );
        }

        JTextField campoDias = new JTextField("5", 6);

        JPanel panel = new JPanel(new GridLayout(0, 1, 8, 8));
        panel.add(new JLabel("Premiar a:"));
        panel.add(selector);
        panel.add(new JLabel("Cantidad de dias a premiar:"));
        panel.add(campoDias);

        int opcion = JOptionPane.showConfirmDialog(
            this,
            panel,
            "Premiar docentes responsables",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );

        if (opcion != JOptionPane.OK_OPTION) {
            return;
        }

        int dias = 0;
        try {
            dias = Integer.parseInt(campoDias.getText().trim());
        } catch (NumberFormatException error) {
            JOptionPane.showMessageDialog(
                this,
                "Debe ingresar un numero entero.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        if (dias <= 0) {
            JOptionPane.showMessageDialog(
                this,
                "La cantidad de dias debe ser mayor que cero.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // A quien premia: (Todos) o el docente elegido en el desplegable
        ArrayList<Docente> aPremiar = new ArrayList<Docente>();

        if (selector.getSelectedIndex() == 0) {
            aPremiar.addAll(responsables);
        } else {
            aPremiar.add(responsables.get(selector.getSelectedIndex() - 1));
        }

        // Modelo primero: el premio toca solo memoria.
        for (Docente unDocente : aPremiar) {
            unDocente.cambiarDiasDePrestamos(dias);
        }

        try {
            for (Docente unDocente : aPremiar) {
                bibliotecaDAO.getSocioDAO().actualizarDiasPrestamo(unDocente);
            }

            StringBuilder detalle = new StringBuilder();

            for (Docente unDocente : aPremiar) {
                detalle
                    .append("- ").append(unDocente.getNombre())
                    .append(" (D.N.I. ").append(unDocente.getDniSocio())
                    .append("): ahora tiene ").append(unDocente.getDiasPrestamo())
                    .append(" dias de prestamo\n");
            }

            JOptionPane.showMessageDialog(
                this,
                "Premio otorgado: +" + dias + " dias de prestamo a "
                + aPremiar.size() + " docente(s) responsable(s):\n\n"
                + detalle,
                "Premio otorgado",
                JOptionPane.INFORMATION_MESSAGE
            );
        } catch (SQLException error) {
            // La base rechazo: deshago el premio en memoria
            for (Docente unDocente : aPremiar) {
                unDocente.cambiarDiasDePrestamos(-dias);
            }

            JOptionPane.showMessageDialog(
                this,
                "No se pudo guardar el premio en la base de datos "
                + "(se deshizo el cambio en memoria).",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
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

    private void mostrarListaSocios() {
        VentanaListaSocios ventana =
            new VentanaListaSocios(
                biblioteca,
                this
            );

        mostrarContenido(ventana);
    }

    private void abrirCatalogo() {
        VentanaCatalogo ventanaCatalogo =
            new VentanaCatalogo(biblioteca, bibliotecaDAO, this);

        mostrarContenido(ventanaCatalogo);
    }

    private void abrirPrestamos() {
        VentanaPrestamos ventanaPrestamos =
            new VentanaPrestamos(biblioteca, bibliotecaDAO, this);

        mostrarContenido(ventanaPrestamos);
    }

    private void abrirDevoluciones() {
        VentanaDevoluciones ventanaDevoluciones =
            new VentanaDevoluciones(biblioteca, bibliotecaDAO, this);

        mostrarContenido(ventanaDevoluciones);
    }

    public void abrirEstadisticas() {
        VentanaEstadisticas ventana =
            new VentanaEstadisticas(
                biblioteca,
                bibliotecaDAO,
                this
            );

        this.mostrarContenido(ventana);
    }

    public void abrirConsultas() {
        VentanaConsultas ventana =
            new VentanaConsultas(biblioteca, bibliotecaDAO, this);

        this.mostrarContenido(ventana);
    }
}