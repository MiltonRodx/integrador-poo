
package MiBiblioteca;

import javax.swing.*;
import java.awt.*;
import java.sql.SQLException;

public class VentanaSociosEstudiantes extends JPanel
{
    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;
    private VentanaPrincipal ventanaPrincipal;

    private JTextField campoDni;
    private JTextField campoNombre;
    private JTextField campoCarrera;
    private JTextArea areaSocios;

    public VentanaSociosEstudiantes(
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

    private void crearInterfaz()
    {
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(Color.WHITE);
        panelPrincipal.setBorder(
            BorderFactory.createEmptyBorder(30, 35, 30, 35)
        );

        JLabel titulo = new JLabel("Registro de estudiantes");
        titulo.setFont(new Font("Arial", Font.BOLD, 28));
        titulo.setForeground(new Color(15, 30, 60));

        JLabel descripcion = new JLabel(
            "Completa los datos para incorporar un estudiante a la biblioteca."
        );
        descripcion.setFont(new Font("Arial", Font.PLAIN, 15));
        descripcion.setForeground(new Color(100, 110, 125));

        JPanel panelEncabezado = new JPanel(new GridLayout(2, 1, 5, 5));
        panelEncabezado.setBackground(Color.WHITE);
        panelEncabezado.add(titulo);
        panelEncabezado.add(descripcion);

        JPanel panelFormulario = new JPanel(new GridLayout(3, 1, 10, 15));
        panelFormulario.setBackground(Color.WHITE);
        panelFormulario.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 226, 235)),
                BorderFactory.createEmptyBorder(25, 30, 25, 30)
            )
        );

        campoDni = new JTextField();
        campoNombre = new JTextField();
        campoCarrera = new JTextField();

        panelFormulario.add(crearCampo("DNI", campoDni));
        panelFormulario.add(crearCampo("Nombre y apellido", campoNombre));
        panelFormulario.add(crearCampo("Carrera", campoCarrera));

        BotonRedondeado botonRegistrar =
            new BotonRedondeado("Registrar estudiante");

        BotonRedondeado botonVolver =
            new BotonRedondeado("Volver");

        JPanel panelBotones = new JPanel(
            new FlowLayout(FlowLayout.RIGHT, 15, 10)
        );
        panelBotones.setBackground(Color.WHITE);
        panelBotones.add(botonVolver);
        panelBotones.add(botonRegistrar);

        JPanel panelDatos = new JPanel(new BorderLayout(10, 10));
        panelDatos.setBackground(Color.WHITE);
        panelDatos.add(panelFormulario, BorderLayout.CENTER);
        panelDatos.add(panelBotones, BorderLayout.SOUTH);

        JLabel tituloLista = new JLabel("Socios registrados");
        tituloLista.setFont(new Font("Arial", Font.BOLD, 19));
        tituloLista.setForeground(new Color(15, 30, 60));

        areaSocios = new JTextArea();
        areaSocios.setFont(new Font("Arial", Font.PLAIN, 14));
        areaSocios.setEditable(false);
        areaSocios.setLineWrap(true);
        areaSocios.setWrapStyleWord(true);
        areaSocios.setText(biblioteca.listaDeSocios());

        JScrollPane desplazamiento = new JScrollPane(areaSocios);
        desplazamiento.setPreferredSize(new Dimension(0, 180));

        JPanel panelLista = new JPanel(new BorderLayout(10, 10));
        panelLista.setBackground(Color.WHITE);
        panelLista.add(tituloLista, BorderLayout.NORTH);
        panelLista.add(desplazamiento, BorderLayout.CENTER);

        JPanel panelCentral = new JPanel(new BorderLayout(20, 20));
        panelCentral.setBackground(Color.WHITE);
        panelCentral.add(panelDatos, BorderLayout.NORTH);
        panelCentral.add(panelLista, BorderLayout.CENTER);

        panelPrincipal.add(panelEncabezado, BorderLayout.NORTH);
        panelPrincipal.add(panelCentral, BorderLayout.CENTER);

        this.add(panelPrincipal, BorderLayout.CENTER);

        botonRegistrar.addActionListener(
            evento -> registrarEstudiante()
        );

        botonVolver.addActionListener(
            evento -> ventanaPrincipal.mostrarSocios()
        );
    }

    private JPanel crearCampo(String p_titulo, JTextField p_campo)
    {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);

        JLabel etiqueta = new JLabel(p_titulo);
        etiqueta.setFont(new Font("Arial", Font.BOLD, 14));
        etiqueta.setForeground(new Color(45, 60, 80));

        p_campo.setFont(new Font("Arial", Font.PLAIN, 15));
        p_campo.setPreferredSize(new Dimension(200, 38));
        p_campo.setBorder(
            BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(210, 218, 230)),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            )
        );

        panel.add(etiqueta, BorderLayout.NORTH);
        panel.add(p_campo, BorderLayout.CENTER);

        return panel;
    }

    private void registrarEstudiante()
    {
        try
        {
            if (campoDni.getText().isEmpty()
                || campoNombre.getText().isEmpty()
                || campoCarrera.getText().isEmpty())
            {
                JOptionPane.showMessageDialog(
                    this,
                    "Completa todos los datos del estudiante."
                );
            }
            else
            {
                int dni = Integer.parseInt(campoDni.getText());

                String nombre = campoNombre.getText();
                String carrera = campoCarrera.getText();

                biblioteca.nuevoSocioEstudiante(
                    dni, nombre,carrera
                );

                Socio nuevoSocio = biblioteca.getSocios().get(
                    biblioteca.getSocios().size() - 1
                );

                bibliotecaDAO.getSocioDAO().agregar(nuevoSocio);

                areaSocios.setText(biblioteca.listaDeSocios());

                campoDni.setText("");
                campoNombre.setText("");
                campoCarrera.setText("");

                JOptionPane.showMessageDialog(
                    this,
                    "Estudiante registrado correctamente."
                );
            }
        }
        catch (NumberFormatException error)
        {
            JOptionPane.showMessageDialog(
                this,
                "El DNI debe ser un numero valido."
            );
        }
        catch (SQLException error)
        {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo guardar el estudiante en la base de datos."
            );
        }
    }
}
