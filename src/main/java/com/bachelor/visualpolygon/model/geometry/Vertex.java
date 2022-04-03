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
public class Vertex extends Coordinate {
    private boolean isVisibleFromCenter;
    private boolean isPrime;
    private Vertex previousVertex;
    private Vertex nextVertex;
    private double r;
    private double theta;
    @ToString.Include
    private double xCoordinate;
    @ToString.Include
    private double yCoordinate;
    @ToString.Include
    private DoubleProperty xProperty;
    @ToString.Include
    private DoubleProperty yProperty;


    public Vertex(Coordinate c) {
        xCoordinate = c.getX();
        yCoordinate = c.getY();
        setVisibleFromCenter(true);
        xProperty = new SimpleDoubleProperty(xCoordinate);
        yProperty = new SimpleDoubleProperty(yCoordinate);
    }

    public Vertex(double xCoordinate, double yCoordinate) {
        this.xCoordinate = xCoordinate;
        this.yCoordinate = yCoordinate;
        xProperty = new SimpleDoubleProperty(xCoordinate);
        yProperty = new SimpleDoubleProperty(yCoordinate);
        xProperty.addListener((observableValue, number, t1) -> this.xCoordinate = t1.doubleValue());
        yProperty.addListener((observableValue, number, t1) -> this.yCoordinate = t1.doubleValue());
        setVisibleFromCenter(true);
    }

    public Coordinate getCoordinate() {
        return new Coordinate(xCoordinate, yCoordinate);
    }


}
