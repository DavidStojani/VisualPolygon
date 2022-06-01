package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.awt.PolygonShape;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.math.Vector2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@NoArgsConstructor
@Getter
public class Builder {

    Polygon polygon;
    public static final GeometryCamera camera = new GeometryCamera();
    List<Vertex> vertices;
    List<Vertex> polarSortedVertices;
    private static final GeometryFactory factory = new GeometryFactory();
    private double maxDistance;
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
        maxDistance = findFurthestVertex();

    }

    //Need 4 coordinates to build the STEP.
    //1. the extent of vertex on polygon
    //2. its Tangent point L on the Camera
    //3. point R, mirror of L on the Camera
    //4. the mirror of the extent
    public Polygon buildStep(Vertex vertex) {
        Coordinate tangentPointL = camera.getLeftTangentPointFor(vertex);
        Coordinate extentOfVertex = getExtentCoordinate(tangentPointL, vertex.getCoordinate());
        LineSegment line = new LineSegment(tangentPointL, extentOfVertex);
        Coordinate mirrorOfTangentPointL = line.pointAlongOffset(0, -camera.getRadius()*2);
        Coordinate mirrorOfExtent = line.pointAlongOffset(1, -camera.getRadius()*2);

        Polygon stepPolygon = factory.createPolygon(new Coordinate[]{tangentPointL,extentOfVertex,mirrorOfExtent,mirrorOfTangentPointL,tangentPointL});
        return stepPolygon;
    }


    private Coordinate getExtentCoordinate(Coordinate tangentPointL, Coordinate vertex) {
        Vector2D initVector = new Vector2D(tangentPointL, vertex);
        double k = maxDistance/ initVector.length();

        System.out.println("K IST :::" + k);

        System.out.println("Initial Vector IST :::" + initVector);

        Vector2D extentVector = initVector.multiply(k);

        System.out.println("NEW VECTOR IST :::" + extentVector);
        return new Coordinate(extentVector.getX(), extentVector.getY());
    }

    private double findFurthestVertex() {
        double max = 0;
        Map<Double, Vertex> map = new HashMap<>();
        for (Vertex vertex : vertices) {
            if (max < vertex.getCoordinate().distance(camera.getLeftTangentPointFor(vertex))) {
                max = vertex.getCoordinate().distance(camera.getLeftTangentPointFor(vertex));
                System.out.println("map ist" + map);
                map.clear();
                map.put(max,vertex);
            }
        }
        System.out.println( "THE MAP IST ::: " + map);
        return max;
    }

    private Polygon createGeometryPolygon(List<Vertex> vertices) {
        ArrayList<Coordinate> tempVertices = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++) {
            tempVertices.add(vertices.get(i).getCoordinate());
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
            if (polygon.contains(segment)) {
                vertex.setVisibleFromCenter(true);
            } else {
                vertex.setVisibleFromCenter(false);
            }
        }
    }


}
