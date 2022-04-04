package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.collections.ObservableList;
import javafx.scene.shape.Line;

import java.util.List;
import java.util.Stack;

public interface DataModel {
    public Stack<Line> printStuff();


    void updateBuilder(List<Vertex> vertices, List<Double> camera);
}
