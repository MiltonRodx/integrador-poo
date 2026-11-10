package biblioteca;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.sql.SQLException;

public class CargadorDeDatosIniciales {

    private Biblioteca biblioteca;
    private BibliotecaDAO bibliotecaDAO;

    // Recibe todo por constructor
    public CargadorDeDatosIniciales(Biblioteca p_biblioteca, BibliotecaDAO p_bibliotecaDAO) {
        this.biblioteca = p_biblioteca;
        this.bibliotecaDAO = p_bibliotecaDAO;
    }

    // Método principal de esta clase
    public void cargarDatosDePrueba() throws SQLException, LibroNoPrestadoException {
        
        // Carga de libros
        this.biblioteca.nuevoLibro("Programando con JAVA", 2, "El Ateneo", 2014);
        this.biblioteca.nuevoLibro("Longman. Diccionario Pocket", 1, "Longman", 2010);
        this.biblioteca.nuevoLibro("Vivir para contarla", 1, "Debolsillo", 2003);
        this.biblioteca.nuevoLibro("JAVA. Como Programar", 3, "Pearson", 2016);
        this.biblioteca.nuevoLibro("JAVA. Como Programar", 1, "Pearson", 2006);
        this.biblioteca.nuevoLibro("Longman. Diccionario Pocket", 2, "Longman", 2014);

        // Guarda los libros en la BD
        for(Libro libro : this.biblioteca.getLibros()){
            this.bibliotecaDAO.getLibroDAO().agregar(libro);
        }

        // Carga de socios
        // Carga de socios
        this.biblioteca.nuevoSocioDocente(10912002, "Maria Alegre", "Fisica");
        this.biblioteca.nuevoSocioDocente(14524782, "Juan Perez", "Sistemas");
        this.biblioteca.nuevoSocioDocente(17982110, "Juan Fernandez", "Matematica");

        this.biblioteca.nuevoSocioEstudiante(28987498, "Francisco Paenza", "Fisica");
        this.biblioteca.nuevoSocioEstudiante(31987123, "Cesar Milstein", "Quimica");

        // Guarda los socios en la BD
        for(Socio socio : this.biblioteca.getSocios()){
        this.bibliotecaDAO.getSocioDAO().agregar(socio);
        }

        // Busqueda de socios
        Socio maria = this.biblioteca.buscarSocio(10912002);
        Socio perez = this.biblioteca.buscarSocio(14524782);
        Socio fernandez = this.biblioteca.buscarSocio(17982110);
        Socio paenza = this.biblioteca.buscarSocio(28987498);
        Socio milstein = this.biblioteca.buscarSocio(31987123);

        // Busqueda de libros
        ArrayList<Libro> libros = this.biblioteca.getLibros();

        // Carga de prestamos
        this.registrarPrestamo(this.fecha(2026, 11, 3), null, perez, libros.get(0));
        this.registrarPrestamo(this.fecha(2026, 10, 10), this.fecha(2026, 11, 3), fernandez, libros.get(1));
        this.registrarPrestamo(this.fecha(2026, 11, 3), null, milstein, libros.get(1));
        this.registrarPrestamo(this.fecha(2026, 11, 3), null, milstein, libros.get(2));
        this.registrarPrestamo(this.fecha(2026, 11, 3), null, milstein, libros.get(3));
        this.registrarPrestamo(this.fecha(2026, 10, 27), this.fecha(2026, 11, 3), perez, libros.get(4));
        this.registrarPrestamo(this.fecha(2026, 10, 5), null, paenza, libros.get(5));
        this.registrarPrestamo(this.fecha(2026, 11, 3), null, maria, libros.get(4));
    }

    private void registrarPrestamo(Calendar p_fechaRetiro, Calendar p_fechaDevolucion, Socio p_socio, Libro p_libro) throws SQLException, LibroNoPrestadoException {
        // Retira siguiendo la regla de la consigna: se autoriza el préstamo
        boolean prestamoRealizado = this.biblioteca.prestarLibro(p_fechaRetiro, p_socio, p_libro);

        if(prestamoRealizado){
            Prestamo nuevoPrestamo = p_libro.ultimoPrestamo();

            // Si el préstamo fue devuelto antes de cargarse, se asienta la fecha actual.
            if(p_fechaDevolucion != null){
                nuevoPrestamo.registrarFechaDevolucion(p_fechaDevolucion);
            }

            this.bibliotecaDAO.getPrestamoDAO().agregar(nuevoPrestamo);
        }
    }

    private Calendar fecha(int p_anio, int p_mes, int p_dia){
        Calendar fecha = new GregorianCalendar(p_anio, p_mes - 1, p_dia);
        fecha.set(Calendar.HOUR_OF_DAY, 0);
        fecha.set(Calendar.MINUTE, 0);
        fecha.set(Calendar.SECOND, 0);
        fecha.set(Calendar.MILLISECOND, 0);
        return fecha;
    }
}