package biblioteca.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import biblioteca.Biblioteca;
import biblioteca.Docente;
import biblioteca.SesionBiblioteca;
import biblioteca.Socio;

/**
 * Pestaña Socios (Swing): tabla completa, altas de docente/estudiante,
 * busqueda por D.N.I., baja (solo sin libros en poder) y premio de dias
 * (solo docentes). Cada operacion de escritura persiste con sesion.guardar().
 *
 * Equivalente del Socios.fxml + SociosController JavaFX: AbstractTableModel
 * sobre List<Socio> y TableRowSorter para la busqueda en vivo (en JavaFX era
 * un FilteredList sobre la ObservableList).
 */
public class PanelSocios extends JPanel {

    private final SesionBiblioteca sesion;

    private final SociosModelo modelo = new SociosModelo();
    private final JTable tabla = new JTable(this.modelo);
    private final TableRowSorter<SociosModelo> ordenador = new TableRowSorter<>(this.modelo);
    private final JTextField campoBusqueda = new JTextField(28);

    public PanelSocios(SesionBiblioteca p_sesion) {
        super(new BorderLayout());
        this.sesion = p_sesion;

        // ---- columna vertical: acciones / busqueda / tabla ----
        JPanel interior = new JPanel(new java.awt.GridBagLayout());
        interior.setOpaque(false);
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.gridx = 0; c.weightx = 1; c.fill = java.awt.GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 0, 8, 0);

        // ---- fila de acciones ----
        JPanel filaAcciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaAcciones.setOpaque(false);
        JButton btnAltaDocente = new JButton("Alta Docente");
        JButton btnAltaEstudiante = new JButton("Alta Estudiante");
        JButton btnBaja = new JButton("Dar de baja");
        btnBaja.setToolTipText("Elimina el socio seleccionado "
            + "(la FK prestamo→socio impide borrar quien tiene historial).");
        JButton btnPremiar = new JButton("Premiar días");
        btnPremiar.setToolTipText("Concede días extra de préstamo "
            + "(premio de responsabilidad: solo docentes).");
        filaAcciones.add(btnAltaDocente);
        filaAcciones.add(btnAltaEstudiante);
        filaAcciones.add(btnBaja);
        filaAcciones.add(btnPremiar);
        interior.add(filaAcciones, c);

        // ---- fila de busqueda (en vivo + boton por dni) ----
        JPanel filaBusqueda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        filaBusqueda.setOpaque(false);
        this.campoBusqueda.setToolTipText(
            "Buscar socio: D.N.I., nombre o tipo (búsqueda en vivo)");
        filaBusqueda.add(this.campoBusqueda);
        JButton btnBuscar = new JButton("Buscar por D.N.I.");
        filaBusqueda.add(btnBuscar);
        c.gridy = 1;
        interior.add(filaBusqueda, c);

        // ---- tabla ----
        this.tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        this.tabla.setRowSorter(this.ordenador);
        this.tabla.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(this.tabla);
        scroll.setPreferredSize(new Dimension(0, 360));
        c.gridy = 2; c.weighty = 1;
        c.fill = java.awt.GridBagConstraints.BOTH; c.insets = new Insets(0, 0, 0, 0);
        interior.add(scroll, c);
        interior.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        this.add(interior, BorderLayout.CENTER);

        // ---- listeners ----
        btnAltaDocente.addActionListener(e -> {
            String[] valores = DialogosSwing.formulario(this, "Alta de docente",
                "Completá los datos del docente",
                "D.N.I.", "Nombre y apellido", "Area").orElse(null);
            if (valores != null) this.conError(this.altaDocente(valores));
        });
        btnAltaEstudiante.addActionListener(e -> {
            String[] valores = DialogosSwing.formulario(this, "Alta de estudiante",
                "Completá los datos del estudiante",
                "D.N.I.", "Nombre y apellido", "Carrera").orElse(null);
            if (valores != null) this.conError(this.altaEstudiante(valores));
        });
        btnBaja.addActionListener(e -> this.bajaDeLaSeleccion());
        btnPremiar.addActionListener(e -> this.premioDeLaSeleccion());
        btnBuscar.addActionListener(e -> buscarPorDni());
        // Enter en la caja de búsqueda: busca por D.N.I. (como en el FXML)
        this.campoBusqueda.addActionListener(e -> buscarPorDni());
        // Busqueda en vivo: mientras se escribe, la tabla se filtra sola
        // (equivalente del textProperty().addListener sobre el FilteredList).
        this.campoBusqueda.getDocument().addDocumentListener(new DocumentListener() {
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
    // Modelo de tabla tipado (las beans del TP no son Property: nada de
    // bindings, el modelo pregunta directo a Socio por cada celda)
    // ------------------------------------------------------------------
    private class SociosModelo extends AbstractTableModel {

        private final String[] columnas =
            { "D.N.I.", "Nombre", "Tipo", "Libros en poder", "Días de préstamo" };
        private List<Socio> filas = new ArrayList<>();

        void setFilas(List<Socio> p_filas) {
            this.filas = p_filas;
            fireTableDataChanged();
        }
        Socio socioEn(int p_fila) { return this.filas.get(p_fila); }
        int indexOfSocio(Socio p_socio) { return this.filas.indexOf(p_socio); }

        @Override public int getRowCount() { return this.filas.size(); }
        @Override public int getColumnCount() { return this.columnas.length; }
        @Override public String getColumnName(int p_col) { return this.columnas[p_col]; }
        @Override public Class<?> getColumnClass(int p_col) {
            return p_col == 0 || p_col == 3 || p_col == 4 ? Integer.class : String.class;
        }
        @Override public Object getValueAt(int p_fila, int p_col) {
            Socio socio = this.filas.get(p_fila);
            switch (p_col) {
                case 0: return socio.getDniSocio();
                case 1: return socio.getNombre();
                case 2: return socio.soyDeLaClase();
                case 3: return socio.cantLibrosPrestados();
                case 4: return socio.getDiasPrestamos();
            }
            return "";
        }
    }

    /** Recarga la tabla desde el estado actual de la biblioteca. */
    public void recargar() {
        this.modelo.setFilas(new ArrayList<>(this.sesion.getBiblioteca().getSocios()));
    }

    // ------------------------------------------------------------------
    // Busqueda en vivo (predicado D.N.I. / nombre / tipo, como FilteredList)
    // ------------------------------------------------------------------
    private void filtroDesdeTexto() {
        String texto = this.campoBusqueda.getText().trim().toLowerCase();
        if (texto.isEmpty()) {
            this.ordenador.setRowFilter(null);
            return;
        }
        this.ordenador.setRowFilter(new RowFilter<SociosModelo, Integer>() {
            @Override public boolean include(
                    Entry<? extends SociosModelo, ? extends Integer> e) {
                Socio socio = PanelSocios.this.modelo.socioEn(e.getIdentifier());
                return String.valueOf(socio.getDniSocio()).contains(texto)
                    || socio.getNombre().toLowerCase().contains(texto)
                    || socio.soyDeLaClase().toLowerCase().contains(texto);
            }
        });
    }

    /** Busqueda por D.N.I.: selecciona (y muestra) la fila del socio indicado. */
    private void buscarPorDni() {
        try {
            int dni = Integer.parseInt(this.campoBusqueda.getText().trim());
            Socio encontrado = this.sesion.getBiblioteca().buscarSocio(dni);
            if (encontrado == null) {
                DialogosSwing.aviso(this, "No hay socio con D.N.I. " + dni + ".");
                return;
            }
            String buscado = String.valueOf(dni);   // dejo solo el dni como filtro
            this.filtro(buscado);
            int filaModelo = this.modelo.indexOfSocio(encontrado);
            int filaVista = this.tabla.convertRowIndexToView(filaModelo);
            if (filaVista == -1) return;
            this.tabla.setRowSelectionInterval(filaVista, filaVista);
            this.tabla.scrollRectToVisible(this.tabla.getCellRect(filaVista, 0, true));
        } catch (NumberFormatException sinNumero) {
            DialogosSwing.error(this, "Ingrese el D.N.I. como numero, sin puntos.");
        }
    }

    // ------------------------------------------------------------------
    // Operaciones (la logica vive en metodos "callados" que devuelven el
    // error o null; los listeners de botones agregan los dialogos. Facilita
    // probar la logica sin ventanas modales)
    // ------------------------------------------------------------------

    /** Alta de docente: mismas validaciones del controller JavaFX. */
    String altaDocente(String[] p_valores) { return this.alta(true, p_valores); }

    /** Alta de estudiante: mismas validaciones del controller JavaFX. */
    String altaEstudiante(String[] p_valores) { return this.alta(false, p_valores); }

    /**
     * Alta de socio: valida (dni numerico, unico, nombre vacio) y persiste.
     * @return null si se pudo, el mensaje de rechazo si no
     */
    private String alta(boolean p_esDocente, String[] p_valores) {
        String campoTres = p_esDocente ? "Area" : "Carrera";
        int dni;
        try {
            dni = Integer.parseInt(p_valores[0]);
        } catch (NumberFormatException sinNumero) {
            return "El D.N.I. debe ser un numero entero.";
        }
        String nombre = p_valores[1].trim();
        String extra = p_valores[2].trim();
        if (dni <= 0) return "El D.N.I. debe ser un numero entero positivo.";
        if (nombre.isEmpty()) return "El nombre no puede quedar vacio.";
        if (extra.isEmpty()) return "El campo " + campoTres + " no puede quedar vacio.";
        if (this.sesion.getBiblioteca().buscarSocio(dni) != null) {
            return "Ya existe un socio con D.N.I. " + dni + ".";
        }
        Biblioteca b = this.sesion.getBiblioteca();
        if (p_esDocente) b.nuevoSocioDocente(dni, nombre, extra);
        else             b.nuevoSocioEstudiante(dni, nombre, extra);
        this.sesion.guardar();
        this.sesion.publicar();
        return null;
    }

    /**
     * Baja del socio dado. La FK prestamo->socio impide borrar con historial.
     * @return null si se pudo, el mensaje de rechazo si no
     */
    String baja(Socio p_elegido) {
        if (p_elegido == null) return "Seleccione un socio en la tabla.";
        if (!p_elegido.getPrestamos().isEmpty()) {
            return p_elegido.getNombre() + " no puede darse de baja: tiene "
                + p_elegido.getPrestamos().size() + " prestamo(s) asociados (FK).";
        }
        try {
            this.sesion.getDao().getSocioDAO().delete(p_elegido);   // DELETE real en la BD
            this.sesion.getBiblioteca().quitarSocio(p_elegido);     // coherencia memoria <-> BD
        } catch (biblioteca.persistencia.DAOException e) {
            return "La base impide la baja.\n" + e.getMessage();
        }
        this.sesion.guardar();
        this.sesion.publicar();
        return null;
    }

    /**
     * Premio de responsabilidad: adiciona dias de prestamo a un DOCENTE.
     * @return null si se pudo, el mensaje de rechazo si no
     */
    String premiar(Socio p_elegido, int p_dias) {
        if (!(p_elegido instanceof Docente)) {
            return "El premio de dias se aplica solo a docentes.";
        }
        if (p_dias <= 0) return "Ingrese una cantidad de dias mayor a cero.";
        ((Docente) p_elegido).cambiarDiasDePrestamos(p_dias);   // consigna: adiciona dias
        this.sesion.guardar();
        this.sesion.publicar();
        return null;
    }

    // ------------------------------------------------------------------
    // Listeners de botones (dialogos + atajos)
    // ------------------------------------------------------------------
    private void bajaDeLaSeleccion() {
        Socio elegido = this.socioSeleccionado();
        if (elegido == null) { DialogosSwing.aviso(this, "Seleccione un socio en la tabla."); return; }
        if (!DialogosSwing.confirm(this, "¿Desea dar de baja al socio "
                + elegido.getNombre() + "?")) {
            return;
        }
        this.conError(this.baja(elegido));
    }

    private void premioDeLaSeleccion() {
        Socio elegido = this.socioSeleccionado();
        if (elegido == null) {
            DialogosSwing.aviso(this, "Seleccione un socio en la tabla.");
            return;
        }
        if (!(elegido instanceof Docente)) {
            DialogosSwing.aviso(this, "El premio de dias se aplica solo a docentes.");
            return;
        }
        String[] valores = DialogosSwing.formulario(this, "Premio de responsabilidad",
            elegido.getNombre() + " tiene "
                + ((Docente) elegido).getDiasPrestamos()
                + " dias de prestamo. ¿Cuantos dias adiciona?",
            "Dias a adicionar").orElse(null);
        if (valores == null) return;
        int dias;
        try {
            dias = Integer.parseInt(valores[0]);
        } catch (NumberFormatException sinNumero) {
            DialogosSwing.error(this, "La cantidad de dias debe ser un numero entero.");
            return;
        }
        this.conError(this.premiar(elegido, dias));
    }

    // ------------------------------------------------------------------
    // Ganchos para la prueba (sin ventanas modales)
    // ------------------------------------------------------------------
    /** Socio seleccionado en la tabla (null si no hay). */
    Socio socioSeleccionado() {
        int filaVista = this.tabla.getSelectedRow();
        if (filaVista == -1) return null;
        return this.modelo.socioEn(this.tabla.convertRowIndexToModel(filaVista));
    }

    /** Selección por dni y devolver socio, sin dialogo (lo usa la prueba). */
    Socio seleccionarPorDni(int p_dni) {
        Socio socio = this.sesion.getBiblioteca().buscarSocio(p_dni);
        if (socio == null) return null;
        int filaModelo = this.modelo.indexOfSocio(socio);
        int filaVista = this.tabla.convertRowIndexToView(filaModelo);
        if (filaVista == -1) this.filtro("");   // estaba filtrada: la destapo
        filaVista = this.tabla.convertRowIndexToView(filaModelo);
        if (filaVista != -1) {
            this.tabla.setRowSelectionInterval(filaVista, filaVista);
            this.tabla.scrollRectToVisible(this.tabla.getCellRect(filaVista, 0, true));
        }
        return socio;
    }

    /** Texto del filtro (lo usa la prueba). */
    void filtro(String p_texto) {
        this.campoBusqueda.setText(p_texto);
    }

    /** Cantidad de filas visibles (con filtro aplicado). Lo usa la prueba. */
    int filasVisibles() { return this.tabla.getRowCount(); }

    /** Acceso al campo de filtro (lo usa la prueba para escribir en vivo). */
    JTextField campoFiltro() { return this.campoBusqueda; }
}
