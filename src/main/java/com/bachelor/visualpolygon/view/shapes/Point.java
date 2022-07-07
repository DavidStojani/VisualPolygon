package com.bachelor.visualpolygon.view.shapes;


import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;


public class Point extends Circle {



    public Point(DoubleProperty x, DoubleProperty y) {

        super(x.get(), y.get(), 6);
        setFill(Color.GOLD.deriveColor(1, 0.7, 1, 0.5));
        setStroke(Color.GOLD);
        setStrokeWidth(2);
        setStrokeType(StrokeType.OUTSIDE);
        x.bindBidirectional(centerXProperty());
        y.bindBidirectional(centerYProperty());
        enableDragForGolden();
    }

   public void changeColorToRed() {
       setFill(Color.RED.deriveColor(1, 0.7, 1, 0.3));
       setStroke(Color.RED);
   }

   public void changeColorToGreen() {
       setFill(Color.GREEN.deriveColor(1, 0.7, 1, 0.3));
       setStroke(Color.GREEN);
   }




    // make a node movable by dragging it around with the mouse.


    private void enableDragForGolden() {
        setOnMousePressed(mouseEvent -> {
            this.getScene().setCursor(javafx.scene.Cursor.MOVE);

        });
        setOnMouseReleased(mouseEvent -> getScene().setCursor(javafx.scene.Cursor.HAND));
        setOnMouseDragged(mouseEvent -> {
            double newX = mouseEvent.getX();

            if (newX > 0 && newX < getScene().getWidth()) {
                setCenterX(newX);
            }

            double newY = mouseEvent.getY();

            if (newY > 0 && newY < getScene().getHeight()) {
                setCenterY(newY);
            }
        });
        setOnMouseEntered(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(javafx.scene.Cursor.HAND);
            }
        });
        setOnMouseExited(mouseEvent -> {
            if (!mouseEvent.isPrimaryButtonDown()) {
                getScene().setCursor(javafx.scene.Cursor.DEFAULT);
            }
        });
    }
}
