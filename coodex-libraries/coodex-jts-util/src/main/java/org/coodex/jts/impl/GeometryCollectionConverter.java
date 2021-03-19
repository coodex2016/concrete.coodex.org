package org.coodex.jts.impl;

import org.coodex.jts.GeometryConvertService;
import org.coodex.jts.JTSUtil;
import org.coodex.util.Common;
import org.locationtech.jts.geom.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static org.coodex.jts.JTSUtil.GEOMETRY_FACTORY;

public class GeometryCollectionConverter implements GeometryConvertService<GeometryCollection> {


    private static final GCCFunction<MultiPoint, Point> MULTI_POINT_POINT_GCC_FUNCTION = new GCCFunction<MultiPoint, Point>() {
        @Override
        protected MultiPoint create(List<Point> geometries) {
            return new MultiPoint(geometries.toArray(new Point[0]), GEOMETRY_FACTORY);
        }
    };

    private static final GCCFunction<MultiLineString, LineString> MULTI_LINE_STRING_LINE_STRING_GCC_FUNCTION = new GCCFunction<MultiLineString, LineString>() {
        @Override
        protected MultiLineString create(List<LineString> geometries) {
            return new MultiLineString(geometries.toArray(new LineString[0]), GEOMETRY_FACTORY);
        }
    };

    private static final GCCFunction<MultiPolygon, Polygon> MULTI_POLYGON_POLYGON_GCC_FUNCTION = new GCCFunction<MultiPolygon, Polygon>() {
        @Override
        protected MultiPolygon create(List<Polygon> geometries) {
            return new MultiPolygon(geometries.toArray(new Polygon[0]), GEOMETRY_FACTORY);
        }
    };

    private static final GCCFunction<GeometryCollection, Geometry> DEFAULT = new GCCFunction<GeometryCollection, Geometry>() {
        @Override
        protected GeometryCollection create(List<Geometry> geometries) {
            return new GeometryCollection(geometries.toArray(new Geometry[0]), GEOMETRY_FACTORY);
        }
    };


    @Override
    public GeometryCollection toMercator(GeometryCollection source) {
        //noinspection DuplicatedCode
        if (source instanceof MultiPoint) {
            return MULTI_POINT_POINT_GCC_FUNCTION.apply((MultiPoint) source, JTSUtil::lngLat2Mercator);
        } else if (source instanceof MultiLineString) {
            return MULTI_LINE_STRING_LINE_STRING_GCC_FUNCTION.apply((MultiLineString) source, JTSUtil::lngLat2Mercator);
        } else if (source instanceof MultiPolygon) {
            return MULTI_POLYGON_POLYGON_GCC_FUNCTION.apply((MultiPolygon) source, JTSUtil::lngLat2Mercator);
        } else {
            return DEFAULT.apply(source, JTSUtil::lngLat2Mercator);
        }
    }

    @Override
    public GeometryCollection toLngLat(GeometryCollection mercator) {
        //noinspection DuplicatedCode
        if (mercator instanceof MultiPoint) {
            return MULTI_POINT_POINT_GCC_FUNCTION.apply((MultiPoint) mercator, JTSUtil::mercator2LngLat);
        } else if (mercator instanceof MultiLineString) {
            return MULTI_LINE_STRING_LINE_STRING_GCC_FUNCTION.apply((MultiLineString) mercator, JTSUtil::mercator2LngLat);
        } else if (mercator instanceof MultiPolygon) {
            return MULTI_POLYGON_POLYGON_GCC_FUNCTION.apply((MultiPolygon) mercator, JTSUtil::mercator2LngLat);
        } else {
            return DEFAULT.apply(mercator, JTSUtil::mercator2LngLat);
        }
    }

    @Override
    public boolean accept(GeometryCollection param) {
        return true;
    }

    static abstract class GCCFunction<M extends GeometryCollection, S extends Geometry> implements BiFunction<M, Function<S, S>, M> {
        protected abstract M create(List<S> geometries);

        @Override
        public M apply(M source, Function<S, S> func) {
            List<S> geometries = new ArrayList<>();
            for (int i = 0; i < source.getNumGeometries(); i++) {
                geometries.add(func.apply(Common.cast(source.getGeometryN(i))));
            }
            return create(geometries);
        }
    }
}
