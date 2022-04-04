package com.bachelor.visualpolygon.model.geometry;


import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@Getter
public class Builder {

    Polygon polygon;
    GeometryCamera camera = new GeometryCamera();
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
        polarSortedVertices.forEach(System.out::println);
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




}
