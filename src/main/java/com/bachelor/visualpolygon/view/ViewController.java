package com.bachelor.visualpolygon.view;

import com.bachelor.visualpolygon.viewmodel.ViewModel;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class ViewController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }

    public void init(ViewModel viewModel) {
        System.out.println("HAHAAAHHAH");
    }
}