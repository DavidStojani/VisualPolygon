package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.locate.IndexedPointInAreaLocator;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.util.PointExtracter;
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
       // findNextVertexToBuildStep();
    }

    private void setActive() {
        System.out.println("SIZE OF STEPPOLYGON COORDINATES LIST:::" + stepPolygon.getCoordinates().length);
        FuzzyPointLocator pointLocator = new FuzzyPointLocator(stepPolygon, 2);
        for (Vertex vertex : builder.getPolarSortedVertices()) {
            if (pointLocator.getLocation(vertex.getCoordinate()) != 2) {
                System.out.println("Coordinate ADDED in ACTIVE :: " + vertex);
                active.add(vertex);
            }
        }
        System.out.println("AKTIVE ::::::" + active);

    }

    public void findNextVertexToBuildStep() {

        if (active.isEmpty()) {
            System.out.println("ACTIVE IS EMPTY");
        }

        Vertex afterAktive = builder.getPolarSortedVertices().stream()
                .filter(vertex -> vertex.getTheta() > active.get(active.size() - 1).getTheta())
                .findFirst().get();

        if (active.size() <= 1) {
            builder.setNextVertex(afterAktive);
            return;
        }

        Vertex insideAktive = active.get(1);

        LineSegment ALPHA = new LineSegment(stepPolygon.getCoordinates()[3], stepPolygon.getCoordinates()[2]);

        LineSegment BETA = new LineSegment(stepPolygon.getCoordinates()[0], stepPolygon.getCoordinates()[1]);


        System.out.println("DISTANCE after to ALPHA:::" + ALPHA.distance(afterAktive.getCoordinate()));
        System.out.println("PERPENDICULAR after to ALPHA:::" + ALPHA.distancePerpendicular(afterAktive.getCoordinate()));

        if (ALPHA.distancePerpendicular(afterAktive.getCoordinate()) > BETA.distancePerpendicular(insideAktive.getCoordinate())) {
            builder.setNextVertex(insideAktive);
        } else {
            builder.setNextVertex(afterAktive);
        }


    }

    //for every point in active build a parallel to the Streifen and check if it intersects with the circle with no interruption
    private void setTemps() {
        for (Vertex vertex : active) {
            LineSegment parallelToBeChecked = getParallelLineForCoordinate(vertex.getCoordinate());
            if (parallelToBeChecked.toGeometry(new GeometryFactory()).within(initialPolygon)) {
                tempVisible.add(vertex);
                vertex.setIsVisible(1);
                addToGreenLines(parallelToBeChecked);
            } else {
                tempInvisible.add(vertex);
                if (vertex.getIsVisible() != 1) {
                    vertex.setIsVisible(-1);
                }
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
        Line parallelLine = new Line(greenLine.getCoordinate(0).getX(), greenLine.getCoordinate(0).getY(), greenLine.getCoordinate(1).getX(), greenLine.getCoordinate(1).getY());
        parallelLine.setStroke(Color.GREEN);
        parallelLine.setStrokeWidth(1.9);
        builder.getLineStack().push(parallelLine);

    }


    public void addToRedLines(LineSegment redLine) {
        Line parallelLine = new Line(redLine.getCoordinate(0).getX(), redLine.getCoordinate(0).getY(), redLine.getCoordinate(1).getX(), redLine.getCoordinate(1).getY());
        parallelLine.setStroke(Color.RED);
        parallelLine.setStrokeWidth(1.9);
        builder.getLineStack().push(parallelLine);

    }

}
