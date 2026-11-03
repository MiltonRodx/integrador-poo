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

    /** DDL centralizado aca: una unica fuente de verdad del esquema (ver sql/esquema.sql). */
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

    /** Primera llamada: abre la base (y migra el esquema) en la ruta dada. */
    public static synchronized ConexionBD instanciar(String p_rutaBD) {
        if (referencia == null) {
            referencia = new ConexionBD(p_rutaBD);
        }
        return referencia;
    }

    /** Llamadas posteriores: devuelve la conexion ya abierta. */
    public static ConexionBD getInstancia() {
        if (referencia == null) {
            throw new DAOException("La conexion no fue instanciada: llamar antes a instanciar(ruta).");
        }
        return referencia;
    }

    public Connection get() { return this.conexion; }

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
