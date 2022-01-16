package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

public class ViewModel {

    private DataModel model;
    private StringProperty labelText = new SimpleStringProperty();
    private ListProperty<Double> coordinates = new SimpleListProperty<>();
    private Camera camera;


    public ViewModel(DataModel model){
        this.model =model;
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


    public void test(){
        setLabelText("Points::"+coordinates.get().toString() +"\n" +
                "Circle::" );
    }


    public String getLabelText() {
        return labelText.get();
    }

    public StringProperty labelTextProperty() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }


    public ObservableList<Double> getCoordinates() {
        return coordinates.get();
    }

    public ListProperty<Double> coordinatesProperty() {
        return coordinates;
    }

    public void setCoordinates(ObservableList<Double> coordinates) {
        this.coordinates.set(coordinates);
    }

    public Camera getCamera() {
        return camera;
    }


}
