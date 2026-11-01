package biblioteca;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Clase GestionBiblioteca (punto 2 del practico): instancia objetos de las clases
 * del diseño y usa los metodos de Biblioteca para responder las 4 preguntas del
 * enunciado y obtener los listados (formatos 1, 2 y 3).
 *
 * Escenario (fecha de ejemplo 31/10/2026; docentes nacen con 5 dias, estudiantes con 20):
 *  - Paenza    (estudiante): retiro el 05/10 y NO devolvio -> vencio el 25/10 -> prestamo VENCIDO activo.
 *  - Fernandez (docente)   : retiro el 10/10 y devolvio HOY  -> vencia el 15/10 -> devolvio TARDE -> NO responsable.
 *  - Perez     (docente)   : retiro el 27/10 y devolvio HOY  -> vencia el 01/11 -> devolvio a tiempo -> RESPONSABLE.
 *  - Perez     (docente)   : se lleva HOY "Programando con JAVA" (activo sano).
 *  - Milstein  (estudiante): se lleva HOY 3 libros (activo sano, al tope).
 *  - Alegre    (docente)   : nunca pidio libros -> RESPONSABLE.
 */
public class GestionBiblioteca {

    public static void main(String[] args) throws LibroNoPrestadoException {
        Biblioteca biblioteca = new Biblioteca("Biblioteca UNLP");

        // ------------------- Carga de libros -------------------
        biblioteca.nuevoLibro("Programando con JAVA", 2, "El Ateneo", 2014);        // 0
        biblioteca.nuevoLibro("Longman. Diccionario Pocket", 1, "Longman", 2010);   // 1
        biblioteca.nuevoLibro("Vivir para contarla", 1, "Debolsillo", 2003);        // 2
        biblioteca.nuevoLibro("JAVA. Como Programar", 3, "Pearson", 2016);          // 3
        biblioteca.nuevoLibro("JAVA. Como Programar", 1, "Pearson", 2006);          // 4 (2.o ejemplar)
        biblioteca.nuevoLibro("Longman. Diccionario Pocket", 2, "Longman", 2014);   // 5 (2.o ejemplar)

        // ------------------- Carga de socios -------------------
        biblioteca.nuevoSocioDocente(14524782, "Juan Perez", "Sistemas");
        biblioteca.nuevoSocioDocente(17982110, "Juan Fernandez", "Matematica");
        biblioteca.nuevoSocioDocente(10912002, "Maria Alegre", "Fisica");
        biblioteca.nuevoSocioEstudiante(28987498, "Francisco Paenza", "Fisica");
        biblioteca.nuevoSocioEstudiante(31987123, "Cesar Milstein", "Quimica");

        // Referencias que se usan en el escenario y en las preguntas
        Socio perez       = biblioteca.buscarSocio(14524782);
        Socio fernandez   = biblioteca.buscarSocio(17982110);
        Socio paenza      = biblioteca.buscarSocio(28987498);
        Socio milstein    = biblioteca.buscarSocio(31987123);
        Libro programandoConJava = biblioteca.getLibros().get(0);
        Libro longman2010 = biblioteca.getLibros().get(1);
        Libro contarla    = biblioteca.getLibros().get(2);
        Libro java2016    = biblioteca.getLibros().get(3);
        Libro java2006    = biblioteca.getLibros().get(4);
        Libro longman2014 = biblioteca.getLibros().get(5);

        // ------------------- Escenario de prestamos -------------------
        // Paenza retira el 05/10 y no devuelve: es su 3er prestamo VENCIDO activo.
        biblioteca.prestarLibro(fecha(2026, 10, 5), paenza, longman2014);

        // Fernandez retira el 10/10 y devuelve HOY: vencio el 15/10, devolvio TARDE.
        biblioteca.prestarLibro(fecha(2026, 10, 10), fernandez, longman2010);
        biblioteca.devolverLibro(longman2010);

        // Perez retira el 27/10 y devuelve HOY: vencia el 01/11, devolvio A TIEMPO.
        biblioteca.prestarLibro(fecha(2026, 10, 27), perez, java2006);
        biblioteca.devolverLibro(java2006);

        // Perez se lleva HOY "Programando con JAVA" (permite responder la pregunta 4).
        biblioteca.prestarLibro(fecha(2026, 10, 31), perez, programandoConJava);

        // Milstein se lleva HOY 3 libros distintos (activo sano, queda al tope del estudiante).
        biblioteca.prestarLibro(fecha(2026, 10, 31), milstein, longman2010);
        biblioteca.prestarLibro(fecha(2026, 10, 31), milstein, contarla);
        biblioteca.prestarLibro(fecha(2026, 10, 31), milstein, java2016);

        // Rechazos de prestarLibro (la biblioteca no crea el prestamo):
        System.out.println("-- Rechazos de prestamo --");
        System.out.println("Milstein (ya tiene 3 libros) intenta llevarse otro:   "
            + biblioteca.prestarLibro(fecha(2026, 10, 31), milstein, java2006));   // false: tope del estudiante
        System.out.println("Paenza (tiene un vencido) intenta llevarse otro:      "
            + biblioteca.prestarLibro(fecha(2026, 10, 31), paenza, java2006));     // false: prestamo vencido

        // Premio a la responsabilidad: Perez devolvio siempre a tiempo -> +5 dias de prestamo.
        Docente perezDocente = (Docente) perez;
        System.out.println();
        System.out.println("-- Premio a la responsabilidad --");
        System.out.println("Perez tenia " + perezDocente.getDiasPrestamos() + " dias de prestamo");
        perezDocente.cambiarDiasDePrestamos(5);
        System.out.println("Perez ahora tiene " + perezDocente.getDiasPrestamos() + " dias de prestamo");

        // ------------------- Informe: prestamos vencidos -------------------
        System.out.println("-- Prestamos vencidos a la fecha --");
        for (Prestamo unPrestamo : biblioteca.prestamosVencidos()) {
            System.out.println(unPrestamo + "\n");
        }

        // ------------------- Pregunta 1 -------------------
        System.out.println("1) Cantidad de socios de tipo Estudiante: "
            + biblioteca.cantidadDeSociosPorTipo("Estudiante"));

        // ------------------- Pregunta 2 -------------------
        System.out.println("2) Docentes que nunca adeudaron ni adeudan libros:");
        System.out.println(biblioteca.listaDeDocentesResponsables());

        // ------------------- Pregunta 3 -------------------
        System.out.println("3) Listas de libros y de socios de la biblioteca:");
        System.out.println(biblioteca.listaDeLibros());
        System.out.println(biblioteca.listaDeSocios());

        // ------------------- Pregunta 4 -------------------
        System.out.println("4) El libro \"Programando con JAVA\" lo tiene: "
            + biblioteca.quienTieneElLibro(programandoConJava));
    }

    /**
     * Fecha a nivel día (hora normalizada a medianoche), para que el escenario
     * no dependa de la hora a la que se ejecute el programa.
     */
    private static GregorianCalendar fecha(int p_anio, int p_mes, int p_dia) {
        GregorianCalendar fecha = new GregorianCalendar(p_anio, p_mes - 1, p_dia);
        fecha.set(Calendar.HOUR_OF_DAY, 0);
        fecha.set(Calendar.MINUTE, 0);
        fecha.set(Calendar.SECOND, 0);
        fecha.set(Calendar.MILLISECOND, 0);
        return fecha;
    }
}
