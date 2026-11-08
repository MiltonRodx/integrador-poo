
package MiBiblioteca;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.text.ParseException;

public class BibliotecaDAO
{
    private ConexionBD conexionBD;
    private LibroDAO libroDAO;
    private SocioDAO socioDAO;
    private PrestamoDAO prestamoDAO;

    public BibliotecaDAO(ConexionBD p_conexionBD) {
        this.setConexionBD(p_conexionBD);
        this.setLibroDAO(new LibroDAO(p_conexionBD));
        this.setSocioDAO(new SocioDAO(p_conexionBD));
        this.setPrestamoDAO(new PrestamoDAO(p_conexionBD, this.getLibroDAO()));
    }

    private void setConexionBD(ConexionBD p_conexionBD) {
        this.conexionBD = p_conexionBD;
    }

    private void setLibroDAO(LibroDAO p_libroDAO) { this.libroDAO = p_libroDAO; }

    private void setSocioDAO(SocioDAO p_socioDAO) { this.socioDAO = p_socioDAO; }

    private void setPrestamoDAO(PrestamoDAO p_prestamoDAO) { this.prestamoDAO = p_prestamoDAO; }

    public ConexionBD getConexionBD() { return this.conexionBD; }

    public LibroDAO getLibroDAO() { return this.libroDAO; }

    public SocioDAO getSocioDAO() { return this.socioDAO; }

    public PrestamoDAO getPrestamoDAO() { return this.prestamoDAO; }

    public void crearTablas() throws SQLException
    {
        Connection conexion = this.getConexionBD().conectar();
        Statement sentencia = conexion.createStatement();

        sentencia.execute(
            "CREATE TABLE IF NOT EXISTS Libro (" +
            "idLibro INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "titulo TEXT NOT NULL, " +
            "edicion INTEGER, " +
            "editorial TEXT, " +
            "anio INTEGER)"
        );

        sentencia.execute(
            "CREATE TABLE IF NOT EXISTS Socio (" +
            "dniSocio INTEGER PRIMARY KEY, " +
            "nombre TEXT NOT NULL, " +
            "diasPrestamo INTEGER, " +
            "tipo TEXT NOT NULL, " +
            "area TEXT, " +
            "carrera TEXT)"
        );

        sentencia.execute(
            "CREATE TABLE IF NOT EXISTS Prestamo (" +
            "idPrestamo INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "fechaRetiro TEXT NOT NULL, " +
            "fechaDevolucion TEXT, " +
            "dniSocio INTEGER NOT NULL, " +
            "idLibro INTEGER NOT NULL, " +
            "diasPrestamo INTEGER, " +
            "FOREIGN KEY (dniSocio) REFERENCES Socio(dniSocio), " +
            "FOREIGN KEY (idLibro) REFERENCES Libro(idLibro))"
        );

        boolean existeColumna = false;

        ResultSet resultado = sentencia.executeQuery(
            "PRAGMA table_info(Prestamo)"
        );

        while(resultado.next()) {
            if(resultado.getString("name").equals("diasPrestamo"))
            {
                existeColumna = true;
            }
        }

        resultado.close();

        if(!existeColumna) {
            sentencia.execute(
                "ALTER TABLE Prestamo ADD COLUMN diasPrestamo INTEGER"
            );
        }

        sentencia.execute(
            "UPDATE Prestamo SET diasPrestamo = " +
            "(SELECT diasPrestamo FROM Socio " +
            "WHERE Socio.dniSocio = Prestamo.dniSocio) " +
            "WHERE diasPrestamo IS NULL"
        );

        // Antes de crear el indice unico, reparamos datos viejos que ya
        // se guardaron mal (un libro con varios prestamos activos a la vez,
        // bug de versiones anteriores de la app). Sin esta reparacion el
        // indice no se puede crear sobre una base que ya trae ese problema.
        this.repararPrestamosDuplicados(sentencia);

        try {
            // Regla de integridad: un libro no puede tener mas de UN
            // préstamo activo a la vez. El índice es parcial: solo aplica
            // a filas con fechaDevolucion NULL (es decir, sin devolver).
            // IF NOT EXISTS hace que la migración sea idempotente.
            sentencia.execute(
                "CREATE UNIQUE INDEX IF NOT EXISTS idx_prestamo_activo_unico " +
                "ON Prestamo (idLibro) " +
                "WHERE fechaDevolucion IS NULL"
            );
        } catch(SQLException error) {
            // Si la base ya trae datos viejos con mas de un prestamo activo
            // para el mismo libro, el índice no se puede crear. Se avisa
            // pero la app sigue: la migración jamás rompe los datos que
            // ya estaban guardados.
            System.out.println(
                "[Aviso] No se pudo crear el indice de prestamo activo unico: " +
                error.getMessage()
            );

            // Detalle de los datos en conflicto, para que se pueda limpiar
            // la base a mano (libros con mas de un prestamo activo):
            ResultSet conflicto = null;

            try
            {
                conflicto = sentencia.executeQuery(
                    "SELECT L.titulo, count(*) AS cantidad " +
                    "FROM Prestamo P " +
                    "JOIN Libro L ON L.idLibro = P.idLibro " +
                    "WHERE P.fechaDevolucion IS NULL " +
                    "GROUP BY P.idLibro " +
                    "HAVING count(*) > 1"
                );

                while(conflicto.next())
                {
                    System.out.println(
                        "  - El libro \"" + conflicto.getString("titulo") +
                        "\" tiene " + conflicto.getInt("cantidad") +
                        " prestamos activos a la vez"
                    );
                }
            }
            catch(SQLException ignorado)
            {
                // el aviso principal ya salio; el detalle es un extra
            }
            finally
            {
                if(conflicto != null)
                {
                    try { conflicto.close(); }
                    catch(SQLException ignorado) { }
                }
            }
        }

        sentencia.close();
        conexion.close();
    }

    // Repara datos viejos inconsistentes: si un libro tiene varios
    // prestamos activos al mismo tiempo (no deberia pasarle a ninguna
    // biblioteca real), el/los sobrantes se dan por devueltos la fecha en
    // que arranco el prestamo activo siguiente. El mas nuevo de cada libro
    // queda activo y ningun dato se borra. Es idempotente.
    private void repararPrestamosDuplicados(Statement p_sentencia)
        throws SQLException
    {
        ResultSet conflicto = null;
        int eventos = 0;

        try
        {
            // ?????????????????????????????????????????????????????????? wtf
            conflicto = p_sentencia.executeQuery(
                "SELECT P.idPrestamo, L.titulo, S.nombre, P.fechaRetiro, " +
                "(" +
                " SELECT MIN(P2.fechaRetiro) FROM Prestamo P2" +
                " WHERE P2.idLibro = P.idLibro" +
                " AND P2.fechaDevolucion IS NULL" +
                " AND (P2.fechaRetiro > P.fechaRetiro" +
                "   OR (P2.fechaRetiro = P.fechaRetiro" +
                "     AND P2.idPrestamo > P.idPrestamo))" +
                ") AS fechaCierre " +
                "FROM Prestamo P " +
                "JOIN Libro L ON L.idLibro = P.idLibro " +
                "LEFT JOIN Socio S ON S.dniSocio = P.dniSocio " +
                "WHERE P.fechaDevolucion IS NULL " +
                "  AND (" +
                "   SELECT count(*) FROM Prestamo P3" +
                "   WHERE P3.idLibro = P.idLibro" +
                "   AND P3.fechaDevolucion IS NULL" +
                "   AND (P3.fechaRetiro > P.fechaRetiro" +
                "     OR (P3.fechaRetiro = P.fechaRetiro" +
                "       AND P3.idPrestamo > P.idPrestamo))" +
                "  ) > 0 " +
                "ORDER BY P.idLibro, P.fechaRetiro"
            );

            while(conflicto.next())
            {
                eventos++;

                System.out.println(
                    "[Reparacion] El prestamo de \"" +
                    conflicto.getString("titulo") + "\" (" +
                    conflicto.getString("nombre") + ", retiro " +
                    conflicto.getString("fechaRetiro") +
                    ") quedo duplicado en datos viejos: " +
                    "se cierra con devolucion al " +
                    conflicto.getString("fechaCierre") + "."
                );
            }
        }
        finally
        {
            if(conflicto != null)
            {
                conflicto.close();
            }
        }

        if(eventos == 0)
        {
            return;
        }

        p_sentencia.executeUpdate(
            "UPDATE Prestamo SET fechaDevolucion = (" +
            " SELECT MIN(P2.fechaRetiro) FROM Prestamo P2" +
            " WHERE P2.idLibro = Prestamo.idLibro" +
            " AND P2.fechaDevolucion IS NULL" +
            " AND (P2.fechaRetiro > Prestamo.fechaRetiro" +
            "   OR (P2.fechaRetiro = Prestamo.fechaRetiro" +
            "     AND P2.idPrestamo > Prestamo.idPrestamo)))" +
            " WHERE Prestamo.fechaDevolucion IS NULL" +
            " AND EXISTS (" +
            "  SELECT 1 FROM Prestamo P3" +
            "  WHERE P3.idLibro = Prestamo.idLibro" +
            "  AND P3.fechaDevolucion IS NULL" +
            "  AND (P3.fechaRetiro > Prestamo.fechaRetiro" +
            "    OR (P3.fechaRetiro = Prestamo.fechaRetiro" +
            "      AND P3.idPrestamo > Prestamo.idPrestamo)))"
        );

        System.out.println(
            "[Reparacion] Se cerraron " + eventos +
            " prestamos duplicados (el mas nuevo de cada libro quedo activo)."
        );
    }

    public ArrayList<Libro> cargarLibros() throws SQLException
    {
        return this.getLibroDAO().obtenerTodos();
    }

    public Biblioteca cargarBiblioteca(String p_nombre)
        throws SQLException, ParseException   {
        ArrayList<Libro> libros = this.getLibroDAO().obtenerTodos();
        ArrayList<Socio> socios = this.getSocioDAO().obtenerTodos();

        this.getPrestamoDAO().cargarPrestamos(socios, libros);

        return new Biblioteca(p_nombre, libros, socios);
    }
}
