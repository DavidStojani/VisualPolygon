package com.bachelor.visualpolygon.viewmodel;

import com.bachelor.visualpolygon.model.DataModel;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.StrokeLineCap;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class ViewModel {

    private DataModel model;
    private StringProperty labelText = new SimpleStringProperty();
    private Camera camera;
    private Polygon polygon;
    private Polyline polyline;


    public ViewModel(DataModel model) {
        this.model = model;
        polygon = new Polygon();
        polyline = new Polyline();
    }

    /**
     * Gives the Polygon and the Camera to the Model
     */
    public void updatePolygon() {
        if (Objects.isNull(camera) || Objects.isNull(polygon)) {
            setLabelText("Camera or Polygon not ready!!");
            throw new RuntimeException("NO CAMERA OR POLYGON FOUND!!!");
        }
        model.updateBuilder(polygon, camera);
        setLabelText("COORDINATES Polygon");
    }

    public void resetView() {
        polyline.getPoints().clear();
        polygon.getPoints().clear();
        setCamera(null);

        setLabelText("Reset done!");
    }

    public Polygon drawPolygon() {
        polygon.setStroke(Color.FORESTGREEN);
        polygon.setStrokeWidth(3);
        polygon.setStrokeLineCap(StrokeLineCap.ROUND);
        polygon.setFill(Color.GOLDENROD.deriveColor(0, 1.2, 1, 0.6));

        return polygon;
    }

    public Polyline drawPolyline() {
        polyline.setStroke(Color.AZURE);
        polyline.setStrokeWidth(3);
        polyline.setStrokeLineCap(StrokeLineCap.ROUND);

        return polyline;
    }


    public Camera createCamera(ObservableList<Double> coordinates) { // make class instead of function

        int idx = 0;

        DoubleProperty cameraXProperty = new SimpleDoubleProperty(coordinates.get(idx));
        DoubleProperty cameraYProperty = new SimpleDoubleProperty(coordinates.get(idx + 1));

        camera = new Camera(cameraXProperty, cameraYProperty);

        cameraXProperty.addListener((ov, oldX, x) -> coordinates.set(idx, (double) x));

        cameraYProperty.addListener((ov, oldY, y) -> coordinates.set(idx + 1, (double) y));

        return camera;
    }

    public StringProperty labelTextProperty() {
        return labelText;
    }

    public void setLabelText(String labelText) {
        this.labelText.set(labelText);
    }

    public Line testFeature() {
        return model.testTangent();

    /*    transition.setNode(camera);
        transition.setDuration(Duration.seconds(4));
        transition.setPath(polygon);
        transition.setCycleCount(PathTransition.INDEFINITE);
        transition.play();*/
     /*   Line radius = new Line();


        radius.startXProperty().bind(camera.centerXProperty());
        radius.startYProperty().bind(camera.centerYProperty());
        radius.setEndY(camera.getBaselineOffset());
        radius.setEndX(camera.getBaselineOffset());

        //radius.setEndX(camera.getLayoutBounds().getMinX());
        //radius.setEndY(camera.get);*/

    }
}
