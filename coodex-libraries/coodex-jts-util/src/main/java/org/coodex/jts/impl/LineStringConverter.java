package org.coodex.jts.impl;

import org.coodex.jts.JTSUtil;
import org.coodex.jts.GeometryConvertService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;

import java.util.function.BiFunction;
import java.util.function.Function;

import static org.coodex.jts.JTSUtil.GEOMETRY_FACTORY;

public class LineStringConverter implements GeometryConvertService<LineString> {
    static final BiFunction<LineString, Function<Coordinate[], Coordinate[]>, LineString> LINESTRING_CONVERTER_FUNCTION =
            (source, func) -> source instanceof LinearRing ?
                    GEOMETRY_FACTORY.createLinearRing(func.apply(source.getCoordinates())) :
                    GEOMETRY_FACTORY.createLineString(func.apply(source.getCoordinates()));

    @Override
    public LineString toMercator(LineString lngLat) {
        return LINESTRING_CONVERTER_FUNCTION.apply(lngLat, JTSUtil::lngLat2Mercator);
    }

    @Override
    public LineString toLngLat(LineString mercator) {
        return LINESTRING_CONVERTER_FUNCTION.apply(mercator, JTSUtil::mercator2LngLat);
    }

    @Override
    public boolean accept(LineString param) {
        return true;
    }
}
