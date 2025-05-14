/*
 * Copyright (c) 2016 - 2023 coodex.org (jujus.shen@126.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.coodex.jts;

import org.coodex.util.SelectableService;
import org.locationtech.jts.geom.*;

import static org.coodex.jts.JTSUtil.get2DGeometry;

public interface AreaOf extends CoordSys, SelectableService<CoordSys.CoordType> {

    double areaOf(Geometry geometry);


    double areaOf(MultiPolygon multiPolygon) ;

    double areaOf(Polygon polygon);

    double areaOf(LinearRing linearRing);
}
