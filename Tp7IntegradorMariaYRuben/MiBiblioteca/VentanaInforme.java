
package MiBiblioteca;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.PrinterException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class VentanaInforme extends JPanel
{
    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;
    private VentanaPrincipal ventanaPrincipal;

    private JTextPane areaInforme;

    private Color azulOscuro = new Color(25, 45, 85);
    private Color azulMedio = new Color(55, 105, 165);
    private Color azulSuave = new Color(190, 210, 235);
    private Color fondo = new Color(242, 246, 251);

    public VentanaInforme(
        Biblioteca p_biblioteca,
        BibliotecaDAO p_bibliotecaDAO,
        VentanaPrincipal p_ventanaPrincipal)
    {
        this.biblioteca = p_biblioteca;
        this.bibliotecaDAO = p_bibliotecaDAO;
        this.ventanaPrincipal = p_ventanaPrincipal;

        this.setLayout(new BorderLayout());
        this.setBackground(fondo);

        this.crearInterfaz();
        this.actualizarInforme();
    }

    private void crearInterfaz()
    {
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setBackground(fondo);
        panelSuperior.setBorder(
            new EmptyBorder(15, 20, 15, 20)
        );

        JButton botonVolver = this.crearBoton("Volver a estadisticas");
        JButton botonImprimir = this.crearBoton("Imprimir informe");

       botonVolver.addActionListener(evento -> {
            VentanaEstadisticas estadisticas = new VentanaEstadisticas(
            biblioteca,
            bibliotecaDAO,
            ventanaPrincipal
        );

      ventanaPrincipal.mostrarContenido(estadisticas);
     });
        botonImprimir.addActionListener(
            evento -> this.imprimirInforme()
        );

        panelSuperior.add(botonVolver, BorderLayout.WEST);

        JLabel titulo = new JLabel("Informe completo de la biblioteca");
        titulo.setFont(new Font("Arial", Font.BOLD, 23));
        titulo.setForeground(azulOscuro);
        titulo.setHorizontalAlignment(SwingConstants.CENTER);

        panelSuperior.add(titulo, BorderLayout.CENTER);
        panelSuperior.add(botonImprimir, BorderLayout.EAST);

        areaInforme = new JTextPane();
        areaInforme.setContentType("text/html");
        areaInforme.setEditable(false);
        areaInforme.setBackground(Color.WHITE);
        areaInforme.setMargin(new Insets(25, 30, 25, 30));

        JScrollPane desplazamiento = new JScrollPane(areaInforme);
        desplazamiento.setBorder(
            BorderFactory.createLineBorder(azulSuave)
        );
        desplazamiento.getVerticalScrollBar().setUnitIncrement(16);

        this.add(panelSuperior, BorderLayout.NORTH);
        this.add(desplazamiento, BorderLayout.CENTER);
    }

    private JButton crearBoton(String texto)
    {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Arial", Font.BOLD, 12));
        boton.setForeground(Color.WHITE);
        boton.setBackground(azulOscuro);
        boton.setFocusPainted(false);
        boton.setBorder(new EmptyBorder(11, 16, 11, 16));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        return boton;
    }

    public void actualizarInforme()
    {
        try
        {
            StringBuilder informe = new StringBuilder();

            SimpleDateFormat formato =
                new SimpleDateFormat("dd/MM/yyyy");

            int docentes =
                biblioteca.cantidadDeSociosPorTipo("Docente");

            int estudiantes =
                biblioteca.cantidadDeSociosPorTipo("Estudiante");

            int librosPrestados = 0;
            int librosDisponibles = 0;

            for(Libro unLibro : biblioteca.getLibros())
            {
                if(unLibro.prestado())
                {
                    librosPrestados++;
                }
                else
                {
                    librosDisponibles++;
                }
            }

            int activos = 0;
            int vencidos = 0;
            int devueltos = 0;

            informe.append("<html><body style='font-family:Arial; color:#193B69;'>");

            informe.append(
                "<div style='text-align:center; padding:15px;'>"
            );

            informe.append("<h1>BIBLIOTECA GESTION</h1>");
            informe.append("<h2>FACENA - UNNE</h2>");
            informe.append("<h3>INFORME GENERAL COMPLETO</h3>");

            informe.append("<p>Fecha de emision: ")
                .append(formato.format(Calendar.getInstance().getTime()))
                .append("</p><hr></div>");

            // Resumen general
            informe.append("<h2>1. RESUMEN GENERAL</h2>");

            informe.append("<table width='100%' border='1' cellspacing='0' cellpadding='8'>");

            informe.append("<tr><td><b>Total de socios</b></td><td>")
                .append(biblioteca.getSocios().size())
                .append("</td></tr>");

            informe.append("<tr><td><b>Docentes</b></td><td>")
                .append(docentes)
                .append("</td></tr>");

            informe.append("<tr><td><b>Estudiantes</b></td><td>")
                .append(estudiantes)
                .append("</td></tr>");

            informe.append("<tr><td><b>Total de libros</b></td><td>")
                .append(biblioteca.getLibros().size())
                .append("</td></tr>");

            informe.append("<tr><td><b>Libros prestados</b></td><td>")
                .append(librosPrestados)
                .append("</td></tr>");

            informe.append("<tr><td><b>Libros disponibles</b></td><td>")
                .append(librosDisponibles)
                .append("</td></tr>");

            informe.append("</table>");

            // Socios
            informe.append("<h2>2. LISTA DE SOCIOS</h2>");

            informe.append("<table width='100%' border='1' cellspacing='0' cellpadding='6'>");

            informe.append(
                "<tr bgcolor='#DCE8F5'><th>N°</th><th>DNI</th>" +
                "<th>Nombre</th><th>Tipo</th><th>Libros prestados</th></tr>"
            );

            int numero = 1;

            for(Socio unSocio : biblioteca.getSocios())
            {
                informe.append("<tr><td>").append(numero).append("</td>");
                informe.append("<td>").append(unSocio.getDniSocio()).append("</td>");
                informe.append("<td>").append(escaparHTML(unSocio.getNombre())).append("</td>");
                informe.append("<td>").append(unSocio.soyDeLaClase()).append("</td>");
                informe.append("<td>").append(unSocio.cantLibrosPrestados()).append("</td></tr>");

                numero++;
            }

            informe.append("</table>");

            informe.append("<p><b>Cantidad de estudiantes:</b> ")
                .append(estudiantes).append("</p>");

            informe.append("<p><b>Cantidad de docentes:</b> ")
                .append(docentes).append("</p>");

            // Libros
            informe.append("<h2>3. LISTA DE LIBROS</h2>");

            informe.append("<table width='100%' border='1' cellspacing='0' cellpadding='6'>");

            informe.append(
                "<tr bgcolor='#DCE8F5'><th>N°</th><th>Titulo</th>" +
                "<th>Edicion</th><th>Editorial</th><th>Año</th>" +
                "<th>Prestado</th></tr>"
            );

            numero = 1;

            for(Libro unLibro : biblioteca.getLibros())
            {
                String estado = "No";

                if(unLibro.prestado())
                {
                    estado = "Si";
                }

                informe.append("<tr><td>").append(numero).append("</td>");
                informe.append("<td>").append(escaparHTML(unLibro.getTitulo())).append("</td>");
                informe.append("<td>").append(unLibro.getEdicion()).append("</td>");
                informe.append("<td>").append(escaparHTML(unLibro.getEditorial())).append("</td>");
                informe.append("<td>").append(unLibro.getAnio()).append("</td>");
                informe.append("<td>").append(estado).append("</td></tr>");

                numero++;
            }

            informe.append("</table>");

            // Titulos
            informe.append("<h2>4. LISTA DE TITULOS</h2>");

            java.util.ArrayList<String> titulos =
                new java.util.ArrayList<String>();

            java.util.ArrayList<Integer> cantidades =
                new java.util.ArrayList<Integer>();

            for(Libro unLibro : biblioteca.getLibros())
            {
                int posicion = titulos.indexOf(unLibro.getTitulo());

                if(posicion == -1)
                {
                    titulos.add(unLibro.getTitulo());
                    cantidades.add(1);
                }
                else
                {
                    cantidades.set(
                        posicion,
                        cantidades.get(posicion) + 1
                    );
                }
            }

            if(titulos.isEmpty())
            {
                informe.append("<p>No hay titulos registrados.</p>");
            }

            for(int i = 0; i < titulos.size(); i++)
            {
                informe.append("<p>")
                    .append(i + 1).append(". ")
                    .append(escaparHTML(titulos.get(i)))
                    .append(" - Ejemplares: ")
                    .append(cantidades.get(i))
                    .append("</p>");
            }

            // Docentes responsables
            informe.append("<h2>5. LISTA DE DOCENTES RESPONSABLES</h2>");

            java.util.ArrayList<Docente> responsables =
                biblioteca.docentesResponsables();

            if(responsables.isEmpty())
            {
                informe.append("<p>No hay docentes responsables registrados.</p>");
            }
            else
            {
                for(Docente unDocente : responsables)
                {
                    informe.append("<p>")
                        .append(escaparHTML(unDocente.toString()))
                        .append("</p>");
                }
            }

            // Prestamos vencidos
            informe.append("<h2>6. PRESTAMOS VENCIDOS</h2>");

            numero = 1;

            for(Socio unSocio : biblioteca.getSocios())
            {
                for(Prestamo unPrestamo : unSocio.getPrestamos())
                {
                    if(!unPrestamo.estaDevuelto())
                    {
                        int dias = bibliotecaDAO.getPrestamoDAO()
                            .obtenerDiasPrestamo(unPrestamo);

                        Calendar fechaVencimiento =
                            (Calendar) unPrestamo.getFechaRetiro().clone();

                        fechaVencimiento.add(Calendar.DAY_OF_MONTH, dias);

                        if(Calendar.getInstance().after(fechaVencimiento))
                        {
                            informe.append("<div style='border:1px solid #AFC5E0; padding:10px; margin:8px;'>");

                            informe.append("<b>Prestamo ")
                                .append(numero).append("</b><br>");

                            informe.append("Retiro: ")
                                .append(formato.format(
                                    unPrestamo.getFechaRetiro().getTime()
                                )).append("<br>");

                            informe.append("Vencimiento: ")
                                .append(formato.format(
                                    fechaVencimiento.getTime()
                                )).append("<br>");

                            informe.append("Libro: ")
                                .append(escaparHTML(
                                    unPrestamo.getLibro().getTitulo()
                                )).append("<br>");

                            informe.append("Socio: ")
                                .append(escaparHTML(
                                    unPrestamo.getSocio().getNombre()
                                ));

                            informe.append("</div>");
                            numero++;
                        }
                    }
                }
            }

            if(numero == 1)
            {
                informe.append("<p>No hay prestamos vencidos sin devolver.</p>");
            }

            // Historial completo
            informe.append("<h2>7. HISTORIAL COMPLETO DE PRESTAMOS</h2>");

            numero = 1;

            for(Socio unSocio : biblioteca.getSocios())
            {
                for(Prestamo unPrestamo : unSocio.getPrestamos())
                {
                    int dias = bibliotecaDAO.getPrestamoDAO()
                        .obtenerDiasPrestamo(unPrestamo);

                    Calendar fechaVencimiento =
                        (Calendar) unPrestamo.getFechaRetiro().clone();

                    fechaVencimiento.add(Calendar.DAY_OF_MONTH, dias);

                    String estado;

                    if(unPrestamo.estaDevuelto())
                    {
                        if(unPrestamo.getFechaDevolucion()
                            .after(fechaVencimiento))
                        {
                            estado = "Devuelto fuera de termino";
                        }
                        else
                        {
                            estado = "Devuelto a tiempo";
                        }

                        devueltos++;
                    }
                    else if(Calendar.getInstance().after(fechaVencimiento))
                    {
                        estado = "Vencido sin devolver";
                        vencidos++;
                    }
                    else
                    {
                        estado = "Activo";
                        activos++;
                    }

                    String fechaDevolucion = "Sin devolver";

                    if(unPrestamo.estaDevuelto())
                    {
                        fechaDevolucion = formato.format(
                            unPrestamo.getFechaDevolucion().getTime()
                        );
                    }

                    informe.append("<div style='border:1px solid #AFC5E0; padding:10px; margin:8px;'>");

                    informe.append("<b>Prestamo ")
                        .append(numero).append("</b><br>");

                    informe.append("Retiro: ")
                        .append(formato.format(
                            unPrestamo.getFechaRetiro().getTime()
                        )).append("<br>");

                    informe.append("Vencimiento: ")
                        .append(formato.format(
                            fechaVencimiento.getTime()
                        )).append("<br>");

                    informe.append("Devolucion: ")
                        .append(fechaDevolucion).append("<br>");

                    informe.append("Libro: ")
                        .append(escaparHTML(
                            unPrestamo.getLibro().getTitulo()
                        )).append("<br>");

                    informe.append("Socio: ")
                        .append(escaparHTML(
                            unPrestamo.getSocio().getNombre()
                        )).append("<br>");

                    informe.append("Estado: <b>")
                        .append(estado)
                        .append("</b></div>");

                    numero++;
                }
            }

            if(numero == 1)
            {
                informe.append("<p>No hay prestamos registrados.</p>");
            }

            // Resumen de prestamos
            informe.append("<h2>8. RESUMEN DE PRESTAMOS</h2>");

            informe.append("<p>Prestamos activos: ")
                .append(activos).append("</p>");

            informe.append("<p>Prestamos vencidos: ")
                .append(vencidos).append("</p>");

            informe.append("<p>Prestamos devueltos: ")
                .append(devueltos).append("</p>");

            informe.append("<hr>");

            informe.append(
                "<p style='text-align:center;'>Biblioteca Gestion FACENA-UNNE</p>"
            );

            informe.append("</body></html>");

            areaInforme.setText(informe.toString());
            areaInforme.setCaretPosition(0);
        }
        catch(SQLException error)
        {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo generar el informe: " + error.getMessage()
            );
        }
    }

    private String escaparHTML(String texto)
    {
        if(texto == null)
        {
            return "";
        }

        return texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }

    private void imprimirInforme()
    {
        try
        {
            boolean imprimir = areaInforme.print();

            if(imprimir)
            {
                JOptionPane.showMessageDialog(
                    this,
                    "El informe se envio a imprimir correctamente."
                );
            }
        }
        catch(PrinterException error)
        {
            JOptionPane.showMessageDialog(
                this,
                "No se pudo imprimir el informe: " +
                error.getMessage()
            );
        }
    }
}
