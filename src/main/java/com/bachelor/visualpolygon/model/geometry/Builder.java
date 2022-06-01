package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


@NoArgsConstructor
@Getter
public class Builder {

    Polygon polygon;
    public final static GeometryCamera camera = new GeometryCamera();
    List<Vertex> vertices;
    List<Vertex> polarSortedVertices;
    private static final GeometryFactory factory = new GeometryFactory();

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
        isVisibleFromCenter(vertices, camera);
    }

    private Polygon createGeometryPolygon(List<Vertex> vertices) {
        ArrayList<Coordinate> tempVertices = new ArrayList<>();
        for (Vertex vertex : vertices) {
            tempVertices.add(vertex.getCoordinate());
        }
        tempVertices.add(vertices.get(0).getCoordinate());
        return factory.createPolygon(tempVertices.toArray(Coordinate[]::new));
    }

    public static LineString createLineStringFor(Coordinate a, Coordinate b) {
        return factory.createLineString(new Coordinate[]{a, b});
    }


    public void isVisibleFromCenter(List<Vertex> vertices, GeometryCamera camera) {
        for (Vertex vertex : vertices) {

            LineString segment = createLineStringFor(vertex.getCoordinate(),camera.getCenter());
            vertex.setVisibleFromCenter(polygon.contains(segment));
        }
    }


    //Should give back the 4 coordinates. Those should be given to form the polygon and
    //to the viewController to render the view. In the view they should not cross the borders of pane
    public List<Line> createStreife(Vertex vertex) {
        Stack<Line> streife = new Stack<>();
        Coordinate rightPointOnCircle = camera.getRightTangentPoint(vertex);
        LineSegment rightTangent = new LineSegment(rightPointOnCircle,getExtentCoordinate(vertex));
        Coordinate leftPointOnCircle = rightTangent.pointAlongOffset(0,-camera.getRadius()*2);
        Coordinate mirrorOfExtent = rightTangent.pointAlongOffset(1,-camera.getRadius()*2);

        Line leftLine = new Line(leftPointOnCircle.getX(),leftPointOnCircle.getY(),mirrorOfExtent.getX(),mirrorOfExtent.getY());
        leftLine.setStroke(Color.RED);
        leftLine.setStrokeLineCap(StrokeLineCap.ROUND);
        leftLine.setStrokeWidth(2.5);


        Line rightLine = new Line(getExtentCoordinate(vertex).getX(), getExtentCoordinate(vertex).getY(), rightPointOnCircle.getX(), rightPointOnCircle.getY());
        rightLine.setStrokeWidth(2.6);
        rightLine.setStroke(Color.CADETBLUE);
        streife.add(leftLine);
        streife.add(rightLine);
        return streife;
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

    public Coordinate getExtentCoordinate(Vertex vertex) {
        Vector2D vector = new Vector2D(camera.getRightTangentPoint(vertex), vertex.getCoordinate());
        System.out.println("FIRST" + vector.toCoordinate());
        System.out.println("FACTOR K " + getMax());

        double k = getMax() / (camera.getRightTangentPoint(vertex).distance(vertex.getCoordinate()));

        System.out.println("FACTOR K " + k);
        Vector2D extentVector = vector.multiply(k);
        System.out.println(extentVector.toCoordinate());
        double x = extentVector.getX() + camera.getRightTangentPoint(vertex).getX();
        double y = extentVector.getY() + camera.getRightTangentPoint(vertex).getY();
        return new Coordinate(x,y);
    }
}
