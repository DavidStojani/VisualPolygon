package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import java.nio.channels.AcceptPendingException;
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
    private LineSegment ALPHA;
    private LineSegment BETA;


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


    /**
     * TODO Here sometimes ends in a loop and sometimes calls wrong function for points inside active
     */
    public List<Coordinate> createStreife(Vertex vertex) {

        if (stepCoordinates.isEmpty()) {
            System.out.println("[ON builder.createStreife] ::: ACTIVE IS EMPTY");
            return createStepFromALPHA(vertex);
        }

        if (isInsideActive(vertex)) {
            return createStepFromBETA(vertex);
        }

        return createStepFromALPHA(vertex);

    }

    public boolean isInsideActive(Vertex vertex) {
        int i = Orientation.index(ALPHA.getCoordinate(0), ALPHA.getCoordinate(1), vertex.getCoordinate());
        if (i == Orientation.LEFT) {
            System.out.println("[ON builder.isInsideActive]"+ i + "ON THE LEFT");
            return true;
        }
        System.out.println("[ON builder.isInsideActive]"+ i + "ON THE RIGHT");
        vertex.setInGrey(true);
        return false;
    }


    //Should give back the 4 coordinates. Those should be given to form the polygon and
    //to the viewController to render the view. In the view they should not cross the borders of pane
    public List<Coordinate> createStepFromBETA(Vertex vertex) {
        List<Coordinate> streifenCoordinates = new ArrayList<>();
        System.out.println("BETA CALLED");

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
        setALPHA(streifenCoordinates.get(3), streifenCoordinates.get(2));
        setBETA(streifenCoordinates.get(0), streifenCoordinates.get(1));
        addRadiusLines();

        System.out.println("BETA TERMINDATED");
        return streifenCoordinates;
    }

    public List<Coordinate> createStepFromALPHA(Vertex vertex) {
        List<Coordinate> streifenCoordinates = new ArrayList<>();
        System.out.println("ALPHA CALLED");
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
        setALPHA(streifenCoordinates.get(0), streifenCoordinates.get(1));
        setBETA(streifenCoordinates.get(3), streifenCoordinates.get(2));
        addRadiusLines();

        System.out.println("ALPHA TERMINATED");
        return streifenCoordinates;
    }
    public void addRadiusLines() {
        Line line = new Line(ALPHA.getCoordinate(0).getX(), ALPHA.getCoordinate(0).getY(), ALPHA.getCoordinate(1).getX(), ALPHA.getCoordinate(1).getY());
        line.setStroke(Color.BLACK);
        line.setStrokeWidth(1.9);
        lineStack.push(line);
    }


    public Polygon createStepPolygon(List<Coordinate> vertices) {
        ArrayList<Coordinate> tempVertices = new ArrayList<>();
        for (Coordinate vertex : vertices) {
            tempVertices.add(vertex);
        }
        tempVertices.add(vertices.get(0));
        return factory.createPolygon(tempVertices.toArray(Coordinate[]::new));
    }

    private Polygon createGeometryPolygon(List<Vertex> vertices) {
        ArrayList<Coordinate> tempVertices = new ArrayList<>();
        for (Vertex vertex : vertices) {
            tempVertices.add(vertex.getCoordinate());
        }
        tempVertices.add(vertices.get(0).getCoordinate());
        return factory.createPolygon(tempVertices.toArray(Coordinate[]::new));
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

    private Coordinate getExtentCoordinateForBETA(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getRightTangentPoint(vertex), vertex.getCoordinate());
        double k = getMax() / (camera.getRightTangentPoint(vertex).distance(vertex.getCoordinate()));
        Vector2D extentVector = vector.multiply(k);
        double x = extentVector.getX() + camera.getRightTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getRightTangentPoint(vertex).getY();
        return new Coordinate(x, y);
    }

    private Coordinate getExtentCoordinateForALPHA(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getLeftTangentPoint(vertex), vertex.getCoordinate());
        double k = getMax() / (camera.getLeftTangentPoint(vertex).distance(vertex.getCoordinate()));
        Vector2D extentVector = vector.multiply(k);
        double x = extentVector.getX() + camera.getLeftTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getLeftTangentPoint(vertex).getY();
        return new Coordinate(x, y);
    }

    private void setALPHA(Coordinate start, Coordinate end) {
        ALPHA = new LineSegment(start, end);
    }

    private void setBETA(Coordinate start, Coordinate end) {
        BETA = new LineSegment(start, end);
    }
}
