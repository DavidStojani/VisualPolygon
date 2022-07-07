package com.bachelor.visualpolygon.model.geometry;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.*;

import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
@Getter
public abstract class Initializer {
    static final GeometryFactory factory = new GeometryFactory();
    final double EPSILON = 0.0000005;
    List<Vertex> polarSortedVertices;
    Stack<Line> lineStack = new Stack<>();
    Vertex firstVertex;


     void calculatePolarCoordinates(List<Vertex> vertexList, GeometryCamera camera) {
        double theta;
        for (Vertex vertex : vertexList) {
            theta = Angle.angleBetweenOriented(vertexList.get(0), camera.getCenter(), vertex.getCoordinate());
            theta = Angle.normalizePositive(theta);
            vertex.setTheta(theta);
        }
    }


    static List<Vertex> sortPolarCoordinate(List<Vertex> vertices) {
        if (vertices.isEmpty()) {
            throw new RuntimeException("THIS List of Vertices IS EMPTY");
        }
        List<Vertex> vertexList = vertices.stream()
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());
        return vertexList;
    }

    void addToGreenLines(LineSegment greenLine) {
        Line parallelLine = new Line(greenLine.getCoordinate(0).getX(), greenLine.getCoordinate(0).getY(), greenLine.getCoordinate(1).getX(), greenLine.getCoordinate(1).getY());
        parallelLine.setStroke(Color.GREEN);
        parallelLine.setStrokeWidth(1.9);
        lineStack.push(parallelLine);
    }

    void addToRedLines(LineSegment redLine) {
        Line parallelLine = new Line(redLine.getCoordinate(0).getX(), redLine.getCoordinate(0).getY(), redLine.getCoordinate(1).getX(), redLine.getCoordinate(1).getY());
        parallelLine.setStroke(Color.RED);
        parallelLine.setStrokeWidth(1.9);
        lineStack.push(parallelLine);
    }


    Polygon createStepPolygon(CoordinateList vertices) {
        vertices.add(vertices.get(0));
        return factory.createPolygon(vertices.toCoordinateArray());
    }

    Polygon createGeometryPolygon(List<Vertex> vertices) {
        CoordinateList tempVertices = new CoordinateList();
        for (Vertex vertex : vertices) {
            tempVertices.add(vertex.getCoordinate());
        }
        tempVertices.add(vertices.get(0).getCoordinate());
        return factory.createPolygon(tempVertices.toCoordinateArray());
    }

    boolean isOnSameLine(Vertex nextVertex, Vertex actual, Coordinate base) {
        if (nextVertex.getCoordinate().equals(actual.getCoordinate())) {
            return false;
        }
        CoordinateList coordinates = new CoordinateList();
        coordinates.add(nextVertex.getCoordinate());
        coordinates.add(actual.getCoordinate());
        coordinates.add(base);
        if (Area.ofRing(coordinates.toCoordinateArray()) == 0) {
            return true;
        }
        return false;
    }

    double getMaxDistanceFrom(GeometryCamera camera) {
        double max = 0;
        for (Vertex vertex : polarSortedVertices) {
            if (max < vertex.getCoordinate().distance(camera.getRightTangentPoint(vertex))) {
                max = vertex.getCoordinate().distance(camera.getRightTangentPoint(vertex));
            }
        }
        return max;
    }

    public boolean IsScanComplete() {
        return firstVertex.getVisited() == 4;
    }

}

