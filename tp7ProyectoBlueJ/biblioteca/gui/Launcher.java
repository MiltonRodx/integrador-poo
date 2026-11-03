package biblioteca.gui;

import javafx.application.Application;

/**
 * Lanzador "no-JavaFX" de la GUI.
 *
 * java exige que una clase que extiende Application arranque por module-path;
 * si se lanza por classpath plano (lo que usa este proyecto, gui.sh/gui.bat y
 * BlueJ) muere con "JavaFX runtime components are missing". El work-around
 * clasico es este Launcher: NO extiende Application, solo delega:
 *
 *   java -cp ... biblioteca.gui.Launcher
 *
 * Desde BlueJ: click derecho en Launcher -> void main(String[] args).
 */
public class Launcher {

    public static void main(String[] args) {
        Application.launch(App.class, args);
    }
}
