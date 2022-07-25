package com.bachelor.visualpolygon.view.shapes;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Camera extends Circle {

    private static final double SCALE_DELTA = 1.1;

    private static Camera camera;
    private static ObservableList<Double> details;

    public Camera(ObservableList<Double> requirements) {
        super(requirements.get(0), requirements.get(1), requirements.get(2));
        drawCamera();
        details = requirements;
        DoubleProperty x = new SimpleDoubleProperty(requirements.get(0));
        DoubleProperty y = new SimpleDoubleProperty(requirements.get(1));
        DoubleProperty r = new SimpleDoubleProperty(requirements.get(2));

        x.bind(centerXProperty());
        y.bind(centerYProperty());
        r.bind(radiusProperty());
        enableDrag();

    }


    private void drawCamera() {
        this.setFill(Color.CADETBLUE.deriveColor(1, 0.9, 0.9, 0.7));
        this.setStrokeType(StrokeType.INSIDE);
        this.setStrokeWidth(3);
        this.setStroke(Color.CADETBLUE);
    }

    private void enableDrag() {


        this.setOnMouseDragged(mouseEvent -> {
            double newX = mouseEvent.getX();
            if (newX > 0 && newX < getScene().getWidth()) {
                setCenterX(newX);
                details.set(0, newX);
            }
            double newY = mouseEvent.getY();
            if (newY > 0 && newY < getScene().getHeight()) {
                setCenterY(newY);
                details.set(1, newY);
            }
        });


        this.setOnMouseEntered(mouseEvent2 -> {
            if (!mouseEvent2.isPrimaryButtonDown()) {
                this.getScene().setCursor(javafx.scene.Cursor.HAND);
            }
        });

        this.setOnMouseExited(mouseEvent -> {

            if (!mouseEvent.isPrimaryButtonDown()) {

                getScene().setCursor(javafx.scene.Cursor.DEFAULT);

            }

        });
        this.setOnScroll(scrollEvent -> {
            scrollEvent.consume();

            if (scrollEvent.getDeltaY() == 0) {
                return;
            }

            double scaleFactor = (scrollEvent.getDeltaY() > 0)
                    ? SCALE_DELTA
                    : 1 / SCALE_DELTA;
            setScaleX(getScaleX() * scaleFactor);
            setScaleY(getScaleY() * scaleFactor);
            setRadius((getRadius() * scaleFactor));
        });
    }

}
