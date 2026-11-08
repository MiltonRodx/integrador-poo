
package MiBiblioteca;

import java.util.Calendar;
import java.util.ArrayList;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

// CONSOLA del practico: este main es solo texto (consigna).
// Para la interfaz grafica esta VentanaPrincipal.main().
public class GestionBiblioteca {
    public static void main(String[] args)
        throws SQLException, ParseException, LibroNoPrestadoException {

        // Todo el programa empieza aca
        Scanner teclado = new Scanner(System.in);

        ConexionBD conexionBD = new ConexionBD("biblioteca.db");

        BibliotecaDAO bibliotecaDAO = new BibliotecaDAO(conexionBD);
        Biblioteca biblioteca = bibliotecaDAO.cargarBiblioteca("Biblioteca Gestion FACENA-UNNE");
        bibliotecaDAO.crearTablas();

        int respuesta = 0;

        while(respuesta != 13){
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
            System.out.println("13. Salir");
            System.out.println("Ingrese una opcion:");

            respuesta = teclado.nextInt();
            teclado.nextLine();

            switch(respuesta){
                
                case 1:
                    System.out.println("Titulo:");
                    String titulo = teclado.nextLine();

                    System.out.println("Edicion:");
                    int edicion = teclado.nextInt();
                    teclado.nextLine();

                    System.out.println("Editorial:");
                    String editorial = teclado.nextLine();

                    System.out.println("Anio:");
                    int anio = teclado.nextInt();
                    teclado.nextLine();

                    biblioteca.nuevoLibro(titulo, edicion, editorial, anio);

                    Libro nuevoLibro = biblioteca.getLibros().get(
                        biblioteca.getLibros().size() - 1
                    );

                    bibliotecaDAO.getLibroDAO().agregar(nuevoLibro);

                    System.out.println("Libro agregado correctamente.");

                    break;

                case 2:
                    System.out.println("D.N.I.:");
                    int dniDocente = teclado.nextInt();
                    teclado.nextLine();

                    System.out.println("Nombre y apellido:");
                    String nombreDocente = teclado.nextLine();

                    System.out.println("Area:");
                    String area = teclado.nextLine();

                    biblioteca.nuevoSocioDocente(dniDocente, nombreDocente, area);

                    Socio nuevoDocente = biblioteca.getSocios().get(
                        biblioteca.getSocios().size() - 1
                    );

                    bibliotecaDAO.getSocioDAO().agregar(nuevoDocente);

                    System.out.println("Docente agregado correctamente.");

                    break;

                case 3:
                    System.out.println("D.N.I.:");
                    int dniEstudiante = teclado.nextInt();
                    teclado.nextLine();

                    System.out.println("Nombre y apellido:");
                    String nombreEstudiante = teclado.nextLine();

                    System.out.println("Carrera:");
                    String carrera = teclado.nextLine();

                   biblioteca.nuevoSocioEstudiante(dniEstudiante, nombreEstudiante, carrera);
                   
                    Socio nuevoEstudiante = biblioteca.getSocios().get(
                        biblioteca.getSocios().size() - 1
                    );

                    bibliotecaDAO.getSocioDAO().agregar(nuevoEstudiante);

                    System.out.println("Estudiante agregado correctamente.");

                    break;

                case 4:

                    System.out.println("D.N.I. del socio:");
                    int dniSocio = teclado.nextInt();
                    teclado.nextLine();

                    System.out.println("Titulo del libro:");
                    String tituloLibro = teclado.nextLine();

                    System.out.println("Asignar dias de prestamos (0 = hoy):");
                    int diasTranscurridos = teclado.nextInt();
                    teclado.nextLine();

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
                    System.out.println("Saliendo del sistema...");
                    break;

                default:
                    System.out.println("Opcion incorrecta.");
                    break;
            }
        }

        teclado.close();
        conexionBD.cerrar();

        System.out.println("Programa finalizado.");
    }
}
