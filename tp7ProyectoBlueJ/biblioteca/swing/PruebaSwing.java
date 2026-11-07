package biblioteca.swing;

import biblioteca.SesionBiblioteca;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import biblioteca.Biblioteca;
import biblioteca.Libro;
import biblioteca.Prestamo;
import biblioteca.Socio;

/**
 * Pruebas de verificación POR FASE de la GUI Swing.
 *
 * Uso:  java -cp classes biblioteca.swing.PruebaSwing faseN
 *
 * Cada fase copia la base a un banco de pruebas (la original NUNCA se toca),
 * arma la(s) pantalla(s) del hito, verifica comportamientos y deja un
 * screenshot de了一点 control visual (/tmp/opencode/swing-faseN.png — en
 * Linux). Termina con codigo 0 = todo OK, 1 = fallo.
 */
public class PruebaSwing {

    /** Directorio de artefactos de prueba (base copiada + screenshots). */
    static final Path TMP = Paths.get("/tmp/opencode");

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            System.err.println("Uso: PruebaSwing fase0..fase7");
            System.exit(1);
        }
        Files.createDirectories(TMP);
        Tema.instalar();
        String fase = args[0];

        boolean ok = false;
        switch (fase) {
            case "fase0":  ok = fase0();  break;
            case "fase1":  ok = fase1();  break;
            case "fase2":  ok = fase2();  break;
            case "fase3":  ok = fase3();  break;
            case "fase4":  ok = fase4();  break;
            case "fase5":  ok = fase5();  break;
            case "fase6":  ok = fase6();  break;
            default: System.err.println("fase no declarada: " + fase); System.exit(1);
        }
        System.out.println((ok ? "OK  " : "FALLA  ") + fase);
        System.exit(ok ? 0 : 1);
    }

    // ---------------------------------------------------------------------
    // FASE 0 — esqueleto: tema, sesion en worker, splash y ventana sin tabs
    // ---------------------------------------------------------------------
    static boolean fase0() throws Exception {
        // 1) Tema instalado (Nimbus del JDK)
        String laf = javax.swing.UIManager.getLookAndFeel().getName();
        if (!laf.toLowerCase().contains("nimbus")) {
            System.out.println("  esperaba Nimbus, hay: " + laf);
            return false;
        }
        System.out.println("  Nimbus activo");

        // 2) Sesion de prueba sobre una COPIA de la base (nada toca biblioteca.db)
        Path banco = bancoDePruebas("fase0");
        AtomicBoolean abierta = new AtomicBoolean(false);
        Thread cargador = new Thread(() -> {
            SesionBiblioteca s = new SesionBiblioteca(banco.toString(),
                AppSwing.CLAVE_INSTITUCION);
            sesionRef.set(s);
            abierta.set(true);
        });
        sesionRef.set(null);
        cargador.start();
        cargador.join(5000);
        final SesionBiblioteca sesion = sesionRef.get();
        if (!abierta.get() || sesion == null) { System.out.println("  sesion no cargó"); return false; }

        Biblioteca b = sesion.getBiblioteca();
        if (b.getSocios().size() < 5) { System.out.println("  socios=" + b.getSocios().size() + " (esperaba >= 5)"); return false; }
        if (b.getLibros().size() < 6) { System.out.println("  libros=" + b.getLibros().size() + " (esperaba >= 6)"); return false; }
        System.out.println("  sesion cargada: " + b.getSocios().size() + " socios / "
            + b.getLibros().size() + " libros");

        // 3) Ventana con 5 pestañas y toggle de tema presentes
        AtomicReference<VentanaSwing> ventana = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try { ventana.set(new VentanaSwing(sesion)); ventana.get().mostrar(); }
            catch (Throwable t) { error.set(t); }
        });
        if (error.get() != null) { System.out.println("  ventana no armó: " + error.get()); return false; }

        // 4) Splash: se muestra, se salta con click-salizado (alListo corre)
        AtomicBoolean listo = new AtomicBoolean(false);
        SplashSwing splash = new SplashSwing(() -> listo.set(true));
        JFrame frameVentana = ventana.get();
        SwingUtilities.invokeLater(() -> {
            // muestro splash DESPUES de la ventana: al terminar no tapa nada
            splash.mostrar();
        });
        // durante la animación: screenshot a ~800ms (tapa abriéndose)
        Thread.sleep(800);
        capturar("swing-fase0-splash", null);   // splash flotando (siempre encima)
        capturar("swing-fase0-ventana", ventana.get());
        Thread.sleep(2200);   // splash completo (~2.5s)
        if (!listo.get()) { System.out.println("  splash no entregó el control"); return false; }
        System.out.println("  splash completo y alListo ejecutado");

        // 5) Fin: cerramos sesion y ventanas cleans
        SwingUtilities.invokeAndWait(() -> {
            Frame[] frames = Frame.getFrames();
            for (Frame f : frames) { f.dispose(); }
        });
        sesion.cerrar();
        System.out.println("  sesion cerrada, frames disposed");
        return true;
    }

    /** Referencia de sesion para el hilo cargador (evita capturar en lambda). */
    static final AtomicReference<SesionBiblioteca> sesionRef = new AtomicReference<>();

    // ---------------------------------------------------------------------
    // FASE 1 — Informes y Socios: contadores, formatos de la consigna,
    // alta/baja/premiar con validaciones y busqueda en vivo
    // ---------------------------------------------------------------------
    static boolean fase1() throws Exception {
        Path banco = bancoDePruebas("fase1");
        sesionRef.set(null);
        Thread cargador = new Thread(() -> sesionRef.set(
            new SesionBiblioteca(banco.toString(), AppSwing.CLAVE_INSTITUCION)));
        cargador.start();
        cargador.join(5000);
        final SesionBiblioteca sesion = sesionRef.get();
        if (sesion == null) { System.out.println("  sesion no cargó"); return false; }

        boolean ok = true;
        biblioteca.Biblioteca b = sesion.getBiblioteca();

        // --- contadores y informes ---
        String textoInforme = enEdt(() -> {
            informesF1.set(new PanelInformes(sesion));
            informesF1.get().informeSocios();
            return informesF1.get().textoDelInforme();
        });
        if (!textoInforme.trim().equals(sesion.getBiblioteca().listaDeSocios().trim())) {
            System.out.println("  informe formato1 no coincide con la fuente"); ok = false;
        }
        String[] contadores = enEdt(() -> informesF1.get().contadoresTexto());
        int esperadosEst = b.cantidadDeSociosPorTipo("Estudiante");
        if (!String.valueOf(esperadosEst).equals(contadores[0])) {
            System.out.println("  contador estudiantes mal: " + contadores[0]); ok = false;
        }
        // informe de vencidos: encabezado con la cantidad del modelo
        String vencidosTexto = enEdt(() -> { informesF1.get().informeVencidos();
            return informesF1.get().textoDelInforme(); });
        String esperadosVencidos = "Prestamos vencidos (sin devolver) a la fecha: "
            + b.prestamosVencidos().size();
        if (!vencidosTexto.startsWith(esperadosVencidos)) {
            System.out.println("  informe vencidos no arranca con la cantidad: "
                + vencidosTexto.split("\n")[0]); ok = false;
        }

        // --- Socios: alta valida / duplicada / dni no numerico ---
        String errorAlta = enEdt(() -> altaF1(sesion)
            .altaDocente(new String[]{ "44441111", "Alta De Prueba", "Fisica" }));
        if (errorAlta != null) { System.out.println("  alta docente: " + errorAlta); ok = false; }
        if (b.buscarSocio(44441111) == null) {
            System.out.println("  alta docente no quedo en memoria"); ok = false;
        }
        String dup = enEdt(() -> altaF1(sesion).altaDocente(
            new String[]{ "44441111", "Dup", "Fisica" }));
        if (dup == null || !dup.contains("Ya existe un socio")) {
            System.out.println("  alta duplicada no rechazó: " + dup); ok = false;
        }
        String noNum = enEdt(() -> altaF1(sesion).altaEstudiante(
            new String[]{ "abc", "X", "Q" }));
        if (noNum == null || !noNum.contains("numero entero")) {
            System.out.println("  dni no numerico no rechazó: " + noNum); ok = false;
        }

        // --- baja con historial: FK -> rechazo ---
        Socio paenza = b.buscarSocio(28987498);   // seeded: tiene prestamo vencido
        String bajaConHistorial = enEdt(() -> altaF1(sesion).baja(paenza));
        if (bajaConHistorial == null || !bajaConHistorial.contains("prestamo(s)")) {
            System.out.println("  baja con historial no rechazó: " + bajaConHistorial); ok = false;
        }

        // --- premiar: solo docentes ---
        String noDocente = enEdt(() -> altaF1(sesion).premiar(paenza, 5));
        if (noDocente == null || !noDocente.contains("solo a docentes")) {
            System.out.println("  premiar estudiante no rechazó: " + noDocente); ok = false;
        }

        // --- busqueda en vivo: filtra por nombre ---
        enEdtVoid(() -> altaF1(sesion).filtro("paenza"));
        Thread.sleep(80);   // el DocumentListener procesa en el EDT
        int visibles = enEdt(() -> altaF1(sesion).filasVisibles());
        if (visibles != 1) {
            System.out.println("  filtro paenza: visibles=" + visibles + " (1)"); ok = false;
        }

        // --- destapar y seleccionar por dni ---
        Socio seleccionado = enEdt(() -> altaF1(sesion).seleccionarPorDni(28987498));
        if (seleccionado != paenza) { System.out.println("  seleccionarPorDni falló"); ok = false; }

        System.out.println(ok ? "  fase1: todos los checks OK" : "  fase1: HAY CHECKS FALLIDOS");

        // screenshot visual de los dos paneles (si hay display)
        try {
            enEdtVoid(() -> ventanaDePrueba("swing-fase1",
                informesF1.get(), altaF1(sesion)));
            Thread.sleep(600);
            capturar("swing-fase1", ventanaRef.get());
        } catch (Exception ig) { System.out.println("  screenshot omitido: " + ig); }

        SwingUtilities.invokeAndWait(() -> { if (ventanaRef.get() != null) ventanaRef.get().dispose(); });
        sesion.cerrar();
        return ok;
    }

    /** Referencias de la fase 1 (paneles + ventana de captura). */
    static final AtomicReference<PanelInformes> informesF1 = new AtomicReference<>();
    static final AtomicReference<PanelSocios> sociosF1 = new AtomicReference<>();
    static final AtomicReference<javax.swing.JFrame> ventanaRef = new AtomicReference<>();

    /** (re)crea el panel de socios para los checks (cache de la fase). */
    private static PanelSocios altaF1(SesionBiblioteca p_sesion) {
        if (sociosF1.get() == null) {
            sociosF1.set(new PanelSocios(p_sesion));
        }
        return sociosF1.get();
    }

    /** Muestra una ventana chica con paneles (para el screenshot del hito). */
    private static void ventanaDePrueba(String p_titulo, java.awt.Component... p_paneles) {
        if (ventanaRef.get() != null) { ventanaRef.get().dispose(); }
        javax.swing.JFrame v = new javax.swing.JFrame(p_titulo);
        javax.swing.JTabbedPane pestañas = new javax.swing.JTabbedPane();
        for (java.awt.Component panel : p_paneles) {
            pestañas.addTab(panel.getClass().getSimpleName().replace("Panel", ""),
                panel);
        }
        v.setContentPane(pestañas);
        v.setSize(1024, 640);
        v.setLocationRelativeTo(null);
        Tema.cambiarTema(Tema.oscuro());   // re-estiliza (y pinta el nuevo frame)
        v.setVisible(true);
        ventanaRef.set(v);
    }

    // ---------------------------------------------------------------------
    // Helpers generales de pruebas (EDT + capturas)
    // ---------------------------------------------------------------------


    /** Ejecuta un Supplier en el EDT y devuelve su valor (rethrow directo). */
    static <T> T enEdt(java.util.function.Supplier<T> p_accion) throws Exception {
        AtomicReference<Object> valor = new AtomicReference<>();
        AtomicReference<Throwable> error = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            try { valor.set(p_accion.get()); }
            catch (Throwable t) { error.set(t); }
        });
        if (error.get() != null) throw new RuntimeException(error.get());
        @SuppressWarnings("unchecked")
        T resultado = (T) valor.get();
        return resultado;
    }

    /** Ejecuta un Runnable en el EDT. */
    static void enEdtVoid(Runnable p_accion) throws Exception {
        SwingUtilities.invokeAndWait(p_accion);
    }

    // ---------------------------------------------------------------------
    // FASE 2 — Libros: alta/baja con validaciones, FK prestada, busqueda
    // ---------------------------------------------------------------------
    static boolean fase2() throws Exception {
        Path banco = bancoDePruebas("fase2");
        sesionRef.set(null);
        Thread cargador = new Thread(() -> sesionRef.set(
            new SesionBiblioteca(banco.toString(), AppSwing.CLAVE_INSTITUCION)));
        cargador.start();
        cargador.join(5000);
        final SesionBiblioteca sesion = sesionRef.get();
        if (sesion == null) { System.out.println("  sesion no cargó"); return false; }

        boolean ok = true;
        biblioteca.Biblioteca b = sesion.getBiblioteca();
        int librosOriginales = b.getLibros().size();
        AtomicReference<PanelLibros> ref = new AtomicReference<>();

        // --- alta valida: 4 campos, enteros ---
        String altaOk = enEdt(() -> {
            ref.set(new PanelLibros(sesion));
            return ref.get().altaLibro(
                new String[]{ "Libro de Test Fase2", "2", "EdiTest", "1999" });
        });
        if (altaOk != null) { System.out.println("  alta libro: " + altaOk); ok = false; }
        if (b.getLibros().size() != librosOriginales + 1) {
            System.out.println("  alta no agrego ejemplar: "
                + b.getLibros().size() + " vs " + (librosOriginales + 1)); ok = false;
        }
        if (!existeEn(b, "Libro de Test Fase2")) {
            System.out.println("  alta libro no quedó en el catalogo"); ok = false;
        }

        // --- alta con letras en anio: rechazo ---
        String noNum = enEdt(() -> ref.get().altaLibro(
            new String[]{ "Otro", "1", "Edi", "mm" }));
        if (noNum == null || !noNum.contains("enteros")) {
            System.out.println("  anio no numerico no rechazó: " + noNum); ok = false;
        }

        // --- alta con titulo vacio ---
        String vacio = enEdt(() -> ref.get().altaLibro(new String[]{ " ", "1", "Edi", "2000" }));
        if (vacio == null || !vacio.contains("titulo")) {
            System.out.println("  titulo vacio no rechazó: " + vacio); ok = false;
        }

        // --- baja de un libro PRESTADO: FK -> rechazo ---
        Libro prestado = prestadoDe(b);
        if (prestado != null) {
            String bajaConFK = enEdt(() -> ref.get().baja(prestado));
            if (bajaConFK == null || !bajaConFK.contains("prestamo(s)")) {
                System.out.println("  baja libro prestado no rechazó: " + bajaConFK); ok = false;
            }
            if (!b.getLibros().contains(prestado)) {
                System.out.println("  baja prestada borró el ejemplar: ERROR"); ok = false;
            }
        } else {
            System.out.println("  (aviso) no hay libro prestado en la base: check omitido");
        }

        // --- busqueda en vivo: "how programar" (-minusculas) filtra ---
        enEdtVoid(() -> ref.get().filtro("como programar"));
        int visibles = enEdt(() -> ref.get().filasVisibles());
        int conTexto = 0;
        for (Libro unLibro : b.getLibros()) {
            String titulo = unLibro.getTitulo().toLowerCase();
            if (titulo.contains("como programar")
                    || unLibro.getEditorial().toLowerCase().contains("como programar")) {
                conTexto++;
            }
        }
        if (visibles != conTexto) {
            System.out.println("  filtro 'como programar': visibles=" + visibles
                + " esperaba " + conTexto); ok = false;
        }

        // --- baja de un libro libre (el de alta): borra de verdad ---
        Libro testLibro = libroDe(b, "Libro de Test Fase2");
        String bajaLibre = enEdt(() -> ref.get().baja(testLibro));
        if (bajaLibre != null) { System.out.println("  baja libro libre: " + bajaLibre); ok = false; }
        if (b.getLibros().contains(testLibro)) {
            System.out.println("  baja no quito el ejemplar"); ok = false;
        }

        System.out.println(ok ? "  fase2: todos los checks OK" : "  fase2: HAY CHECKS FALLIDOS");

        try {
            enEdtVoid(() -> ventanaDePrueba("swing-fase2", ref.get()));
            Thread.sleep(600);
            capturar("swing-fase2", ventanaRef.get());
        } catch (Exception ig) { System.out.println("  screenshot omitido: " + ig); }
        SwingUtilities.invokeAndWait(() -> { if (ventanaRef.get() != null) ventanaRef.get().dispose(); });
        sesion.cerrar();
        return ok;
    }

    /** El primer libro con prestamo activo en la base (o null). */
    private static Libro prestadoDe(biblioteca.Biblioteca p_b) {
        for (Libro unLibro : p_b.getLibros()) {
            if (unLibro.prestado()) return unLibro;
        }
        return null;
    }

    /** El primer ejemplar con ese titulo (o null). */
    private static Libro libroDe(biblioteca.Biblioteca p_b, String p_titulo) {
        for (Libro unLibro : p_b.getLibros()) {
            if (unLibro.getTitulo().equals(p_titulo)) return unLibro;
        }
        return null;
    }

    /** ¿Hay ejemplar con ese titulo? */
    private static boolean existeEn(biblioteca.Biblioteca p_b, String p_titulo) {
        return libroDe(p_b, p_titulo) != null;
    }

    // ---------------------------------------------------------------------
    // FASE 3 — Préstamos: combos, retiro con reglas de negocio, radios de
    // filtro, busqueda en vivo, pildora de estado y devolución
    // ---------------------------------------------------------------------
    static boolean fase3() throws Exception {
        Path banco = bancoDePruebas("fase3");
        sesionRef.set(null);
        Thread cargador = new Thread(() -> sesionRef.set(
            new SesionBiblioteca(banco.toString(), AppSwing.CLAVE_INSTITUCION)));
        cargador.start();
        cargador.join(5000);
        final SesionBiblioteca sesion = sesionRef.get();
        if (sesion == null) { System.out.println("  sesion no cargó"); return false; }

        boolean ok = true;
        biblioteca.Biblioteca b = sesion.getBiblioteca();
        AtomicReference<PanelPrestamos> ref = new AtomicReference<>();

        // --- el panel arranca en modo Activos ---
        enEdt(() -> { ref.set(new PanelPrestamos(sesion,
            new ConsultasBiblioteca(sesion))); return null; });
        PanelPrestamos prestamos = ref.get();

        // --- retiro valido: socio sin vencidos + libro disponible ---
        Socio perez = b.buscarSocio(14524782);   // seed: responsable, con 1 prestamo hoy
        Libro libre = prestadoFree(b);
        if (libre != null) {
            String retiro = enEdt(() -> prestamos.registrarPrestamo(
                perez, libre, LocalDate.now()));
            if (retiro != null) { System.out.println("  retiro valido: " + retiro); ok = false; }
            if (!libre.prestado()) { System.out.println("  retiro no marco prestado"); ok = false; }
        }

        // --- libro ya prestado: rechazo con el motivo EXACTO del servicio ---
        Libro ocupado = prestadoDe(b);
        String yaPrestado = enEdt(() -> prestamos.registrarPrestamo(
            perez, ocupado, LocalDate.now()));
        if (yaPrestado == null || !yaPrestado.contains("ya está prestado")) {
            System.out.println("  libro prestado no rechazó: " + yaPrestado); ok = false;
        }

        // --- tope de 3 del estudiante (milstein tiene 3 del seed) ---
        Socio milstein = b.buscarSocio(31987123);
        Libro otroLibre = libre == null ? null : otroLibreQue(b, libre);
        if (otroLibre != null) {
            String tope = enEdt(() -> prestamos.registrarPrestamo(
                milstein, otroLibre, LocalDate.now()));
            if (tope == null || !tope.contains("tope para estudiantes")) {
                System.out.println("  tope de 3 no rechazó: " + tope); ok = false;
            }
        }

        // --- socio con vencidos: rechazo (eligiendo socio que TENGA un
        //     prestamo vencido activo en el estado real de la base) ---
        Socio conVencido = socioConPrestamoVencido(b);
        if (conVencido != null && otroLibre != null) {
            String vencidos = enEdt(() -> prestamos.registrarPrestamo(
                conVencido, otroLibre, LocalDate.now()));
            if (vencidos == null || !vencidos.contains("vencidos")) {
                System.out.println("  socio con vencidos no rechazó: " + vencidos); ok = false;
            }
        } else {
            System.out.println("  (aviso) no hay socio con vencido activo: check omitido");
        }

        // --- datos incompletos: rechazo con aviso ---
        String vacio = enEdt(() -> prestamos.registrarPrestamo(null, null, null));
        if (vacio == null || !vacio.contains("Elija")) {
            System.out.println("  sin datos no avisó: " + vacio); ok = false;
        }

        // --- radios de filtro: cantidades consistentes con el estado ---
        enEdtVoid(() -> prestamos.modoActivos());
        int activosVisibles = enEdt(() -> prestamos.filasVisibles());
        int activosModelo = 0;
        for (Libro unLibro : b.getLibros()) {
            if (unLibro.prestado()) activosModelo++;
        }
        if (activosVisibles != activosModelo) {
            System.out.println("  modo Activos: visibles=" + activosVisibles
                + " modelo=" + activosModelo); ok = false;
        }
        enEdtVoid(() -> prestamos.modoTodos());
        int todosVisibles = enEdt(() -> prestamos.filasVisibles());
        // El panel (como el JavaFX) deduplica por IDENTIDAD del prestamo: dos
        // registros distintos pueden compartir titulo+fecha (dos ediciones del
        // mismo libro prestadas el mismo dia) y son DOS filas legítimas.
        java.util.Set<Object> distinctos =
            java.util.Collections.newSetFromMap(new java.util.IdentityHashMap<>());
        for (Libro unLibro : b.getLibros()) {
            for (Prestamo unPrestamo : unLibro.getPrestamos()) {
                distinctos.add(unPrestamo);
            }
        }
        if (todosVisibles != distinctos.size()) {
            System.out.println("  modo Todos: visibles=" + todosVisibles
                + " distintos=" + distinctos.size()); ok = false;
        }

        // --- busqueda en vivo sobre la tabla ---
        enEdtVoid(() -> { prestamos.modoTodos(); prestamos.filtro("milstein"); });
        int filtradas = enEdt(() -> prestamos.filasVisibles());
        String nombreMilstein = milstein.getNombre().toLowerCase();
        int conNombre = 0;
        for (Libro unLibro : b.getLibros()) {
            for (biblioteca.Prestamo unPrestamo : unLibro.getPrestamos()) {
                if (unPrestamo.getSocio().getNombre().toLowerCase()
                        .contains(nombreMilstein)) conNombre++;
            }
        }
        if (filtradas != conNombre) {
            System.out.println("  filtro milstein: visibles=" + filtradas
                + " esperaba " + conNombre); ok = false;
        }

        // --- pildora: el estado de cada fila existe y su color se mapea ---
        enEdtVoid(() -> prestamos.modoTodos());
        boolean pildorasOK = enEdt(() -> {
            for (int i = 0; i < prestamos.modelRows(); i++) {
                EstadoPrestamo est = prestamos.estadoEnFila(i);
                if (est == null) return false;
                if (Tema.coloresDePildora(est, Tema.oscuro()) == null) return false;
            }
            return true;
        });
        if (!pildorasOK) { System.out.println("  pildora sin estado/color"); ok = false; }

        // --- devolucion: es fila activa OK; ya devuelta avisa fecha ---
        Prestamo primero = prestamoActivoDe(b);
        String devol = enEdt(() -> prestamos.devolver(primero));
        if (devol != null) { System.out.println("  devolucion: " + devol); ok = false; }
        if (primero.getFechaDevolucion() == null) {
            System.out.println("  devolucion no registro fecha"); ok = false;
        }
        String devolDos = enEdt(() -> prestamos.devolver(primero));
        if (devolDos == null || !devolDos.contains("ya fue devuelto")) {
            System.out.println("  doble devolución no avisó: " + devolDos); ok = false;
        }

        System.out.println(ok ? "  fase3: todos los checks OK" : "  fase3: HAY CHECKS FALLIDOS");

        try {
            enEdtVoid(() -> ventanaDePrueba("swing-fase3", prestamos));
            Thread.sleep(600);
            capturar("swing-fase3", ventanaRef.get());
        } catch (Exception ig) { System.out.println("  screenshot omitido: " + ig); }
        SwingUtilities.invokeAndWait(() -> { if (ventanaRef.get() != null) ventanaRef.get().dispose(); });
        sesion.cerrar();
        return ok;
    }

    /** El primer ejemplar LIBRE (o null). */
    private static Libro prestadoFree(biblioteca.Biblioteca p_b) {
        for (Libro unLibro : p_b.getLibros()) {
            if (!unLibro.prestado()) return unLibro;
        }
        return null;
    }

    /** Otro ejemplar libre distinto del dado (o null). */
    private static Libro otroLibreQue(biblioteca.Biblioteca p_b, Libro p_distinto) {
        for (Libro unLibro : p_b.getLibros()) {
            if (!unLibro.prestado() && unLibro != p_distinto) return unLibro;
        }
        return null;
    }

    /** El primer prestamo aun sin devolver (o null). */
    private static biblioteca.Prestamo prestamoActivoDe(biblioteca.Biblioteca p_b) {
        for (Libro unLibro : p_b.getLibros()) {
            if (unLibro.prestado()) return unLibro.ultimoPrestamo();
        }
        return null;
    }

    /** Un socio con prestamo vencido activo (o null). */
    private static Socio socioConPrestamoVencido(biblioteca.Biblioteca p_b) {
        biblioteca.swing.ServicioPrestamos servicio =
            new biblioteca.swing.ServicioPrestamos(
                sesionRef.get(), new ConversorDeFechas(),
                new ReglasDePlazo(new ConversorDeFechas()));
        for (Socio unSocio : p_b.getSocios()) {
            for (Libro unLibro : p_b.getLibros()) {
                for (Prestamo unPrestamo : unLibro.getPrestamos()) {
                    if (unPrestamo.getFechaDevolucion() == null
                            && unPrestamo.getSocio() == unSocio
                            && servicio.estadoDe(unPrestamo) == EstadoPrestamo.VENCIDO) {
                        return unSocio;
                    }
                }
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // FASE 4 — Estadísticas: los 4 números, la leyenda del donut y el
    // refresco en vivo ante un cambio de estado
    // ---------------------------------------------------------------------
    static boolean fase4() throws Exception {
        Path banco = bancoDePruebas("fase4");
        sesionRef.set(null);
        Thread cargador = new Thread(() -> sesionRef.set(
            new SesionBiblioteca(banco.toString(), AppSwing.CLAVE_INSTITUCION)));
        cargador.start();
        cargador.join(5000);
        final SesionBiblioteca sesion = sesionRef.get();
        if (sesion == null) { System.out.println("  sesion no cargó"); return false; }

        boolean ok = true;
        ConsultasBiblioteca consultas = new ConsultasBiblioteca(sesion);
        ServicioEstadisticas servicio = new ServicioEstadisticas(consultas,
            new ReglasDePlazo(new ConversorDeFechas()));
        AtomicReference<PanelEstadisticasSwing> ref = new AtomicReference<>();

        enEdt(() -> { ref.set(new PanelEstadisticasSwing(servicio, consultas, sesion)); return null; });
        PanelEstadisticasSwing stats = ref.get();

        // --- los 4 numeros coinciden con el servicio ---
        EstadisticasBiblioteca est = servicio.calcular();
        String[] valores = enEdt(() -> stats.valoresDeTarjetas());
        if (!String.valueOf(est.totalLibros()).equals(valores[0])
                || !String.valueOf(est.totalSocios()).equals(valores[1])
                || !String.valueOf(est.prestamosActivos()).equals(valores[2])
                || !String.valueOf(est.prestamosVencidos()).equals(valores[3])) {
            System.out.println("  tarjetas != servicio: " + java.util.Arrays.toString(valores)
                + " vs libros=" + est.totalLibros() + " socios=" + est.totalSocios()
                + " activos=" + est.prestamosActivos() + " vencidos=" + est.prestamosVencidos());
            ok = false;
        }

        // --- leyenda del donut ---
        String[] leyenda = enEdt(() -> stats.textosDeLeyenda());
        int esperadosDisponibles = 0;
        for (Libro unLibro : sesion.getBiblioteca().getLibros()) {
            if (!unLibro.prestado()) esperadosDisponibles++;
        }
        if (!leyenda[0].equals("Disponibles: " + esperadosDisponibles)) {
            System.out.println("  leyenda disponibles mal: " + leyenda[0]); ok = false;
        }
        if (!leyenda[1].equals("En préstamo: "
                + (est.totalLibros() - est.librosDisponibles()))) {
            System.out.println("  leyenda en prestamo mal: " + leyenda[1]); ok = false;
        }

        // --- refresco en vivo: un retiro debe cambiar las tarjetas SOLO ---
        int activosAntes = est.prestamosActivos();
        String fechaHoy = enEdt(() -> stats.fechaDeReferencia());
        Socio perez = sesion.getBiblioteca().buscarSocio(14524782);
        Libro libre = prestadoFree(sesion.getBiblioteca());
        if (perez != null && libre != null) {
            String retiro = enEdt(() -> stats.prestaParaPrueba(perez, libre));
            if (retiro != null) { System.out.println("  retiro de prueba: " + retiro); ok = false; }
            EstadisticasBiblioteca ajustado = servicio.calcular();
            String[] despues = enEdt(() -> stats.valoresDeTarjetas());
            if (!String.valueOf(ajustado.prestamosActivos()).equals(despues[2])
                    || ajustado.prestamosActivos() != activosAntes + 1) {
                System.out.println("  tarjeta activos no se refresco: "
                    + despues[2] + " vs " + (ajustado.prestamosActivos())); ok = false;
            }
        } else System.out.println("  (aviso) sin socio/libro libre: refresco omitido");

        // el pie queda con datos coherentes (el retiro no cambia el total)
        int[] torta = enEdt(() -> stats.valoresDelPastel());
        if (torta[0] + torta[1] != est.totalLibros()) {
            System.out.println("  donut: disponibles(" + torta[0] + ")+enPrestamo("
                + torta[1] + ") != totalLibros(" + est.totalLibros() + ")"); ok = false;
        }

        System.out.println(ok ? "  fase4: todos los checks OK" : "  fase4: HAY CHECKS FALLIDOS");

        try {
            enEdtVoid(() -> ventanaDePrueba("swing-fase4", stats));
            Thread.sleep(600);
            capturar("swing-fase4", ventanaRef.get());
        } catch (Exception ig) { System.out.println("  screenshot omitido: " + ig); }
        SwingUtilities.invokeAndWait(() -> { if (ventanaRef.get() != null) ventanaRef.get().dispose(); });
        sesion.cerrar();
        return ok;
    }

    // ---------------------------------------------------------------------
    // FASE 5 — Splash: flip progresivo (capturas) + duración completa con
    // pase de control (~2.5 s) + click para saltar
    // ---------------------------------------------------------------------
    static boolean fase5() throws Exception {
        boolean ok5 = true;
        AtomicBoolean listo = new AtomicBoolean(false);
        long[] timestamp = { 0 };
        SplashSwing splash = new SplashSwing(() -> {
            listo.set(true);
            timestamp[0] = System.currentTimeMillis();
        });
        SwingUtilities.invokeAndWait(splash::mostrar);
        long inicio = System.currentTimeMillis();
        // capturas del flip progresivo
        Thread.sleep(400);  capturar("swing-fase5-t04", null);
        Thread.sleep(400);  capturar("swing-fase5-t08", null);
        Thread.sleep(600);  capturar("swing-fase5-t14", null);
        // hasta el fin natural (~2.2s de animación + 0.3 de fundido)
        long tope = inicio + 6000;
        while (!listo.get() && System.currentTimeMillis() < tope) Thread.sleep(50);
        if (!listo.get()) { System.out.println("  splash no terminó (sin callback)"); return false; }
        long duracion = timestamp[0] - inicio;
        if (duracion < 2000 || duracion > 4500) {
            System.out.println("  duración fuera de rango: " + duracion + "ms"); ok5 = false;
        } else {
            System.out.println("  duración completa: " + duracion + "ms");
        }

        // --- click para saltar: otro splash, matado a mitad con un click real ---
        AtomicBoolean salto = new AtomicBoolean(false);
        SplashSwing splashDos = new SplashSwing(() -> salto.set(true));
        SwingUtilities.invokeAndWait(splashDos::mostrar);
        Thread.sleep(700);
        try {
            java.awt.Point centro = (java.awt.Point) enEdt(() -> {
                java.awt.Window quien = splashVentana();
                if (quien == null || !quien.isShowing()) return null;
                java.awt.Point s = quien.getLocationOnScreen();
                s.x += 100;
                return s;
            });
            if (centro == null) {
                System.out.println("  (aviso) splashDos no visible: click check omitido");
            } else {
                java.awt.Robot robot = new java.awt.Robot();
                robot.mouseMove(centro.x, centro.y);
                robot.mousePress(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                robot.mouseRelease(java.awt.event.InputEvent.BUTTON1_DOWN_MASK);
                long topeDos = System.currentTimeMillis() + 3000;
                while (!salto.get() && System.currentTimeMillis() < topeDos) Thread.sleep(50);
                if (!salto.get()) { System.out.println("  click no saltó el splash"); ok5 = false; }
                else System.out.println("  click saltó el splash: OK");
            }
        } catch (Exception e) { System.out.println("  click check omitido: " + e); }
        SwingUtilities.invokeAndWait(() -> {
            for (java.awt.Window w : java.awt.Window.getWindows()) w.dispose();
        });
        return ok5 && salto.get();
    }

    /** El JDialog mostrado con el splash ya sea (lo usa la prueba). */
    private static java.awt.Window splashVentana() {
        for (java.awt.Window w : java.awt.Window.getWindows()) {
            if (w.isShowing() && w instanceof javax.swing.JDialog) return w;
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // FASE 6 — Toggle claro/oscuro: la ventana se re-estiliza completa
    // (fondo raiz, tablas, labels) y vuelve al modo claro
    // ---------------------------------------------------------------------
    static boolean fase6() throws Exception {
        Path banco = bancoDePruebas("fase6");
        sesionRef.set(null);
        Thread cargador = new Thread(() -> sesionRef.set(
            new SesionBiblioteca(banco.toString(), AppSwing.CLAVE_INSTITUCION)));
        cargador.start();
        cargador.join(5000);
        final SesionBiblioteca sesion = sesionRef.get();
        if (sesion == null) { System.out.println("  sesion no cargó"); return false; }

        boolean ok6 = true;
        AtomicReference<VentanaSwing> ventana = new AtomicReference<>();
        SwingUtilities.invokeAndWait(() -> {
            ventana.set(new VentanaSwing(sesion));
            ventana.get().mostrar();
        });
        java.awt.Color claro = Tema.colorFondo();
        capturar("swing-fase6-claro", ventana.get());

        // --- al modo oscuro (doClick INVIERTE el toggle: arranca apagado) ---
        enEdtVoid(() -> toggleDe(ventana.get()).doClick());
        if (!Tema.oscuro()) { System.out.println("  Tema.oscuro() no quedó true"); ok6 = false; }
        java.awt.Color oscuro = Tema.colorFondo();
        if (claro.equals(oscuro)) { System.out.println("  la paleta no cambió"); ok6 = false; }
        // el label del toggle cambió
        String texto = enEdt(() -> toggleDe(ventana.get()).getText());
        if (!texto.contains("Claro")) {
            System.out.println("  toggle no mostró '☀ Claro': " + texto); ok6 = false;
        }
        // el fondo de la raiz quedó oscuro (re-estilizado en runtime)
        java.awt.Color fondoRaiz = enEdt(() -> ventana.get().getContentPane().getBackground());
        if (!fondoRaiz.equals(new java.awt.Color(0x0d1117))) {
            System.out.println("  raiz no quedó oscura: " + fondoRaiz); ok6 = false;
        }
        Thread.sleep(400);
        capturar("swing-fase6-oscuro", ventana.get());

        // --- volver al claro (segundo doClick: vuelve a invertir) ---
        enEdtVoid(() -> toggleDe(ventana.get()).doClick());
        if (Tema.oscuro()) { System.out.println("  no volvió al claro"); ok6 = false; }
        // las tablas también quedaron re-estilizadas al modo vigente
        System.out.println(ok6 ? "  fase6: todos los checks OK"
            : "  fase6: HAY CHECKS FALLIDOS");
        SwingUtilities.invokeAndWait(() -> ventana.get().dispose());
        sesion.cerrar();
        return ok6;
    }

    /** Busca el toggle de tema en el arbol de la ventana. */
    private static javax.swing.JToggleButton toggleDe(java.awt.Container p_raiz) {
        for (java.awt.Component c : p_raiz.getComponents()) {
            if (c instanceof javax.swing.JToggleButton
                    && "toggleTema".equals(c.getName())) {
                return (javax.swing.JToggleButton) c;
            }
            if (c instanceof java.awt.Container) {
                javax.swing.JToggleButton hallado = toggleDe((java.awt.Container) c);
                if (hallado != null) return hallado;
            }
        }
        return null;
    }

    // ---------------------------------------------------------------------
    // Helpers de banco de pruebas
    // ---------------------------------------------------------------------

    /** Copia biblioteca.db a /tmp/opencode/<paso>.db y devuelve esa ruta. */
    static Path bancoDePruebas(String p_paso) throws IOException {
        Path origen = Paths.get("biblioteca.db");
        Path destino = TMP.resolve("prueba-" + p_paso + ".db");
        if (Files.exists(destino)) Files.delete(destino);
        if (Files.exists(origen)) {
            Files.copy(origen, destino);
        } else {
            Files.copy(Paths.get("tp7ProyectoBlueJ", "biblioteca.db"), destino);
        }
        return destino;
    }

    /** Screenshot recortado a una ventana (si hay); si no, de todo el screen. */
    static void capturar(String p_nombre, Component p_recorte) {
        try {
            Robot robot = new Robot();
            Rectangle zona;
            if (p_recorte != null && p_recorte.isShowing()) {
                zona = p_recorte.getBounds();
                zona.setLocation(p_recorte.getLocationOnScreen());
                zona.grow(4, 4);
            } else {
                zona = new Rectangle(java.awt.Toolkit.getDefaultToolkit().getScreenSize());
            }
            BufferedImage foto = robot.createScreenCapture(zona);
            ImageIO.write(foto, "png", TMP.resolve(p_nombre + ".png").toFile());
            System.out.println("  screenshot -> " + TMP.resolve(p_nombre + ".png"));
        } catch (AWTException | IOException e) {
            System.out.println("  screenshot no disponible: " + e);
        }
    }
}
