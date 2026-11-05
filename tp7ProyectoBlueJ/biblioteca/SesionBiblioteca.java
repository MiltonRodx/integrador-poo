package biblioteca;

import biblioteca.persistencia.BibliotecaDAO;
import biblioteca.persistencia.ConexionBD;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * Sesion de una biblioteca sobre la base SQLite: unifica abrir/cargar/siembra
 * y el guardado post-operacion, para que consola y GUI compartan la misma
 * logica de persistencia (una unica fuente de verdad del ciclo de vida).
 *
 * - Constructor: abre la BD y carga el estado; si la base es nueva, siembra
 *   el catalogo de la consigna solo si el modo lo indica (la GUI siembra
 *   directo; el menu de consola pregunta S/N antes).
 * - guardar(): persiste el estado actual (upsert por identidad). Toda
 *   operacion de escritura invoca guardar() inmediatamente despues. OJO: el
 *   upsert NO borra filas ausentes del grafo; las bajas van con
 *   getDao().delete() + FK (asi lo hacen menu y GUI).
 * - recargar(): reemplaza el estado en memoria por el guardado en la BD.
 * - suscribir/publicar: la GUI se entera de los cambios para refrescar vistas.
 */
public class SesionBiblioteca {

    private final ConexionBD conexionBD;
    private final BibliotecaDAO dao;
    private final String nombreInstitucion;
    private Biblioteca biblioteca;      // no final: recargar() reemplaza el estado
    private final List<Runnable> oyentes = new ArrayList<>();

    /** Modo GUI: si la base es nueva, siembra la consigna automaticamente. */
    public SesionBiblioteca(String p_rutaBD, String p_nombreInstitucion) {
        this(p_rutaBD, p_nombreInstitucion, true);
    }

    /** Modo consola: la siembra de una base nueva depende de la respuesta S/N del menu. */
    public SesionBiblioteca(String p_rutaBD, String p_nombreInstitucion, boolean p_sembrarSiNueva) {
        this.conexionBD = ConexionBD.instanciar(p_rutaBD);
        this.dao = new BibliotecaDAO(this.conexionBD);
        this.nombreInstitucion = p_nombreInstitucion;

        if (this.dao.existeBiblioteca(p_nombreInstitucion)) {
            this.biblioteca = this.dao.cargar(p_nombreInstitucion);
        } else {
            this.biblioteca = new Biblioteca(p_nombreInstitucion);
            if (p_sembrarSiNueva) {
                sembrarCatalogoDeLaConsigna(this.biblioteca);
                this.guardar();
            }
        }
    }

    /**
     * Estado actual de la sesion: el agregado que se opera y persiste.
     *
     * @return la biblioteca cargada de la base (o nueva/sembrada)
     */
    public Biblioteca getBiblioteca() { return this.biblioteca; }

    /** Acceso a los DAOs para las bajas (fk: DELETE explicito, igual que el menu). */
    public BibliotecaDAO getDao() { return this.dao; }

    /** Persistencia inmediata tras cada operacion de escritura. */
    public void guardar() { this.dao.guardar(this.biblioteca); }

    /** Reemplaza el estado en memoria por el persistido en la BD. */
    public Biblioteca recargar() {
        this.biblioteca = this.dao.cargar(this.nombreInstitucion);
        this.publicar();
        return this.biblioteca;
    }

    /** Cierra la base (al cortar la aplicacion). */
    public void cerrar() { this.conexionBD.cerrar(); }

    /** Consulta si ya existe el registro institucional en esa base (sin abrir sesion). */
    public static boolean existeBibliotecaEn(String p_rutaBD, String p_clave) {
        ConexionBD conexion = ConexionBD.instanciar(p_rutaBD);
        return new BibliotecaDAO(conexion).existeBiblioteca(p_clave);
    }

    // -------------------
    // Refresco de vistas GUI
    // -------------------

    /** Registra una callback (tabla/combo que debe recargarse ante cambios). */
    public void suscribir(Runnable p_oyente) { this.oyentes.add(p_oyente); }

    /** Notifica a las vistas registradas que el estado cambio. */
    public void publicar() { this.oyentes.forEach(Runnable::run); }

    /**
     * Siembra el escenario de la consigna sobre una biblioteca nueva:
     * mismo catalogo y mismo historial historico (vencidos, devueltos, topes).
     */
    public static void sembrarCatalogoDeLaConsigna(Biblioteca p_biblioteca) {
        p_biblioteca.nuevoLibro("Programando con JAVA", 2, "El Ateneo", 2014);
        p_biblioteca.nuevoLibro("Longman. Diccionario Pocket", 1, "Longman", 2010);
        p_biblioteca.nuevoLibro("Vivir para contarla", 1, "Debolsillo", 2003);
        p_biblioteca.nuevoLibro("JAVA. Como Programar", 3, "Pearson", 2016);
        p_biblioteca.nuevoLibro("JAVA. Como Programar", 1, "Pearson", 2006);
        p_biblioteca.nuevoLibro("Longman. Diccionario Pocket", 2, "Longman", 2014);

        p_biblioteca.nuevoSocioDocente(14524782, "Juan Perez", "Sistemas");
        p_biblioteca.nuevoSocioDocente(17982110, "Juan Fernandez", "Matematica");
        p_biblioteca.nuevoSocioDocente(10912002, "Maria Alegre", "Fisica");
        p_biblioteca.nuevoSocioEstudiante(28987498, "Francisco Paenza", "Fisica");
        p_biblioteca.nuevoSocioEstudiante(31987123, "Cesar Milstein", "Quimica");

        Socio perez     = p_biblioteca.buscarSocio(14524782);
        Socio fernandez = p_biblioteca.buscarSocio(17982110);
        Socio paenza    = p_biblioteca.buscarSocio(28987498);
        Socio milstein  = p_biblioteca.buscarSocio(31987123);

        // Paenza retiro el 05/10 y NUNCA devuelve -> prestamo vencido y activo.
        p_biblioteca.prestarLibro(fecha(2026, 10, 5), paenza, p_biblioteca.getLibros().get(5));

        // Fernandez devolvio TARDE (retiro 10/10, vencia 15/10) -> no responsable.
        p_biblioteca.prestarLibro(fecha(2026, 10, 10), fernandez, p_biblioteca.getLibros().get(1));
        devolverCallado(p_biblioteca, p_biblioteca.getLibros().get(1));

        // Perez devolvio A TIEMPO (retiro 27/10, vencia 01/11) -> si responsable.
        p_biblioteca.prestarLibro(fecha(2026, 10, 27), perez, p_biblioteca.getLibros().get(4));
        devolverCallado(p_biblioteca, p_biblioteca.getLibros().get(4));

        // Prestamos activos sanos de hoy: Perez 1, Milstein al tope (3).
        p_biblioteca.prestarLibro(hoy(), perez, p_biblioteca.getLibros().get(0));
        p_biblioteca.prestarLibro(hoy(), milstein, p_biblioteca.getLibros().get(1));
        p_biblioteca.prestarLibro(hoy(), milstein, p_biblioteca.getLibros().get(2));
        p_biblioteca.prestarLibro(hoy(), milstein, p_biblioteca.getLibros().get(3));
    }

    /** Devolucion tolerante (ignora la excepcion: en la siembra siempre hay prestamo activo). */
    private static void devolverCallado(Biblioteca p_biblioteca, Libro p_libro) {
        try {
            p_biblioteca.devolverLibro(p_libro);
        } catch (LibroNoPrestadoException e) {
            // no deberia ocurrir en la siembra; se ignora a proposito
        }
    }

    /**
     * Fecha a medianoche: mismo criterio de dia completo que usan las reglas
     * del negocio (publica: el menu de consola la usa el input "dia mes anio").
     */
    public static GregorianCalendar fecha(int p_anio, int p_mes, int p_dia) {
        GregorianCalendar fechaCal = new GregorianCalendar(p_anio, p_mes - 1, p_dia);
        fechaCal.set(GregorianCalendar.HOUR_OF_DAY, 0);
        fechaCal.set(GregorianCalendar.MINUTE, 0);
        fechaCal.set(GregorianCalendar.SECOND, 0);
        fechaCal.set(GregorianCalendar.MILLISECOND, 0);
        return fechaCal;
    }

    /** Hoy a medianoche (publica: el menu de consola reutiliza para "Enter=hoy"). */
    public static GregorianCalendar hoy() {
        GregorianCalendar fechaHoy = new GregorianCalendar();
        fechaHoy.set(GregorianCalendar.HOUR_OF_DAY, 0);
        fechaHoy.set(GregorianCalendar.MINUTE, 0);
        fechaHoy.set(GregorianCalendar.SECOND, 0);
        fechaHoy.set(GregorianCalendar.MILLISECOND, 0);
        return fechaHoy;
    }
}
