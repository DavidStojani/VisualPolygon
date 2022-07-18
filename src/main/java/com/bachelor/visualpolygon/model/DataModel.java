package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.scene.shape.Line;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.Polygon;

import java.util.List;


public interface DataModel {

    Polygon getPolygon();

    List<Coordinate> getStepCoordinates();

    void updateBuilder(List<Vertex> vertices, List<Double> camera);

    List<Line> getAllLines();

    boolean isScanReady();

    void createVisPolygon();

    void reset();

    CoordinateList getVisualPolygon();
}
