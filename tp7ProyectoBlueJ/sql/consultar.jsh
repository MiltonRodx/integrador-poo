/*
Archivo de tipo .jsh  (java shell script) que en este caso,
nos permite conectar a la base de datos local (biblioteca.db)
*/


import java.sql.DriverManager;

// Conexión a la base de datos
var c = DriverManager.getConnection("jdbc:sqlite:biblioteca.db");

// respuesta será de la conexión hacer un statement del cual ejecutamos select * from socio.
// obtenemos todos los socios en una variable abstracta 'rs' (lista de objetos).
var rs = c.createStatement().executeQuery("select * from socio");

// para cada respuesta, o mientras haya respuestas.
while (rs.next()) {
    // Imprimimos el registro
    System.out.println(rs.getInt("dni") + " | " + rs.getString("nombre") + " | "
        + rs.getString("tipo") + " | dias=" + rs.getInt("dias_prestamos"));
}


System.out.println("---- prestamos ----");
// ahora una response 2  de prestamos

// ejecuto la query de sql
var rs2 = c.createStatement().executeQuery(
    "select p.id, s.nombre, l.titulo, p.fecha_devolucion is null as activo from prestamo p "
    + "join socio s on s.dni = p.socio_dni join libro l on l.id = p.libro_id");


// loopeo y muestro
while (rs2.next()) {
    System.out.println(rs2.getInt("id") + " | " + rs2.getString("nombre") + " tomo '"
        + rs2.getString("titulo") + "' | " + (rs2.getBoolean("activo") ? "ACTIVO" : "devuelto"));
}

// cierro las conexiones en orden.
rs.close();
rs2.close();
c.close();
