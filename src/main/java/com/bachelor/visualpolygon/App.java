package com.bachelor.visualpolygon;

import com.bachelor.visualpolygon.model.ModelFactory;
import com.bachelor.visualpolygon.view.ViewHandler;
import com.bachelor.visualpolygon.viewmodel.ViewModelFactory;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) throws Exception {

        ModelFactory modelFactory = new ModelFactory();
        ViewModelFactory viewModelFactory = new ViewModelFactory(modelFactory);
        ViewHandler viewHandler = new ViewHandler(stage, viewModelFactory);

        viewHandler.start();
    }


}