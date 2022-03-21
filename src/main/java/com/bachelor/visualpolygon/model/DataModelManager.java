package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Builder;
import com.bachelor.visualpolygon.model.geometry.GeometryCamera;
import com.bachelor.visualpolygon.viewmodel.Camera;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import lombok.*;
import org.locationtech.jts.geom.*;

import java.util.*;


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
    public void updateBuilder(Polygon polygon, Camera camera) {
        builder.updateBuilder(polygon, camera);
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

}
