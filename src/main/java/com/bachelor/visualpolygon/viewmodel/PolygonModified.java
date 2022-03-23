package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.view.Point;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PolygonModified extends javafx.scene.shape.Polygon {

    private ObservableList<Vertex> vertices;

    public PolygonModified(List<Vertex> vertices) {
        super();
        this.vertices = FXCollections.observableArrayList(vertices);
        for (int i = 0; i < vertices.size(); i++) {
            getPoints().add(vertices.get(i).getX());
            getPoints().add(vertices.get(i).getY());
        }
    }


    public ObservableList<Point> createModeratePoints() {
        ObservableList<Point> points = FXCollections.observableArrayList();

        for (int i = 0; i < vertices.size(); i++) {
            final int idx = i;

            DoubleProperty xProperty = vertices.get(idx).getXProperty();
            DoubleProperty yProperty = vertices.get(idx).getYProperty();

            xProperty.addListener((observableValue, number, t1) -> {
                vertices.get(idx).setX((double) t1);
                updatePoints();
            });
            yProperty.addListener((observableValue, number, t1) -> {
                vertices.get(idx).setY((double) t1);
                updatePoints();
            });

            if (!vertices.get(idx).isVisibleFromCenter()) {
                Point p = new Point(xProperty, yProperty);
                p.changeColor();
                points.add(p);
            } else {
                Point p = new Point(xProperty, yProperty);
                points.add(p);
            }
        }
        return points;
    }

    public void addVertexAndPoint(Vertex vertex) {
        this.vertices.add(vertex);
        getPoints().add(vertex.getX());
        getPoints().add(vertex.getY());
    }

    public void removeVertexAndPoint(Point point) {
        int indexOfX = getPoints().indexOf(point.getCenterX());
        int indexOfY = getPoints().indexOf(point.getCenterY());

        if (indexOfY == indexOfX + 1) {
            vertices.removeIf(v -> v.getX() == point.getCenterX() & v.getY() == point.getCenterY());
            getPoints().remove(indexOfX);
            getPoints().remove(indexOfX);
        }
    }

    public javafx.scene.shape.Polygon draw() {
        this.setStroke(Color.FORESTGREEN);
        this.setStrokeWidth(3);
        this.setStrokeLineCap(StrokeLineCap.ROUND);
        this.setFill(Color.GOLDENROD.deriveColor(0, 1.2, 1, 0.6));

        return this;
    }

    private void updatePoints() {
        getPoints().clear();
        for (int i = 0; i < vertices.size(); i++) {
            getPoints().add(vertices.get(i).getX());
            getPoints().add(vertices.get(i).getY());
        }
    }
}
