package com.bachelor.visualpolygon.model.geometry;

import org.locationtech.jts.math.Vector2D;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Initializer {

    public static void calculatePolarCoordinates(List<Vertex> vertexList, GeometryCamera camera) {
        double theta;
        double r;
        double x;
        double y;                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
        for (Vertex vertex : vertexList) {

            r = vertex.getCoordinate().distance(camera.getCenter());
            vertex.setR(r);
            x = vertex.getXCoordinate() - camera.getCenterX();
            y = camera.getCenterY() - vertex.getYCoordinate();
            theta = Math.atan2(y , x);
            vertex.setTheta(Math.toRadians(theta));
        }
    }


    public static List<Vertex> sortPolarCoordinate(List<Vertex> vertices) {
        if (vertices.isEmpty()) {
            throw new RuntimeException("THIS List of Vertices IS EMPTY");
        }
        return vertices.stream()
                .sorted(Comparator.comparing(Vertex::getTheta))
                .collect(Collectors.toList());
    }


}

