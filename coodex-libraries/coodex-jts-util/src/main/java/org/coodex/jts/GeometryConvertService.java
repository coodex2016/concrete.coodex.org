package org.coodex.jts;

import org.coodex.util.SelectableService;
import org.locationtech.jts.geom.Geometry;

public interface GeometryConvertService<T extends Geometry> extends SelectableService<T> {

    T toMercator(T lngLat);

    T toLngLat(T mercator);
}
