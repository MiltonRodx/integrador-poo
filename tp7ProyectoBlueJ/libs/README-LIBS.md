# Dependencias de `libs/` (versiones exactas)

El ZIP de entrega no incluye los jars (pesaban ~30 MB y Moodle limita a 20 MB).
Para correr el proyecto, colocar los **10 jars** de abajo en esta misma carpeta
(`libs/`) tal como estan nombrados.

Descargas directas (Maven Central):

| Jar | URL |
|---|---|
| `sqlite-jdbc-3.54.1.0.jar` | <https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.54.1.0/sqlite-jdbc-3.54.1.0.jar> |
| `atlantafx-base-2.1.0.jar` | <https://repo1.maven.org/maven2/io/github/mkalten/atlantafx-base/2.1.0/atlantafx-base-2.1.0.jar> |
| `javafx-base-21.0.13-linux.jar` | <https://repo1.maven.org/maven2/org/openjfx/javafx-base/21.0.13/javafx-base-21.0.13-linux.jar> |
| `javafx-base-21.0.13-win.jar` | <https://repo1.maven.org/maven2/org/openjfx/javafx-base/21.0.13/javafx-base-21.0.13-win.jar> |
| `javafx-graphics-21.0.13-linux.jar` | <https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/21.0.13/javafx-graphics-21.0.13-linux.jar> |
| `javafx-graphics-21.0.13-win.jar` | <https://repo1.maven.org/maven2/org/openjfx/javafx-graphics/21.0.13/javafx-graphics-21.0.13-win.jar> |
| `javafx-controls-21.0.13-linux.jar` | <https://repo1.maven.org/maven2/org/openjfx/javafx-controls/21.0.13/javafx-controls-21.0.13-linux.jar> |
| `javafx-controls-21.0.13-win.jar` | <https://repo1.maven.org/maven2/org/openjfx/javafx-controls/21.0.13/javafx-controls-21.0.13-win.jar> |
| `javafx-fxml-21.0.13-linux.jar` | <https://repo1.maven.org/maven2/org/openjfx/javafx-fxml/21.0.13/javafx-fxml-21.0.13-linux.jar> |
| `javafx-fxml-21.0.13-win.jar` | <https://repo1.maven.org/maven2/org/openjfx/javafx-fxml/21.0.13/javafx-fxml-21.0.13-win.jar> |

Notas:

- Basta con los jars del **sistema operativo en uso** (`-linux` en Linux/macOS,
  `-win` en Windows): los 4 de `base/graphics/controls/fxml` + `atlantafx-base`
  + `sqlite-jdbc`.
- `gui.sh` (Linux) y `gui.bat` (Windows) ya arman el classpath con los nombres
  de arriba; no hay que tocar nada.
- _Versiones fijadas deliberadamente_ (ver README del repo, seccion "Dependencias
versionadas"): `atlantafx-base 2.1.0` es el bytecode Java 21 exacto (la 3.0.0
exige Java 25) y `sqlite-jdbc 3.54.1.0` el driver sin cambios de API.
