package com.bachelor.visualpolygon.model.geometry;

import com.bachelor.visualpolygon.viewmodel.Camera;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


@NoArgsConstructor
public class Builder {

    Polygon polygon;
    GeometryCamera camera;
    List<LineString> segment;
    List<Vertex> vertices;
    GeometryFactory factory = new GeometryFactory();


    public Builder(Polygon geomPolygon, GeometryCamera geometryCamera) {
        // camera = new GeometryCamera();
        polygon = geomPolygon;
        System.out.println(camera);
        System.out.println(polygon);
    }

    public void updateBuilder(javafx.scene.shape.Polygon shapePolygon, Camera shapeCamera) {
        polygon = convertPolygonToGeometry(shapePolygon);
        camera = new GeometryCamera(shapeCamera);

        System.out.println("BUILDER:: " + polygon);

        System.out.println("BUILDER:: " + camera);
    }


    public void init() {
        PolarCoordinatesSort.convertToPolar((LinkedList<Vertex>) convertToListOfVertex(polygon.getCoordinates()), camera);
        System.out.println("AFTER INI" + vertices);

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


}
