package com.bachelor.visualpolygon.model.geometry;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PolarCoordinatesSort {


    public static void convertToPolar(LinkedList<Vertex> vertexList, GeometryCamera camera) {
        double x;
        double radius;
        for (Vertex vertex : vertexList) {
            if (camera.getCenter() == null) {
                throw new RuntimeException("Camera Center is empty");
            }
            x = vertex.getX() - camera.getCenterX();
            radius = vertex.distance(camera.getCenter().getCoordinate());

            vertex.setR(radius);
            vertex.setTheta(Math.toDegrees(Math.acos(x / vertex.getR())));
        }
    }

    public static void convertToPolar(Vertex[] vertexList, GeometryCamera camera) {
        double x;
        for (Vertex vertex : vertexList) {
            x = vertex.getX() - camera.getCenterX();
            vertex.setR(vertex.distance(camera.getCenter().getCoordinate()));
            vertex.setTheta(Math.toDegrees(Math.acos(x / vertex.getR())));
        }
    }

    public static List<Vertex> sortPolarCoordinate(List<Vertex> vertices) {
        return vertices.stream()
                .sorted(Comparator.comparing(Vertex::getTheta))
                .collect(Collectors.toList());
    }

}
