package biblioteca;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Scanner;

import biblioteca.persistencia.DAOException;

/**
 * Clase main del sistema: dos modos de uso.
 *
 * 1) MENU INTERACTIVO (por defecto): consola con todas las operaciones de
 * gestion (libros, socios, prestamos, docentes) conectada a SQLite via la
 * capa biblioteca.persistencia: cada operacion queda persistida y al abrir
 * se recarga el estado guardado.
 *
 * 2) ESCENARIO DE LA CONSIGNA (TP7): salida fija que responde las 4 preguntas
 * con los 3 listados, como la pide el enunciado. Se activa con el argumento
 * "escenario":   java biblioteca.GestionBiblioteca escenario
 */
public class GestionBiblioteca {

    private static final String NOMBRE_INSTITUCION = "Biblioteca UNLP";

    private static Scanner consola = new Scanner(System.in);
    private static SesionBiblioteca sesion;

    public static void main(String[] args) throws LibroNoPrestadoException {
        if (args.length > 0 && args[0].equalsIgnoreCase("escenario")) {
            escenarioConsigna();
        } else {
            menuInteractivo();
        }
    }

    // =========================================================================
    // MENU INTERACTIVO
    // =========================================================================

    /** Abre la base y recarga el estado persistido (o arranca con estado vacio). */
    private static void abrirBase() {
        // La pregunta de siembra se reparte ANTES (si la base es nueva) para
        // que SesionBiblioteca reciba el modo decidido; los prints quedan
        // identicos a la version que abria/sembraba manualmente.
        boolean hayEstado = SesionBiblioteca.existeBibliotecaEn("biblioteca.db", NOMBRE_INSTITUCION);
        boolean sembrar = false;
        if (!hayEstado) {
            System.out.print("[BD] Base nueva. \u00bfSembrar los datos de la consigna? (S/N): ");
            String respuesta = leerLinea();
            sembrar = respuesta.equalsIgnoreCase("S") || respuesta.equalsIgnoreCase("Si");
            if (!sembrar) {
                System.out.println("[BD] Catalogo vacio: de alta los datos desde el menu.");
            }
        }

        sesion = new SesionBiblioteca("biblioteca.db", NOMBRE_INSTITUCION, sembrar);
        Biblioteca biblioteca = sesion.getBiblioteca();

        if (hayEstado) {
            System.out.println("[BD] Estado recargado: " + sesion.getBiblioteca().getLibros().size()
                    + " libros, " + sesion.getBiblioteca().getSocios().size() + " socios.");
        } else if (sembrar) {
            System.out.println("[BD] Sembrado: " + sesion.getBiblioteca().getLibros().size()
                    + " libros, " + sesion.getBiblioteca().getSocios().size() + " socios e historial de prestamos.");
        }
    }

    /** Guarda el estado (upsert por identidad) y cierra la conexion. */
    private static void cerrarBase() {
        if (sesion != null) {
            sesion.guardar();
            System.out.println("[BD] Estado final guardado.");
            sesion.cerrar();
        }
    }

    /** Persistencia automatica tras cada operacion de escritura. */
    private static void persistir() {
        sesion.guardar();
    }

    /** Bucle principal del menu interactivo. */
    private static void menuInteractivo() {
        abrirBase();
        int opcion;
        do {
            imprimirMenu();
            opcion = leerEntero("Opci\u00f3n: ", 0, 18);
            System.out.println();
            try {
                ejecutarOpcion(opcion);
            } catch (DAOException e) {
                System.out.println("ERROR: " + e.getMessage());
            }
        } while (opcion != 0);
        cerrarBase();
    }

    private static void imprimirMenu() {
        System.out.println("\n========= " + sesion.getBiblioteca().getNombre() + " =========");
        System.out.println("                        LIBROS");
        System.out.println(" 1) Alta de libro            2) Lista de libros         3) Lista de t\u00edtulos");
        System.out.println("                        SOCIOS");
        System.out.println(" 4) Alta docente             5) Alta estudiante         6) Lista de socios");
        System.out.println(" 7) Buscar por DNI           8) Socios por tipo         9) Baja de socio");
        System.out.println("                        PR\u00c9STAMOS");
        System.out.println("10) Prestar un libro        11) Devolver libro         12) Pr\u00e9stamos vencidos");
        System.out.println("13) \u00bfQui\u00e9n tiene el libro?");
        System.out.println("                        DOCENTES");
        System.out.println("14) Docentes responsables   15) Premiar docente (+d\u00edas)");
        System.out.println("                        BASE DE DATOS / SALIR");
        System.out.println("16) Guardar estado          17) Recargar desde BD      18) Baja de libro");
        System.out.println(" 0) Salir");
    }

    private static void ejecutarOpcion(int p_opcion) {
        switch (p_opcion) {
            case 1: altaLibro(); break;
            case 2: System.out.println(sesion.getBiblioteca().listaDeLibros()); break;
            case 3: System.out.println(sesion.getBiblioteca().listaDeTitulos()); break;
            case 4: altaDocente(); break;
            case 5: altaEstudiante(); break;
            case 6: System.out.println(sesion.getBiblioteca().listaDeSocios()); break;
            case 7: buscarSocio(); break;
            case 8: sociosPorTipo(); break;
            case 9: bajaSocio(); break;
            case 10: prestar(); break;
            case 11: devolver(); break;
            case 12: listarPrestamosVencidos(); break;
            case 13: quienTieneElLibro(); break;
            case 14: System.out.println(sesion.getBiblioteca().listaDeDocentesResponsables()); break;
            case 15: premiarDocente(); break;
            case 16: persistir(); System.out.println("[BD] Estado guardado."); break;
            case 17: Biblioteca recargada = sesion.recargar(); System.out.println(
                    "[BD] Recargado: " + recargada.getLibros().size() + " libros, "
                    + recargada.getSocios().size() + " socios."); break;
            case 18: bajaLibro(); break;
            case 0: break;
            default: System.out.println("Opcion inexistente.");
        }
    }

    // -------------------------------------------------------------------------
    // Opciones: LIBROS
    // -------------------------------------------------------------------------
    private static void altaLibro() {
        System.out.println("-- Alta de libro --");
        String titulo = leerTextoObligatorio("Titulo: ");
        if (titulo == null) { System.out.println("Operacion cancelada."); return; }
        int edicion = leerEntero("Edicion: ", 1, Integer.MAX_VALUE);
        if (edicion == 0) { System.out.println("Operacion cancelada."); return; }
        String editorial = leerTextoObligatorio("Editorial: ");
        if (editorial == null) { System.out.println("Operacion cancelada."); return; }
        int anio = leerEntero("Anio de edicion: ", 1000, 3000);
        if (anio == 0) { System.out.println("Operacion cancelada."); return; }

        sesion.getBiblioteca().nuevoLibro(titulo, edicion, editorial, anio);
        persistir(); // el DAO INSERTa y le devuelve el id a Libro
        Libro creado = sesion.getBiblioteca().getLibros().get(sesion.getBiblioteca().getLibros().size() - 1);
        System.out.println("Libro guardado con id = " + creado.getId());
    }

    private static void bajaLibro() {
        System.out.println("-- Baja de libro --");
        Libro libro = elegirLibro();
        if (libro == null) return;
        if (!libro.getPrestamos().isEmpty()) {
            // La FK prestamo -> libro impide borrar un ejemplar con historial.
            System.out.println("No se puede borrar: el libro tiene "
                    + libro.getPrestamos().size() + " prestamo(s) asociados (FK).");
            return;
        }
        sesion.getDao().getLibroDAO().delete(libro);
        sesion.getBiblioteca().quitarLibro(libro); // coherencia memoria <-> BD
        System.out.println("Libro dado de baja: " + libro.getTitulo());
    }

    // -------------------------------------------------------------------------
    // Opciones: SOCIOS
    // -------------------------------------------------------------------------
    private static void altaDocente() {
        System.out.println("-- Alta de socio docente --");
        int dni = leerDniNuevo();
        if (dni == -1) return;
        String nombre = leerTextoObligatorio("Nombre y apellido: ");
        if (nombre == null) { System.out.println("Operacion cancelada."); return; }
        String area = leerTextoObligatorio("Area: ");
        if (area == null) { System.out.println("Operacion cancelada."); return; }

        sesion.getBiblioteca().nuevoSocioDocente(dni, nombre, area);
        persistir();
        System.out.println("Docente dado de alta (dias de prestamo iniciales: 5).");
    }

    private static void altaEstudiante() {
        System.out.println("-- Alta de socio estudiante --");
        int dni = leerDniNuevo();
        if (dni == -1) return;
        String nombre = leerTextoObligatorio("Nombre y apellido: ");
        if (nombre == null) { System.out.println("Operacion cancelada."); return; }
        String carrera = leerTextoObligatorio("Carrera: ");
        if (carrera == null) { System.out.println("Operacion cancelada."); return; }

        sesion.getBiblioteca().nuevoSocioEstudiante(dni, nombre, carrera);
        persistir();
        System.out.println("Estudiante dado de alta (dias de prestamo iniciales: 20).");
    }

    private static void buscarSocio() {
        System.out.println("-- Busqueda por DNI --");
        int dni = leerEntero("DNI: ", 1, Integer.MAX_VALUE);
        Socio socio = sesion.getBiblioteca().buscarSocio(dni);
        System.out.println(socio != null ? socio.toString() : "No existe un socio con ese DNI.");
    }

    private static void sociosPorTipo() {
        String tipo = leerTipoSocio();
        int cantidad = sesion.getBiblioteca().cantidadDeSociosPorTipo(tipo);
        System.out.println("Cantidad de socios del tipo " + tipo + ": " + cantidad);
    }

    private static void bajaSocio() {
        System.out.println("-- Baja de socio --");
        int dni = leerEntero("DNI: ", 1, Integer.MAX_VALUE);
        Socio socio = sesion.getBiblioteca().buscarSocio(dni);
        if (socio == null) {
            System.out.println("No existe un socio con ese DNI.");
            return;
        }
        if (!socio.getPrestamos().isEmpty()) {
            // La FK prestamo -> socio impide borrar con historial.
            System.out.println("No se puede borrar: el socio tiene "
                    + socio.getPrestamos().size() + " prestamo(s) asociados (FK).");
            return;
        }
        sesion.getDao().getSocioDAO().delete(socio);
        sesion.getBiblioteca().quitarSocio(socio);
        System.out.println("Socio dado de baja: " + socio.getNombre());
    }

    // -------------------------------------------------------------------------
    // Opciones: PRESTAMOS
    // -------------------------------------------------------------------------
    private static void prestar() {
        System.out.println("-- Prestar un libro --");
        Socio socio = leerSocioExistente();
        if (socio == null) return;
        Libro libro = elegirLibro();
        if (libro == null) return;
        Calendar fechaRetiro = leerFechaOpcional("Fecha de retiro");

        boolean realizado = sesion.getBiblioteca().prestarLibro(fechaRetiro, socio, libro);
        if (realizado) {
            persistir(); // INSERT del prestamo + posibles UPDATEs de estado del socio/libro
            System.out.println("Prestamo registrado: '" + libro.getTitulo() + "' para "
                    + socio.getNombre() + ".");
        } else if (libro.prestado()) {
            System.out.println("Rechazado: el libro ya esta prestado.");
        } else {
            System.out.println("Rechazado: " + socio.getNombre()
                    + " no puede pedir (tope de libros o prestamo vencido).");
        }
    }

    private static void devolver() {
        System.out.println("-- Devolver un libro --");
        Libro libro = elegirLibro();
        if (libro == null) return;
        try {
            sesion.getBiblioteca().devolverLibro(libro); // lanza la excepcion de negocio si no estaba prestado
            persistir();                     // UPDATE de la fecha de devolucion
            System.out.println("Devolucion registrada para: " + libro.getTitulo());
        } catch (LibroNoPrestadoException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    private static void listarPrestamosVencidos() {
        System.out.println("-- Prestamos vencidos a la fecha --");
        java.util.ArrayList<Prestamo> vencidos = sesion.getBiblioteca().prestamosVencidos();
        if (vencidos.isEmpty()) {
            System.out.println("No hay prestamos vencidos.");
            return;
        }
        for (Prestamo unPrestamo : vencidos) {
            System.out.println(unPrestamo + "\n");
        }
    }

    private static void quienTieneElLibro() {
        System.out.println("-- \u00bfQuien tiene el libro? --");
        Libro libro = elegirLibro();
        if (libro == null) return;
        try {
            String nombre = sesion.getBiblioteca().quienTieneElLibro(libro); // lanza la excepcion si esta en la biblioteca
            System.out.println("El libro '" + libro.getTitulo() + "' lo tiene: " + nombre);
        } catch (LibroNoPrestadoException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    // -------------------------------------------------------------------------
    // Opciones: DOCENTES
    // -------------------------------------------------------------------------
    private static void premiarDocente() {
        System.out.println("-- Premio al docente responsable (+dias de prestamo) --");
        int dni = leerEntero("DNI del docente: ", 1, Integer.MAX_VALUE);
        Socio socio = sesion.getBiblioteca().buscarSocio(dni);
        if (socio == null) {
            System.out.println("No existe un socio con ese DNI.");
            return;
        }
        if (!(socio instanceof Docente)) {
            System.out.println(socio.getNombre() + " no es docente: el premio no corresponde.");
            return;
        }
        Docente docente = (Docente) socio;
        int dias = leerEntero("Dias a adicionar: ", 1, 365);
        docente.cambiarDiasDePrestamos(dias);
        persistir(); // UPDATE socio.dias_prestamos
        System.out.println(docente.getNombre() + " ahora tiene "
                + docente.getDiasPrestamos() + " dias de prestamo.");
    }

    // =========================================================================
    // Entrada por consola con validacion
    // =========================================================================

    /** true cuando el stdin se agoto (ejecucion automatizada): el menu cierra sin crash. */
    private static boolean stdinAgotada = false;

    /** nextLine blindado: al agotarse la entrada devuelve "" y marca EOF. */
    private static String leerLinea() {
        try {
            return consola.nextLine().trim();
        } catch (java.util.NoSuchElementException e) {
            stdinAgotada = true;
            System.out.println("\n[STDIN] Entrada agotada: cerrando de forma ordenada.");
            return "";
        }
    }

    private static int leerEntero(String p_prompt, int p_min, int p_max) {
        while (true) {
            System.out.print(p_prompt);
            String linea = leerLinea();
            if (stdinAgotada) {
                return 0; // con EOF el menu interpreta 0 = salir ordenadamente
            }
            try {
                int valor = Integer.parseInt(linea);
                if (valor >= p_min && valor <= p_max) {
                    return valor;
                }
            } catch (NumberFormatException e) { /* re-pregunta */ }
            System.out.println("    Ingrese un numero entre " + p_min + " y " + p_max + ".");
        }
    }

    private static String leerTextoObligatorio(String p_prompt) {
        while (true) {
            System.out.print(p_prompt);
            String linea = leerLinea();
            if (stdinAgotada) {
                return null; // los flujos de alta chequean null y cancelan
            }
            if (!linea.isEmpty()) {
                return linea;
            }
            System.out.println("    El texto no puede quedar vacio.");
        }
    }

    /** DNI garantizado nuevo (evita el PK repetido de la tabla socio). */
    private static int leerDniNuevo() {
        int dni = leerEntero("DNI (0 para cancelar): ", 0, Integer.MAX_VALUE);
        if (dni == 0) {
            System.out.println("Operacion cancelada.");
            return -1;
        }
        if (sesion.getBiblioteca().buscarSocio(dni) != null) {
            System.out.println("Ya existe un socio con ese DNI.");
            return -1;
        }
        return dni;
    }

    private static String leerTipoSocio() {
        while (true) {
            System.out.print("Tipo (Docente/Estudiante): ");
            String tipo = leerLinea();
            if (stdinAgotada) return "";
            if (tipo.equalsIgnoreCase("Docente") || tipo.equalsIgnoreCase("Estudiante")) {
                return Character.toUpperCase(tipo.charAt(0))
                        + tipo.substring(1).toLowerCase(); // normalizo al literal del informe
            }
            System.out.println("    El tipo solo puede ser Docente o Estudiante.");
        }
    }

    private static Socio leerSocioExistente() {
        int dni = leerEntero("DNI del socio (0 para cancelar): ", 0, Integer.MAX_VALUE);
        if (dni == 0) return null;
        Socio socio = sesion.getBiblioteca().buscarSocio(dni);
        if (socio == null) {
            System.out.println("No existe un socio con ese DNI.");
            return null;
        }
        return socio;
    }

    /** Lista los libros y toma la eleccion por numero (1-based). */
    private static Libro elegirLibro() {
        if (sesion.getBiblioteca().getLibros().isEmpty()) {
            System.out.println("El catalogo esta vacio.");
            return null;
        }
        System.out.println(sesion.getBiblioteca().listaDeLibros());
        int numero = leerEntero("Numero del libro (0 para cancelar): ", 0,
                sesion.getBiblioteca().getLibros().size());
        if (numero == 0) {
            System.out.println("Operacion cancelada.");
            return null;
        }
        return sesion.getBiblioteca().getLibros().get(numero - 1);
    }

    /** Enter = hoy; dia mes anio = fecha elegida (retroactiva para probar vencidos). */
    private static Calendar leerFechaOpcional(String p_prompt) {
        System.out.print(p_prompt + " [Enter]=hoy, o ingrese dia mes anio: ");
        String linea = leerLinea();
        if (linea.isEmpty()) {
            return SesionBiblioteca.hoy(); // helper compartido de hoy normalizado
        }
        String[] partes = linea.split("\\s+");
        try {
            int p_dia = Integer.parseInt(partes[0]);
            int p_mes = Integer.parseInt(partes[1]);
            int p_anio = Integer.parseInt(partes[2]);
            if (p_dia >= 1 && p_dia <= 31 && p_mes >= 1 && p_mes <= 12) {
                return SesionBiblioteca.fecha(p_anio, p_mes, p_dia);
            }
        } catch (Exception e) { /* re-pregunta */ }
        System.out.println("    Fecha invalida. Formato: dia mes anio (ej: 25 10 2026). Intente de nuevo.");
        return leerFechaOpcional(p_prompt);
    }

    // =========================================================================
    // ESCENARIO DE LA CONSIGNA (TP7)
    // =========================================================================

    /** Salida exacta que pide el TP7: instanciar, operar y responder las 4 preguntas. */
    private static void escenarioConsigna() throws LibroNoPrestadoException {
        Biblioteca bibliotecaEscenario = new Biblioteca("Biblioteca UNLP");

        // ------------------- Carga de libros -------------------
        bibliotecaEscenario.nuevoLibro("Programando con JAVA", 2, "El Ateneo", 2014);        // 0
        bibliotecaEscenario.nuevoLibro("Longman. Diccionario Pocket", 1, "Longman", 2010);   // 1
        bibliotecaEscenario.nuevoLibro("Vivir para contarla", 1, "Debolsillo", 2003);        // 2
        bibliotecaEscenario.nuevoLibro("JAVA. Como Programar", 3, "Pearson", 2016);          // 3
        bibliotecaEscenario.nuevoLibro("JAVA. Como Programar", 1, "Pearson", 2006);          // 4 (2.o ejemplar)
        bibliotecaEscenario.nuevoLibro("Longman. Diccionario Pocket", 2, "Longman", 2014);   // 5 (2.o ejemplar)

        // ------------------- Carga de socios -------------------
        bibliotecaEscenario.nuevoSocioDocente(14524782, "Juan Perez", "Sistemas");
        bibliotecaEscenario.nuevoSocioDocente(17982110, "Juan Fernandez", "Matematica");
        bibliotecaEscenario.nuevoSocioDocente(10912002, "Maria Alegre", "Fisica");
        bibliotecaEscenario.nuevoSocioEstudiante(28987498, "Francisco Paenza", "Fisica");
        bibliotecaEscenario.nuevoSocioEstudiante(31987123, "Cesar Milstein", "Quimica");

        // Referencias que se usan en el escenario y en las preguntas
        Socio perez       = bibliotecaEscenario.buscarSocio(14524782);
        Socio fernandez   = bibliotecaEscenario.buscarSocio(17982110);
        Socio paenza      = bibliotecaEscenario.buscarSocio(28987498);
        Socio milstein    = bibliotecaEscenario.buscarSocio(31987123);
        Libro programandoConJava = bibliotecaEscenario.getLibros().get(0);
        Libro longman2010 = bibliotecaEscenario.getLibros().get(1);
        Libro contarla    = bibliotecaEscenario.getLibros().get(2);
        Libro java2016    = bibliotecaEscenario.getLibros().get(3);
        Libro java2006    = bibliotecaEscenario.getLibros().get(4);
        Libro longman2014 = bibliotecaEscenario.getLibros().get(5);

        // ------------------- Escenario de prestamos -------------------
        // Paenza retira el 05/10 y no devuelve: es su 3er prestamo VENCIDO activo.
        bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 5), paenza, longman2014);

        // Fernandez retira el 10/10 y devuelve HOY: vencio el 15/10, devolvio TARDE.
        bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 10), fernandez, longman2010);
        bibliotecaEscenario.devolverLibro(longman2010);

        // Perez retira el 27/10 y devuelve HOY: vencia el 01/11, devolvio A TIEMPO.
        bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 27), perez, java2006);
        bibliotecaEscenario.devolverLibro(java2006);

        // Perez se lleva HOY "Programando con JAVA" (permite responder la pregunta 4).
        bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 31), perez, programandoConJava);

        // Milstein se lleva HOY 3 libros distintos (activo sano, al tope).
        bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 31), milstein, longman2010);
        bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 31), milstein, contarla);
        bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 31), milstein, java2016);

        // Rechazos de prestarLibro (la biblioteca no crea el prestamo):
        System.out.println("-- Rechazos de prestamo --");
        System.out.println("Milstein (ya tiene 3 libros) intenta llevarse otro:   "
            + bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 31), milstein, java2006));   // false: tope del estudiante
        System.out.println("Paenza (tiene un vencido) intenta llevarse otro:      "
            + bibliotecaEscenario.prestarLibro(SesionBiblioteca.fecha(2026, 10, 31), paenza, java2006));     // false: prestamo vencido

        // Premio a la responsabilidad: Perez devolvio siempre a tiempo -> +5 dias de prestamo.
        Docente perezDocente = (Docente) perez;
        System.out.println();
        System.out.println("-- Premio a la responsabilidad --");
        System.out.println("Perez tenia " + perezDocente.getDiasPrestamos() + " dias de prestamo");
        perezDocente.cambiarDiasDePrestamos(5);
        System.out.println("Perez ahora tiene " + perezDocente.getDiasPrestamos() + " dias de prestamo");

        // ------------------- Informe: prestamos vencidos -------------------
        System.out.println("-- Prestamos vencidos a la fecha --");
        for (Prestamo unPrestamo : bibliotecaEscenario.prestamosVencidos()) {
            System.out.println(unPrestamo + "\n");
        }

        // ------------------- Pregunta 1 -------------------
        System.out.println("1) Cantidad de socios de tipo Estudiante: "
            + bibliotecaEscenario.cantidadDeSociosPorTipo("Estudiante"));

        // ------------------- Pregunta 2 -------------------
        System.out.println("2) Docentes que nunca adeudaron ni adeudan libros:");
        System.out.println(bibliotecaEscenario.listaDeDocentesResponsables());

        // ------------------- Pregunta 3 -------------------
        System.out.println("3) Listas de libros y de socios de la biblioteca:");
        System.out.println(bibliotecaEscenario.listaDeLibros());
        System.out.println(bibliotecaEscenario.listaDeSocios());

        // ------------------- Pregunta 4 -------------------
        System.out.println("4) El libro \"Programando con JAVA\" lo tiene: "
            + bibliotecaEscenario.quienTieneElLibro(programandoConJava));
    }
}
