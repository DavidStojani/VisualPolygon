package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Builder;
import com.bachelor.visualpolygon.model.geometry.GeometryCamera;
import com.bachelor.visualpolygon.model.geometry.Step;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.scene.shape.Line;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;

import java.util.List;
import java.util.Objects;
import java.util.Stack;


@Getter
public class DataModelManager implements DataModel {

    org.locationtech.jts.geom.Polygon geomPolygon;
    GeometryFactory factory;
    GeometryCamera geometryCamera;
    Builder builder;
    Step step;


    public DataModelManager() {
        factory = new GeometryFactory();
        builder = new Builder();

    }


    @Override
    public void updateBuilder(List<Vertex> vertices, List<Double> camera) {
        builder.updateBuilder(vertices, camera);
    }

    @Override
    public void createStep() {
        step = new Step(builder);
        step.initStep();
    }

    @Override
    public String getStepInfo() {
        return String.valueOf(step.getActive().size());
    }

    @Override
    public List<Coordinate> getStreifenCoordinates(int index) {
        if (builder.getPolarSortedVertices().isEmpty()) {
            System.out.println("PolarCoordinatesFromBuilder IS EMPTY");
            return null;
        }

        if (Objects.isNull(builder.getNextVertex())) {
            System.out.println("WHZ");
        }

        return builder.createStreifeForBETA(builder.getPolarSortedVertices().get(index));

        //return builder.createStreifeForALPHA(builder.getNextVertex());
    }


    @Override
    public Stack<Line> getTheParallels() {
        return builder.getLineStack();
    }
}
