package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Builder;
import com.bachelor.visualpolygon.model.geometry.GeometryCamera;
import com.bachelor.visualpolygon.model.geometry.Step;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.scene.shape.Line;
import lombok.Getter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.Objects;
import java.util.Stack;


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
    public String getStepInfo() {
        if(Objects.isNull(builder.getActive())){
            return "Active is empty";
        }
        return String.valueOf(builder.getActive().size());
    }

    @Override
    public Polygon getPolygon() {
        return builder.getPolygon();
    }

    @Override
    public List<Coordinate> getStreifenCoordinates() {
        if (builder.getPolarSortedVertices().isEmpty()) {
            return null;
        }

        builder.createStep(builder.findNextAfterALPHA());
        return builder.getStepCoordinates();

        //return builder.createStreifeForALPHA(builder.getNextVertex());
    }


    @Override
    public Stack<Line> getTheParallels() {
        return builder.getLineStack();
    }

    @Override
    public void reset() {
        builder.getLineStack().clear();
        if(Objects.nonNull(builder.getActive())){
            builder.getActive().clear();
            //builder.getTempInvisible().clear();
            //builder.getTempVisible().clear();
        }
    }
}
