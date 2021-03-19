package test.org.coodex.jts;

import org.coodex.jts.JTSUtil;
import org.locationtech.jts.geom.*;

import java.util.Arrays;

public class ConvertServiceTest {

    private static Coordinate[] coordinates = new Coordinate[]{
            new Coordinate(110d, 40d),
            new Coordinate(110d, 50d),
            new Coordinate(120d, 50d),
            new Coordinate(120d, 40d),
            new Coordinate(110d, 40d)

    };

    private static <T extends Geometry> T print(T geometry) {
        System.out.println(geometry.toText());
        return geometry;
    }

    private static <T extends Geometry> T test(T geometry) {
        return print(JTSUtil.mercator2LngLat(print(JTSUtil.lngLat2Mercator(geometry))));
    }

    public static void main(String[] args) {
        Point point = test(JTSUtil.GEOMETRY_FACTORY.createPoint(coordinates[0]));

        LineString lineString = test(JTSUtil.GEOMETRY_FACTORY.createLineString(coordinates));

        LinearRing linearRing = test(JTSUtil.GEOMETRY_FACTORY.createLinearRing(coordinates));

        MultiPoint multiPoint = test(new MultiPoint(Arrays.stream(coordinates)
                .map(c -> JTSUtil.GEOMETRY_FACTORY.createPoint(c))
                .toArray(value -> new Point[coordinates.length])
                , JTSUtil.GEOMETRY_FACTORY));

        test(new GeometryCollection(new Geometry[]{
                point, linearRing, lineString, multiPoint
        }, JTSUtil.GEOMETRY_FACTORY));

    }
}
