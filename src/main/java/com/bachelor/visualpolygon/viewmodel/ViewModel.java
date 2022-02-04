package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;

public class ViewModel {

    private DataModel model;
    private StringProperty labelText = new SimpleStringProperty();
    private Camera camera;
    private Polygon polygon;

    private Polyline polyline;


    public ViewModel(DataModel model){
        this.model =model;
        polygon = new Polygon();
        polyline = new Polyline();
    }


    public void updatePolygon(){
        setLabelText("COORDINATES Polygon" + polygon.getPoints() + "\n" + " CAM: " + camera.toString());

    }

    public void resetView() {
        polyline.getPoints().clear();
        polygon.getPoints().clear();
        setCamera(null);
        setLabelText("Reset done!");
    }

    public Polygon drawPolygon() {

        polygon.setStroke(Color.FORESTGREEN);
        polygon.setStrokeWidth(3);
        polygon.setStrokeLineCap(StrokeLineCap.ROUND);
        polygon.setFill(Color.GOLDENROD.deriveColor(0, 1.2, 1, 0.6));

        return polygon;
    }

    public Polyline drawPolyline() {
        polyline.setStroke(Color.AZURE);
        polyline.setStrokeWidth(3);
        polyline.setStrokeLineCap(StrokeLineCap.ROUND);

        return polyline;
    }

    public Polyline getPolyline() {
        return polyline;
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

    public void setCamera(Camera camera) {
        this.camera = camera;
    }




}
