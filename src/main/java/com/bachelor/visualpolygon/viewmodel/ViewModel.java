package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.StrokeLineCap;

public class ViewModel {

    private DataModel model;
    private StringProperty labelText = new SimpleStringProperty();
    private Camera camera;
    private Polygon polygon;


    public ViewModel(DataModel model){
        this.model =model;
        polygon = new Polygon();
    }


    public void updatePolygon(){
        setLabelText("COORDINATES" + polygon.getPoints() + "\n" + "No of Points: " + polygon.getPoints().size()/2);
    }

    public void resetView() {
        polygon.getPoints().clear();
        setLabelText("Reset done!");
    }

    public Polygon drawPolygon() {
        polygon.setStroke(Color.FORESTGREEN);
        polygon.setStrokeWidth(3);
        polygon.setStrokeLineCap(StrokeLineCap.ROUND);
        polygon.setFill(Color.GOLDENROD.deriveColor(0, 1.2, 1, 0.6));

        return polygon;
    }


    public Camera createCamera(ObservableList<Double> coordinates) { // make class instead of function

        int idx = 0;

        DoubleProperty cameraXProperty = new SimpleDoubleProperty(coordinates.get(idx));
        DoubleProperty cameraYProperty = new SimpleDoubleProperty(coordinates.get(idx+1));

        camera = new Camera(cameraXProperty,cameraYProperty);

        cameraXProperty.addListener((ov, oldX, x) -> coordinates.set(idx, (double) x));

        cameraYProperty.addListener((ov, oldY, y) -> coordinates.set(idx + 1, (double) y));

        return camera;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public StringProperty labelTextProperty() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }


    public Camera getCamera() {
        return camera;
    }




}
