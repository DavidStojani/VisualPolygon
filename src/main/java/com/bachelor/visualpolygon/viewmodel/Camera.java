package com.bachelor.visualpolygon.viewmodel;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;

public class Camera extends Circle {


     public Camera(DoubleProperty x, DoubleProperty y) {

        super(x.get(),y.get(),24);
        this.setFill(Color.CADETBLUE.deriveColor(1,0.9,0.9,0.7));
        this.setStrokeType(StrokeType.OUTSIDE);
        this.setStrokeWidth(3.5);
        this.setStroke(Color.CADETBLUE);
        x.bind(centerXProperty());
        y.bind(centerYProperty());


        enableDrag();
    }



    private void enableDrag() {
        this.setOnMousePressed(mouseEvent -> this.getScene().setCursor(Cursor.MOVE));
        this.setOnMouseReleased(mouseEvent -> this.getScene().setCursor(Cursor.HAND));
        this.setOnMouseDragged(mouseEvent -> {

            double newX = mouseEvent.getX();

            if (newX > 0 && newX < getScene().getWidth()) {

                setCenterX(newX);

            }

            double newY = mouseEvent.getY(); //+ dragDelta.y;

            if (newY > 0 && newY <getScene().getHeight()) {

                setCenterY(newY);

            }});
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
            if (deltaY > 0) {
                zoomFactor = 2.0 - zoomFactor;
                setScaleX(getScaleX() * zoomFactor);
                setScaleY(getScaleY() * zoomFactor);
                setRadius(getRadius() * zoomFactor);
            }else if (deltaY < 0) {
                zoomFactor = 0.1 + zoomFactor;
                setScaleX(getScaleX() * zoomFactor);
                setScaleY(getScaleY() * zoomFactor);
                setRadius(getRadius() * zoomFactor);
            }
        });

    }

}
