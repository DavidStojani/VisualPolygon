package com.bachelor.visualpolygon.model.geometry;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.locationtech.jts.geom.Coordinate;

@Getter
@Setter
@ToString
public class Vertex extends Coordinate {

    private boolean isVisibleFromCenter;
    private boolean isPrime;
    private Vertex previousVertex;
    private Vertex nextVertex;
    private double r;
    private double theta;
    private double x;
    private double y;

    public Vertex(Coordinate c) {
        x = c.getX();
        y = c.getY();
    }

    public Vertex(double x, double y) {
        this.x = x;
        this.y = y;
    }


}
