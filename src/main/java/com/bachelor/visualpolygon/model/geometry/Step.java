package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.algorithm.locate.SimplePointInAreaLocator;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

@NoArgsConstructor
@Getter
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
        stepPolygon = builder.getStepPolygon();
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
            System.out.println("ACTIVE SIZE===" + active.size());
        }
    }

    //for every point in active build a parallel to the Streifen and check if it intersects with the circle with no interruption
    private void setTemps() {
        for (Coordinate coordinate : active) {
            if (getParallelLineForCoordinate(coordinate).toGeometry(new GeometryFactory()).within(initialPolygon)) {
                System.out.println("IS INSIDE POLYGON");
                tempVisible.add(coordinate);
            } else {
                System.out.println("IS NOT INSIDE POLYGON");
                tempInvisible.add(coordinate);
            }
        }

    }

    public LineSegment getParallelLineForCoordinate(Coordinate point) {

        LineSegment lineSegment = new LineSegment(stepPolygon.getCoordinates()[0], stepPolygon.getCoordinates()[1]);
        Coordinate baseMirror = lineSegment.pointAlongOffset(0, -lineSegment.distance(point));
        Coordinate endMirror = lineSegment.pointAlongOffset(1, -lineSegment.distance(point));
        LineSegment parallelToStep = new LineSegment(baseMirror, point);


        Line parallelLine =  new Line(baseMirror.getX(),baseMirror.getY(),point.getX(),point.getY());
        parallelLine.setFill(Color.BLACK);
        parallelLine.setStrokeWidth(1.5);
        builder.getLineStack().push(parallelLine);

        return parallelToStep;
    }



}
