package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import com.bachelor.visualpolygon.model.geometry.Step;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;

import java.util.List;
import java.util.Stack;

@Getter
@Setter
public class ViewModel {

    private DataModel model;
    private StringProperty labelText = new SimpleStringProperty("DAvid");
    private ObservableList<Vertex> vertices = FXCollections.observableArrayList();
    private ObservableList<Double> cameraDetails = FXCollections.observableArrayList();



    public ViewModel(DataModel model) {
        this.model = model;
    }

    /**
     * Gives the Polygon and the Camera to the Model
     */
    public void updatePolygon() {
        model.updateBuilder(vertices, cameraDetails);
        setLabelText("Model Updated");
    }

    public void resetView() {
        setLabelText("All Cleared Out");
    }

    public StringProperty labelTextProperty() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    public Polygon testFeature(int index) {
        Polygon polygon = new Polygon();
        for (Coordinate coordinate : model.getStreifenCoordinates(index)) {
            polygon.getPoints().add(coordinate.getX());
            polygon.getPoints().add(coordinate.getY());
        }

        polygon.setStroke(Color.DARKRED);
        polygon.setStrokeWidth(3);
        polygon.setStrokeLineCap(StrokeLineCap.ROUND);
        polygon.setFill(Color.RED.deriveColor(0, 1.2, 1, 0.6));

        return polygon;
    }



    /*    transition.setNode(camera);
        transition.setDuration(Duration.seconds(4));
        transition.setPath(polygon);
        transition.setCycleCount(PathTransition.INDEFINITE);
        transition.play();*/
     /*   Line radius = new Line();


        radius.startXProperty().bind(camera.centerXProperty());
        radius.startYProperty().bind(camera.centerYProperty());
        radius.setEndY(camera.getBaselineOffset());
        radius.setEndX(camera.getBaselineOffset());

        //radius.setEndX(camera.getLayoutBounds().getMinX());
        //radius.setEndY(camera.get);*/


}
