package com.bachelor.visualpolygon.view.shapes;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import com.bachelor.visualpolygon.view.shapes.Point;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode
public class PolygonModified extends Polygon {

    public static final ObservableList<Vertex> vertices = FXCollections.observableArrayList();

    public PolygonModified() {
        super();
        System.out.println("POINTS ARE EMPTY: " + getPoints().isEmpty());
        for (int i = 0; i < vertices.size(); i++) {
            getPoints().add(vertices.get(i).getX());
            getPoints().add(vertices.get(i).getY());
        }
    }


    /*public ObservableList<Point> createModeratePoints() {
        ObservableList<Point> points = FXCollections.observableArrayList();

        for (int i = 0; i < this.vertices.size(); i++) {
            final int idx = i;

            DoubleProperty xProperty = this.vertices.get(idx).getXProperty();
            DoubleProperty yProperty = this.vertices.get(idx).getYProperty();

            xProperty.addListener((observableValue, number, t1) -> {
                this.vertices.get(idx).setX((double) t1);
                updatePoints();
            });
            yProperty.addListener((observableValue, number, t1) -> {
                this.vertices.get(idx).setY((double) t1);
                updatePoints();
            });

            //Here can be added smth like "isVisible" at all not only from center
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
*/
    public void addVertexAndPoint(Vertex vertex) {
        vertices.add(vertex);
        getPoints().add(vertex.getX());
        getPoints().add(vertex.getY());
    }

    /**
     * MALFUNCTION AT SOME CASES
     */
    public void removeVertexAndPoint(Point point) {
        System.out.println("Removing "+point);
        if (vertices.removeIf(v -> v.getX() == point.getCenterX() & v.getY() == point.getCenterY())) {
            updatePoints();
        }
    }

    public Polygon draw() {
        this.setStroke(Color.FORESTGREEN);
        this.setStrokeWidth(3);
        this.setStrokeLineCap(StrokeLineCap.ROUND);
        this.setFill(Color.GOLDENROD.deriveColor(0, 1.2, 1, 0.6));

        return this;
    }

    public void updatePoints() {

        getPoints().clear();
        for (int i = 0; i < vertices.size(); i++) {
            getPoints().add(vertices.get(i).getX());
            getPoints().add(vertices.get(i).getY());
        }

    }

    public ObservableList<Vertex> getVertices() {
        return vertices;
    }
}
