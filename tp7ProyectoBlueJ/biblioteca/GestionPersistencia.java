package biblioteca;

import java.util.Calendar;

import biblioteca.persistencia.BibliotecaDAO;
import biblioteca.persistencia.ConexionBD;
import biblioteca.persistencia.DAOException;

/**
 * Demo del CRUD completo sobre SQLite: materia practica de persistencia.
 *
 * Primera ejecucion: siembra el catalogo (CREATE) y lo deja guardado en
 * tp7ProyectoBlueJ/biblioteca.db. Ejecuciones siguientes: CARGA el estado
 * persistido de la ejecucion anterior (READ), aplica operaciones de
 * UPDATE, CREATE y DELETE sobre ese estado, y vuelve a guardar.
 *
 * Con el argumento "reset" se borra todo y arranca desde cero:
 *   java biblioteca.GestionPersistencia reset
 */
public class GestionPersistencia {

    private static final String NOMBRE_BIBLIOTECA = "Biblioteca UNLP";

    /**
     * Demo del CRUD completo (CREATE / READ / UPDATE / DELETE) sobre la
     * base, dejando log por consola de cada operacion.
     *
     * Sin argumentos: reutiliza el estado persistido de la corrida previa;
     * con "reset" borra TODAS las tablas y arranca desde la siembra.
     *
     * @param args argumentos de la linea de comandos ("reset" o vacio)
     */
    public static void main(String[] args) {
        ConexionBD conexion = ConexionBD.instanciar("biblioteca.db");
        BibliotecaDAO dao = new BibliotecaDAO(conexion);

        boolean reset = args.length > 0 && args[0].equalsIgnoreCase("reset");
        if (reset) {
            System.out.println("\n== RESET: borrado masivo (DELETE) de las 3 tablas ==");
            // Orden invertido al de dependencia: prestamo (hija) -> socio/libro (padres).
            dao.getPrestamoDAO().deleteAll();
            dao.getSocioDAO().deleteAll();
            dao.getLibroDAO().deleteAll();
        }

        // -----------------------------------------------------------------
        // READ: se carga el estado persistido (o se siembra si es la 1.a vez).
        // -----------------------------------------------------------------
        Biblioteca biblioteca;
        if (!reset && dao.existeBiblioteca(NOMBRE_BIBLIOTECA)) {
            System.out.println("\n== READ: estado persistido de la ejecucion anterior ==");
            biblioteca = dao.cargar(NOMBRE_BIBLIOTECA);
        } else {
            System.out.println("\n== CREATE: siembra inicial (libros, socios, prestamo) ==");
            biblioteca = sembrarCatalogo(dao);
        }

        // -----------------------------------------------------------------
        // UPDATE 1: premio a la responsabilidad (cambia dias_prestamos del docente).
        // -----------------------------------------------------------------
        System.out.println("\n== UPDATE: premio de dias al docente responsable ==");
        Docente docente = (Docente) biblioteca.buscarSocio(14524782); // Juan Perez
        System.out.println("Perez tenia " + docente.getDiasPrestamos() + " dias de prestamo");
        docente.cambiarDiasDePrestamos(5);
        dao.guardar(biblioteca); // upsert: los objetos con id persistente van por UPDATE
        System.out.println("Perez ahora tiene " + docente.getDiasPrestamos()
                + " dias de prestamo (persistido)");

        // -----------------------------------------------------------------
        // CREATE suelto: nuevo ejemplar + prestamo del mismo, negocio operando normal.
        // -----------------------------------------------------------------
        System.out.println("\n== CREATE: nuevo libro, nuevo prestamo ==");
        biblioteca.nuevoLibro("Design Patterns", 1, "Addison-Wesley", 1994);
        dao.guardar(biblioteca);
        Libro designPatterns = biblioteca.getLibros().get(biblioteca.getLibros().size() - 1);
        System.out.println("Insertado con id = " + designPatterns.getId());

        Socio milstein = biblioteca.buscarSocio(31987123); // Cesar Milstein
        boolean presto = biblioteca.prestarLibro(fechaHoy(), milstein, designPatterns);
        dao.guardar(biblioteca); // el prestamo nuevo (id = -1) se inserta
        System.out.println("prestarLibro(Milstein, Design Patterns) = " + presto);

        // -----------------------------------------------------------------
        // UPDATE 2: devolucion de un prestamo activo (setea fecha_devolucion).
        // -----------------------------------------------------------------
        System.out.println("\n== UPDATE: devolucion de prestamo ==");
        Libro programandoConJava = biblioteca.getLibros().get(0);
        if (programandoConJava.prestado()) {
            try {
                biblioteca.devolverLibro(programandoConJava);
                dao.guardar(biblioteca); // el prestamo ya persistido va por UPDATE
                System.out.println("Devolucion registrada para: " + programandoConJava.getTitulo());
            } catch (LibroNoPrestadoException e) {
                System.out.println("No se pudo devolver: " + e.getMessage());
            }
        } else {
            System.out.println("No quedan prestamos activos de ese libro (ya devuelto en ejecucion previa)");
        }

        // -----------------------------------------------------------------
        // DELETE: baja de socio sin prestamos y de libro sin prestamos.
        // La FK de la base evita borrar registros con dependencias (sistema real).
        // -----------------------------------------------------------------
        System.out.println("\n== DELETE: baja de registros ==");
        Socio fernandez = biblioteca.buscarSocio(17982110); // sin prestamos asociados
        if (fernandez != null) {
            dao.getSocioDAO().delete(fernandez);
            biblioteca.quitarSocio(fernandez); // coherencia memoria <-> BD
            System.out.println("Socio dado de baja: " + fernandez.getNombre());
        } else {
            System.out.println("Fernandez ya habia sido dado de baja en una ejecucion previa");
        }

        if (docente != null) {
            try {
                dao.getSocioDAO().delete(docente); // Perez SI tiene prestamos: la FK debe frenarlo
            } catch (DAOException e) {
                System.out.println("FK impidio borrar a Perez (tiene prestamos asociados): "
                        + e.getCause().getMessage());
            }
        }

        // -----------------------------------------------------------------
        // Verificacion final: se recarga TODO desde la BD en otro objeto.
        // -----------------------------------------------------------------
        System.out.println("\n== Estado final recargado 100% desde SQLite ==");
        Biblioteca verificacion = dao.cargar(NOMBRE_BIBLIOTECA);
        System.out.println(verificacion.listaDeSocios());
        System.out.println();
        System.out.println(verificacion.listaDeLibros());
        System.out.println();
        System.out.println("Estado final recargado en memoria. La conexion se cierra a continuacion.");

        conexion.cerrar();
    }

    /** Catalogo inicial: CREATE + asociacion de un prestamo, todo persistido. */
    private static Biblioteca sembrarCatalogo(BibliotecaDAO p_dao) {
        Biblioteca biblioteca = new Biblioteca(NOMBRE_BIBLIOTECA);

        biblioteca.nuevoLibro("Programando con JAVA", 2, "El Ateneo", 2014);
        biblioteca.nuevoLibro("JAVA. Como Programar", 3, "Pearson", 2016);
        biblioteca.nuevoLibro("Vivir para contarla", 1, "Debolsillo", 2003);

        biblioteca.nuevoSocioDocente(14524782, "Juan Perez", "Sistemas");
        biblioteca.nuevoSocioDocente(17982110, "Juan Fernandez", "Matematica");
        biblioteca.nuevoSocioEstudiante(31987123, "Cesar Milstein", "Quimica");

        // Negocio vivo ANTES de guardar: prestarLibro crea el prestamo en ficha de libro y socio.
        biblioteca.prestarLibro(fechaHoy(), biblioteca.buscarSocio(14524782),
                biblioteca.getLibros().get(0));

        p_dao.guardar(biblioteca); // CREATE masivo: libros/socios/prestamo sin id se insertan

        System.out.println("Libros insertados con ids: "
            + biblioteca.getLibros().stream().map(l -> String.valueOf(l.getId()))
                .collect(java.util.stream.Collectors.joining(", ")));
        System.out.println("Prestamos guardados: " + biblioteca.getLibros().get(0).getPrestamos().size());
        return biblioteca;
    }

    /** Fecha de hoy a medianoche, mismo criterio de dia completo que usa el negocio. */
    private static Calendar fechaHoy() {
        Calendar fecha = Calendar.getInstance();
        fecha.set(Calendar.HOUR_OF_DAY, 0);
        fecha.set(Calendar.MINUTE, 0);
        fecha.set(Calendar.SECOND, 0);
        fecha.set(Calendar.MILLISECOND, 0);
        return fecha;
    }
}
