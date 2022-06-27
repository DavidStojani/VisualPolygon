package com.bachelor.visualpolygon.model.geometry;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@EqualsAndHashCode
@ToString
public class Vertex extends Coordinate {
    @ToString.Exclude
    private boolean isVisibleFromCenter;
    @ToString.Exclude
    private boolean isPrime;
    @ToString.Exclude
    private Vertex previousVertex;
    @ToString.Exclude
    private Vertex nextVertex;
    @ToString.Exclude
    private double r;
    @ToString.Exclude
    private double theta;
    @ToString.Exclude
    private double xCoordinate;
    @ToString.Exclude
    private double yCoordinate;

    private DoubleProperty xProperty;
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
