import java.sql.DriverManager;
var c = DriverManager.getConnection("jdbc:sqlite:biblioteca.db");
var rs = c.createStatement().executeQuery("select * from socio");
while (rs.next()) {
    System.out.println(rs.getInt("dni") + " | " + rs.getString("nombre") + " | "
        + rs.getString("tipo") + " | dias=" + rs.getInt("dias_prestamos"));
}
System.out.println("---- prestamos ----");
var rs2 = c.createStatement().executeQuery(
    "select p.id, s.nombre, l.titulo, p.fecha_devolucion is null as activo from prestamo p "
    + "join socio s on s.dni = p.socio_dni join libro l on l.id = p.libro_id");
while (rs2.next()) {
    System.out.println(rs2.getInt("id") + " | " + rs2.getString("nombre") + " tomo '"
        + rs2.getString("titulo") + "' | " + (rs2.getBoolean("activo") ? "ACTIVO" : "devuelto"));
}
rs.close(); rs2.close(); c.close();
