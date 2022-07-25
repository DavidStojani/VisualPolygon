package com.bachelor.visualpolygon.model.geometry;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Coordinate;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.*;

@Data
@NoArgsConstructor
public class GeometryCamera {

    static final double EPS = 0.0000001;
    private double radius;
    private double centerX;
    private double centerY;
    private Coordinate center = new Coordinate();



    public void setDetails(List<Double> cameraDetails) {
        centerX = cameraDetails.get(0);
        centerY = cameraDetails.get(1);
        radius = cameraDetails.get(2);
        center.setX(centerX);
        center.setY(centerY);
    }

    // Due to double rounding precision the value passed into the asin
    // function may be outside its domain of [-1, +1] which would return
    // the value Double.NaN which we do not want.
    private static double arcSinSafe(double x) {
        if (x <= -1.0) return -PI / 2.0;
        if (x >= 1.0) return PI / 2.0;
        return asin(x);
    }


    public Coordinate getRightTangentPoint(Vertex vertex) {
        return findTangentPointsOnCameraFor(vertex).get(0);
    }

    public Coordinate getLeftTangentPoint(Vertex vertex) {
        return findTangentPointsOnCameraFor(vertex).get(1);
    }

    private List<Coordinate> findTangentPointsOnCameraFor(Vertex pt) {
        double px = pt.getXCoordinate();
        double py = pt.getYCoordinate();
        double cx = centerX;
        double cy = centerY;
        // Compute the distance to the circle center
        double dx = cx - px;
        double dy = cy - py;
        double dist = sqrt(dx * dx + dy * dy);

        // Point is strictly contained within the circle
        if (dist < radius) throw new RuntimeException("Vertex within Camera");

        double angle;
        double angle1;
        double angle2;

        angle1 = arcSinSafe(radius / dist);
        angle2 = atan2(dy, dx);

        angle = angle2 - angle1;
        Coordinate p1 = new Coordinate(cx + radius * sin(angle), cy + radius * -cos(angle));

        angle = angle1 + angle2;
        Coordinate p2 = new Coordinate(cx + radius * -sin(angle), cy + radius * cos(angle));

        List<Coordinate> coordinates = new ArrayList<>();
        coordinates.add(p1);
        coordinates.add(p2);
        return coordinates;
    }

}
