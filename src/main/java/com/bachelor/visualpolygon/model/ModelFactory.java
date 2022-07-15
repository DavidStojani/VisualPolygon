package com.bachelor.visualpolygon.model;
/**TODO: RENAME THIS */
public class ModelFactory {

    private DataModel dataModel;

    public DataModel getDataModel() {
        if (dataModel == null) {
            dataModel = new DataModelManager();
        }
        return dataModel;
    }

}
