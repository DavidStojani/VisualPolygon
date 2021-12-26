package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.ModelFactory;

public class ViewModelFactory {

    private ViewModel viewModel;

    public ViewModelFactory (ModelFactory modelFactory) {
        viewModel = new ViewModel(modelFactory.getDataModel());
    }

    public ViewModel getViewModel() {
        return viewModel;
    }
}
