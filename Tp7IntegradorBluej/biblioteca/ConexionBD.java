package biblioteca;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * ConexionBD: punto de acceso a la base SQLite del sistema.
 *
 * Aplica la idea del patron Singleton a la conexion: guarda un unico
 * Connection y lo reutiliza. Mientras siga abierta, conectar() devuelve
 * siempre la misma, sin abrir una nueva en cada consulta. Asi se evita
 * el costo de abrir/cerrar la base y se mantiene un solo estado
 * (por ejemplo, PRAGMA foreign_keys = ON, que se activa una sola vez).
 *
 * Limitacion: todavia NO es un Singleton "puro". El constructor es publico
 * y no hay un atributo static con la instancia, asi que se pueden crear
 * varias ConexionBD, cada una con su propio Connection.
 *
 * Para completarlo:
 *   1. Agregar: private static ConexionBD instancia;
 *   2. Pasar el constructor a private.
 *   3. Agregar: public static ConexionBD getInstancia(...), que cree la
 *      instancia la primera vez y despues devuelva siempre la misma.
 */
public class ConexionBD {
    // Atributos
    // Ruta del archivo .db de SQLite (lo que va despues de "jdbc:sqlite:").
    private String rutaBaseDatos;
    // La conexion "unica" que administra esta instancia: se crea una vez en
    // conectar() y se reutiliza mientras siga abierta (idea de Singleton).
    private Connection conexion; // ACÁ GUARDAMOS LA CONEXIÓN

    // Constructor
    // Guarda la ruta y deja la conexion en null (todavia no se abrio la base).
    // Es una inicializacion perezosa: el objeto se puede crear sin tocar el
    // disco; la base se abre recien cuando alguien llama a conectar().
    public ConexionBD(String p_rutaBaseDatos) {
        this.setRutaBaseDatos(p_rutaBaseDatos);
        this.conexion = null;
    }

    // Accessors
    // Setter privado: la ruta solo se cambia desde adentro de la clase.
    private void setRutaBaseDatos(String p_rutaBaseDatos) { this.rutaBaseDatos = p_rutaBaseDatos; }

    // Devuelve la ruta del archivo de la base de datos.
    public String getRutaBaseDatos() { return this.rutaBaseDatos; }
    
    // Setter privado: solo la propia clase decide cuando reemplazar la conexion.
    private void setConexion(Connection p_conexion) { this.conexion = p_conexion; }
    
    // Devuelve la conexion actual SIN abrirla: puede ser null si todavia
    // nadie llamo a conectar(). Para usar la base, lo correcto es conectar().
    public Connection getConexion() { return this.conexion; }
    
    // Metodos
    // Método para conectar (si ya está conectado, devuelve la misma)
    // Aca esta el corazon de la idea de Singleton: la conexion se crea una
    // sola vez y se reutiliza. Solo se abre una Connection nueva si todavia
    // no existe (null) o si la anterior ya fue cerrada.
    public Connection conectar() throws SQLException {
        if (this.conexion == null || this.conexion.isClosed()) {
            try {
                // Registro explicito del driver JDBC de SQLite: si el jar no
                // esta en el classpath (por ejemplo BlueJ abierto en la
                // subcarpeta biblioteca, que no ve la carpeta +libs del
                // proyecto) falla aca con un mensaje claro y no con un
                // "No suitable driver" criptico.
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException error) {
                throw new SQLException(
                    "No se encontro el driver de SQLite (sqlite-jdbc). "
                    + "Agregue el jar al classpath: en BlueJ abra el proyecto "
                    + "Tp7IntegradorBluej, que tiene el driver en +libs.",
                    error
                );
            }

            // Se guarda la conexion en el atributo para reutilizarla despues:
            // este es el "unico" objeto Connection que administra la clase.
            this.conexion = DriverManager.getConnection(
                "jdbc:sqlite:" + this.getRutaBaseDatos()
            );

            // Usamos try-with-resources para el Statement así se cierra solo
            // PRAGMA foreign_keys = ON se ejecuta una sola vez por conexion:
            // como la conexion se reutiliza, la configuracion se mantiene.
            try (Statement sentencia = this.conexion.createStatement()) {
                sentencia.execute("PRAGMA foreign_keys = ON");
            }
        }
        
        // Se devuelve siempre el mismo objeto Connection (recien creado o el
        // que ya estaba abierto), nunca una copia.
        return this.getConexion();
    }

    // Cierra la conexion si esta abierta. No la pone en null: el atributo
    // queda apuntando a una Connection cerrada y conectar() lo detecta con
    // isClosed() para abrir una nueva cuando haga falta.
    public void cerrar() throws SQLException {
        if (this.getConexion() != null && !this.getConexion().isClosed()) {
            this.getConexion().close();
        }
    }

    // Ejecuta un trabajo dentro de una transaccion: todo o nada.
    // Si el trabajo termina bien se hace COMMIT; si algo explota a mitad,
    // se hace ROLLBACK y el error sube para que quien llamo decida que
    // mostrar. Al terminar (salga como salga) la conexion queda con el
    // autoCommit como estaba, lista para el resto de la app.
    //
    // Nota: se guarda la conexion en una variable local para que, aunque
    // el atributo cambie, toda la transaccion use el mismo objeto. Se usa
    // la conexion compartida (la de conectar()), no una nueva.
    public void enTransaccion(TrabajoBD p_trabajo) throws SQLException {
        Connection conexion = this.conectar();

        try {
            // Se desactiva el autoCommit para manejar el COMMIT/ROLLBACK a mano.
            conexion.setAutoCommit(false);

            try {
                p_trabajo.ejecutar(conexion);
                // Si todo salio bien, se confirman los cambios de una vez.
                conexion.commit();
            }
            catch (SQLException error) {
                // Si algo fallo, se deshacen todos los cambios del bloque
                // y se relanza el error original para no ocultarlo.
                conexion.rollback();
                throw error;
            }
        }
        finally {
            // Pase lo que pase (bien o mal) se restaura el autoCommit para
            // que la conexion compartida quede usable por el resto de la app.
            try {
                conexion.setAutoCommit(true);
            }
            catch (SQLException ignorado) {
                // Si ya no se puede reconfigurar la conexion, poco se puede
                // hacer aca: el error real ya esta subiendo al llamador.
            }
        }
    }
}