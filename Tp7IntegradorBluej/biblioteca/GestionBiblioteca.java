package biblioteca;

import java.util.Calendar;
import java.util.ArrayList;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

// CONSOLA del practico: este main es solo texto (consigna).
// Para la interfaz grafica esta VentanaPrincipal.main().
public class GestionBiblioteca {

    // Lee un entero por teclado sin explotar si el usuario escribe texto.
    // Repite la pregunta hasta que ingrese un numero valido.
    private static int leerEntero(Scanner p_teclado, String p_mensaje) {
        while (true) {
            System.out.println(p_mensaje);
            String linea = p_teclado.nextLine().trim();
            try {
                return Integer.parseInt(linea);
            } catch (NumberFormatException e) {
                System.out.println("Debe ingresar un numero entero.");
            }
        }
    }

    // Si la base rechaza un cambio, se recarga el modelo desde la base para
    // no quedar desincronizados. La transaccion fallida ya dejo la base como
    // estaba, asi que recargar deshace el cambio hecho solo en memoria.
    private static Biblioteca recargarBiblioteca(BibliotecaDAO p_bibliotecaDAO,
            Biblioteca p_bibliotecaActual) {
        try {
            return p_bibliotecaDAO.cargarBiblioteca(
                "Biblioteca Gestion FACENA-UNNE"
            );
        } catch (SQLException | ParseException e) {
            System.out.println(
                "No se pudo recargar la base de datos: " + e.getMessage()
                + ". El estado en memoria puede no coincidir con la base: "
                + "reinicie el programa."
            );
            return p_bibliotecaActual;
        }
    }

    public static void main(String[] args)
        throws SQLException, ParseException, LibroNoPrestadoException {

        // Todo el programa empieza aca
        Scanner teclado = new Scanner(System.in);

        ConexionBD conexionBD = new ConexionBD("biblioteca.db");

        BibliotecaDAO bibliotecaDAO = new BibliotecaDAO(conexionBD);

        // Primero las tablas, despues la carga: en la primera ejecucion
        // la base esta vacia y cargar sin tablas fallaria.
        bibliotecaDAO.crearTablas();
        Biblioteca biblioteca = bibliotecaDAO.cargarBiblioteca("Biblioteca Gestion FACENA-UNNE");

        int respuesta;

        // do...while: el menu se muestra al menos una vez y se sale con 0
        do {
            System.out.println("\n===== BIBLIOTECA GESTION FACENA-UNNE =====");
            System.out.println("1. Agregar libro");
            System.out.println("2. Agregar socio docente");
            System.out.println("3. Agregar socio estudiante");
            System.out.println("4. Prestar libro");
            System.out.println("5. Devolver libro");
            System.out.println("6. Cantidad de socios por tipo");
            System.out.println("7. Prestamos vencidos");
            System.out.println("8. Docentes responsables");
            System.out.println("9. Consultar quien tiene un libro");
            System.out.println("10. Listar socios");
            System.out.println("11. Listar libros");
            System.out.println("12. Listar titulos");
            System.out.println("13. Eliminar socio");
            System.out.println("14. Premiar docentes responsables");
            System.out.println("0. Salir");

            respuesta = leerEntero(teclado, "Ingrese una opcion:");

            switch(respuesta){
                
                case 1:
                    System.out.println("Titulo:");
                    String titulo = teclado.nextLine();

                    int edicion = leerEntero(teclado, "Edicion:");

                    System.out.println("Editorial:");
                    String editorial = teclado.nextLine();

                    int anio = leerEntero(teclado, "Anio:");

                    biblioteca.nuevoLibro(titulo, edicion, editorial, anio);

                    Libro nuevoLibro = biblioteca.getLibros().get(
                        biblioteca.getLibros().size() - 1
                    );

                    try {

                        bibliotecaDAO.getLibroDAO().agregar(nuevoLibro);

                        System.out.println("Libro agregado correctamente.");

                    } catch (SQLException excepcionSQL) {

                        // La persistencia manda: si la base rechazo el insert,
                        // se saca el libro de memoria.
                        biblioteca.getLibros().remove(nuevoLibro);

                        System.out.println(
                            "Error al guardar el libro en la base de datos: "
                            + excepcionSQL.getMessage()
                        );
                    }

                    break;

                case 2:
                    int dniDocente = leerEntero(teclado, "D.N.I.:");

                    System.out.println("Nombre y apellido:");
                    String nombreDocente = teclado.nextLine();

                    System.out.println("Area:");
                    String area = teclado.nextLine();

                    biblioteca.nuevoSocioDocente(dniDocente, nombreDocente, area);

                    Socio nuevoDocente = biblioteca.getSocios().get(
                        biblioteca.getSocios().size() - 1
                    );

                    try {

                        bibliotecaDAO.getSocioDAO().agregar(nuevoDocente);

                        System.out.println("Docente agregado correctamente.");

                    } catch (SQLException excepcionSQL) {

                        biblioteca.getSocios().remove(nuevoDocente);

                        System.out.println(
                            "Error al guardar el docente en la base de datos: "
                            + excepcionSQL.getMessage()
                        );
                    }

                    break;

                case 3:
                    int dniEstudiante = leerEntero(teclado, "D.N.I.:");

                    System.out.println("Nombre y apellido:");
                    String nombreEstudiante = teclado.nextLine();

                    System.out.println("Carrera:");
                    String carrera = teclado.nextLine();

                    biblioteca.nuevoSocioEstudiante(dniEstudiante, nombreEstudiante, carrera);

                    Socio nuevoEstudiante = biblioteca.getSocios().get(
                        biblioteca.getSocios().size() - 1
                    );

                    try {

                        bibliotecaDAO.getSocioDAO().agregar(nuevoEstudiante);

                        System.out.println("Estudiante agregado correctamente.");

                    } catch (SQLException excepcionSQL) {

                        biblioteca.getSocios().remove(nuevoEstudiante);

                        System.out.println(
                            "Error al guardar el estudiante en la base de datos: "
                            + excepcionSQL.getMessage()
                        );
                    }

                    break;

                case 4:

                    int dniSocio = leerEntero(teclado, "D.N.I. del socio:");

                    System.out.println("Titulo del libro:");
                    String tituloLibro = teclado.nextLine();

                    int diasTranscurridos = leerEntero(
                        teclado, "Asignar dias de prestamos (0 = hoy):"
                    );

                    Socio socio = biblioteca.buscarSocio(dniSocio);

                    // Si hay varios ejemplares del mismo titulo, se presta uno libre.
                    Libro libro = null;
                    boolean existeTitulo = false;

                    for (Libro unLibro : biblioteca.getLibros()) {
                        if (unLibro.getTitulo().equalsIgnoreCase(tituloLibro)) {
                            existeTitulo = true;
                            if (!unLibro.prestado()) {
                                libro = unLibro;
                                break;
                            }
                        }
                    }

                    if(socio == null){

                        System.out.println("No se encontro el socio.");

                    } else if(!existeTitulo){

                        System.out.println("No se encontro el libro.");

                    } else if(libro == null){

                        System.out.println(
                            "Todas las copias de ese titulo estan prestadas."
                        );

                    } else {

                        Calendar fechaRetiro = Calendar.getInstance();
                        fechaRetiro.add(Calendar.DAY_OF_MONTH, -diasTranscurridos);

                        boolean prestamoRealizado = biblioteca.prestarLibro(
                            fechaRetiro , socio, libro
                        );

                        if(prestamoRealizado){

                            ArrayList<Prestamo> prestamos = socio.getPrestamos();

                            Prestamo nuevoPrestamo = prestamos.get(
                                prestamos.size() - 1
                            );

                            try {

                                bibliotecaDAO.getPrestamoDAO().agregar(nuevoPrestamo);

                                System.out.println("Prestamo registrado correctamente.");

                            } catch (SQLException excepcionSQL) {

                                // La persistencia manda: si la base rechazo el
                                // insert, se deshace el prestamo en memoria para
                                // no quedar desincronizados con la base.
                                prestamos.remove(nuevoPrestamo);
                                libro.getPrestamos().remove(nuevoPrestamo);

                                System.out.println(
                                    "Error al guardar el prestamo en la base de datos: "
                                    + excepcionSQL.getMessage()
                                );
                            }

                        } else {

                            System.out.println("No se pudo realizar el prestamo.");
                        }

                    }

                    break;

                case 5:

                    System.out.println("Titulo del libro:");
                    String tituloDevolucion = teclado.nextLine();

                    // Si hay varios ejemplares del mismo titulo, la devolucion
                    // se hace sobre el que esta prestado.
                    Libro libroDevolucion = null;

                    for (Libro unLibro : biblioteca.getLibros()) {
                        if (unLibro.getTitulo().equalsIgnoreCase(tituloDevolucion)
                                && unLibro.prestado()) {
                            libroDevolucion = unLibro;
                            break;
                        }
                    }

                    if(libroDevolucion != null){

                        // Biblioteca.devolverLibro() registra la fecha sobre
                        // ultimoPrestamo(): se usa el mismo prestamo para el
                        // UPDATE, asi modelo y base apuntan a la misma fila.
                        Prestamo prestamoDevuelto = libroDevolucion.ultimoPrestamo();

                        if(prestamoDevuelto != null && !prestamoDevuelto.estaDevuelto()){

                            try {

                                biblioteca.devolverLibro(libroDevolucion);

                                int filas = bibliotecaDAO.getPrestamoDAO()
                                    .actualizarDevolucion(prestamoDevuelto);

                                if(filas == 0){
                                    System.out.println(
                                        "No se encontro el prestamo activo en la base: "
                                        + "la devolucion no se registro."
                                    );
                                    biblioteca = recargarBiblioteca(
                                        bibliotecaDAO, biblioteca
                                    );
                                } else {
                                    System.out.println("Devolucion registrada correctamente.");
                                }

                            } catch (SQLException excepcionSQL) {

                                // La persistencia manda: el UPDATE corrio en
                                // transaccion, asi que se recarga el modelo
                                // desde la base para deshacer el cambio en
                                // memoria (Prestamo no permite volver a null).
                                System.out.println(
                                    "Error al guardar la devolucion en la base de datos: "
                                    + excepcionSQL.getMessage()
                                );
                                biblioteca = recargarBiblioteca(
                                    bibliotecaDAO, biblioteca
                                );

                            } catch (LibroNoPrestadoException excepcionNegocio) {

                                System.out.println(excepcionNegocio.getMessage());
                            }

                        } else {

                            System.out.println("El libro no tiene un prestamo activo.");
                        }

                    } else if(biblioteca.buscarLibro(tituloDevolucion) != null){

                        System.out.println("El libro no tiene un prestamo activo.");

                    } else {

                        System.out.println("No se encontro el libro.");
                    }
                    break;

                case 6:
                    System.out.println("Tipo de socio (Docente/Estudiante):");

                    String tipoSocio = teclado.nextLine();

                    System.out.println(
                        "Cantidad de socios: "
                        + biblioteca.cantidadDeSociosPorTipo(tipoSocio)
                    );
                    break;

                case 7:
                    System.out.println(biblioteca.prestamosVencidos());
                    break;

                case 8:
                    System.out.println(biblioteca.listaDeDocentesResponsables());
                    break;

                case 9:
                    System.out.println("Titulo del libro:");

                    String tituloConsulta = teclado.nextLine();

                    // Con varios ejemplares del mismo titulo se consulta el
                    // que esta prestado; si ninguno lo esta, buscarLibro()
                    // devuelve el primero y quienTieneElLibro() avisa que esta
                    // en la biblioteca.
                    Libro libroConsulta = null;

                    for (Libro unLibro : biblioteca.getLibros()) {
                        if (unLibro.getTitulo().equalsIgnoreCase(tituloConsulta)
                                && unLibro.prestado()) {
                            libroConsulta = unLibro;
                            break;
                        }
                    }

                    if(libroConsulta == null){
                        libroConsulta = biblioteca.buscarLibro(tituloConsulta);
                    }

                    if(libroConsulta != null){

                        try {

                            System.out.println(
                                "Lo tiene: " + biblioteca.quienTieneElLibro(libroConsulta)
                            );

                        } catch (LibroNoPrestadoException excepcionPrestamo) {

                            System.out.println(excepcionPrestamo.getMessage());
                        }

                    } else {

                        System.out.println("No se encontro el libro.");
                    }
                    break;

                case 10:
                    System.out.println(biblioteca.listaDeSocios());
                    break;

                case 11:
                    System.out.println(biblioteca.listaDeLibros());
                    break;

                case 12:
                    System.out.println(biblioteca.listaDeTitulos());
                    break;

                case 13:
                    int dni = leerEntero(teclado, "D.N.I. del socio a eliminar:");

                    Socio socioEncontrado = biblioteca.buscarSocio(dni);

                    if (socioEncontrado == null) {
                        System.out.println("No se encontro un socio con DNI: " + dni);
                        break;
                    }

                    // Verifico que NO tenga prestamos activos
                    boolean tieneActivos = socioEncontrado.getPrestamos().stream()
                            .anyMatch(p -> !p.estaDevuelto());

                    if (tieneActivos) {
                        System.out.println("No se puede eliminar al socio "
                                + socioEncontrado.getNombre()
                                + ": tiene " + socioEncontrado.cantLibrosPrestados()
                                + " libro(s) prestado(s) activo(s).");
                        break;
                    }

                    try {

                        // Primero la base: si falla, no se toca la memoria y
                        // no quedan desincronizadas. SocioDAO elimina tambien
                        // el historial de prestamos del socio en la misma
                        // transaccion.
                        int filas = bibliotecaDAO.getSocioDAO()
                                                 .eliminar(socioEncontrado);

                        if (filas == 0) {
                            System.out.println(
                                "No se encontro el socio en la base de datos: "
                                + "no se elimino nada."
                            );
                            break;
                        }

                        boolean eliminado = biblioteca.quitarSocio(socioEncontrado);

                        if (eliminado) {
                            System.out.println("Socio eliminado correctamente: "
                                    + socioEncontrado.getNombre());
                        } else {
                            System.out.println("Error: no se pudo eliminar el socio.");
                        }

                    } catch (SQLException excepcionSQL) {

                        System.out.println(
                            "Error al eliminar el socio de la base de datos: "
                            + excepcionSQL.getMessage()
                        );
                    }
                    break;

                case 14:
                    // Modelo primero: el premio toca solo memoria. La base se
                    // actualiza docente por docente; si alguna escritura falla,
                    // se revierte solo lo que no llego a persistirse.
                    ArrayList<Docente> premiados =
                        biblioteca.premiarDocentesResponsables(5);

                    if (premiados.isEmpty()) {
                        System.out.println(
                            "No hay docentes responsables a quienes premiar."
                        );
                        break;
                    }

                    ArrayList<Docente> guardados = new ArrayList<>();

                    try {
                        for (Docente unDocente : premiados) {
                            int filas = bibliotecaDAO.getSocioDAO()
                                .actualizarDiasPrestamo(unDocente);

                            if (filas > 0) {
                                guardados.add(unDocente);
                            }
                        }

                        // Los que no quedaron en la base vuelven a sus dias
                        // anteriores: memoria y base quedan sincronizadas.
                        for (Docente unDocente : premiados) {
                            if (!guardados.contains(unDocente)) {
                                unDocente.cambiarDiasDePrestamos(-5);
                            }
                        }

                        if (guardados.isEmpty()) {
                            System.out.println(
                                "No se pudo guardar el premio de ningun docente "
                                + "en la base de datos."
                            );
                        } else if (guardados.size() < premiados.size()) {
                            System.out.println(
                                "Premio aplicado a " + guardados.size() + " de "
                                + premiados.size() + " docente(s) responsable(s)."
                            );
                        } else {
                            System.out.println(
                                "Premio otorgado: +5 dias de prestamo a "
                                + premiados.size() + " docente(s) responsable(s)."
                            );
                        }

                        for (Docente unDocente : guardados) {
                            System.out.println(
                                "  - " + unDocente.getNombre()
                                + " (D.N.I. " + unDocente.getDniSocio() + ")"
                                + ": ahora tiene " + unDocente.getDiasPrestamo()
                                + " dias de prestamo."
                            );
                        }

                    } catch (SQLException excepcionSQL) {
                        // Solo se revierte lo que no llego a persistirse.
                        for (Docente unDocente : premiados) {
                            if (!guardados.contains(unDocente)) {
                                unDocente.cambiarDiasDePrestamos(-5);
                            }
                        }

                        System.out.println(
                            "Error al guardar el premio en la base de datos: "
                            + excepcionSQL.getMessage()
                            + " (se deshicieron los premios no persistidos)."
                        );
                    }
                    break;

                case 0:
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opcion incorrecta.");
                    break;
            }
        } while(respuesta != 0);

        teclado.close();
        conexionBD.cerrar();

        System.out.println("Programa finalizado.");
    }
}