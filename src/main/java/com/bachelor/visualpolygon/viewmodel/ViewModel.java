package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.beans.binding.FloatExpression;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.Popup;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTFileReader;
import org.locationtech.jts.io.WKTReader;
import org.locationtech.jts.io.WKTWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Stack;

@Getter
@Setter
public class ViewModel {

    private DataModel model;
    private StringProperty labelText = new SimpleStringProperty("Welcome");
    private ObservableList<Vertex> vertices = FXCollections.observableArrayList();
    private ObservableList<Double> cameraDetails = FXCollections.observableArrayList();
    private WKTWriter wktWriter = new WKTWriter(3);
    private ObservableList<File> fileObservableList = FXCollections.observableArrayList();



    public ViewModel(DataModel model) {
        this.model = model;
        initListOfFiles();
    }

    /**
     * Gives the Polygon and the Camera to the Model
     */
    public void updatePolygon() {
        model.updateBuilder(vertices, cameraDetails);
        setLabelText("Model Updated");
    }

    public void resetView() {
        model.reset();
        setLabelText("Reset Pressed! All Cleared Out!");
    }

    public void initListOfFiles() {
        File folder = new File("src/test/resources");
        File[] listOfFiles = folder.listFiles();

        for (File listOfFile : listOfFiles) {
            if (listOfFile.isFile()) {
                fileObservableList.add(listOfFile);
            }
        }
    }

    public StringProperty labelTextProperty() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    public Polygon getStepPolygon() {
        Polygon stepPolygon = new Polygon();
        for (Coordinate coordinate : model.getStreifenCoordinates()) {
            stepPolygon.getPoints().add(coordinate.getX());
            stepPolygon.getPoints().add(coordinate.getY());
        }
        return stepPolygon;
    }


    public void setStepInfo() {
        setLabelText("Step Created! " + "ACTIVE size: " + model.getStepInfo());
    }

    public Stack<Line> getParallels() {
        return model.getTheParallels();
    }

    public void save() {
        try {
            File saveFile = setFile();
            Writer writer = new FileWriter(saveFile);
            wktWriter.writeFormatted(model.getPolygon(), writer);
            writer.close();
            fileObservableList.add(saveFile);
            setLabelText("Polygon Saved!!");
        } catch (IOException e) {
            setLabelText("Save Failed!");
            throw new RuntimeException(e);
        }
    }

    private File setFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new File("src/test/resources"));
        fileChooser.setInitialFileName("Test.txt");
        fileChooser.setTitle("Save");
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);
        File file = fileChooser.showSaveDialog(new Popup());
        return file;
    }

    public void uploadFile(File file) {
        WKTReader wktReader = new WKTReader();
        WKTFileReader wktFileReader = new WKTFileReader(file, wktReader);

        try {
            List a = wktFileReader.read();
            Geometry p = wktReader.read(a.get(0).toString());
            Coordinate[] coordinates = p.getCoordinates();
            for (int i = 0; i < coordinates.length - 1; i++) {
                Coordinate coordinate = coordinates[i];
                vertices.add(new Vertex(coordinate));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }
}
