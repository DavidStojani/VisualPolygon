package com.bachelor.visualpolygon.model.geometry;

import org.junit.jupiter.api.Disabled;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.LinkedList;

@Disabled
class PolarCoordinatesSortTest {
    Initializer sort;
    GeometryFactory factory;
    Polygon polygon;
    GeometryCamera camera;
    LinkedList<Vertex> vertices;

    // @BeforeEach
    void setUp() {
        factory = new GeometryFactory();
        polygon = factory.createPolygon(new Coordinate[]{
                new Coordinate(26, 60),
                new Coordinate(25, 312),
                new Coordinate(70, 554),
                new Coordinate(309, 619),
                new Coordinate(576, 572),
                new Coordinate(559, 397),
                new Coordinate(515, 348),
                new Coordinate(416, 312),
                new Coordinate(469, 220),
                new Coordinate(746, 184),
                new Coordinate(26, 60)});

    }


  /*  @Test
    void test_Convert_To_Polar() {
        System.out.println(vertices);
        PolarCoordinatesSort.convertToPolar(vertices, camera);
        System.out.println(vertices);

    }
*/
  /*  @Test
    void sortPolarCoordinate() {
    }*/
}