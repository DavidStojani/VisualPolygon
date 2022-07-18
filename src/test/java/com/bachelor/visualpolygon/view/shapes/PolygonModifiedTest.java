package com.bachelor.visualpolygon.view.shapes;

import com.bachelor.visualpolygon.model.geometry.Vertex;
import javafx.beans.property.SimpleDoubleProperty;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PolygonModifiedTest {

    PolygonModified polygonModified;
    final String pointsString = "273 582, 579 555, 639 353, 854 473, 946 319, 825 297, 809 155";
    //, 958 148, 958 72, 743 76, 743 202, 538 247, 475 108, 240 128, 243 364, 168 420, 109 479, 200 628, 451 666, 273 582
    Point point;
    @EqualsAndHashCode.Include
    List<Vertex> vertices;
    private List<Vertex> createVertices(String pointsString) {
        List<Vertex> vertices = new ArrayList<>();
        String[] splited = pointsString.replaceAll(",", "").split(" ");

        for (int i = 0; i < splited.length; i += 2) {
            double x = Double.parseDouble(splited[i]);
            double y = Double.parseDouble(splited[i + 1]);
            vertices.add(new Vertex(x,y));
        }
        return vertices;
    }

    @BeforeEach
    void setUp() {
        point = new Point(new SimpleDoubleProperty(825), new SimpleDoubleProperty(297),true);
        vertices = createVertices(pointsString);

        polygonModified = new PolygonModified();

    }

    @Test
    @Disabled("Adding a vertex")
    void testAddVertexAndPoint() {
        vertices.add(new Vertex(100,100));

        polygonModified.addVertexAndPoint(new Vertex(100,100));

        assertEquals(vertices,polygonModified.getVertices());
        assertEquals(100, polygonModified.getPoints().get(polygonModified.getPoints().size()-2));
        assertEquals(100, polygonModified.getPoints().get(polygonModified.getPoints().size()-1));
    }


}