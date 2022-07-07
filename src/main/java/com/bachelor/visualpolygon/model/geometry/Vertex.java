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
    private Vertex previousVertex;
    private Vertex nextVertex;
    private boolean isPrime;
    private double r;

    private double theta;

    private double angleToBETA;


    private DoubleProperty xProperty;
    private DoubleProperty yProperty;
    private boolean inWhite;
    private int visited = 0;


    public Vertex(Coordinate c) {

        xProperty = new SimpleDoubleProperty(c.getX());
        yProperty = new SimpleDoubleProperty(c.getY());
    }

    public Vertex(double xCoordinate, double yCoordinate) {
        xProperty = new SimpleDoubleProperty(xCoordinate);
        yProperty = new SimpleDoubleProperty(yCoordinate);
        xProperty.addListener((observableValue, number, t1) -> xProperty.set(t1.doubleValue()));
        yProperty.addListener((observableValue, number, t1) -> yProperty.set(t1.doubleValue()));

    }

    public Coordinate getCoordinate() {
        return new Coordinate(xProperty.doubleValue(), yProperty.doubleValue());
    }

    public double getXCoordinate() {
        return xProperty.doubleValue();
    }

    public double getYCoordinate() {
        return yProperty.doubleValue();
    }

    public void increaseVisited() {
        visited++;
    }

}
