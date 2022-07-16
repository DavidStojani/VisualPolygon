package com.bachelor.visualpolygon.model.geometry;

import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.geom.*;

import java.util.*;
import java.util.stream.Collectors;

@Getter

public abstract class Initializer {
    static final PrecisionModel precision = new PrecisionModel(100);
    static final GeometryFactory factory = new GeometryFactory();
    static final double EPSILON = 0.0000005;
    List<Vertex> polarSortedVertices;
    Stack<Line> lineStack = new Stack<>();
    Vertex firstVertex;
    @Setter
    private int count = 0;


    void calculatePolarCoordinates(List<Vertex> vertexList) {
        double theta;
        for (Vertex vertex : vertexList) {
            theta = Angle.angleBetweenOriented(vertexList.get(0), Builder.camera.getCenter(), vertex);
            theta = Angle.normalizePositive(theta);
            vertex.setTheta(theta);
        }
    }


    static List<Vertex> sortPolarCoordinate(List<Vertex> vertices) {
        if (vertices.isEmpty()) {
            throw new RuntimeException("THIS List of Vertices IS EMPTY");
        }
        return vertices.stream()
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());
    }

    void addToBlackLine(LineSegment greenLine) {
        Line parallelLine = new Line(greenLine.p0.getX(), greenLine.p0.getY(), greenLine.p1.getX(), greenLine.p1.getY());
        parallelLine.setStroke(Color.BLACK);
        parallelLine.setStrokeWidth(1.9);
        lineStack.push(parallelLine);
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
        tempVertices.addAll(vertices);
        tempVertices.add(vertices.get(0));
        return factory.createPolygon(tempVertices.toCoordinateArray());
    }

    boolean isOnSameLine(Vertex nextVertex, Vertex actual, Coordinate base) {
        if (nextVertex.equals(actual)) {
            return false;
        }
        CoordinateList coordinates = new CoordinateList();
        coordinates.add(nextVertex);
        coordinates.add(actual);
        coordinates.add(base);
        return Area.ofRing(coordinates.toCoordinateArray()) == 0;
    }

    double getMaxDistanceFrom() {
        double max = 0;
        for (Vertex vertex : polarSortedVertices) {
            if (max < vertex.distance(Builder.camera.getRightTangentPoint(vertex))) {
                max = vertex.distance(Builder.camera.getRightTangentPoint(vertex));
            }
        }
        return max;
    }

    public boolean isScanComplete() {
        return count == (polarSortedVertices.size() * 2) + 3;
    }

    public void increaseCount() {
        count++;
    }

    public Map<String, Double> getParametersOfEquation(Coordinate v1, Coordinate v2) {
        HashMap<String, Double> parameter = new HashMap<>();
        double a = v1.getY() - v2.getY();
        double b = v2.getX() - v1.getX();
        double c = (v1.getX() * v2.getY()) - (v2.getX() * v1.getY());

        parameter.put("A", a);
        parameter.put("B", b);
        parameter.put("C", c);
        return parameter;
    }

    public boolean isInCollisionWithCamera(LineSegment segment) {

        return (Builder.camera.getRadius() >= segment.distancePerpendicular(Builder.camera.getCenter()));
    }

    public Coordinate getIntersectionPointWithCamera(LineSegment segment) {
        return getCircleLineIntersectionPoint(segment.p0,
                segment.p1,
                Builder.camera.getCenter(),
                Builder.camera.getRadius()).get(0);
    }

    public List<Coordinate> getCircleLineIntersectionPoint(Coordinate pointA,
                                                           Coordinate pointB,
                                                           Coordinate center,
                                                           double radius) {
        double baX = pointB.x - pointA.x;
        double baY = pointB.y - pointA.y;
        double caX = center.x - pointA.x;
        double caY = center.y - pointA.y;

        double a = baX * baX + baY * baY;
        double bBy2 = baX * caX + baY * caY;
        double c = caX * caX + caY * caY - radius * radius;

        double pBy2 = bBy2 / a;
        double q = c / a;

        double disc = pBy2 * pBy2 - q;
        if (disc < 0) {
            return Collections.emptyList();
        }
        // if disc == 0 ... dealt with later
        double tmpSqrt = Math.sqrt(disc);
        double abScalingFactor1 = -pBy2 + tmpSqrt;
        double abScalingFactor2 = -pBy2 - tmpSqrt;

        Coordinate p1 = new Coordinate(pointA.x - baX * abScalingFactor1, pointA.y
                - baY * abScalingFactor1);
        if (disc == 0) { // abScalingFactor1 == abScalingFactor2
            return Collections.singletonList(p1);
        }
        Coordinate p2 = new Coordinate(pointA.x - baX * abScalingFactor2, pointA.y
                - baY * abScalingFactor2);
        return Arrays.asList(p1, p2);
    }

}

