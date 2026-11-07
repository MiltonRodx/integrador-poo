package biblioteca.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SpinnerDateModel;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import biblioteca.Biblioteca;
import biblioteca.Libro;
import biblioteca.Prestamo;
import biblioteca.SesionBiblioteca;
import biblioteca.Socio;

/**
 * Pestaña Préstamos (Swing): registro de retiros (combo socio + combo de
 * libros disponibles + fecha de retiro), tabla con filtro Activos / Vencidos
 * / Todos, busqueda en vivo y PILDORA de estado por fila (En plazo / Por
 * vencer / Vencido / Devuelto) con boton "..." para devolver.
 *
 * Las reglas de negocio se preguntan al modelo y la escritura pasa por
 * ServicioPrestamos (aporte de la compañera: persistencia con rollback).
 * Si el prestamo se rechaza se muestra un dialogo con el MOTIVO, en el mismo
 * orden de revisiones que Biblioteca.prestarLibro().
 *
 * Equivalente del Prestamos.fxml + PrestamosController JavaFX. Respecto de
 * la version JavaFX: el DatePicker es un JSpinner con SpinnerDateModel (Swing
 * no trae DatePicker) y el MenuButton de acciones es un JButton con popup.
 */
public class PanelPrestamos extends JPanel {

    private final SesionBiblioteca sesion;
    private final ConversorDeFechas conversorDeFechas;
    private final ReglasDePlazo reglasDePlazo;
    private final ServicioPrestamos servicioPrestamos;

    private final PrestamosModelo modelo = new PrestamosModelo();
    private final JTable tabla = new JTable(this.modelo);
    private final TableRowSorter<PrestamosModelo> ordenador = new TableRowSorter<>(this.modelo);
    private final JComboBox<Socio> comboSocios = new JComboBox<>();
    private final JComboBox<Libro> comboLibros = new JComboBox<>();
    private final JSpinner fechaRetiro;
    private final JTextField campoFiltro = new JTextField(20);
    private final JRadioButton radioActivos = new JRadioButton("Activos", true);
    private final JRadioButton radioVencidos = new JRadioButton("Vencidos");
    private final JRadioButton radioTodos = new JRadioButton("Todos");

    public PanelPrestamos(SesionBiblioteca p_sesion, ConsultasBiblioteca p_consultas) {
        super(new BorderLayout());
        this.sesion = p_sesion;
        this.conversorDeFechas = new ConversorDeFechas();
        this.reglasDePlazo = new ReglasDePlazo(this.conversorDeFechas);
        this.servicioPrestamos = new ServicioPrestamos(p_sesion, this.conversorDeFechas,
            this.reglasDePlazo);

        JPanel interior = new JPanel(new java.awt.GridBagLayout());
        interior.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);

        // ---- grilla de registro de retiro ----
        JPanel grilla = new JPanel(new java.awt.GridBagLayout());
        grilla.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(4, 6, 4, 6);
        g.anchor = GridBagConstraints.LINE_START;

        this.fechaRetiro = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editorFecha =
            new JSpinner.DateEditor(this.fechaRetiro, "dd/MM/yyyy");
        this.fechaRetiro.setEditor(editorFecha);
        this.fechaRetiro.setValue(new Date());   // hoy (la fecha viaja a medianoche abajo)
        g.gridx = 0; g.gridy = 0; g.weightx = 0;
        grilla.add(new JLabel("Socio:"), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        grilla.add(this.comboSocios, g);
        g.gridx = 2; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        grilla.add(new JLabel("Fecha de retiro:"), g);
        g.gridx = 3; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        grilla.add(this.fechaRetiro, g);
        g.gridx = 0; g.gridy = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        grilla.add(new JLabel("Libro disponible:"), g);
        g.gridx = 1; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        grilla.add(this.comboLibros, g);
        g.gridx = 2; g.gridy = 1; g.weightx = 0; g.fill = GridBagConstraints.NONE;
        JPanel filaPrestar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaPrestar.setOpaque(false);
        JButton btnPrestar = new JButton("Prestar");
        btnPrestar.setToolTipText(
            "Registra el retiro: valida tope de 3 libros y préstamos vencidos.");
        JButton btnDevolver = new JButton("Devolver");
        btnDevolver.setToolTipText(
            "Registra la devolución de hoy del préstamo seleccionado en la tabla.");
        filaPrestar.add(btnPrestar);
        filaPrestar.add(btnDevolver);
        g.gridx = 3; g.weightx = 1; g.fill = GridBagConstraints.HORIZONTAL;
        grilla.add(filaPrestar, g);
        c.gridy = 0;
        interior.add(grilla, c);

        // ---- grilla de filtros: radios Activos/Vencidos/Todos + buscador ----
        JPanel filaFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filaFiltros.setOpaque(false);
        javax.swing.ButtonGroup grupo = new javax.swing.ButtonGroup();
        grupo.add(this.radioActivos);
        grupo.add(this.radioVencidos);
        grupo.add(this.radioTodos);
        filaFiltros.add(new JLabel("Filtro:"));
        filaFiltros.add(this.radioActivos);
        filaFiltros.add(this.radioVencidos);
        filaFiltros.add(this.radioTodos);
        filaFiltros.add(BoxStrut(12));
        filaFiltros.add(this.campoFiltro);
        c.gridy = 1;
        interior.add(filaFiltros, c);

        // ---- tabla ----
        this.tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tabla.setRowSorter(this.ordenador);
        this.tabla.setFillsViewportHeight(true);
        // la pildora y el boton no dependen del LookingAndFeel: renderers propios
        this.tabla.getColumnModel().getColumn(5).setCellRenderer(new PildoraRenderer());
        this.tabla.getColumnModel().getColumn(6).setCellRenderer(new BotonAccionesRenderer());
        this.tabla.getColumnModel().getColumn(6).setCellEditor(
            new BotonAccionesEditor(this::devolverEl));
        JScrollPane scroll = new JScrollPane(this.tabla);
        scroll.setPreferredSize(new Dimension(0, 300));
        c.gridy = 2; c.weighty = 1;
        c.fill = GridBagConstraints.BOTH; c.insets = new Insets(0, 0, 0, 0);
        interior.add(scroll, c);
        interior.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.add(interior, BorderLayout.CENTER);

        // ---- renderers de los combos (como los StringConverter JavaFX) ----
        this.comboSocios.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> p_lista,
                    Object p_valor, int p_indice, boolean p_sel, boolean p_foco) {
                JLabel etiqueta = (JLabel) super.getListCellRendererComponent(p_lista,
                    p_valor, p_indice, p_sel, p_foco);
                if (p_valor instanceof Socio) {
                    Socio socio = (Socio) p_valor;
                    etiqueta.setText(socio.getNombre() + " (" + socio.soyDeLaClase() + ")");
                }
                return etiqueta;
            }
        });
        this.comboLibros.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> p_lista,
                    Object p_valor, int p_indice, boolean p_sel, boolean p_foco) {
                JLabel etiqueta = (JLabel) super.getListCellRendererComponent(p_lista,
                    p_valor, p_indice, p_sel, p_foco);
                if (p_valor instanceof Libro) {
                    Libro libro = (Libro) p_valor;
                    etiqueta.setText(libro.getTitulo() + " (edicion " + libro.getEdicion() + ")");
                }
                return etiqueta;
            }
        });

        // ---- listeners ----
        btnPrestar.addActionListener(e -> {
            String error = this.registrarPretiro();
            if (error != null) DialogosSwing.error(this, error);
        });
        btnDevolver.addActionListener(e -> {
            int filaVista = this.tabla.getSelectedRow();
            Prestamo elegido = filaVista == -1 ? null
                : this.modelo.prestamoEn(this.tabla.convertRowIndexToModel(filaVista));
            if (elegido == null) { DialogosSwing.aviso(this, "Seleccione un prestamo en la tabla."); return; }
            this.conErrorDevolver(this.devolver(elegido));
        });
        this.radioActivos.addActionListener(e -> this.recargar());
        this.radioVencidos.addActionListener(e -> this.recargar());
        this.radioTodos.addActionListener(e -> this.recargar());
        this.campoFiltro.setToolTipText("Buscar socio o libro");
        this.campoFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtroDesdeTexto(); }
            @Override public void removeUpdate(DocumentEvent e) { filtroDesdeTexto(); }
            @Override public void changedUpdate(DocumentEvent e) { filtroDesdeTexto(); }
        });

        this.sesion.suscribir(this::recargar);
        this.recargar();
    }

    /** Espaciador chico (FlowLayout no tiene insertar gap). */
    private static Component BoxStrut(int p_px) {
        return javax.swing.Box.createHorizontalStrut(p_px);
    }

    /** Muestra error de devolucion si lo hay. */
    private void conErrorDevolver(String p_error) {
        if (p_error != null) DialogosSwing.error(this, p_error);
    }

    // ------------------------------------------------------------------
    // Modelo de tabla tipado: el estado y las acciones son columnas propias
    // ------------------------------------------------------------------
    private class PrestamosModelo extends AbstractTableModel {

        private final String[] columnas =
            { "Socio", "Libro", "Retiro", "Vence", "Devolución", "Estado", "⋯" };
        private List<Prestamo> filas = new ArrayList<>();

        void setFilas(List<Prestamo> p_filas) {
            this.filas = p_filas;
            fireTableDataChanged();
        }
        Prestamo prestamoEn(int p_fila) { return this.filas.get(p_fila); }

        @Override public int getRowCount() { return this.filas.size(); }
        @Override public int getColumnCount() { return this.columnas.length; }
        @Override public String getColumnName(int p_col) { return this.columnas[p_col]; }
        @Override public Class<?> getColumnClass(int p_col) {
            return p_col == 5 ? EstadoPrestamo.class
                : p_col == 6 ? Prestamo.class : String.class;
        }
        @Override public boolean isCellEditable(int p_fila, int p_col) { return p_col == 6; }
        @Override public Object getValueAt(int p_fila, int p_col) {
            Prestamo prestamo = this.filas.get(p_fila);
            switch (p_col) {
                case 0: return prestamo.getSocio().getNombre();
                case 1: return prestamo.getLibro().getTitulo();
                case 2: return PanelPrestamos.this.conversorDeFechas.aTextoCorto(
                    PanelPrestamos.this.conversorDeFechas.aLocalDate(
                        prestamo.getFechaRetiro()));
                case 3: return PanelPrestamos.this.conversorDeFechas.aTextoCorto(
                    PanelPrestamos.this.reglasDePlazo.calcularFechaDeVencimiento(prestamo));
                case 4: return prestamo.getFechaDevolucion() == null ? "-"
                    : PanelPrestamos.this.conversorDeFechas.aTextoCorto(
                        PanelPrestamos.this.conversorDeFechas.aLocalDate(
                            prestamo.getFechaDevolucion()));
                case 5: return PanelPrestamos.this.servicioPrestamos.estadoDe(prestamo);
            }
            return "";
        }
    }

    /** Recarga combos + tabla desde el estado actual, respetando el filtro elegido. */
    public void recargar() {
        this.comboSocios.removeAllItems();
        for (Socio unSocio : this.sesion.getBiblioteca().getSocios()) {
            this.comboSocios.addItem(unSocio);
        }
        this.comboLibros.removeAllItems();
        for (Libro unLibro : this.sesion.getBiblioteca().getLibros()) {
            if (!unLibro.prestado()) this.comboLibros.addItem(unLibro);
        }
        List<Prestamo> visibles = new ArrayList<>();
        for (Prestamo unPrestamo : todosLosPrestamos()) {
            boolean activo = unPrestamo.getFechaDevolucion() == null;
            boolean vencido = this.servicioPrestamos.estadoDe(unPrestamo)
                == EstadoPrestamo.VENCIDO;
            if (this.radioTodos.isSelected()
                    || (this.radioActivos.isSelected() && activo)
                    || (this.radioVencidos.isSelected() && vencido)) {
                visibles.add(unPrestamo);
            }
        }
        this.modelo.setFilas(visibles);
    }

    /** Predicado de busqueda en vivo: nombre del socio o titulo del libro. */
    private void filtroDesdeTexto() {
        String texto = this.campoFiltro.getText().trim().toLowerCase();
        if (texto.isEmpty()) {
            this.ordenador.setRowFilter(null);
            return;
        }
        this.ordenador.setRowFilter(new RowFilter<PrestamosModelo, Integer>() {
            @Override public boolean include(
                    Entry<? extends PrestamosModelo, ? extends Integer> e) {
                Prestamo prestamo = PanelPrestamos.this.modelo.prestamoEn(e.getIdentifier());
                return prestamo.getSocio().getNombre().toLowerCase().contains(texto)
                    || prestamo.getLibro().getTitulo().toLowerCase().contains(texto);
            }
        });
    }

    /** Fecha seleccionada del spinner, como LocalDate (a medianoche). */
    LocalDate fechaDeRetiroElegida() {
        return ((Date) this.fechaRetiro.getValue()).toInstant()
            .atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // ------------------------------------------------------------------
    // Operaciones (logica callada: devuelve el mensaje o null, igual que
    // ServicePrestamos de la compañera)
    // ------------------------------------------------------------------

    /**
     * Registra el retiro con lo seleccionado en los combos y el spinner.
     * @return null si se pudo, el mensaje de rechazo/problema si no
     */
    String registrarPretiro() {
        Socio socio = (Socio) (this.comboSocios.getSelectedItem() instanceof Socio
            ? this.comboSocios.getSelectedItem() : null);
        Libro libro = (Libro) (this.comboLibros.getSelectedItem() instanceof Libro
            ? this.comboLibros.getSelectedItem() : null);
        return this.registrarPrestamo(socio, libro, this.fechaDeRetiroElegida());
    }

    /**
     * Registra el retiro con datos explícitos (lo usa la prueba).
     * @return null si se pudo, el mensaje de rechazo/problema si no
     */
    String registrarPrestamo(Socio p_socio, Libro p_libro, LocalDate p_fecha) {
        if (p_socio == null || p_libro == null || p_fecha == null) {
            return "Elija socio, libro disponible y fecha de retiro.";
        }
        try {
            this.servicioPrestamos.registrarPrestamo(p_socio, p_libro, p_fecha);
            return null;
        } catch (PrestamoRechazadoException e) {
            return "No se puede registrar el prestamo.\n\n" + e.getMessage();
        } catch (ErrorDePersistenciaException e) {
            return "Problema con la base de datos:\n\n" + e.getMessage();
        }
    }

    /**
     * Devuelve UN prestamo dado (boton "Devolver" y menu "..." de cada fila).
     * @return null si se pudo, el mensaje de rechazo/problema si no
     */
    String devolver(Prestamo p_elegido) {
        if (p_elegido == null) return "Seleccione un prestamo en la tabla.";
        if (p_elegido.getFechaDevolucion() != null) {
            return "Ese prestamo ya fue devuelto el "
                + this.conversorDeFechas.aTextoCorto(this.conversorDeFechas.aLocalDate(
                    p_elegido.getFechaDevolucion())) + ".";
        }
        try {
            this.servicioPrestamos.registrarDevolucion(p_elegido);
            return null;
        } catch (ErrorDePersistenciaException e) {
            return "Problema con la base de datos:\n\n" + e.getMessage();
        }
    }

    /** Todos los prestamos del estado actual (por libro, sin repetir). */
    private List<Prestamo> todosLosPrestamos() {        List<Prestamo> todos = new ArrayList<>();
        for (Libro unLibro : this.sesion.getBiblioteca().getLibros()) {
            for (Prestamo unPrestamo : unLibro.getPrestamos()) {
                if (!todos.contains(unPrestamo)) todos.add(unPrestamo);
            }
        }
        return todos;
    }

    /** Devuelve UN prestamo dado con dialogos (lo usa el menu "..." de cada fila). */
    private void devolverEl(Prestamo p_elegido) {
        String error = this.devolver(p_elegido);
        if (error != null) DialogosSwing.error(this, error);
    }

    // ------------------------------------------------------------------
    // La PILDORA de estado (equivalente de CeldaEstadoPrestamo + CSS)
    // ------------------------------------------------------------------
    private static class PildoraRenderer extends DefaultTableCellRenderer {

        private EstadoPrestamo estado;

        PildoraRenderer() {
            super();
            this.setOpaque(false);
            this.setHorizontalAlignment(SwingConstants.CENTER);
            this.setFont(this.getFont().deriveFont(Font.BOLD, 11f));
            this.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        }

        @Override public Component getTableCellRendererComponent(JTable p_tabla,
                Object p_valor, boolean p_sel, boolean p_foco, int p_fila, int p_col) {
            this.estado = p_valor instanceof EstadoPrestamo ? (EstadoPrestamo) p_valor : null;
            this.setText(this.estado == null ? "" : this.estado.getEtiqueta());
            return this;
        }

        @Override protected void paintComponent(java.awt.Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(java.awt.RenderingHints.KEY_ANTIALIASING,
                java.awt.RenderingHints.VALUE_ANTIALIAS_ON);
            if (this.estado != null) {
                Color[] colores = Tema.coloresDePildora(this.estado, Tema.oscuro());
                g2.setPaint(colores[0]);
                int h = Math.min(getHeight() - 6, 22);
                int w = pillWidth(this);
                g2.fillRoundRect((getWidth() - w) / 2, (getHeight() - h) / 2, w, h, h, h);
                this.setForeground(colores[1]);
            }
            g2.dispose();
            super.paintComponent(g);
        }

        /** Ancho de la pildora: acorde al texto + padding. */
        private static int pillWidth(javax.swing.JLabel p_label) {
            return p_label.getFontMetrics(p_label.getFont())
                .stringWidth(p_label.getText()) + 24;
        }
    }

    // ------------------------------------------------------------------
    // El boton "..." de acciones (equivalente de CeldaAccionesPrestamo)
    // ------------------------------------------------------------------

    /** Renderer del boton "..." (solo pinta el boton en la fila). */
    private static class BotonAccionesRenderer extends DefaultTableCellRenderer {
        private final JButton boton = new JButton("⋯");
        BotonAccionesRenderer() {
            this.boton.setMargin(new Insets(0, 6, 0, 6));
        }
        @Override public Component getTableCellRendererComponent(JTable p_tabla,
                Object p_valor, boolean p_sel, boolean p_foco, int p_fila, int p_col) {
            return this.boton;
        }
    }

    /** Editor del boton "..." que abre el menu "Registrar devolución". */
    private static class BotonAccionesEditor extends DefaultCellEditor {
        private final JButton boton = new JButton("⋯");
        private final JPopupMenu menu = new JPopupMenu();

        BotonAccionesEditor(Consumer<Prestamo> p_alRegistrarDevolucion) {
            super(new JTextField());
            this.boton.setMargin(new Insets(0, 6, 0, 6));
            javax.swing.JMenuItem devolver = new javax.swing.JMenuItem("Registrar devolución");
            devolver.addActionListener(e -> {
                fireEditingStopped();
                p_alRegistrarDevolucion.accept((Prestamo) this.delegado);
            });
            this.menu.add(devolver);
            this.setClickCountToStart(1);
            this.editorComponent = this.boton;
            this.boton.addActionListener(e -> {
                if (this.delegado == null) return;
                this.menu.show(this.boton, 0, this.boton.getHeight());
            });
        }

        /** El prestamo de la fila (se setea al abrir el editor). */
        private Object delegado;

        @Override public Component getTableCellEditorComponent(JTable p_tabla,
                Object p_valor, boolean p_sel, int p_fila, int p_col) {
            this.delegado = p_valor;
            return this.boton;
        }

        @Override public boolean stopCellEditing() {
            this.delegado = null;
            return super.stopCellEditing();
        }
    }

    // ------------------------------------------------------------------
    // Ganchos para la prueba (sin ventanas modales)
    // ------------------------------------------------------------------
    /** Selecciona el modo de filtro (lo usa la prueba). */
    void modoActivos()  { esteRadio(this.radioActivos); }
    void modoVencidos() { esteRadio(this.radioVencidos); }
    void modoTodos()    { esteRadio(this.radioTodos); }
    private void esteRadio(JRadioButton p_radio) {
        p_radio.setSelected(true);
        this.recargar();
    }

    /** Estado visible de la fila i (de la fuente, no la vista). Lo usa la prueba. */
    EstadoPrestamo estadoEnFila(int p_fila) {
        return this.servicioPrestamos.estadoDe(this.modelo.prestamoEn(p_fila));
    }

    /** Cantidad de filas de la fuente (sin filtro). Lo usa la prueba. */
    int modelRows() { return this.modelo.filas.size(); }

    /** Cantidad de filas visibles (con filtro de texto aplicado). */
    int filasVisibles() { return this.tabla.getRowCount(); }

    /** Texto del filtro (lo usa la prueba). */
    void filtro(String p_texto) { this.campoFiltro.setText(p_texto); }
}
