package com.bachelor.visualpolygon.model.geometry;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {

    @Test
    void isOnSameLine() {
        Vertex a = new Vertex(1,1);
        Vertex b = new Vertex( 1,4);
        Coordinate c = new Coordinate(1,5);

        Builder builder = new Builder();

        assertTrue(builder.isOnSameLine(a,b,c));
    }
}