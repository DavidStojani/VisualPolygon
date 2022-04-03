package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.Setter;

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

    public Line testFeature() {
        return model.printStuff();

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
}
