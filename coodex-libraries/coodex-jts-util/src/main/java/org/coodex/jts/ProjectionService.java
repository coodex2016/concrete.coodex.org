/*
 * Copyright (c) 2016 - 2021 coodex.org (jujus.shen@126.com)
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

public interface ProjectionService extends SelectableService<ProjectionService.ProjectionType> {

    /**
     * 基于经纬度获得投影坐标值
     *
     * @param lngLat 经纬度坐标
     * @return 投影值
     */
    double[] toProjection(double[] lngLat);

    /**
     * 投影坐标转经纬度坐标
     *
     * @param projection 投影坐标
     * @return 经纬度坐标
     */
    double[] toLngLat(double[] projection);

    enum ProjectionType {
        Mercator,
        UTM
    }
}
