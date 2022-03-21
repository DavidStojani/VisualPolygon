package com.bachelor.visualpolygon.model.geometry;

import com.bachelor.visualpolygon.viewmodel.Camera;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@NoArgsConstructor
@Getter
public class Builder {

    Polygon polygon;
    GeometryCamera camera;
    List<Vertex> vertices;
    private static final GeometryFactory factory = new GeometryFactory();


    /**
     * Takes Polygon and Camera as Shape Objects from View and updates with those the Geometry Objects
     */
    public void updateBuilder(javafx.scene.shape.Polygon shapePolygon, Camera shapeCamera) {
        polygon = convertPolygonToGeometry(shapePolygon);
        camera = new GeometryCamera(shapeCamera);
        init();
    }

    /**
     * After every update from View initializes the vertices
     */
    public void init() {
        vertices = convertToListOfVertex(polygon.getCoordinates());
        Initializer.calculatePolarCoordinates((LinkedList<Vertex>) vertices, camera);
        vertices = Initializer.sortPolarCoordinate(vertices);
        isVisibleFromCenter(vertices, camera);
        vertices.forEach(System.out::println);

    }

    private Polygon convertPolygonToGeometry(javafx.scene.shape.Polygon polygon) {
        ArrayList<Coordinate> coordList = new ArrayList<>();

        for (int i = 0; i < polygon.getPoints().size(); i += 2) {
            Coordinate coordinate = new Coordinate(polygon.getPoints().get(i), polygon.getPoints().get(i + 1));
            coordList.add(coordinate);
        }
        coordList.add(coordList.get(0));
        return factory.createPolygon(coordList.toArray(Coordinate[]::new));
    }

    private List<Vertex> convertToListOfVertex(Coordinate[] coordinates) {
        List<Vertex> result = new LinkedList<>();
        for (Coordinate c : coordinates) {
            result.add(new Vertex(c));
        }
        return result;
    }

    public static LineString createLineStringFor(Coordinate a, Coordinate b) {
        return factory.createLineString(new Coordinate[]{a, b});
    }

    public static LineString createLineStringFor(Vertex a, Vertex b) {
        return factory.createLineString(new Coordinate[]{a, b});
    }

    public void isVisibleFromCenter(List<Vertex> vertices, GeometryCamera camera) {
        for (Vertex vertex : vertices) {
            LineString segment = createLineStringFor(vertex.getCoordinate(), camera.getCenter());
            if (polygon.contains(segment)) {
                System.out.println("YES");
                vertex.setVisibleFromCenter(true);
            } else {
                System.out.println("NO");
                vertex.setVisibleFromCenter(false);
            }
        }


    }
}
