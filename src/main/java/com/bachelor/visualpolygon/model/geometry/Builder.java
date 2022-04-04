package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;


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


    public Line createStreife(Vertex vertex) {
        Coordinate rightPointOnCircle = camera.findTangentPointsOnCameraFor(vertex).get(0);
        LineSegment leftTangent = new LineSegment(rightPointOnCircle,vertex.getCoordinate());
        Coordinate leftPointOnCircle = leftTangent.pointAlongOffset(0,-camera.getRadius()*2);
        Coordinate endRight = leftTangent.pointAlongOffset(1,-camera.getRadius()*2);

        Line line = new Line(leftPointOnCircle.getX(),leftPointOnCircle.getY(),endRight.getX(),endRight.getY());
        line.setStroke(Color.CADETBLUE);
        line.setStrokeLineCap(StrokeLineCap.ROUND);
        line.setStrokeWidth(2.5);
        return line;
    }

}
