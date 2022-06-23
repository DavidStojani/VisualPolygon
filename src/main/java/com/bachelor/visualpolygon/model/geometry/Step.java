package com.bachelor.visualpolygon.model.geometry;


import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class Step {
    Builder builder;
    Polygon initialPolygon;
    Polygon stepPolygon;
    List<Coordinate> active;
    List<Coordinate> tempVisible;
    List<Coordinate> tempInvisible;

    public Step(Builder builder) {
        this.builder = builder;
        this.initialPolygon = builder.getPolygon();
        builder.stepPolygon = builder.getStepPolygon();
        active = new ArrayList<>();
        tempInvisible = new ArrayList<>();
        tempVisible = new ArrayList<>();
    }

    public void initStep() {
        stepPolygon = builder.getStepPolygon();
        setActive();
        setTemps();
    }

    private void setActive() {
        for (Coordinate coordinate : builder.getPolygon().getCoordinates()) {
            if (SimplePointInAreaLocator.containsPointInPolygon(coordinate, stepPolygon)) {
                System.out.println("Coordinate inside polygon");
                active.add(coordinate);
            }
        }
    }

    //for every point in active build a parallel to the Streifen and check if it intersects with the circle with no interruption
    private void setTemps() {
        for (Coordinate coordinate : active) {
            if (builder.getParallelLineForCoordinate(coordinate).toGeometry(new GeometryFactory()).within(initialPolygon)) {
                System.out.println("IS INSIDE POLYGON");
                tempVisible.add(coordinate);
            } else {
                System.out.println("IS NOT INSIDE POLYGON");
                tempInvisible.add(coordinate);
            }
        }

    }

}
