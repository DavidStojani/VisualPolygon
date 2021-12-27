package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;

import java.util.ArrayList;

public class ViewModel {

    private DataModel model;
    private ListProperty<Double> xCoordinate = new SimpleListProperty<>();
    private ListProperty<Double> yCoordinate = new SimpleListProperty<>();

    public ViewModel(DataModel model){
        this.model =model;
    }


}
