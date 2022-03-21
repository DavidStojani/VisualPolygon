package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.viewmodel.Camera;
import javafx.geometry.Point2D;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public interface DataModel {
    public Line testTangent();

    void updateBuilder(Polygon polygon, Camera camera);
}
