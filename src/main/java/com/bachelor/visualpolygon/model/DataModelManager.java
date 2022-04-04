package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.model.geometry.Builder;
import com.bachelor.visualpolygon.model.geometry.GeometryCamera;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.*;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
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
    public Stack<Line> printStuff() {

        Stack<Line> lines = new Stack<>();
        for (Vertex vertex : builder.getPolarSortedVertices()) {
            Coordinate leftTangentOnCircle = new Coordinate(builder.camera.findTangentPointsOnCameraFor(vertex).get(0));
            // Line line = new Line(vertex.getXCoordinate(),vertex.getYCoordinate(),builder.getCamera().getCenterX(),builder.getCamera().getCenterY());
            Line line = new Line(vertex.getXCoordinate(), vertex.getYCoordinate(), leftTangentOnCircle.getX(), leftTangentOnCircle.getY());
            line.setStrokeWidth(2.6);
            line.setStroke(Color.CADETBLUE);

            lines.push(builder.createStreife(vertex));
            lines.push(line);
        }
        return lines;
    }
}
