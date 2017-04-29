package com.marsdev.alm.scratch;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.effect.BoxBlur;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * http://stackoverflow.com/questions/22622034/frosted-glass-effect-in-javafx
 * slides a frost pane in on scroll or swipe up; slides it out on scroll or swipe down.
 */
public class Frosty extends Application {

    private static final double W = 330;
    private static final double H = 590;

    private static final double BLUR_AMOUNT = 10;
    private static final Duration SLIDE_DURATION = Duration.seconds(0.4);

    private static final double UPPER_SLIDE_POSITION = 100;

    private static final Effect frostEffect =
            new BoxBlur(BLUR_AMOUNT, BLUR_AMOUNT, 3);

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        DoubleProperty y = new SimpleDoubleProperty(H);

        Node background = createBackground();
        Node frost = freeze(background, y);
        Node content = createContent();
        content.setVisible(false);

        Scene scene = new Scene(
                new StackPane(
                        background,
                        frost,
                        content
                )
        );

        stage.setScene(scene);
        stage.show();

        addSlideHandlers(y, content, scene);
    }

    // create a background node to be frozen over.
    private Node createBackground() {
        Image backgroundImage = new Image(
                getClass().getResourceAsStream("ios-screenshot.png")
        );
        ImageView background = new ImageView(backgroundImage);
        Rectangle2D viewport = new Rectangle2D(0, 0, W, H);
        background.setViewport(viewport);

        return background;
    }

    // create some content to be displayed on top of the frozen glass panel.
    private Label createContent() {
        Label label = new Label("The overlaid text is clear and the background below is frosty.");

        label.setStyle("-fx-font-size: 25px; -fx-text-fill: midnightblue;");
        label.setEffect(new Glow());
        label.setMaxWidth(W - 20);
        label.setWrapText(true);

        return label;
    }

    // add handlers to slide the glass panel in and out.
    private void addSlideHandlers(DoubleProperty y, Node content, Scene scene) {
        Timeline slideIn = new Timeline(
                new KeyFrame(
                        SLIDE_DURATION,
                        new KeyValue(
                                y,
                                UPPER_SLIDE_POSITION
                        )
                )
        );

        slideIn.setOnFinished(e -> content.setVisible(true));

        Timeline slideOut = new Timeline(
                new KeyFrame(
                        SLIDE_DURATION,
                        new KeyValue(
                                y,
                                H
                        )
                )
        );

        scene.setOnSwipeUp(e -> {
            slideOut.stop();
            slideIn.play();
        });

        scene.setOnSwipeDown(e -> {
            slideIn.stop();
            slideOut.play();
            content.setVisible(false);
        });

        // scroll handler isn't necessary if you have a touch screen.
        scene.setOnScroll((ScrollEvent e) -> {
            if (e.getDeltaY() < 0) {
                slideOut.stop();
                slideIn.play();
            } else {
                slideIn.stop();
                slideOut.play();
                content.setVisible(false);
            }
        });
    }

    // create a frosty pane from a background node.
    private StackPane freeze(Node background, DoubleProperty y) {
        Image frostImage = background.snapshot(
                new SnapshotParameters(),
                null
        );
        ImageView frost = new ImageView(frostImage);

        Rectangle filler = new Rectangle(0, 0, W, H);
        filler.setFill(Color.AZURE);

        Pane frostPane = new Pane(frost);
        frostPane.setEffect(frostEffect);

        StackPane frostView = new StackPane(
                filler,
                frostPane
        );

        Rectangle clipShape = new Rectangle(0, y.get(), W, H);
        frostView.setClip(clipShape);

        clipShape.yProperty().bind(y);

        return frostView;
    }
}
