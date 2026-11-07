package biblioteca.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import biblioteca.SesionBiblioteca;

/**
 * Pestaña Informes (Swing): los listados de la consigna con sus FORMATOS
 * EXACTOS (formato1/2/3) en un JTextArea monoespaciado, titulos sin repetir,
 * prestamos vencidos y contadores por tipo de socio.
 *
 * Equivalente del PanelInformes JavaFX + su controller en un solo panel.
 */
public class PanelInformes extends JPanel {

    private final SesionBiblioteca sesion;

    private final JLabel contEstudiantes = new JLabel();
    private final JLabel contDocentes = new JLabel();
    private final JLabel contVencidos = new JLabel();
    private final JTextArea areaInforme = new JTextArea();

    public PanelInformes(SesionBiblioteca p_sesion) {
        super(new BorderLayout(10, 10));
        this.sesion = p_sesion;

        // ---- fila de botones de informes ----
        final JPanel filaBotones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaBotones.setOpaque(false);
        JButton btnSocios = new JButton("Formato 1: Socios");
        JButton btnLibros = new JButton("Formato 2: Libros");
        JButton btnTitulos = new JButton("Titulos sin repetir");
        JButton btnResponsables = new JButton("Formato 3: Docentes Resp.");
        JButton btnVencidos = new JButton("Prestamos vencidos");
        btnSocios.addActionListener(e -> informeSocios());
        btnLibros.addActionListener(e -> informeLibros());
        btnTitulos.addActionListener(e -> informeTitulos());
        btnResponsables.addActionListener(e -> informeResponsables());
        btnVencidos.addActionListener(e -> informeVencidos());
        filaBotones.add(btnSocios);
        filaBotones.add(btnLibros);
        filaBotones.add(btnTitulos);
        filaBotones.add(btnResponsables);
        filaBotones.add(btnVencidos);

        // ---- contadores por tipo de socio ----
        final JPanel filaContadores = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filaContadores.setOpaque(false);
        filaContadores.add(new JLabel("Estudiantes: "));
        this.contEstudiantes.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        filaContadores.add(this.contEstudiantes);
        filaContadores.add(new JLabel("Docentes: "));
        this.contDocentes.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        filaContadores.add(this.contDocentes);
        filaContadores.add(new JLabel("Vencidos: "));
        this.contVencidos.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
        filaContadores.add(this.contVencidos);

        // ---- area central monoespaciada (los formatos son de consola) ----
        this.areaInforme.setEditable(false);
        this.areaInforme.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        this.areaInforme.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        final JScrollPane scroll = new JScrollPane(this.areaInforme);
        scroll.setPreferredSize(new Dimension(0, 300));

        this.add(filaBotones, BorderLayout.NORTH);
        // envoltura: los contadores arriba del scroll, sin estirarse
        JPanel conArea = new JPanel(new BorderLayout());
        conArea.setOpaque(false);
        conArea.add(filaContadores, BorderLayout.NORTH);
        conArea.add(scroll, BorderLayout.CENTER);
        this.add(conArea, BorderLayout.CENTER);
        this.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        this.sesion.suscribir(this::actualizar);
        this.actualizar();
    }

    /** Contadores vivos (se refrescan con cada cambio del estado). */
    public void actualizar() {
        biblioteca.Biblioteca b = this.sesion.getBiblioteca();
        this.contEstudiantes.setText(String.valueOf(b.cantidadDeSociosPorTipo("Estudiante")));
        this.contDocentes.setText(String.valueOf(b.cantidadDeSociosPorTipo("Docente")));
        this.contVencidos.setText(String.valueOf(b.prestamosVencidos().size()));
    }

    /** Informe formato1: socios con totales por tipo. */
    public void informeSocios()      { this.mostrar(this.sesion.getBiblioteca().listaDeSocios()); }

    /** Informe formato2: ejemplares con estado de prestamo. */
    public void informeLibros()      { this.mostrar(this.sesion.getBiblioteca().listaDeLibros()); }

    /** Titulos sin repetir (los ejemplares si pueden duplicarse). */
    public void informeTitulos()     { this.mostrar(this.sesion.getBiblioteca().listaDeTitulos()); }

    /** Informe formato3: docentes responsables de hoy. */
    public void informeResponsables(){ this.mostrar(this.sesion.getBiblioteca().listaDeDocentesResponsables()); }

    /** Informe de vencidos activos: encabezado con la cantidad + cada prestamo. */
    public void informeVencidos() {
        StringBuilder informe = new StringBuilder(
            "Prestamos vencidos (sin devolver) a la fecha: ")
            .append(this.sesion.getBiblioteca().prestamosVencidos().size()).append("\n\n");
        for (Object unPrestamo : this.sesion.getBiblioteca().prestamosVencidos()) {
            informe.append("--------------------------------------------------\n")
                .append(unPrestamo).append("\n");
        }
        this.mostrar(informe.toString());
    }

    /** Volca el informe elegido en el area de texto central. */
    private void mostrar(String p_texto) {
        this.areaInforme.setText(p_texto);
        this.areaInforme.setCaretPosition(0);
    }

    /** Texto del área de informe (lo usa la prueba). */
    String textoDelInforme() { return this.areaInforme.getText(); }

    /** Contadores en texto (lo usa la prueba): {estudiantes, docentes, vencidos}. */
    String[] contadoresTexto() {
        return new String[]{ this.contEstudiantes.getText(),
            this.contDocentes.getText(), this.contVencidos.getText() };
    }
}
