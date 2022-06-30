package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Step;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.scene.shape.Line;
import org.locationtech.jts.geom.Coordinate;


import java.util.List;
import java.util.Stack;

public interface DataModel {
    List<Coordinate> getStreifenCoordinates(int index);

    void updateBuilder(List<Vertex> vertices, List<Double> camera);

    String getStepInfo();

    Stack<Line> getTheParallels();
}
