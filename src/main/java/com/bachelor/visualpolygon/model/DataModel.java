package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.view.shapes.Camera;
import javafx.collections.ObservableList;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

import java.util.ArrayList;
import java.util.List;

public interface DataModel {
    public Line testTangent();
    public ObservableList<Vertex> getVertices();

    void updateBuilder(List<Vertex> vertices, List<Double> camera);
}
