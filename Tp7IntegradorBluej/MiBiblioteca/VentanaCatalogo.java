package MiBiblioteca;

import javax.swing.*;
import java.awt.*;

/**
 * Pantalla del menu Catalogo.
 * Muestra dos tarjetas: una para consultar la lista de libros y otra para
 * agregar o eliminar libros. No contiene reglas de negocio: solo navega
 * hacia las pantallas VentanaListaLibros y VentanaGestionLibros.
 */
public class VentanaCatalogo extends JPanel
{
    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;
    private VentanaPrincipal ventanaPrincipal;

    /**
     * Crea la pantalla del catalogo.
     *
     * @param p_biblioteca el modelo con los libros y socios
     * @param p_bibliotecaDAO acceso a la base de datos (se pasa a las pantallas hijas)
     * @param p_ventanaPrincipal ventana contenedora, usada para cambiar de pantalla
     */
    public VentanaCatalogo(
        Biblioteca p_biblioteca,
        BibliotecaDAO p_bibliotecaDAO,
        VentanaPrincipal p_ventanaPrincipal)
    {
        this.biblioteca = p_biblioteca;
        this.bibliotecaDAO = p_bibliotecaDAO;
        this.ventanaPrincipal = p_ventanaPrincipal;

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        this.crearInterfaz();
    }

    /**
     * Arma todos los componentes visibles: el encabezado con titulo y
     * descripcion, las dos tarjetas con sus botones y el boton "Volver al
     * inicio". Tambien asigna a cada boton la accion que dispara.
     */
    private void crearInterfaz()
    {
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(
            BorderFactory.createEmptyBorder(40, 45, 40, 45)
        );

        JLabel titulo = new JLabel("Nuestro catalogo");
        titulo.setFont(new Font("Arial", Font.BOLD, 30));
        titulo.setForeground(new Color(15, 30, 60));

        JLabel descripcion = new JLabel(
            "Cada libro guarda una historia esperando ser descubierta."
        );
        descripcion.setFont(new Font("Arial", Font.PLAIN, 16));
        descripcion.setForeground(new Color(100, 110, 125));

        JPanel panelEncabezado = new JPanel(new GridLayout(2, 1, 5, 5));
        panelEncabezado.setBackground(Color.WHITE);
        panelEncabezado.add(titulo);
        panelEncabezado.add(descripcion);

        JPanel panelOpciones = new JPanel(new GridLayout(1, 2, 25, 25));
        panelOpciones.setBackground(Color.WHITE);

        JPanel tarjetaLista = crearTarjeta(
            "Consultar libros",
            "Explora todos los libros disponibles y busca por titulo."
        );

        JPanel tarjetaGestion = crearTarjeta(
            "Gestionar libros",
            "Agrega nuevos ejemplares o elimina libros del catalogo."
        );

        BotonRedondeado botonLista =
            new BotonRedondeado("Mostrar lista");

        BotonRedondeado botonGestion =
            new BotonRedondeado("Agregar o eliminar");

        tarjetaLista.add(botonLista);
        tarjetaGestion.add(botonGestion);

        panelOpciones.add(tarjetaLista);
        panelOpciones.add(tarjetaGestion);

        BotonRedondeado botonVolver =
            new BotonRedondeado("Volver al inicio");

        JPanel panelInferior = new JPanel(
            new FlowLayout(FlowLayout.RIGHT)
        );
        panelInferior.setBackground(Color.WHITE);
        panelInferior.add(botonVolver);

        panelPrincipal.add(panelEncabezado, BorderLayout.NORTH);
        panelPrincipal.add(panelOpciones, BorderLayout.CENTER);
        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        this.add(panelPrincipal, BorderLayout.CENTER);

        botonLista.addActionListener(
            evento -> abrirLista()
        );

        botonGestion.addActionListener(
            evento -> abrirGestion()
        );

        botonVolver.addActionListener(
            evento -> ventanaPrincipal.mostrarInicio()
        );
    }

    /**
     * Construye una tarjeta con un titulo y una descripcion, con el estilo
     * visual del resto de la aplicacion. El boton se agrega despues, desde
     * quien llama a este metodo.
     *
     * @param p_titulo texto del titulo de la tarjeta
     * @param p_descripcion texto explicativo debajo del titulo
     * @return el panel de la tarjeta, listo para agregarle un boton
     */
    private JPanel crearTarjeta(
        String p_titulo,
        String p_descripcion)
    {
        JPanel tarjeta = new JPanel();
        tarjeta.setLayout(new BoxLayout(tarjeta, BoxLayout.Y_AXIS));
        tarjeta.setBackground(Color.WHITE);
        tarjeta.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235)),
                BorderFactory.createEmptyBorder(35, 25, 35, 25)
            )
        );

        JLabel titulo = new JLabel(p_titulo);
        titulo.setFont(new Font("Arial", Font.BOLD, 21));
        titulo.setForeground(new Color(15, 30, 60));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descripcion = new JLabel(
            "<html><div style='text-align:center; width:250px;'>"
            + p_descripcion + "</div></html>"
        );
        descripcion.setFont(new Font("Arial", Font.PLAIN, 14));
        descripcion.setForeground(new Color(100, 110, 125));
        descripcion.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjeta.add(Box.createVerticalGlue());
        tarjeta.add(titulo);
        tarjeta.add(Box.createVerticalStrut(20));
        tarjeta.add(descripcion);
        tarjeta.add(Box.createVerticalStrut(35));
        tarjeta.add(Box.createVerticalGlue());

        return tarjeta;
    }

    /**
     * Muestra en el area central la pantalla con la lista de libros del
     * catalogo (VentanaListaLibros). Se ejecuta al apretar "Mostrar lista".
     */
    private void abrirLista()
    {
        VentanaListaLibros ventana = new VentanaListaLibros(
            biblioteca,
            this
        );

        ventanaPrincipal.mostrarContenido(ventana);
    }

    /**
     * Muestra en el area central la pantalla para agregar o eliminar libros
     * (VentanaGestionLibros). Se ejecuta al apretar "Agregar o eliminar".
     * Recibe tambien el DAO porque esa pantalla guarda los cambios en la base.
     */
    private void abrirGestion()
    {
        VentanaGestionLibros ventana = new VentanaGestionLibros(
            biblioteca,
            bibliotecaDAO,
            this
        );

        ventanaPrincipal.mostrarContenido(ventana);
    }

    /**
     * Vuelve a mostrar el menu del catalogo creando una pantalla nueva.
     * Es publico para que las pantallas hijas puedan regresar a este menu.
     */
    public void mostrarCatalogo()
    {
        ventanaPrincipal.mostrarContenido(
            new VentanaCatalogo(
                biblioteca,
                bibliotecaDAO,
                ventanaPrincipal
            )
        );
    }
}
