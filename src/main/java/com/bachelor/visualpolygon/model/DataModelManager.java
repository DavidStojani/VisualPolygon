package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Builder;
import com.bachelor.visualpolygon.model.geometry.GeometryCamera;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.view.shapes.Camera;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import lombok.*;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;


@Getter
public class DataModelManager implements DataModel {

    org.locationtech.jts.geom.Polygon geomPolygon;
    GeometryFactory factory;
    GeometryCamera geometryCamera;
    Builder builder;

    public DataModelManager() {
        factory = new GeometryFactory();
        builder = new Builder();
    }


    @Override
    public void updateBuilder(List<Vertex> vertices, List<Double> camera) {
        builder.updateBuilder(vertices, camera);
    }

    @Override
    public Line testTangent() {
       /* if (Objects.isNull(geometryCamera)) {
            System.out.println("Camera null");
            return null;
        } else {
            Point vertex = factory.createPoint(geomPolygon.getCoordinates()[0]);
            Coordinate[] onCircle = geometryCamera.findTangentPointsOnCameraFor(geomPolygon.getCoordinates()[0]);
            Line line = new Line(vertex.getX(),vertex.getY(), onCircle[0].getX(),onCircle[0].getY());
            line.setStrokeWidth(3);
            line.setStroke(Color.RED);*/
        return null;
    }

    @Override
    public ObservableList<Vertex> getVertices() {
        ObservableList<Vertex> vertices = FXCollections.observableArrayList();
        vertices.setAll(builder.getVertices());
        return vertices;
    }



}
