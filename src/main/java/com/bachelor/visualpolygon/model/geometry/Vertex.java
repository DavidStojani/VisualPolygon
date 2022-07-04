package com.bachelor.visualpolygon.model.geometry;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Coordinate;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Vertex extends Coordinate {
    @ToString.Exclude
    private int isVisible = 0;
    @ToString.Exclude
    private boolean isPrime;
    @ToString.Exclude
    private Vertex previousVertex;
    @ToString.Exclude
    private Vertex nextVertex;
    @ToString.Exclude
    private double r;
    @ToString.Exclude
    @Getter
    private boolean inBlue;
    @ToString.Exclude

    private boolean grey;
    private double theta;

    private double distanceToALPHA;

    private DoubleProperty xProperty;
    private DoubleProperty yProperty;


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

    public void setInGrey(boolean b) {
        grey= b;
    }
}
