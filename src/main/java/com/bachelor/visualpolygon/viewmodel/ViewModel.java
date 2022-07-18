package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import com.bachelor.visualpolygon.model.geometry.Vertex;
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
import java.util.Objects;

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

    public void reset() {
        model.reset();
        setLabelText("Reset Pressed! All Cleared Out!");
    }

    public void initListOfFiles() {
        File folder = new File("src/test/resources");
        File[] listOfFiles = folder.listFiles();
        if (Objects.isNull(listOfFiles)){
            setLabelText("NO FILE TO BE UPLOADED, CREATE A POLYGON BY CLICKING");
            return;
        }

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
        for (Coordinate coordinate : model.getStepCoordinates()) {
            stepPolygon.getPoints().add(coordinate.getX());
            stepPolygon.getPoints().add(coordinate.getY());
        }
        return stepPolygon;
    }

    public List<Line> getAllLines() {
        return model.getAllLines();
    }

    public boolean isScanDone() {
        if (model.isScanReady()) {
            model.createVisPolygon();
            setLabelText("!!SCAN IS DONE!!");
            return true;
        }
        return false;
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
        return fileChooser.showSaveDialog(new Popup());
    }

    public void uploadFile(File file) {
        WKTReader wktReader = new WKTReader();
        WKTFileReader wktFileReader = new WKTFileReader(file, wktReader);

        try {
            List<Geometry> a = wktFileReader.read();
            Geometry p = wktReader.read(a.get(0).toString());
            Coordinate[] coordinates = p.getCoordinates();
            for (int i = 0; i < coordinates.length - 1; i++) {
                Coordinate coordinate = coordinates[i];
                vertices.add(new Vertex(coordinate));
            }
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public Polygon getVisPoly() {
        Polygon visPoly = new Polygon();
        for (Coordinate coordinate : model.getVisualPolygon()) {
            visPoly.getPoints().add(coordinate.getX());
            visPoly.getPoints().add(coordinate.getY());
        }
        return visPoly;
    }
}
