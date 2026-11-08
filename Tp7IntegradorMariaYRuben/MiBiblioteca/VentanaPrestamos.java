package MiBiblioteca;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.sql.SQLException;

public class VentanaPrestamos extends JPanel
{
    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;
    private VentanaPrincipal ventanaPrincipal;

    private JComboBox<Socio> comboSocios;
    private JComboBox<Libro> comboLibros;
    private JSpinner fechaRetiro;

    private DefaultTableModel modeloTabla;
    private JTable tablaPrestamos;

    public VentanaPrestamos(
        Biblioteca p_biblioteca,
        BibliotecaDAO p_bibliotecaDAO,
        VentanaPrincipal p_ventanaPrincipal)
    {
        this.biblioteca = p_biblioteca;
        this.bibliotecaDAO = p_bibliotecaDAO;
        this.ventanaPrincipal = p_ventanaPrincipal;

        this.setLayout(new BorderLayout());
        this.setBackground(Color.WHITE);

        this.crearPantalla();
    }

    private void crearPantalla()
    {
        JPanel panelSuperior = new JPanel();
        panelSuperior.setLayout(
            new BoxLayout(panelSuperior, BoxLayout.Y_AXIS)
        );
        panelSuperior.setBackground(Color.WHITE);
        panelSuperior.setBorder(
            BorderFactory.createEmptyBorder(25, 30, 10, 30)
        );

        JLabel titulo = new JLabel("Gestion de prestamos");
        titulo.setFont(new Font("Arial", Font.BOLD, 27));
        titulo.setForeground(new Color(25, 45, 85));

        JLabel subtitulo = new JLabel(
            "Registra prestamos y consulta sus fechas de vencimiento"
        );
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitulo.setForeground(Color.GRAY);

        panelSuperior.add(titulo);
        panelSuperior.add(Box.createVerticalStrut(5));
        panelSuperior.add(subtitulo);
        panelSuperior.add(Box.createVerticalStrut(20));
        panelSuperior.add(this.crearFormulario());

        this.add(panelSuperior, BorderLayout.NORTH);

        JPanel panelTabla = this.crearTabla();
        panelTabla.setBorder(
            BorderFactory.createEmptyBorder(5, 30, 25, 30)
        );

        this.add(panelTabla, BorderLayout.CENTER);

        this.actualizarSocios();
        this.actualizarLibros();
        this.actualizarTabla();
    }

    private JPanel crearFormulario()
    {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 220, 220)),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
            )
        );

        GridBagConstraints posicion = new GridBagConstraints();
        posicion.insets = new Insets(8, 8, 8, 8);
        posicion.fill = GridBagConstraints.HORIZONTAL;
        posicion.weightx = 1;

        JLabel titulo = new JLabel("Agregar prestamo");
        titulo.setFont(new Font("Arial", Font.BOLD, 19));
        titulo.setForeground(new Color(25, 45, 85));

        posicion.gridx = 0;
        posicion.gridy = 0;
        posicion.gridwidth = 3;
        panel.add(titulo, posicion);

        posicion.gridwidth = 1;

        JLabel etiquetaSocio = new JLabel("Socio");
        etiquetaSocio.setFont(new Font("Arial", Font.BOLD, 13));

        posicion.gridx = 0;
        posicion.gridy = 1;
        panel.add(etiquetaSocio, posicion);

        comboSocios = new JComboBox<Socio>();
        comboSocios.setFont(new Font("Arial", Font.PLAIN, 14));

        posicion.gridx = 0;
        posicion.gridy = 2;
        panel.add(comboSocios, posicion);

        JLabel etiquetaFecha = new JLabel("Fecha de retiro");
        etiquetaFecha.setFont(new Font("Arial", Font.BOLD, 13));

        posicion.gridx = 1;
        posicion.gridy = 1;
        panel.add(etiquetaFecha, posicion);

        fechaRetiro = new JSpinner(
            new SpinnerDateModel(
                new Date(),
                null,
                null,
                Calendar.DAY_OF_MONTH
            )
        );

        JSpinner.DateEditor editor =
            new JSpinner.DateEditor(fechaRetiro, "dd/MM/yyyy");

        fechaRetiro.setEditor(editor);

        posicion.gridx = 1;
        posicion.gridy = 2;
        panel.add(fechaRetiro, posicion);

        JLabel etiquetaLibro = new JLabel("Libro disponible");
        etiquetaLibro.setFont(new Font("Arial", Font.BOLD, 13));

        posicion.gridx = 0;
        posicion.gridy = 3;
        posicion.gridwidth = 2;
        panel.add(etiquetaLibro, posicion);

        comboLibros = new JComboBox<Libro>();
        comboLibros.setFont(new Font("Arial", Font.PLAIN, 14));

        posicion.gridx = 0;
        posicion.gridy = 4;
        panel.add(comboLibros, posicion);

        BotonRedondeado botonAgregar =
            new BotonRedondeado("Registrar prestamo");

        botonAgregar.addActionListener(
            evento -> this.registrarPrestamo()
        );

        posicion.gridx = 2;
        posicion.gridy = 4;
        posicion.gridwidth = 1;
        panel.add(botonAgregar, posicion);

        return panel;
    }

    private JPanel crearTabla()
    {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Prestamos registrados");
        titulo.setFont(new Font("Arial", Font.BOLD, 20));
        titulo.setForeground(new Color(25, 45, 85));

        panel.add(titulo, BorderLayout.NORTH);

        modeloTabla = new DefaultTableModel(
            new Object[] {
                "Socio",
                "Libro",
                "Fecha de retiro",
                "Fecha de vencimiento",
                "Estado"
            },
            0
        )
        {
            public boolean isCellEditable(int fila, int columna)
            {
                return false;
            }
        };

        tablaPrestamos = new JTable(modeloTabla);
        tablaPrestamos.setFont(new Font("Arial", Font.PLAIN, 13));
        tablaPrestamos.setRowHeight(38);

        tablaPrestamos.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        tablaPrestamos.getTableHeader().setBackground(
            new Color(25, 45, 85)
        );

        tablaPrestamos.getTableHeader().setForeground(Color.WHITE);

       tablaPrestamos.getColumnModel().getColumn(4) .setCellRenderer(new RenderizadorEstadoPrestamo());
        JScrollPane desplazamiento = new JScrollPane(tablaPrestamos);

        panel.add(desplazamiento, BorderLayout.CENTER);

        return panel;
    }

    private void actualizarSocios()
    {
        comboSocios.removeAllItems();

        for(Socio unSocio : this.biblioteca.getSocios())
        {
            comboSocios.addItem(unSocio);
        }
    }

    private void actualizarLibros()
    {
        comboLibros.removeAllItems();

        for(Libro unLibro : this.biblioteca.getLibros())
        {
            if(!unLibro.prestado())
            {
                comboLibros.addItem(unLibro);
            }
        }
    }

 private void actualizarTabla()
{
    modeloTabla.setRowCount(0);

    try
    {
        for(Socio unSocio : this.biblioteca.getSocios())
        {
            for(Prestamo unPrestamo : unSocio.getPrestamos())
            {
                Calendar vencimiento =
                    UtilidadesPrestamos.calcularVencimiento(
                        unPrestamo,
                        this.bibliotecaDAO.getPrestamoDAO()
                    );

                String estado;

                if(unPrestamo.estaDevuelto())
                {
                    if(unPrestamo.getFechaDevolucion().after(vencimiento))
                    {
                        estado = "Devuelto tarde";
                    }
                    else
                    {
                        estado = "Devuelto";
                    }
                }
                else if(Calendar.getInstance().after(vencimiento))
                {
                    estado = "Vencido";
                }
                else
                {
                    estado = "A tiempo";
                }

                modeloTabla.addRow(new Object[] {
                    unPrestamo.getSocio().getNombre(),
                    unPrestamo.getLibro().getTitulo(),
                    UtilidadesPrestamos.formatearFecha(
                        unPrestamo.getFechaRetiro()
                    ),
                    UtilidadesPrestamos.formatearFecha(vencimiento),
                    estado
                });
            }
        }
    }
    catch(SQLException error)
    {
        JOptionPane.showMessageDialog(
            this,
            "No se pudieron consultar los prestamos: " +
            error.getMessage()
        );
    }
}

private void registrarPrestamo()
{
    Socio socio = (Socio) comboSocios.getSelectedItem();
    Libro libro = (Libro) comboLibros.getSelectedItem();

    // Verificar que haya un socio seleccionado
    if(socio == null)
    {
        UtilidadesInterfaz.mostrarMensajeError(
            this,
            "No hay socios registrados."
        );
        return;
    }

    // Verificar que haya un libro seleccionado
    if(libro == null)
    {
        UtilidadesInterfaz.mostrarMensajeError(
            this,
            "No hay libros disponibles."
        );
        return;
    }

    // Verificar que el libro no este prestado
    if(libro.prestado())
    {
        UtilidadesInterfaz.mostrarMensajeError(
            this,
            "No se puede prestar este libro porque ya se encuentra prestado."
        );

        this.actualizarLibros();
        return;
    }

    // Verificar si el socio puede solicitar prestamos
    if(!socio.puedePedir())
    {
        UtilidadesInterfaz.mostrarMensajeError(
            this,
            "No se le puede prestar libros a este socio porque tiene un prestamo vencido o alcanzo el limite permitido."
        );
        return;
    }

    // Obtener la fecha seleccionada
    Calendar fecha = Calendar.getInstance();
    fecha.setTime((java.util.Date) fechaRetiro.getValue());

    // Registrar el prestamo
    boolean prestado = this.biblioteca.prestarLibro(
        fecha,
        null,
        socio,
        libro
    );

    if(!prestado)
    {
        UtilidadesInterfaz.mostrarMensajeError(
            this,
            "Prestamo rechazado por las reglas de la biblioteca."
        );

        this.actualizarLibros();
        return;
    }

    // Guardar el prestamo en la base de datos
    try
    {
        Prestamo prestamo = libro.ultimoPrestamo();

        this.bibliotecaDAO.getPrestamoDAO().agregar(
            prestamo,
            socio.getDiasPrestamo()
        );

        this.actualizarLibros();
        this.actualizarTabla();

        UtilidadesInterfaz.mostrarMensajeExito(
            this,
            "El prestamo se registro correctamente."
        );
    }
    catch(SQLException error)
    {
        // La persistencia manda: si la base rechazo el insert (por ejemplo
        // el indice unico de prestamo activo duplicado), el prestamo que
        // biblioteca.prestarLibro() agrego en memoria se deshace para que
        // la GUI y la base queden coherentes.
        Prestamo prestamoRechazado = libro.ultimoPrestamo();

        libro.getPrestamos().remove(prestamoRechazado);
        socio.getPrestamos().remove(prestamoRechazado);

        this.actualizarLibros();
        this.actualizarTabla();

        UtilidadesInterfaz.mostrarMensajeError(
            this,
            "No se pudo guardar el prestamo en la base: " +
            error.getMessage()
        );
    }
}

}