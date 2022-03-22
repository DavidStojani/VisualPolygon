package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class PolygonModified extends javafx.scene.shape.Polygon {

    private  ObservableList<Vertex> vertices;

    public PolygonModified() {
        super();
        vertices = FXCollections.observableArrayList();
    }

    public PolygonModified(List<Vertex> vertices) {
        super();
        this.vertices = FXCollections.observableArrayList(vertices);
        for(int i = 0; i < vertices.size(); i++) {
            getPoints().add(vertices.get(i).getX());
            getPoints().add(vertices.get(i).getY());
        }
    }

    public ObservableList<Vertex> getVertexFromPoints() {

        for (int i = 0; i < getPoints().size(); i += 2) {
            vertices.add(new Vertex(getPoints().get(i), getPoints().get(i+1)));
        }

        return vertices;
    }

}
