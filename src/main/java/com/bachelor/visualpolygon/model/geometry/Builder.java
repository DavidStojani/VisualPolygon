package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


@NoArgsConstructor
@Getter
public class Builder {

    Polygon polygon;
    Polygon stepPolygon;
    public final static GeometryCamera camera = new GeometryCamera();
    List<Vertex> vertices;
    List<Vertex> polarSortedVertices;
    private static final GeometryFactory factory = new GeometryFactory();
    List<Coordinate> stepCoordinates = new ArrayList<>();
    Stack<Line> lineStack = new Stack<>();
    @Setter
    Vertex nextVertex;


    /**
     * Takes Polygon and Camera as Shape Objects from View and updates with those the Geometry Objects
     */
    public void updateBuilder(List<Vertex> vertices, List<Double> cameraDetails) {
        this.vertices = vertices;
        if (!cameraDetails.isEmpty()) {
            camera.setDetails(cameraDetails);
            init();
        }
    }

    /**
     * After every update from View initializes the vertices
     */
    public void init() {
        polygon = createGeometryPolygon(vertices);
        Initializer.calculatePolarCoordinates(vertices, camera);
        polarSortedVertices = Initializer.sortPolarCoordinate(vertices);
        nextVertex = polarSortedVertices.get(0);
    }

    private Polygon createGeometryPolygon(List<Vertex> vertices) {
        ArrayList<Coordinate> tempVertices = new ArrayList<>();
        for (Vertex vertex : vertices) {
            tempVertices.add(vertex.getCoordinate());
        }
        tempVertices.add(vertices.get(0).getCoordinate());
        return factory.createPolygon(tempVertices.toArray(Coordinate[]::new));
    }

    public Polygon createStepPolygon(List<Coordinate> vertices) {
        ArrayList<Coordinate> tempVertices = new ArrayList<>();
        for (Coordinate vertex : vertices) {
            tempVertices.add(vertex);
        }
        tempVertices.add(vertices.get(0));
        return factory.createPolygon(tempVertices.toArray(Coordinate[]::new));
    }

    public static LineString createLineStringFor(Coordinate a, Coordinate b) {
        return factory.createLineString(new Coordinate[]{a, b});
    }


    public void isVisibleFromCenter(List<Vertex> vertices, GeometryCamera camera) {
        for (Vertex vertex : vertices) {

            LineString segment = createLineStringFor(vertex.getCoordinate(), camera.getCenter());
            if (polygon.contains(segment)) {
                vertex.setIsVisible(1);
            }
        }
    }


    //Should give back the 4 coordinates. Those should be given to form the polygon and
    //to the viewController to render the view. In the view they should not cross the borders of pane
    public List<Coordinate> createStreifeForBETA(Vertex vertex) {
        List<Coordinate> streifenCoordinates = new ArrayList<>();

        Coordinate rightPointOnCircle = camera.getRightTangentPoint(vertex);
        streifenCoordinates.add(rightPointOnCircle);
        streifenCoordinates.add(getExtentCoordinateForBETA(vertex));
        LineSegment rightTangent = new LineSegment(rightPointOnCircle, getExtentCoordinateForBETA(vertex));

        Coordinate leftPointOnCircle = rightTangent.pointAlongOffset(0, -camera.getRadius() * 2);
        Coordinate mirrorOfExtent = rightTangent.pointAlongOffset(1, -camera.getRadius() * 2);
        streifenCoordinates.add(mirrorOfExtent);
        streifenCoordinates.add(leftPointOnCircle);

        stepPolygon = createStepPolygon(streifenCoordinates);
        stepCoordinates = streifenCoordinates;

        return streifenCoordinates;
    }

    /**TODO Here sometimes ends in a loop and sometimes calls wrong function for points inside active*/
    public List<Coordinate> createStreife(Vertex vertex) {
        if (stepCoordinates.isEmpty()) {
            return createStreifeForBETA(vertex);
        }

        if (nextVertex.isInsideActive()) {
            nextVertex.setInsideActive(false);
            return createStreifeForBETA(vertex);
        }

        nextVertex.setInsideActive(false);
        return createStreifeForALPHA(vertex);

    }

    public List<Coordinate> createStreifeForALPHA(Vertex vertex) {
        List<Coordinate> streifenCoordinates = new ArrayList<>();

        Coordinate leftPointOnCircle = camera.getLeftTangentPoint(vertex);
        streifenCoordinates.add(leftPointOnCircle);
        streifenCoordinates.add(getExtentCoordinateForALPHA(vertex));
        LineSegment leftTangent = new LineSegment(leftPointOnCircle, getExtentCoordinateForALPHA(vertex));

        Coordinate rightPointOnCircle = leftTangent.pointAlongOffset(0, camera.getRadius() * 2);
        Coordinate mirrorOfExtent = leftTangent.pointAlongOffset(1, camera.getRadius() * 2);
        streifenCoordinates.add(mirrorOfExtent);
        streifenCoordinates.add(rightPointOnCircle);

        stepPolygon = createStepPolygon(streifenCoordinates);
        stepCoordinates = streifenCoordinates;
        addRadiusLines();

        return streifenCoordinates;
    }

    public void addRadiusLines() {
        Line line = new Line(stepCoordinates.get(0).getX(),stepCoordinates.get(0).getY(),stepCoordinates.get(3).getX(),stepCoordinates.get(3).getY());
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1.9);
        lineStack.push(line);
    }


    private double getMax() {
        double max = 0;
        for (Vertex vertex : polarSortedVertices) {
            if (max < vertex.getCoordinate().distance(camera.getRightTangentPoint(vertex))) {
                max = vertex.getCoordinate().distance(camera.getRightTangentPoint(vertex));
            }
        }
        return max;
    }

    public Coordinate getExtentCoordinateForBETA(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getRightTangentPoint(vertex), vertex.getCoordinate());
        double k = getMax() / (camera.getRightTangentPoint(vertex).distance(vertex.getCoordinate()));
        Vector2D extentVector = vector.multiply(k);
        double x = extentVector.getX() + camera.getRightTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getRightTangentPoint(vertex).getY();
        return new Coordinate(x, y);
    }

    public Coordinate getExtentCoordinateForALPHA(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getLeftTangentPoint(vertex), vertex.getCoordinate());
        double k = getMax() / (camera.getLeftTangentPoint(vertex).distance(vertex.getCoordinate()));
        Vector2D extentVector = vector.multiply(k);
        double x = extentVector.getX() + camera.getLeftTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getLeftTangentPoint(vertex).getY();
        return new Coordinate(x, y);
    }

}
