package com.bachelor.visualpolygon.model.geometry;

import org.locationtech.jts.algorithm.Angle;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.math.Vector2D;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Initializer {

    public static void calculatePolarCoordinates(List<Vertex> vertexList, GeometryCamera camera) {
        double theta;
        for (Vertex vertex : vertexList) {
            theta = Angle.angleBetweenOriented(vertexList.get(0),camera.getCenter(),vertex.getCoordinate());
            theta = Angle.normalizePositive(theta);
            vertex.setTheta(theta);
        }
    }


    public static List<Vertex> sortPolarCoordinate(List<Vertex> vertices) {
        if (vertices.isEmpty()) {
            throw new RuntimeException("THIS List of Vertices IS EMPTY");
        }
        List<Vertex> vertexList = vertices.stream()
                .sorted(Comparator.comparing(Vertex::getTheta).reversed())
                .collect(Collectors.toList());
        return vertexList;
    }


}

