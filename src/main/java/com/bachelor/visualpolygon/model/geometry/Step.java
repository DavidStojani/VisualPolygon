package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.overlay.validate.FuzzyPointLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@Getter
public class Step {
    Builder builder;
    Polygon initialPolygon;
    Polygon stepPolygon;
    List<Vertex> active;
    List<Vertex> tempVisible;
    List<Vertex> tempInvisible;
    LineSegment ALPHA;
    LineSegment BETA;

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
        ALPHA = builder.getALPHA();
        BETA = builder.getBETA();
        setActive();
        //setTemps();
        findNextVertexToBuildStep();
    }

    private void setActive() {
        /**TODO Warum ist Tolerance 2 ?*/
        FuzzyPointLocator pointLocator = new FuzzyPointLocator(stepPolygon, 2);
        for (Vertex vertex : builder.getPolarSortedVertices()) {
            if (pointLocator.getLocation(vertex.getCoordinate()) != 2) {
                active.add(vertex);
            }
        }
    }

    public void findNextVertexToBuildStep() {
        if (active.isEmpty()) {
            System.out.println("===============");
            System.out.println("ACTIVE IS EMPTY");

            System.out.println("===============");
            builder.getPolarSortedVertices().get(0).setInBlue(true);
            builder.setNextVertex(builder.getPolarSortedVertices().get(0));
            return;
        }


        /**TODO THIS needs to be checked again**/
        //ALPHA and BETA not always the same coordinates, depending on wich step is called
        Optional<Vertex> afterAktive = Optional.ofNullable(builder.getPolarSortedVertices().stream()
                .filter(vertex -> vertex.getTheta() > active.get(active.size() - 1).getTheta())
                .findFirst().orElse(builder.getPolarSortedVertices().get(0)));

        if (active.size() <= 1) {
            System.out.println("ACTIVE KLEINER/GLEICH ALS 1");
            afterAktive.get().setInBlue(true);
            builder.setNextVertex(afterAktive.get());
            return;
        }


        Vertex firstInsideAktive = null;
        for (Vertex vertex : active) {
            double mindistace = 99999;
            if (BETA.distance(vertex.getCoordinate()) <= mindistace) {
                System.out.println("FOUND IN AKTIVE ");
                mindistace = BETA.distance(vertex.getCoordinate());
                firstInsideAktive = vertex;
                firstInsideAktive.isGrey();
            }else {

                firstInsideAktive = active.get(0);
    //            firstInsideAktive.setInGrey(true);
            }
        }



        if (ALPHA.distancePerpendicular(afterAktive.get().getCoordinate()) > BETA.distancePerpendicular(firstInsideAktive.getCoordinate())) {
            builder.setNextVertex(firstInsideAktive);
        } else {
            builder.setNextVertex(afterAktive.get());
        }

    }

    //for every point in active build a parallel to the Streifen and check if it intersects with the circle with no interruption
    private void setTemps() {
        for (Vertex vertex : active) {
            LineSegment parallelToBeChecked = getParallelLineForALPHA(vertex.getCoordinate());
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

    public LineSegment getParallelLineForBETA(Coordinate point) {
        LineSegment lineSegment = new LineSegment(stepPolygon.getCoordinates()[0], stepPolygon.getCoordinates()[1]);
        Coordinate baseMirror = lineSegment.pointAlongOffset(0, -lineSegment.distance(point));
        Coordinate endMirror = lineSegment.pointAlongOffset(1, -lineSegment.distance(point));
        LineSegment parallelToStep = new LineSegment(baseMirror, point);

        return parallelToStep;
    }

    public LineSegment getParallelLineForALPHA(Coordinate point) {
        LineSegment lineSegment = new LineSegment(stepPolygon.getCoordinates()[0], stepPolygon.getCoordinates()[1]);
        Coordinate baseMirror = lineSegment.pointAlongOffset(0, lineSegment.distancePerpendicular(point));
        Coordinate endMirror = lineSegment.pointAlongOffset(1, lineSegment.distancePerpendicular(point));
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
