package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.scene.shape.Polygon;

import java.util.List;

public interface DataModel {

    List<Double> getStepPolygonPoints(int index);

    void updateBuilder(List<Vertex> vertices, List<Double> camera);
}
