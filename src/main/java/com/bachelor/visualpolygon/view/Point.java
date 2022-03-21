package com.bachelor.visualpolygon.view;

import javafx.beans.property.DoubleProperty;
import javafx.scene.Group;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeType;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;

public class Point extends Circle {

    private static int id;
    Text idText;
    Group group;


    Point(DoubleProperty x, DoubleProperty y) {

        super(x.get(), y.get(), 6);
        setFill(Color.GOLD.deriveColor(1, 0.7, 1, 0.5));
        setStroke(Color.GOLD);
        setStrokeWidth(2);
        setStrokeType(StrokeType.OUTSIDE);
        x.bind(centerXProperty());
        y.bind(centerYProperty());
        enableDrag();

    }

    // make a node movable by dragging it around with the mouse.

    private void enableDrag() {
        setOnMousePressed(mouseEvent -> this.getScene().setCursor(javafx.scene.Cursor.MOVE));

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

    public Group getGroup() {
        idText = new Text(""+id);
        idText.setBoundsType(TextBoundsType.VISUAL);
        id ++;
        return new Group(this,idText);
    }


}
