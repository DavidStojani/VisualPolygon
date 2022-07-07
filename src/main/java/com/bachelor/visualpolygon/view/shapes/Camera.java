package com.bachelor.visualpolygon.view.shapes;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Camera extends Circle {

    private static Camera camera;

    public Camera(DoubleProperty x, DoubleProperty y, DoubleProperty r) {

        super(x.get(), y.get(), 30);
        drawCamera();
        x.bind(centerXProperty());
        y.bind(centerYProperty());
        r.bindBidirectional(radiusProperty());
        enableDrag();
    }

    private void drawCamera() {
        this.setFill(Color.CADETBLUE.deriveColor(1, 0.9, 0.9, 0.7));
        this.setStrokeType(StrokeType.OUTSIDE);
        this.setStrokeWidth(3);
        this.setStroke(Color.CADETBLUE);
    }

    public static Camera createCamera(ObservableList<Double> coordinate) { // make class instead of function

        DoubleProperty cameraXProperty = new SimpleDoubleProperty(coordinate.get(0));
        DoubleProperty cameraYProperty = new SimpleDoubleProperty(coordinate.get(1));
        DoubleProperty cameraRProperty = new SimpleDoubleProperty(coordinate.get(2));

        camera = new Camera(cameraXProperty, cameraYProperty, cameraRProperty);

        cameraXProperty.addListener((ov, oldX, x) -> coordinate.set(0, x.doubleValue()));

        cameraYProperty.addListener((ov, oldY, y) -> coordinate.set(1, y.doubleValue()));

        cameraRProperty.addListener((observableValue, number, t1) -> coordinate.set(2, t1.doubleValue()));

        return camera;
    }

    private void enableDrag() {
        this.setOnMousePressed(mouseEvent -> this.getScene().setCursor(Cursor.MOVE));
        this.setOnMouseReleased(mouseEvent -> this.getScene().setCursor(Cursor.HAND));
        this.setOnMouseDragged(mouseEvent -> {
            double newX = mouseEvent.getX();
            if (newX > 0 && newX < getScene().getWidth()) {
                setCenterX(newX);
            }
            double newY = mouseEvent.getY();
            if (newY > 0 && newY < getScene().getHeight()) {
                setCenterY(newY);
            }
        });
        this.setOnMouseEntered(mouseEvent -> {

            if (!mouseEvent.isPrimaryButtonDown()) {

                getScene().setCursor(javafx.scene.Cursor.HAND);

            }

        });
        this.setOnMouseExited(mouseEvent -> {

            if (!mouseEvent.isPrimaryButtonDown()) {

                getScene().setCursor(javafx.scene.Cursor.DEFAULT);

            }

        });
        this.setOnScroll(scrollEvent -> {
            double zoomFactor = 1.05;
            double deltaY = scrollEvent.getDeltaY();
            /***TODO: Radius scrolling makes problem*/
            if (deltaY > 0) {
                zoomFactor = 2.0 - zoomFactor;
                setScaleX(getScaleX() * zoomFactor);
                setScaleY(getScaleY() * zoomFactor);
                setRadius(getRadius() * zoomFactor);
                setStrokeWidth(getStrokeWidth() * zoomFactor);


            } else if (deltaY < 0) {
                zoomFactor = 0.1 + zoomFactor;
                setScaleX(getScaleX() * zoomFactor);
                setScaleY(getScaleY() * zoomFactor);
                setRadius(getRadius() * zoomFactor);
                setStrokeWidth(getStrokeWidth() * zoomFactor);

            }
        });
    }

}
