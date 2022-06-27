package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineSegment;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.overlay.validate.FuzzyPointLocator;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
public class Step {
    Builder builder;
    Polygon initialPolygon;
    Polygon stepPolygon;
    List<Vertex> active;
    List<Vertex> tempVisible;
    List<Vertex> tempInvisible;


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
        FuzzyPointLocator pointLocator = new FuzzyPointLocator(stepPolygon, 2);
        for (Vertex coordinate : builder.getPolarSortedVertices()) {
            if (pointLocator.getLocation(coordinate.getCoordinate()) != 2) {
                System.out.println("Coordinate ADDED in ACTIVE :: " + coordinate);
                active.add(coordinate);
            }
        }
    }

    //for every point in active build a parallel to the Streifen and check if it intersects with the circle with no interruption
    private void setTemps() {
        for (Vertex vertex : active) {
            LineSegment parallelToBeChecked = getParallelLineForCoordinate(vertex.getCoordinate());
            if (parallelToBeChecked.toGeometry(new GeometryFactory()).within(initialPolygon)) {
                tempVisible.add(vertex);
                addToGreenLines(parallelToBeChecked);
            } else {
                tempInvisible.add(vertex);
                addToRedLines(parallelToBeChecked);
            }
        }

    }

    public LineSegment getParallelLineForCoordinate(Coordinate point) {
        LineSegment lineSegment = new LineSegment(stepPolygon.getCoordinates()[0], stepPolygon.getCoordinates()[1]);
        Coordinate baseMirror = lineSegment.pointAlongOffset(0, -lineSegment.distance(point));
        Coordinate endMirror = lineSegment.pointAlongOffset(1, -lineSegment.distance(point));
        LineSegment parallelToStep = new LineSegment(baseMirror, point);

        return parallelToStep;
    }

    public void addToGreenLines(LineSegment greenLine) {
        Line parallelLine = new Line(greenLine.getCoordinate(0).getX(),greenLine.getCoordinate(0).getY(),greenLine.getCoordinate(1).getX(),greenLine.getCoordinate(1).getY());
        parallelLine.setStroke(Color.GREEN);
        parallelLine.setStrokeWidth(1.9);
        builder.getLineStack().push(parallelLine);

    }


    public void addToRedLines(LineSegment redLine) {
        Line parallelLine = new Line(redLine.getCoordinate(0).getX(),redLine.getCoordinate(0).getY(),redLine.getCoordinate(1).getX(),redLine.getCoordinate(1).getY());
        parallelLine.setStroke(Color.RED);
        parallelLine.setStrokeWidth(1.9);
        builder.getLineStack().push(parallelLine);

    }

}
