package com.bachelor.visualpolygon.model.geometry;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Initializer {

    public static void calculatePolarCoordinates(List<Vertex> vertexList, GeometryCamera camera) {
        double x;
        double radius;
        for (Vertex vertex : vertexList) {
            if (camera.getCenter() == null) {
                throw new RuntimeException("Camera Center is empty");
            }
            x = vertex.getXCoordinate() - camera.getCenterX();
            radius = vertex.distance(camera.getCenter());

            vertex.setR(radius);
            vertex.setTheta(Math.toDegrees(Math.acos(x / vertex.getR())));
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

