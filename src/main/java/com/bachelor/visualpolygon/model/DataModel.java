package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Step;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.scene.shape.Line;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Polygon;


import java.util.List;
import java.util.Stack;

public interface DataModel {
    Polygon getPolygon();

    List<Coordinate> getStreifenCoordinates();

    void updateBuilder(List<Vertex> vertices, List<Double> camera);

    String getStepInfo();

    Stack<Line> getTheParallels();

    void reset();
}
