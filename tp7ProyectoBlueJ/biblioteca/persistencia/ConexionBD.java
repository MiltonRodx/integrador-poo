package biblioteca.persistencia;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Conexion unica a la base SQLite (patron Singleton perezoso).
 *
 * - Encapsula el driver JDBC de SQLite (org.sqlite.JDBC, jar en tp7ProyectoBlueJ/libs).
 * - Crea el esquema si la base esta vacia (DDL auto-migrado, ver esquema.sql).
 * - Entrega una unica Connection que los DAOs comparten; SQLite la serializa.
 *
 * Rutas relativas: la base se crea respecto del directorio de trabajo actual
 * (ejecutar desde tp7ProyectoBlueJ para obtener tp7ProyectoBlueJ/biblioteca.db).
 */
public class ConexionBD {

    /**
     * DDL centralizado: una unica fuente de verdad del esquema (ver sql/esquema.sql).
     *
     * Decisiones de diseño que viven en la base:
     *  - libro y prestamo usan id AUTOINCREMENT (identidad surrogate);
     *    socio usa su DNI como clave natural (ya es unica en el dominio).
     *  - socio es tabla plana: Docentes y Estudiantes comparten la tabla y
     *    se distinguen por la columna tipo (patron single-table inheritance);
     *    area/carrera quedan NULL segun el tipo (las CHECK protege el literal).
     *  - las fechas van como epoch millis a medianoche (evaluacion a nivel dia).
     *  - el indice parcial unico impide DOS prestamos ACTIVOS del mismo
     *    ejemplar: es la regla de negocio critica reforzada a nivel base.
     */
    private static final String DDL = """
         CREATE TABLE IF NOT EXISTS biblioteca (
            nombre TEXT PRIMARY KEY
         );

         CREATE TABLE IF NOT EXISTS libro (
            id        INTEGER PRIMARY KEY AUTOINCREMENT,
            titulo    TEXT    NOT NULL,
            edicion   INTEGER NOT NULL,
            editorial TEXT    NOT NULL,
            anio      INTEGER NOT NULL
         );

         CREATE TABLE IF NOT EXISTS socio (
            dni            INTEGER PRIMARY KEY,
            nombre         TEXT    NOT NULL,
            tipo           TEXT    NOT NULL CHECK (tipo IN ('Docente', 'Estudiante')),
            dias_prestamos INTEGER NOT NULL,
            area           TEXT,
            carrera        TEXT
         );

         CREATE TABLE IF NOT EXISTS prestamo (
            id               INTEGER PRIMARY KEY AUTOINCREMENT,
            socio_dni        INTEGER NOT NULL REFERENCES socio (dni),
            libro_id         INTEGER NOT NULL REFERENCES libro (id),
            fecha_retiro     INTEGER NOT NULL, -- epoch millis
            fecha_devolucion INTEGER            -- epoch millis, NULL = activo
         );

         -- Regla a nivel base: un mismo ejemplar no puede tener mas de un prestamo activo.
         CREATE UNIQUE INDEX IF NOT EXISTS idx_libro_un_prestamo_activo
            ON prestamo (libro_id)
            WHERE fecha_devolucion IS NULL;
        """;

    // -------------------
    // Atributos
    // -------------------
    private static ConexionBD referencia;
    private final Connection conexion;

    // -------------------
    // Constructores
    // -------------------
    private ConexionBD(String p_rutaBD) {
        try {
            // Carga explicita del driver; en Java moderno es opcional, aqui queda visible en consola.
            Class.forName("org.sqlite.JDBC");
            this.conexion = DriverManager.getConnection("jdbc:sqlite:" + p_rutaBD);

            try (Statement stmt = this.conexion.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.executeUpdate(DDL);
            }
        } catch (ClassNotFoundException e) {
            throw new DAOException("Falta el driver SQLite en el classpath (org.sqlite.JDBC)."
                    + " En BlueJ: Tools -> Preferences -> Libraries -> Add, elegir libs/sqlite-jdbc-...jar"
                    + " y reiniciar BlueJ. Por consola: agregar el jar al -cp con ';'.", e);
        } catch (SQLException e) {
            throw new DAOException("No se pudo abrir la base de datos: " + p_rutaBD, e);
        }
        System.out.println("[BD] Conexion SQLite lista: " + p_rutaBD);
    }

    // -------------------
    // Metodos especificos.
    // -------------------

    /**
     * Abre la base la PRIMERA vez (conexion + esquema en una pasada).
     *
     * Si ya habia una referencia activa devuelve esa (el singleton perezoso
     * ignora la ruta nueva). synchronized: protege el arranque en multiples
     * hilos de la GUI.
     *
     * @param p_rutaBD ruta del archivo SQLite (relativa al workdir actual)
     * @return la conexion unica
     * @throws DAOException si falta el driver o falla la apertura/conexion
     */
    public static synchronized ConexionBD instanciar(String p_rutaBD) {
        if (referencia == null) {
            referencia = new ConexionBD(p_rutaBD);
        }
        return referencia;
    }

    /**
     * Devuelve la referencia ya instanciada sin abrir nada (llamadas posteriores).
     *
     * @return la conexion unica activa
     * @throws DAOException si nunca se llamo a instanciar(ruta) en esta JVM
     */
    public static ConexionBD getInstancia() {
        if (referencia == null) {
            throw new DAOException("La conexion no fue instanciada: llamar antes a instanciar(ruta).");
        }
        return referencia;
    }

    /** Entrega la conexion JDBC unica que comparten todos los DAOs. */
    public Connection get() { return this.conexion; }

    /**
     * Cierra la conexion SQLite (fin de la aplicacion o de la demo).
     *
     * Resetea la referencia del singleton, lo que permite volver a llamar
     * instanciar(ruta) en la misma JVM (asi lo usa la GUI para liberar en
     * stop() y la prueba del CRUD que re-abre).
     *
     * @throws DAOException si el close() de JDBC falla
     */
    public void cerrar() {
        try {
            this.conexion.close();
            // Singleton: deja la referencia en null para permitir una proxima
            // instanciar(ruta) en la misma JVM (resetea el singleton perezoso).
            referencia = null;
            System.out.println("[BD] Conexion cerrada.");
        } catch (SQLException e) {
            throw new DAOException("No se pudo cerrar la conexion SQLite", e);
        }
    }
}
