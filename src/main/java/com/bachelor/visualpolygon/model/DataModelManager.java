package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Builder;
import com.bachelor.visualpolygon.model.geometry.GeometryCamera;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.scene.shape.Line;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateList;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
        System.out.println("CAMERA incoming " + camera);
        builder.updateBuilder(vertices, camera);
    }

    @Override
    public boolean isScanReady() {
       return builder.isScanComplete();
    }

    @Override
    public void createVisPolygon() {
        builder.createVisPolygon();
    }

    @Override
    public Polygon getPolygon() {
        return builder.getPolygon();
    }

    @Override
    public List<Coordinate> getStepCoordinates() {
        if (builder.getVertices().isEmpty()) {
            return Collections.emptyList();
        }
        builder.createStep(builder.getNextVertex());
        return builder.getStepCoordinates();
    }

    @Override
    public List<Line> getAllLines() {
        return builder.getAllLines();
    }

    @Override
    public void reset() {
        builder.clearLines();
        builder.setNextVertex(null);
        builder.setCount(0);
        if(Objects.nonNull(builder.getActive())){
            builder.getActive().clear();
            builder.getTempInvisible().clear();
            builder.getTempVisible().clear();
        }
    }

    @Override
    public CoordinateList getVisualPolygon() {
        return builder.getVisPolygonVertices();
    }

    @Override
    public CoordinateList getInstantVisualPolygon(List<Double> camera) {
        return builder.getInstantVisualPolygon(camera);
    }


}
