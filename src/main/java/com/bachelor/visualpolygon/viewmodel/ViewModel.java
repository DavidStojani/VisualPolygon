package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;

import java.util.ArrayList;

public class ViewModel {

    private DataModel model;

    public String getLabelText() {
        return labelText.get();
    }

    public StringProperty labelTextProperty() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    private StringProperty labelText = new SimpleStringProperty();
    private ListProperty<Double> xCoordinate = new SimpleListProperty<>();
    private ListProperty<Double> yCoordinate = new SimpleListProperty<>();

    public ViewModel(DataModel model){
        this.model =model;
    }


    public void test() {
        setLabelText("X :: "+ xCoordinate.toString()+ "\nY :: "+ yCoordinate.toString());
    }

    public ObservableList<Double> getxCoordinate() {
        return xCoordinate.get();
    }

    public ListProperty<Double> xCoordinateProperty() {
        return xCoordinate;
    }

    public void setxCoordinate(ObservableList<Double> xCoordinate) {
        this.xCoordinate.set(xCoordinate);
    }

    public ObservableList<Double> getyCoordinate() {
        return yCoordinate.get();
    }

    public ListProperty<Double> yCoordinateProperty() {
        return yCoordinate;
    }

    public void setyCoordinate(ObservableList<Double> yCoordinate) {
        this.yCoordinate.set(yCoordinate);
    }
}
