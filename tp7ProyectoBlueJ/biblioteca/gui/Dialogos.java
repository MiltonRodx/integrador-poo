package biblioteca.gui;

import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Window;

/**
 * Ventanitas de apoyo para la GUI (reuso para las 4 pestañas).
 *
 * - formulario(): dialogo generico de N campos de texto; devuelve los valores
 *   si el usuario confirma con OK, o vacio si cancela. La validacion de TIPO
 *   (enteros, etc.) queda en el controller que conoce su dominio.
 * - error()/aviso()/informacion(): alertas de una sola linea de invocacion.
 */
public class Dialogos {

    /** Formulario generico: N etiquetas -> devuelve N Strings (trim) si OK. */
    public static Optional<String[]> formulario(Window p_dueno, String p_titulo,
            String p_cabecera, String... p_etiquetas) {
        Dialog<String[]> dialogo = new Dialog<>();
        dialogo.setTitle(p_titulo);
        dialogo.setHeaderText(p_cabecera);
        if (p_dueno != null) dialogo.initOwner(p_dueno);

        GridPane grilla = new GridPane();
        grilla.setHgap(10);
        grilla.setVgap(10);
        TextField[] campos = new TextField[p_etiquetas.length];
        for (int i = 0; i < p_etiquetas.length; i++) {
            campos[i] = new TextField();
            grilla.add(new Label(p_etiquetas[i] + ":"), 0, i);
            grilla.add(campos[i], 1, i);
        }
        dialogo.getDialogPane().setContent(grilla);
        dialogo.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialogo.setResultConverter(queBoton ->
            queBoton == ButtonType.OK ? valoresDe(campos) : null);

        return dialogo.showAndWait();
    }

    private static String[] valoresDe(TextField[] p_campos) {
        String[] valores = new String[p_campos.length];
        for (int i = 0; i < p_campos.length; i++) valores[i] = p_campos[i].getText().trim();
        return valores;
    }

    public static void error(String p_mensaje) {
        mostrar(Alert.AlertType.ERROR, "Error", p_mensaje);
    }

    public static void aviso(String p_mensaje) {
        mostrar(Alert.AlertType.WARNING, "Atencion", p_mensaje);
    }

    public static void informacion(String p_mensaje) {
        mostrar(Alert.AlertType.INFORMATION, "Informacion", p_mensaje);
    }

    private static void mostrar(Alert.AlertType p_tipo, String p_titulo, String p_mensaje) {
        Alert alerta = new Alert(p_tipo, p_mensaje, ButtonType.OK);
        alerta.setHeaderText(p_titulo);
        alerta.showAndWait();
    }
}
