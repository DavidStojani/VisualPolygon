package com.bachelor.visualpolygon.model.geometry;


import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.locationtech.jts.algorithm.Orientation;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.overlay.validate.FuzzyPointLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@Getter
public class Step {
    Builder builder;
    Polygon initialPolygon;
    Polygon stepPolygon;
    List<Vertex> active;
    List<Vertex> tempVisible;
    List<Vertex> tempInvisible;
    LineSegment ALPHA;
    LineSegment BETA;

    public Step(Builder builder) {
        this.builder = builder;
        this.initialPolygon = builder.getPolygon();
        stepPolygon = builder.getStepPolygon();
        active = new ArrayList<>();
        tempInvisible = new ArrayList<>();
        tempVisible = new ArrayList<>();
    }

    public void initStep() {
        stepPolygon = builder.getStepPolygon();
        ALPHA = builder.getALPHA();
        BETA = builder.getBETA();
      //  setActive();
        //setTemps();
        //findNextVertexToBuildStep();
    }







}
