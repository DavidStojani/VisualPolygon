package com.bachelor.visualpolygon.model.geometry;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Coordinate;

@Getter
@Setter
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
    @EqualsAndHashCode.Include
    private double x;
    @EqualsAndHashCode.Include
    private double y;
    @ToString.Exclude
    private DoubleProperty xProperty;
    @ToString.Exclude
    private DoubleProperty yProperty;


    public Vertex(Coordinate c) {
        x = c.getX();
        y = c.getY();
        setVisibleFromCenter(true);
        xProperty = new SimpleDoubleProperty(x);
        yProperty = new SimpleDoubleProperty(y);

    }

    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
        xProperty = new SimpleDoubleProperty(x);
        yProperty = new SimpleDoubleProperty(y);
        xProperty.addListener((observableValue, number, t1) -> this.x = t1.doubleValue());
        yProperty.addListener((observableValue, number, t1) -> this.y = t1.doubleValue());
        setVisibleFromCenter(true);
    }

    public Coordinate getCoordinate() {
        return new Coordinate(x, y);
    }


}
