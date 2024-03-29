package com.bachelor.visualpolygon.model.geometry;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;

@Getter
@Setter
@EqualsAndHashCode
public class Vertex extends Coordinate {
    private int isVisible = 0;
    private double theta;
    private transient DoubleProperty xProperty;
    private transient DoubleProperty yProperty;
    boolean forVisualPolygon;


    public Vertex(Coordinate c) {
        super(c);
        xProperty = new SimpleDoubleProperty(c.getX());
        yProperty = new SimpleDoubleProperty(c.getY());
    }

    public Vertex(double xCoordinate, double yCoordinate) {
        super(xCoordinate,yCoordinate);
        xProperty = new SimpleDoubleProperty(xCoordinate);
        yProperty = new SimpleDoubleProperty(yCoordinate);
    }

    public double getXCoordinate() {
        return xProperty.doubleValue();
    }

    public double getYCoordinate() {
        return yProperty.doubleValue();
    }
    public boolean equalCoordinate(Coordinate coordinate) {
        return this.getXCoordinate() == coordinate.getX() && this.getYCoordinate() == coordinate.getY();
    }

    @Override
    public String toString() {
        return "Vertex {" + x + " ; " + y +'}';
    }
}
