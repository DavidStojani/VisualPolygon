package com.bachelor.visualpolygon.model.geometry;

import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BuilderTest {

    @Test
    void isOnSameLine() {
        Vertex a = new Vertex(1, 1);
        Vertex b = new Vertex(1, 4);
        Coordinate c = new Coordinate(1, 5);

        Builder builder = new Builder();

        assertTrue(builder.isOnSameLine(a, b, c));
    }

    @Test
    void testParameterOfLine_gives_true() {
        Vertex v1 = new Vertex(2, 1);
        Vertex v2 = new Vertex(5, 7);
        Builder builder = new Builder();

        Map parameter = builder.getParametersOfEquation(v1, v2);

        assertEquals(-6.0,parameter.get("A"));
        assertEquals(3.0,parameter.get("B"));
        assertEquals(9.0,parameter.get("C"));
    }


    @Test
    void testParameterOfLine_gives_false() {
        Vertex v1 = new Vertex(2, 1);
        Vertex v2 = new Vertex(3, 1);
        Builder builder = new Builder();

        Map parameter = builder.getParametersOfEquation(v1, v2);

        assertNotEquals(-6.0,parameter.get("A"));
        assertNotEquals(3.0,parameter.get("B"));
        assertNotEquals(9.0,parameter.get("C"));
    }
}