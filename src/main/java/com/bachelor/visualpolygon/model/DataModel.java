package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.collections.ObservableList;
import javafx.scene.shape.Line;

import java.util.List;

public interface DataModel {
    public Line printStuff();
    public ObservableList<Vertex> getVertices();

    void updateBuilder(List<Vertex> vertices, List<Double> camera);
}
