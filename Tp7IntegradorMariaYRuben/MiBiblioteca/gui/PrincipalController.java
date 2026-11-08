package MiBiblioteca.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;

/**
 * Marco de la ventana: barra lateral + zona central.
 * Cada boton del menu declara en el FXML (userData) que pantalla abre,
 * asi agregar una pantalla nueva no obliga a tocar esta clase.
 */
public class PrincipalController {

    private final FabricaControladores fabricaDeControladores;
    private NavegadorPantallas navegadorPantallas;

    @FXML private StackPane contenedorDePantalla;
    @FXML private ToggleGroup grupoMenu;
    @FXML private ToggleButton botonPrestamos;

    public PrincipalController(FabricaControladores p_fabricaDeControladores) {
        this.fabricaDeControladores = p_fabricaDeControladores;
    }

    @FXML
    private void initialize() {
        this.navegadorPantallas = new NavegadorPantallas(
            this.contenedorDePantalla, this.fabricaDeControladores);

        this.grupoMenu.selectedToggleProperty().addListener(
            (p_observable, p_anterior, p_actual) -> this.alCambiarOpcionDeMenu(p_anterior, p_actual));

        this.botonPrestamos.setSelected(true);
    }

    private void alCambiarOpcionDeMenu(Toggle p_anterior, Toggle p_actual) {
        if (p_actual == null) {            // no permito dejar el menu sin opcion elegida
            p_anterior.setSelected(true);
            return;
        }
        this.navegadorPantallas.mostrar((String) p_actual.getUserData());
    }
}
