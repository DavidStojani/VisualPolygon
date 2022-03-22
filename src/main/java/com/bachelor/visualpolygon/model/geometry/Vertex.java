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
@EqualsAndHashCode
public class Vertex extends Coordinate {

    private boolean isVisibleFromCenter;
    private boolean isPrime;
    private Vertex previousVertex;
    private Vertex nextVertex;
    private double r;
    private double theta;
    private double x;
    private double y;
    private DoubleProperty xProperty;
    private DoubleProperty yProperty;


    public Vertex(Coordinate c) {
        x = c.getX();
        y = c.getY();
        xProperty = new SimpleDoubleProperty(x);
        yProperty = new SimpleDoubleProperty(y);

    }

    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
        xProperty = new SimpleDoubleProperty(this.x);
        yProperty = new SimpleDoubleProperty(this.y);
    }

    public Coordinate getCoordinate() {
        return new Coordinate(x,y);
    }


}
