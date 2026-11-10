
package biblioteca;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.Calendar;
import java.sql.SQLException;

public class VentanaDevoluciones extends JPanel
{
    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;
    private VentanaPrincipal ventanaPrincipal;

    private DefaultTableModel modeloTabla;
    private JTable tablaDevoluciones;

    private JButton botonDevolver;

    public VentanaDevoluciones(
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
        JPanel panelPrincipal = new JPanel(new BorderLayout(15, 15));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        );

        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(Color.WHITE);

        BotonRedondeado botonVolver =
            new BotonRedondeado("Volver al inicio");

        botonVolver.addActionListener(
            evento -> ventanaPrincipal.mostrarInicio()
        );

        panelSuperior.add(botonVolver, BorderLayout.WEST);

        JPanel panelTitulos = new JPanel();
        panelTitulos.setLayout(
            new BoxLayout(panelTitulos, BoxLayout.Y_AXIS)
        );
        panelTitulos.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Gestion de devoluciones");
        titulo.setFont(new Font("Arial", Font.BOLD, 27));
        titulo.setForeground(new Color(25, 45, 85));
        titulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitulo = new JLabel(
            "Consulta los prestamos activos y registra las devoluciones"
        );
        subtitulo.setFont(new Font("Arial", Font.PLAIN, 14));
        subtitulo.setForeground(Color.GRAY);
        subtitulo.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelTitulos.add(titulo);
        panelTitulos.add(Box.createVerticalStrut(5));
        panelTitulos.add(subtitulo);

        panelSuperior.add(panelTitulos, BorderLayout.CENTER);

        panelPrincipal.add(panelSuperior, BorderLayout.NORTH);

        JPanel panelTabla = this.crearTabla();
        panelPrincipal.add(panelTabla, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel();
        panelInferior.setBackground(Color.WHITE);

        botonDevolver =
            new BotonRedondeado("Registrar devolucion");

        botonDevolver.addActionListener(
            evento -> this.registrarDevolucion()
        );

        panelInferior.add(botonDevolver);

        panelPrincipal.add(panelInferior, BorderLayout.SOUTH);

        this.add(panelPrincipal, BorderLayout.CENTER);

        this.actualizarTabla();
    }

    private JPanel crearTabla()
    {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);

        JLabel titulo = new JLabel("Prestamos pendientes de devolucion");
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

        tablaDevoluciones = new JTable(modeloTabla);
        tablaDevoluciones.setFont(
            new Font("Arial", Font.PLAIN, 13)
        );
        tablaDevoluciones.setRowHeight(38);
        tablaDevoluciones.setSelectionMode(
            ListSelectionModel.SINGLE_SELECTION
        );

        tablaDevoluciones.getTableHeader().setFont(
            new Font("Arial", Font.BOLD, 13)
        );

        tablaDevoluciones.getTableHeader().setBackground(
            new Color(25, 45, 85)
        );

        tablaDevoluciones.getTableHeader().setForeground(
            Color.WHITE
        );

        tablaDevoluciones.getColumnModel()
            .getColumn(4)
            .setCellRenderer(new EstadoRenderer());

        JScrollPane desplazamiento =
            new JScrollPane(tablaDevoluciones);

        desplazamiento.setBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220))
        );

        panel.add(desplazamiento, BorderLayout.CENTER);

        return panel;
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
                    if(!unPrestamo.estaDevuelto())
                    {
                        Calendar vencimiento =
                            UtilidadesPrestamos.calcularVencimiento(
                                unPrestamo,
                                this.bibliotecaDAO.getPrestamoDAO()
                            );

                        String estado;

                        if(Calendar.getInstance().after(vencimiento))
                        {
                            estado = "Vencido";
                        }
                        else
                        {
                            estado = "A tiempo";
                        }

                        modeloTabla.addRow(new Object[] {
                            unSocio.getNombre(),
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

    private void registrarDevolucion()
    {
        int fila = tablaDevoluciones.getSelectedRow();

        if(fila == -1)
        {
            JOptionPane.showMessageDialog(
                this,
                "Selecciona un prestamo de la tabla."
            );
            return;
        }

        int respuesta = JOptionPane.showConfirmDialog(
            this,
            "¿Deseas registrar la devolucion de este libro?",
            "Confirmar devolucion",
            JOptionPane.YES_NO_OPTION
        );

        if(respuesta == JOptionPane.YES_OPTION)
        {
            int filaModelo =
                tablaDevoluciones.convertRowIndexToModel(fila);

            String nombreSocio =
                modeloTabla.getValueAt(filaModelo, 0).toString();

            String tituloLibro =
                modeloTabla.getValueAt(filaModelo, 1).toString();

            Prestamo prestamoSeleccionado = null;

            for(Socio unSocio : this.biblioteca.getSocios())
            {
                for(Prestamo unPrestamo : unSocio.getPrestamos())
                {
                    if(!unPrestamo.estaDevuelto() &&
                       unPrestamo.getSocio().getNombre().equals(nombreSocio) &&
                       unPrestamo.getLibro().getTitulo().equals(tituloLibro))
                    {
                        prestamoSeleccionado = unPrestamo;
                    }
                }
            }

            if(prestamoSeleccionado == null)
            {
                JOptionPane.showMessageDialog(
                    this,
                    "No se encontro el prestamo seleccionado."
                );

                this.actualizarTabla();
                return;
            }

            // La devolucion pasa por el MODELO (regla del enunciado):
            // biblioteca.devolverLibro() valida que el libro este prestado
            // y avisa con su excepcion si no corresponde devolver.
            try
            {
                this.biblioteca.devolverLibro(
                    prestamoSeleccionado.getLibro()
                );
            }
            catch(LibroNoPrestadoException error)
            {
                JOptionPane.showMessageDialog(
                    this,
                    error.getMessage()
                );

                this.actualizarTabla();
                return;
            }

            // La persistencia manda: si el guardado en la base falla, se
            // deshace la devolucion en memoria para que la GUI y la DB
            // queden coherentes (quedo sin registrar, como antes).
            try
            {
                int filas = this.bibliotecaDAO.getPrestamoDAO()
                    .actualizarDevolucion(prestamoSeleccionado);

                if(filas == 0)
                {
                    prestamoSeleccionado.registrarFechaDevolucion(null);

                    JOptionPane.showMessageDialog(
                        this,
                        "No se encontro el prestamo activo en la base: " +
                        "la devolucion no se registro."
                    );
                }
                else
                {
                    this.actualizarTabla();

                    JOptionPane.showMessageDialog(
                        this,
                        "La devolucion se registro correctamente."
                    );
                }
            }
            catch(SQLException error)
            {
                prestamoSeleccionado.registrarFechaDevolucion(null);

                JOptionPane.showMessageDialog(
                    this,
                    "No se pudo registrar la devolucion: " +
                    error.getMessage()
                );
            }
        }
    }

    private class EstadoRenderer extends DefaultTableCellRenderer
    {
        public Component getTableCellRendererComponent(
            JTable tabla,
            Object valor,
            boolean seleccionado,
            boolean tieneFoco,
            int fila,
            int columna)
        {
            JButton boton = new JButton(valor.toString());

            boton.setFont(new Font("Arial", Font.BOLD, 12));
            boton.setFocusPainted(false);
            boton.setBorderPainted(false);
            boton.setOpaque(true);

            if(valor.toString().equals("A tiempo"))
            {
                boton.setBackground(new Color(210, 240, 220));
                boton.setForeground(new Color(30, 110, 60));
            }
            else
            {
                boton.setBackground(new Color(250, 215, 215));
                boton.setForeground(new Color(160, 40, 40));
            }

            return boton;
        }
    }
}
