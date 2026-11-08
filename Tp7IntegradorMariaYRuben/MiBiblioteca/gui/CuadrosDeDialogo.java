package MiBiblioteca.gui;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/** Mensajes emergentes de una linea (error, aviso, informacion). */
public class CuadrosDeDialogo {

    public void mostrarError(String p_mensaje) {
        this.mostrar(Alert.AlertType.ERROR, "Error", p_mensaje);
    }

    public void mostrarAviso(String p_mensaje) {
        this.mostrar(Alert.AlertType.WARNING, "Atención", p_mensaje);
    }

    public void mostrarInformacion(String p_mensaje) {
        this.mostrar(Alert.AlertType.INFORMATION, "Listo", p_mensaje);
    }

    private void mostrar(Alert.AlertType p_tipo, String p_titulo, String p_mensaje) {
        Alert alerta = new Alert(p_tipo, p_mensaje, ButtonType.OK);
        alerta.setHeaderText(p_titulo);
        alerta.showAndWait();
    }
}
