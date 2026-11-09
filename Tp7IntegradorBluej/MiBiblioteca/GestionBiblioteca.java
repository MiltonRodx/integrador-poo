package MiBiblioteca;

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
                    Libro libro = biblioteca.buscarLibro(tituloLibro);

                    if(socio != null && libro != null){

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

                    } else {

                        System.out.println("No se encontro el socio o el libro.");
                    }

                    break;

                case 5:

                    System.out.println("Titulo del libro:");
                    String tituloDevolucion = teclado.nextLine();

                    Libro libroDevolucion = biblioteca.buscarLibro(tituloDevolucion);

                    if(libroDevolucion != null){

                        Prestamo prestamoDevuelto = null;

                        ArrayList<Prestamo> prestamosLibro = libroDevolucion.getPrestamos();

                        for(Prestamo prestamo : prestamosLibro){

                            if(!prestamo.estaDevuelto()){

                                prestamoDevuelto = prestamo;
                            }
                        }

                        if(prestamoDevuelto != null){

                            try {

                                biblioteca.devolverLibro(libroDevolucion);

                                int filas = bibliotecaDAO.getPrestamoDAO()
                                    .actualizarDevolucion(prestamoDevuelto);

                                if(filas == 0){
                                    prestamoDevuelto.registrarFechaDevolucion(null);
                                    System.out.println(
                                        "No se encontro el prestamo activo en la base: "
                                        + "la devolucion no se registro."
                                    );
                                } else {
                                    System.out.println("Devolucion registrada correctamente.");
                                }

                            } catch (SQLException excepcionSQL) {

                                // La persistencia manda: si el UPDATE falla, se
                                // deshace la devolucion en memoria.
                                prestamoDevuelto.registrarFechaDevolucion(null);

                                System.out.println(
                                    "Error al guardar la devolucion en la base de datos: "
                                    + excepcionSQL.getMessage()
                                );

                            } catch (LibroNoPrestadoException excepcionNegocio) {

                                System.out.println(excepcionNegocio.getMessage());
                            }

                        } else {

                            System.out.println("El libro no tiene un prestamo activo.");
                        }

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

                    Libro libroConsulta = biblioteca.buscarLibro(tituloConsulta);

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
                        // no quedan desincronizadas.
                        // OJO: ajustar el nombre/firma al metodo real de tu SocioDAO.
                        bibliotecaDAO.getSocioDAO().eliminar(socioEncontrado);

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