package com.bachelor.visualpolygon.view.shapes;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class PolygonModified extends Polygon {

    public static final ObservableList<Vertex> vertices = FXCollections.observableArrayList();

    public PolygonModified() {
        super();
        for (Vertex vertex : vertices) {
            getPoints().add(vertex.getXCoordinate());
            getPoints().add(vertex.getYCoordinate());
        }
    }

    public void addVertexAndPoint(Vertex vertex) {
        vertices.add(vertex);
        getPoints().add(vertex.getXCoordinate());
        getPoints().add(vertex.getYCoordinate());
    }

    /**
     * MALFUNCTION AT SOME CASES
     */
    public void removeVertexAndPoint(Point point) {
        if (vertices.removeIf(v -> v.getXCoordinate() == point.getCenterX() && v.getYCoordinate() == point.getCenterY())) {
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
        for (Vertex vertex : vertices) {
            getPoints().add(vertex.getXCoordinate());
            getPoints().add(vertex.getYCoordinate());
        }
    }

    public ObservableList<Point> createModeratePoints() {
        ObservableList<Point> points = FXCollections.observableArrayList();
        for (int i = 0; i < vertices.size(); i++) {
            final int idx = i;

            DoubleProperty xProperty = vertices.get(idx).getXProperty();
            DoubleProperty yProperty = vertices.get(idx).getYProperty();

            xProperty.addListener((observableValue, number, t1) -> {
                vertices.get(idx).getXProperty().set(t1.doubleValue());
                updatePoints();
            });
            yProperty.addListener((observableValue, number, t1) -> {
                vertices.get(idx).getYProperty().set(t1.doubleValue());
                updatePoints();
            });

            Point p = new Point(xProperty, yProperty);

            //p.setOnMouseReleased(mouseEvent -> updateStatus());
            //Here can be added smth like "isVisible" at all not only from center
            if (vertices.get(idx).getIsVisible() == -1) {
                p.changeColorToRed();
            } else if (vertices.get(idx).getIsVisible() == 1) {
                p.changeColorToGreen();
            }
            if (vertices.get(idx).isInBlue()){
                p.changeColorToBlue();
            }
            if (vertices.get(idx).isGrey()){
                p.changeColorToGrey();
            }
            points.add(p);
        }
        return points;
    }


    public ObservableList<Vertex> getVertices() {
        return vertices;
    }
}
