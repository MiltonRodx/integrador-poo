package biblioteca.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import biblioteca.Biblioteca;
import biblioteca.Libro;
import biblioteca.SesionBiblioteca;

/**
 * Pestaña Libros (Swing): alta de ejemplares (los titulos pueden repetir:
 * son multiples ejemplares) y baja (bloqueada si el libro esta prestado).
 * Cada operacion de escritura persiste con sesion.guardar().
 *
 * Equivalente del Libros.fxml + LibrosController JavaFX.
 */
public class PanelLibros extends JPanel {

    private final SesionBiblioteca sesion;

    private final LibrosModelo modelo = new LibrosModelo();
    private final JTable tabla = new JTable(this.modelo);
    private final TableRowSorter<LibrosModelo> ordenador = new TableRowSorter<>(this.modelo);
    private final JTextField campoFiltro = new JTextField(28);

    public PanelLibros(SesionBiblioteca p_sesion) {
        super(new BorderLayout());
        this.sesion = p_sesion;

        JPanel interior = new JPanel(new java.awt.GridBagLayout());
        interior.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);

        // ---- fila de acciones ----
        JPanel filaAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaAcciones.setOpaque(false);
        JButton btnAltaLibro = new JButton("Alta Libro");
        JButton btnBaja = new JButton("Dar de baja");
        btnBaja.setToolTipText("Elimina el ejemplar seleccionado "
            + "(la FK prestamo→libro impide borrar con historial).");
        filaAcciones.add(btnAltaLibro);
        filaAcciones.add(btnBaja);
        interior.add(filaAcciones, c);

        // ---- fila de filtro en vivo ----
        JPanel filaFiltro = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaFiltro.setOpaque(false);
        this.campoFiltro.setToolTipText(
            "Buscar libro: título, editorial, año o edición");
        filaFiltro.add(this.campoFiltro);
        c.gridy = 1;
        interior.add(filaFiltro, c);

        // ---- tabla ----
        this.tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tabla.setRowSorter(this.ordenador);
        this.tabla.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(this.tabla,
            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setPreferredSize(new Dimension(0, 360));
        c.gridy = 2; c.weighty = 1;
        c.fill = GridBagConstraints.BOTH; c.insets = new Insets(0, 0, 0, 0);
        interior.add(scroll, c);
        interior.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.add(interior, BorderLayout.CENTER);

        // ---- listeners ----
        btnAltaLibro.addActionListener(e -> {
            String[] valores = DialogosSwing.formulario(this, "Alta de libro",
                "Completá los datos del ejemplar",
                "Titulo", "Nro. de edicion", "Editorial", "Anio de edicion").orElse(null);
            if (valores != null) this.conError(this.altaLibro(valores));
        });
        btnBaja.addActionListener(e -> this.bajaDeLaSeleccion());
        // Busqueda en vivo: mientras se escribe, la tabla se filtra sola.
        this.campoFiltro.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtroDesdeTexto(); }
            @Override public void removeUpdate(DocumentEvent e) { filtroDesdeTexto(); }
            @Override public void changedUpdate(DocumentEvent e) { filtroDesdeTexto(); }
        });

        this.sesion.suscribir(this::recargar);
        this.recargar();
    }

    /** Muestra un error solo si lo hay (closure de los botones de alta). */
    private void conError(String p_error) {
        if (p_error != null) DialogosSwing.error(this, p_error);
    }

    // ------------------------------------------------------------------
    // Modelo de tabla tipado
    // ------------------------------------------------------------------
    private class LibrosModelo extends AbstractTableModel {

        private final String[] columnas =
            { "Título", "Edición", "Editorial", "Año", "Prestado" };
        private List<Libro> filas = new ArrayList<>();

        void setFilas(List<Libro> p_filas) {
            this.filas = p_filas;
            fireTableDataChanged();
        }
        Libro libroEn(int p_fila) { return this.filas.get(p_fila); }
        int indexOfLibro(Libro p_libro) { return this.filas.indexOf(p_libro); }

        @Override public int getRowCount() { return this.filas.size(); }
        @Override public int getColumnCount() { return this.columnas.length; }
        @Override public String getColumnName(int p_col) { return this.columnas[p_col]; }
        @Override public Class<?> getColumnClass(int p_col) {
            return p_col == 1 || p_col == 3 ? Integer.class : String.class;
        }
        @Override public Object getValueAt(int p_fila, int p_col) {
            Libro libro = this.filas.get(p_fila);
            switch (p_col) {
                case 0: return libro.getTitulo();
                case 1: return libro.getEdicion();
                case 2: return libro.getEditorial();
                case 3: return libro.getAnio();
                case 4: return libro.prestado() ? "Si" : "No";
            }
            return "";
        }
    }

    /** Recarga la tabla desde el estado actual de la biblioteca. */
    public void recargar() {
        this.modelo.setFilas(new ArrayList<>(this.sesion.getBiblioteca().getLibros()));
    }

    // ------------------------------------------------------------------
    // Busqueda en vivo (predicado titulo / editorial / anio / edicion)
    // ------------------------------------------------------------------
    private void filtroDesdeTexto() {
        String texto = this.campoFiltro.getText().trim().toLowerCase();
        if (texto.isEmpty()) {
            this.ordenador.setRowFilter(null);
            return;
        }
        this.ordenador.setRowFilter(new RowFilter<LibrosModelo, Integer>() {
            @Override public boolean include(
                    Entry<? extends LibrosModelo, ? extends Integer> e) {
                Libro libro = PanelLibros.this.modelo.libroEn(e.getIdentifier());
                return libro.getTitulo().toLowerCase().contains(texto)
                    || libro.getEditorial().toLowerCase().contains(texto)
                    || String.valueOf(libro.getAnio()).contains(texto)
                    || String.valueOf(libro.getEdicion()).contains(texto);
            }
        });
    }

    // ------------------------------------------------------------------
    // Operaciones (logica callada: devuelve el error o null)
    // ------------------------------------------------------------------

    /**
     * Alta de libro: dialogo de 4 campos + validacion de enteros.
     * @return null si se pudo, el mensaje de rechazo si no
     */
    String altaLibro(String[] p_valores) {
        int edicion, anio;
        try {
            edicion = Integer.parseInt(p_valores[1]);
            anio = Integer.parseInt(p_valores[3]);
        } catch (NumberFormatException sinNumero) {
            return "Edicion y anio deben ser numeros enteros.";
        }
        String titulo = p_valores[0].trim();
        String editorial = p_valores[2].trim();
        if (titulo.isEmpty()) return "El titulo no puede quedar vacio.";
        if (editorial.isEmpty()) return "La editorial no puede quedar vacia.";
        if (edicion <= 0) return "La edicion debe ser 1 o mayor.";
        if (anio <= 0) return "El anio debe ser mayor a cero.";
        Biblioteca b = this.sesion.getBiblioteca();
        b.nuevoLibro(titulo, edicion, editorial, anio);
        this.sesion.guardar();
        this.sesion.publicar();
        return null;
    }

    /**
     * Baja del libro dado; FK prestamo->libro impide borrar con historial.
     * @return null si se pudo, el mensaje de rechazo si no
     */
    String baja(Libro p_elegido) {
        if (p_elegido == null) return "Seleccione un libro en la tabla.";
        if (!p_elegido.getPrestamos().isEmpty()) {
            return "\"" + p_elegido.getTitulo() + "\" (edicion "
                + p_elegido.getEdicion() + ") no puede darse de baja: tiene "
                + p_elegido.getPrestamos().size() + " prestamo(s) asociados (FK).";
        }
        try {
            this.sesion.getDao().getLibroDAO().delete(p_elegido);   // DELETE real en la BD
            this.sesion.getBiblioteca().quitarLibro(p_elegido);     // coherencia memoria <-> BD
        } catch (biblioteca.persistencia.DAOException e) {
            return "La base impide la baja.\n" + e.getMessage();
        }
        this.sesion.guardar();
        this.sesion.publicar();
        return null;
    }

    // ------------------------------------------------------------------
    // Listeners de botones (dialogos + confirmaciones)
    // ------------------------------------------------------------------
    private void bajaDeLaSeleccion() {
        int filaVista = this.tabla.getSelectedRow();
        if (filaVista == -1) { DialogosSwing.aviso(this, "Seleccione un libro en la tabla."); return; }
        Libro elegido = this.modelo.libroEn(this.tabla.convertRowIndexToModel(filaVista));
        if (!DialogosSwing.confirm(this, "¿Desea dar de baja el libro "
                + elegido.getTitulo() + "?")) {
            return;
        }
        this.conError(this.baja(elegido));
    }

    // ------------------------------------------------------------------
    // Ganchos para la prueba (sin ventanas modales)
    // ------------------------------------------------------------------
    /** Libro seleccionado en la tabla (null si no hay). */
    Libro libroSeleccionado() {
        int filaVista = this.tabla.getSelectedRow();
        if (filaVista == -1) return null;
        return this.modelo.libroEn(this.tabla.convertRowIndexToModel(filaVista));
    }

    /** Texto del filtro (lo usa la prueba). */
    void filtro(String p_texto) { this.campoFiltro.setText(p_texto); }

    /** Cantidad de filas visibles (con filtro). Lo usa la prueba. */
    int filasVisibles() { return this.tabla.getRowCount(); }
}
