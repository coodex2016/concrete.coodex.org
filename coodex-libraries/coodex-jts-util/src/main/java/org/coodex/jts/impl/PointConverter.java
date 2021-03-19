package org.coodex.jts.impl;

import org.coodex.jts.GeometryConvertService;
import org.locationtech.jts.geom.Point;

import static org.coodex.jts.JTSUtil.*;


public class PointConverter implements GeometryConvertService<Point> {
    @Override
    public Point toMercator(Point lngLat) {
        return GEOMETRY_FACTORY.createPoint(lngLat2Mercator(lngLat.getCoordinate()));
    }

    @Override
    public Point toLngLat(Point mercator) {
        return GEOMETRY_FACTORY.createPoint(mercator2LngLat(mercator.getCoordinate()));
    }

    @Override
    public boolean accept(Point param) {
        return true;
    }
}
