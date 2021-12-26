package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ViewController {
    @FXML
    private Label statusText;



    public void init(ViewModel viewModel) {
        System.out.println("Works");
    }

    public void resetApplicationAction(ActionEvent actionEvent) {
        statusText.setText("RESETTING");
    }

    public void updateStatus(ActionEvent actionEvent) {
        statusText.setText("UPDATING");
    }
}