package com.bachelor.visualpolygon.model;

import com.bachelor.visualpolygon.viewmodel.Camera;
import javafx.geometry.Point2D;
import lombok.Data;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.math.Vector2D;

import static java.lang.Math.*;

@Data
public class GeometryCamera {

    static final double EPS = 0.0000001;
    private double radius;
    private double centerX;
    private double centerY;


    public double distanceBetweenTwoPoints(Point a, Point b) {
        return a.distance(b);
    }
    // Due to double rounding precision the value passed into the asin
    // function may be outside its domain of [-1, +1] which would return
    // the value Double.NaN which we do not want.
    private static double arcsinSafe(double x) {
        if (x <= -1.0) return -PI / 2.0;
        if (x >= +1.0) return +PI / 2.0;
        return asin(x);
    }

/*
    public Point2D calculateTangentPointOnCameraFrom(Point vertex){

        // Finds the point(s) of intersection of the lines tangent to the circle centered
        // at 'center' from the point 'point'.


            double px = vertex.getX(), py = vertex.getY();

            // Compute the distance to the circle center
            double dx = centerX - px;
            double dy = centerY - py;
            double distance = sqrt(dx * dx + dy * dy);


            double angle, angle1, angle2;

            angle1 = arcsinSafe(radius / distance);
            angle2 = atan2(dy, dx);

            angle = angle2 - angle1;
            double x1 = centerX + radius * sin(angle);
            double y1 = centerY + radius * -cos(angle);

            angle = angle1 + angle2;
            double x2 = centerX + radius * -sin(angle);
            double y2 = centerY + radius * cos(angle);

            return new Point2D(x1,x2);
            // Points are sufficiently close to be considered the same point
            // (i.e the original point is on the circle circumference)
        */
/*    if (p1.distance(p2) < EPS) return new Point2D[] {vertex};
            return new Point2D[] {p1, p2};
            *//*

    }
*/

    public Point2D[] pointCircleTangentPoints(Point pt) {

        double px = pt.getX(), py = pt.getY();
        double cx = centerX, cy = centerY;

        // Compute the distance to the circle center
        double dx = cx - px;
        double dy = cy - py;
        double dist = sqrt(dx * dx + dy * dy);

        // Point is strictly contained within the circle
        if (dist < radius) return new Point2D[] {};

        double angle, angle1, angle2;

        angle1 = arcsinSafe(radius / dist);
        angle2 = atan2(dy, dx);

        angle = angle2 - angle1;
        Point2D p1 = new Point2D(cx + radius * sin(angle), cy + radius * -cos(angle));

        angle = angle1 + angle2;
        Point2D p2 = new Point2D(cx + radius * -sin(angle), cy + radius * cos(angle));

        // Points are sufficiently close to be considered the same point
        // (i.e the original point is on the circle circumference)
        //if (p1.distance(p2) < EPS) return new Point2D[] {pt};
        return new Point2D[] {p1, p2};
    }

    public Point2D fromInternetMethod(Point2D vertex) {
        Point2D center = new Point2D(centerX,centerY);
        double MQ = center.distance(vertex);
        double koef1 = (radius*radius)/(MQ*MQ);
        double koef2 = sqrt((MQ*MQ) - (radius*radius)) * radius/(MQ*MQ);
        Point2D sub = vertex.subtract(center);
        Point2D inv = new Point2D((-vertex.getY() + center.getY()), (vertex.getX() - center.getX()));
        sub = sub.multiply(koef1);
        inv = inv.multiply(koef2);

        Point2D s1 = center.add(sub).subtract(inv);
        return s1;

    }


}
