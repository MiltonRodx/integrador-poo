
package biblioteca;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.sql.SQLException;

public class VentanaConsultas extends JPanel
{
    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;
    private VentanaPrincipal ventanaPrincipal;

    private JComboBox<Libro> comboLibros;
    private JLabel resultadoLibro;

    public VentanaConsultas(
        Biblioteca p_biblioteca,
        BibliotecaDAO p_bibliotecaDAO,
        VentanaPrincipal p_ventanaPrincipal)
    {
        this.biblioteca = p_biblioteca;
        this.bibliotecaDAO = p_bibliotecaDAO;
        this.ventanaPrincipal = p_ventanaPrincipal;

        this.crearInterfaz();
    }

    private void crearInterfaz()
    {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(Color.WHITE);
        this.setBorder(
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        );

        JLabel titulo = new JLabel(
            "Consultas",
            SwingConstants.CENTER
        );
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(15, 30, 60));

        JLabel descripcion = new JLabel(
            "Consulta quien tiene un libro y su fecha de vencimiento.",
            SwingConstants.CENTER
        );
        descripcion.setFont(new Font("Arial", Font.PLAIN, 15));
        descripcion.setForeground(new Color(100, 110, 125));

        JPanel textos = new JPanel(new GridLayout(2, 1, 5, 5));
        textos.setBackground(Color.WHITE);
        textos.add(titulo);
        textos.add(descripcion);

        BotonRedondeado volver =
            new BotonRedondeado("Volver al inicio");

        volver.addActionListener(
            evento -> ventanaPrincipal.mostrarInicio()
        );

        JPanel panelVolver = new JPanel(
            new FlowLayout(FlowLayout.LEFT)
        );
        panelVolver.setBackground(Color.WHITE);
        panelVolver.add(volver);

        JPanel encabezado = new JPanel(new BorderLayout());
        encabezado.setBackground(Color.WHITE);
        encabezado.add(panelVolver, BorderLayout.WEST);
        encabezado.add(textos, BorderLayout.CENTER);

        this.add(encabezado, BorderLayout.NORTH);
        this.add(this.crearTarjetaLibro(), BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(String p_titulo)
    {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 10));
        tarjeta.setBackground(Color.WHITE);

        tarjeta.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(
                    new Color(220, 226, 235)
                ),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
            )
        );

        JLabel titulo = new JLabel(p_titulo);
        titulo.setFont(new Font("Arial", Font.BOLD, 19));
        titulo.setForeground(new Color(15, 30, 60));

        tarjeta.add(titulo, BorderLayout.NORTH);

        return tarjeta;
    }

    private JPanel crearTarjetaLibro()
    {
        JPanel tarjeta = crearTarjeta("¿Quien tiene el libro?");

        comboLibros = new JComboBox<Libro>();

        for(Libro unLibro : biblioteca.getLibros()) {
            comboLibros.addItem(unLibro);
        }

        BotonRedondeado consultar =
            new BotonRedondeado("Consultar");

        consultar.addActionListener(
            evento -> this.consultarLibro()
        );

        resultadoLibro = new JLabel(
            "Selecciona un libro para realizar la consulta.",
            SwingConstants.CENTER
        );

        resultadoLibro.setFont(
            new Font("Arial", Font.BOLD, 16)
        );
        resultadoLibro.setForeground(new Color(15, 30, 60));

        JPanel fila = new JPanel(new BorderLayout(10, 10));
        fila.setBackground(Color.WHITE);
        fila.add(comboLibros, BorderLayout.CENTER);
        fila.add(consultar, BorderLayout.EAST);

        JPanel cuerpo = new JPanel(new BorderLayout(10, 10));
        cuerpo.setBackground(Color.WHITE);
        cuerpo.add(fila, BorderLayout.NORTH);
        cuerpo.add(resultadoLibro, BorderLayout.CENTER);

        tarjeta.add(cuerpo, BorderLayout.CENTER);

        return tarjeta;
    }

    private void consultarLibro()
    {
        Libro libro = (Libro) comboLibros.getSelectedItem();

        if(libro == null)
        {
            resultadoLibro.setText("No hay libros cargados.");
            return;
        }

        try
        {
            String socio = biblioteca.quienTieneElLibro(libro);

            Prestamo prestamo = libro.ultimoPrestamo();

            Calendar vencimiento =
                UtilidadesPrestamos.calcularVencimiento(
                    prestamo,
                    this.bibliotecaDAO.getPrestamoDAO()
                );

            String estado = "A tiempo";

            if(Calendar.getInstance().after(vencimiento))
            {
                estado = "Vencido";
            }

            String retiro =
                UtilidadesPrestamos.formatearFecha(
                    prestamo.getFechaRetiro()
                );

            String vence =
                UtilidadesPrestamos.formatearFecha(vencimiento);

            resultadoLibro.setText(
                "<html><div style='text-align:center'>"
                + "Lo tiene: " + socio + "<br>"
                + "Desde: " + retiro + "<br>"
                + "Vence: " + vence + " (" + estado + ")"
                + "</div></html>"
            );
        }
        catch(LibroNoPrestadoException error)
        {
            resultadoLibro.setText(error.getMessage());
        }
        catch(SQLException error)
        {
            UtilidadesInterfaz.mostrarMensajeError(
                this,
                "No se pudo consultar el vencimiento: "
                + error.getMessage()
            );
        }
    }
}
