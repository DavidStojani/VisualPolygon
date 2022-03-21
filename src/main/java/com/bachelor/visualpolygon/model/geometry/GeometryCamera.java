package com.bachelor.visualpolygon.model.geometry;

import com.bachelor.visualpolygon.viewmodel.Camera;
import javafx.geometry.Point2D;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.algorithm.Centroid;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import static java.lang.Math.*;

@Data
public class GeometryCamera {

    static final double EPS = 0.0000001;
    private double radius;
    private double centerX;
    private double centerY;
    private Coordinate center;
    private static final GeometryFactory factory = new GeometryFactory();

    public GeometryCamera(Camera camera) {
        radius = camera.getRadius();        //nicht accurate wegen zoom in and out
        centerX = camera.getCenterX();
        centerY = camera.getCenterY();
        center = new Coordinate(centerX, centerY);

    }

    // Due to double rounding precision the value passed into the asin
    // function may be outside its domain of [-1, +1] which would return
    // the value Double.NaN which we do not want.
    private static double arcsinSafe(double x) {
        if (x <= -1.0) return -PI / 2.0;
        if (x >= +1.0) return +PI / 2.0;
        return asin(x);
    }


    public Coordinate[] findTangentPointsOnCameraFor(Vertex pt) {

        double px = pt.getX(), py = pt.getY();
        double cx = centerX, cy = centerY;
        // Compute the distance to the circle center
        double dx = cx - px;
        double dy = cy - py;
        double dist = sqrt(dx * dx + dy * dy);

        // Point is strictly contained within the circle
        if (dist < radius) throw new RuntimeException("Vertex within Camera");

        double angle, angle1, angle2;

        angle1 = arcsinSafe(radius / dist);
        angle2 = atan2(dy, dx);

        angle = angle2 - angle1;
        Coordinate p1 = new Coordinate(cx + radius * sin(angle), cy + radius * -cos(angle));

        angle = angle1 + angle2;
        Coordinate p2 = new Coordinate(cx + radius * -sin(angle), cy + radius * cos(angle));

        // Points are sufficiently close to be considered the same point
        // (i.e the original point is on the circle circumference)
        //if (p1.distance(p2) < EPS) return new Point2D[] {pt};
        return new Coordinate[]{p1, p2};
    }

}
