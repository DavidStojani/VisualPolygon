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

        //model.updateBuilder(polygon, camera);
        setLabelText("COORDINATES Polygon");
        //setVertices(model.getVertices());
       // System.out.println("Test:STANDALONEVertices::::" + vertices);

        System.out.println("============THE VERTICES IN VIEWMODEL=============");
        vertices.forEach(System.out::println);
        System.out.println("============CAMERA DETAILS IN VIEWMODEL=========");
        /*cameraDetails.forEach(System.out::println);*/
        System.out.println(cameraDetails);


    }

    public void resetView() {
        setLabelText("JEPi");
    }

    public StringProperty labelTextProperty() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    public Line testFeature() {
        return model.testTangent();

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
