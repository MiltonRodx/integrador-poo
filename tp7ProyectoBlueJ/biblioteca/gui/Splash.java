package biblioteca.gui;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Splash de arranque: un libro 3D que se abre solo sobre su lomo, con el
 * titulo de la institucion, antes de mostrar la ventana principal.
 *
 * - Stage TRANSPARENT sin bordes: solo se ve la "tarjeta" azul con el libro.
 * - Perspectiva 3D real: Scene con depthBuffer + PerspectiveCamera y una
 *   Rotate sobre el eje Y con pivote en el lomo (la tapa gira en profundidad).
 * - Duracion ~1.5 segundos; al terminar entrega el control a App mediante
 *   el Runnable alListo (aqui App arma y muestra la ventana principal).
 * - Click en cualquier parte para saltar la animacion.
 */
public class Splash {

    private static final double ANCHO = 460;
    private static final double ALTO = 300;

    private final Stage ventana = new Stage();
    private final Runnable alListo;
    private StackPane tarjeta;
    private Rotate rotacionTapa;
    private Timeline linea;
    private boolean pasandoEscena;

    public Splash(Runnable p_alListo) { this.alListo = p_alListo; }

    /** Construye la tarjeta, arma la animacion y muestra el splash. */
    public void mostrar() {
        // ---- Tarjeta de fondo (rect azul, bordes redondeados) ----
        Rectangle fondo = new Rectangle(ANCHO, ALTO);
        fondo.setArcWidth(22);
        fondo.setArcHeight(22);
        fondo.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0.0, Color.web("#16294a")),
            new Stop(1.0, Color.web("#203a66"))));
        fondo.setEffect(new DropShadow(18, Color.rgb(0, 0, 0, 0.45)));

        // ---- El libro (piezas armadas en armaLibro()) ----
        Group librito = new Group();
        librito.setTranslateY(-12);
        this.armaLibro(librito);

        // ---- Titulos ----
        Text titulo = new Text("Gestión de Biblioteca");
        titulo.setFont(Font.font("Segoe UI", FontWeight.BOLD, 26));
        titulo.setFill(Color.web("#f5f2ea"));
        titulo.setOpacity(0);

        Text subtitulo = new Text("FaCENA · UNNE · Equipo 49");
        subtitulo.setFont(Font.font("Segoe UI", 13));
        subtitulo.setFill(Color.web("#a9c0e0"));
        subtitulo.setOpacity(0);

        // ---- Tarjeta: fondo + libro + titulos apilados ----
        this.tarjeta = new StackPane(fondo, librito, titulo, subtitulo);
        this.tarjeta.setPrefSize(ANCHO, ALTO);
        this.tarjeta.setMaxSize(ANCHO, ALTO);
        StackPane.setAlignment(librito, Pos.TOP_CENTER);
        StackPane.setMargin(librito, new Insets(42, 0, 0, 0));
        StackPane.setAlignment(titulo, Pos.BOTTOM_CENTER);
        StackPane.setMargin(titulo, new Insets(0, 0, 44, 0));
        StackPane.setAlignment(subtitulo, Pos.BOTTOM_CENTER);
        StackPane.setMargin(subtitulo, new Insets(0, 0, 20, 0));

        // ---- Escena transparente ----
        // (sin camara especial: la ParallelCamera por defecto ya proyecta el
        // giro sobre Y con el acortamiento visual que da el efecto 3D, sin
        // riesgos de plano-cercano que recortan el libro)
        Scene escena = new Scene(this.tarjeta, ANCHO, ALTO);
        escena.setFill(Color.TRANSPARENT);              // tarjeta "flotando"
        escena.setOnMouseClicked(e -> this.terminar()); // click = saltar

        this.ventana.initStyle(StageStyle.TRANSPARENT);
        this.ventana.setScene(escena);
        this.ventana.centerOnScreen();
        this.ventana.show();

        this.linea = this.construyeMovimiento(titulo, subtitulo);
        this.linea.setOnFinished(e -> this.terminar());
        this.linea.play();
    }

    /**
     * Arma las piezas del libro dentro de p_libro: hojas apoyadas en abanico,
     * tapa trasera fija, y la tapa frontal que girara en construyeMovimiento().
     * El lomo del libro queda en x=0 (pivote de todos los giros).
     */
    private void armaLibro(Group p_libro) {
        double w = 104;   // ancho de cada tapa
        double h = 132;   // alto de cada tapa

        // Hojas interiores: 3 rectangulos claros levemente abanicados
        // (girados un poco menos que la tapa trasera, dan grosor).
        for (int i = 0; i < 3; i++) {
            Rectangle hoja = new Rectangle(w - 6, h - 12, Color.web("#efe6d4"));
            hoja.setX(0);
            hoja.setY(-h / 2);
            hoja.setArcWidth(8);
            hoja.setArcHeight(8);
            hoja.getTransforms().add(new Rotate(-(8 + 7 * (2 - i)), 0, 0, 0, Rotate.Y_AXIS));
            hoja.setTranslateX(2);
            p_libro.getChildren().add(hoja);
        }

        // Tapa trasera: queda fija, abierta hacia la izquierda.
        Rectangle trasera = new Rectangle(w, h, Color.web("#7e2f26"));
        trasera.setX(0);
        trasera.setY(-h / 2);
        trasera.setArcWidth(10);
        trasera.setArcHeight(10);
        trasera.getTransforms().add(new Rotate(-24, 0, 0, 0, Rotate.Y_AXIS));
        p_libro.getChildren().add(trasera);

        // Tapa frontal: arranca cerrada (apoyada sobre las hojas) y la
        // animacion la hace girar alrededor del lomo hasta abrirse.
        Group tapa = new Group();
        Rectangle cara = new Rectangle(w, h, Color.web("#a63f32"));
        cara.setX(0);
        cara.setY(-h / 2);
        cara.setArcWidth(10);
        cara.setArcHeight(10);
        Rectangle cerradura = new Rectangle(18, 12, Color.web("#f0c987"));
        cerradura.setX(-w + 16);
        cerradura.setY(h / 2 - 22);
        cerradura.setArcWidth(4);
        cerradura.setArcHeight(4);
        tapa.getChildren().addAll(cara, cerradura);
        this.rotacionTapa = new Rotate(0, 0, 0, 0, Rotate.Y_AXIS);
        tapa.getTransforms().add(this.rotacionTapa);
        p_libro.getChildren().add(tapa);
    }

    /**
     * KeyFrames del gesto completo: la tapa se abre en 3D y aparecen los
     * titulos, en ~1.5 segundos. El fundido de salida lo maneja terminar().
     */
    private Timeline construyeMovimiento(Text p_titulo, Text p_subtitulo) {
        KeyValue cerrado = new KeyValue(this.rotacionTapa.angleProperty(), 0,
            Interpolator.EASE_BOTH);
        KeyValue abierto = new KeyValue(this.rotacionTapa.angleProperty(), -105,
            Interpolator.EASE_OUT);

        Timeline linea = new Timeline(
            //      0 - 90ms: la tarjeta aparece suavemente (libro cerrado)
            new KeyFrame(Duration.millis(0),
                new KeyValue(this.tarjeta.opacityProperty(), 0.0),
                cerrado),
            new KeyFrame(Duration.millis(90),
                new KeyValue(this.tarjeta.opacityProperty(), 1.0,
                    Interpolator.EASE_OUT)),
            //    150 - 900ms: LA TAPA SE ABRE (el momento 3D)
            new KeyFrame(Duration.millis(150), cerrado),
            new KeyFrame(Duration.millis(900), abierto),
            //    850 - 1200ms: titulos suben mientras el libro termina de abrirse
            new KeyFrame(Duration.millis(850),
                new KeyValue(p_titulo.opacityProperty(), 0.0),
                new KeyValue(p_titulo.translateYProperty(), 12),
                new KeyValue(p_subtitulo.opacityProperty(), 0.0)),
            new KeyFrame(Duration.millis(1200),
                new KeyValue(p_titulo.opacityProperty(), 1.0),
                new KeyValue(p_titulo.translateYProperty(), 0.0,
                    Interpolator.EASE_OUT),
                new KeyValue(p_subtitulo.opacityProperty(), 1.0)),
            //   1550ms: marca de fin (OnFinished del Timeline -> terminar())
            new KeyFrame(Duration.millis(1550)));
        return linea;
    }

    /** Fundido de salida del splash y pase de control a la ventana principal. */
    private void terminar() {
        if (this.pasandoEscena || this.tarjeta == null) return;
        this.pasandoEscena = true;
        if (this.linea != null) {
            this.linea.stop();
        }

        FadeTransition salida = new FadeTransition(Duration.millis(260), this.tarjeta);
        salida.setToValue(0.0);
        salida.setOnFinished(e -> {
            this.ventana.close();
            this.alListo.run();
        });
        salida.play();
    }
}
