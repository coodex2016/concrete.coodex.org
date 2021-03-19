package org.coodex.jts.impl;

import org.coodex.jts.JTSUtil;
import org.coodex.jts.GeometryConvertService;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.coodex.jts.JTSUtil.GEOMETRY_FACTORY;


public class PolygonConverter implements GeometryConvertService<Polygon> {
    static BiFunction<Polygon, Function<LinearRing, LinearRing>, Polygon> POLYGON_CONVERTER =
            (source, func) -> {
                LinearRing shell = func.apply(source.getExteriorRing());
                List<LinearRing> holes = new ArrayList<>();
                for (int i = 0; i < source.getNumInteriorRing(); i++) {
                    holes.add(func.apply(source.getInteriorRingN(i)));
                }
                return holes.size() > 0 ?
                        GEOMETRY_FACTORY.createPolygon(shell, holes.toArray(new LinearRing[0])) :
                        GEOMETRY_FACTORY.createPolygon(shell);
            };

    @Override
    public Polygon toMercator(Polygon lngLat) {
        return POLYGON_CONVERTER.apply(lngLat, JTSUtil::lngLat2Mercator);
    }

    @Override
    public Polygon toLngLat(Polygon mercator) {
        return POLYGON_CONVERTER.apply(mercator, JTSUtil::mercator2LngLat);
    }

    @Override
    public boolean accept(Polygon param) {
        return true;
    }
}
