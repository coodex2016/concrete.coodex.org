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

package test.org.coodex.jts;

import org.coodex.concurrent.ExecutorsHelper;
import org.coodex.config.Config;
import org.coodex.jts.JTSUtil;
import org.coodex.util.Clock;
import org.coodex.util.Common;
import org.coodex.util.Singleton;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.operation.buffer.BufferParameters;
import org.locationtech.jts.simplify.DouglasPeuckerSimplifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import static org.coodex.util.Common.slice;
import static test.org.coodex.jts.CoordinatesLoader.randomCoordinates;

public class MemoryTest {

    private static final Logger log = LoggerFactory.getLogger(MemoryTest.class);
    private static final ExecutorService BLOCK_BUILDER_EXECUTORS = ExecutorsHelper.newFixedThreadPool(8, "block-builder");

//    private static Geometry build(Coordinate[] coordinates) {
//
//    }

    private static TestResult oneCase(int dots) {
        Coordinate[] coordinates = randomCoordinates(dots);

        LineString lineString = JTSUtil.GEOMETRY_FACTORY.createLineString(coordinates);
        TestResult result = new TestResult();

        long start = Clock.currentTimeMillis();
//        Geometry simplified= DouglasPeuckerSimplifier.simplify(lineString, 5);

        Geometry geometry = lineString.buffer(14, 4, BufferParameters.CAP_FLAT);
        result.used = Clock.currentTimeMillis() - start;
        start = Clock.currentTimeMillis();
        Geometry simplified = simplify(geometry);
        result.simplifiedUsed = Clock.currentTimeMillis() - start;
        result.geometry = simplified;
        double o = geometry.getArea(), n = simplified.getArea();
        System.out.printf("%.6f - %.6f = %.6f %.3f%%%n", o, n, o - n, (o - n) * 100 / o);
        int co = geometry.getNumPoints(), cn = simplified.getNumPoints();
        System.out.printf("%d - %d = %d %.3f%%%n", co, cn, co - cn, (co - cn) * 100d / co);
        result.simplified = geometry.getNumPoints() - simplified.getNumPoints();
        return result;
    }

    private static Geometry build(Coordinate[] coordinates, int blockSize) throws InterruptedException {
        Analyzer analyzer = new Analyzer();
        try {
            Collection<Common.ArrayBlockRef> refs = slice(coordinates, blockSize, true, 0.1f);

            CountDownLatch countDownLatch = new CountDownLatch(refs.size());
            List<Geometry> geometries = new ArrayList<>();
            for (Common.ArrayBlockRef ref : refs) {
                BLOCK_BUILDER_EXECUTORS.submit(() -> {
                    try {
                        Coordinate[] coors = new Coordinate[ref.getLength()];
                        System.arraycopy(coordinates, ref.getStartIndex(), coors, 0, ref.getLength());
                        LineString lineString = analyzer.analyze("buildLineString", () -> JTSUtil.GEOMETRY_FACTORY.createLineString(coors));
                        geometries.add(analyzer.analyze("lineStringBuffer", () -> JTSUtil.get2DGeometry(lineString.buffer(14, 4, BufferParameters.CAP_FLAT))));
                    } finally {
                        countDownLatch.countDown();
                    }
                });
            }

            countDownLatch.await();
            Geometry geometry = null;
            for (Geometry g : geometries) {
                if (geometry == null) {
                    geometry = g;
                } else {
                    Geometry finalGeometry = geometry;
                    geometry = analyzer.analyze("mergePolygon", () -> finalGeometry.union(g));
                }
            }
            return geometry;

//            final Geometry[] result = {null};
//            forEachBlock(coordinates, blockSize, true, 0.1f, Coordinate[]::new, coors -> {
//
////                log.info("build geometry: {} points", coors.length);
//
//                LineString lineString = analyzer.analyze("buildLineString", () -> JTSUtil.GEOMETRY_FACTORY.createLineString(coors));
//
//                Geometry geometry = analyzer.analyze("lineStringBuffer", () -> JTSUtil.get2DGeometry(lineString.buffer(14, 4, BufferParameters.CAP_FLAT)));
//
//                if (result[0] == null) {
//                    result[0] = analyzer.analyze("simplifyPolygon", () -> simplify(geometry));
//                } else {
//
//                    Geometry finalResult1 = result[0];
//                    result[0] = analyzer.analyze("mergePolygon", () -> finalResult1.union(geometry));
//                    Geometry finalResult = result[0];
//                    result[0] = analyzer.analyze("simplifyPolygon", () -> simplify(
//                            finalResult
//                    ));
//                }
//            });
//            return result[0];
        } finally {
            analyzer.trace();
        }
    }

    private static Geometry simplify(Geometry geometry) {
//        double init = 25;
//        double rateLimit = 0.002d;
//        Geometry simplified = null;
//        double areaOfGeo = geometry.getArea();
//        for (; ; ) {
////         simplified = VWSimplifier.simplify(geometry,init);
//            simplified = DouglasPeuckerSimplifier.simplify(geometry, init);
//            double rate = simplified.getArea() / areaOfGeo;
//            if (rate > 1 - rateLimit && rate < 1 + rateLimit) {
//                break;
//            } else {
//                System.out.println("half " + init);
//                init = init / 2;
//            }
//
//        }
//        return JTSUtil.get2DGeometry(simplified);
        return geometry;
    }

    private static void testCase(int dots, int times) {
        long used = 0;
        long unionUsed = 0;
        Geometry geometry = null;
        long simplified = 0;
        long simplifiedUsed = 0;
        for (int i = 0; i < times; i++) {
            TestResult result = oneCase(dots);
            simplified += result.simplified;
            simplifiedUsed += result.simplifiedUsed;
            used += result.used;
            if (geometry == null) {
                geometry = JTSUtil.get2DGeometry(result.geometry);
            } else {
                long mergeStart = Clock.currentTimeMillis();
                System.out.println("union: " + geometry.getCoordinates().length + ", " + result.geometry.getCoordinates().length);
                geometry = simplify(geometry.union(result.geometry));
                unionUsed += Clock.currentTimeMillis() - mergeStart;
            }
        }
        Geometry simplifiedGeo = simplify(geometry);
        System.out.printf("geo: %d, %d%n", geometry.getCoordinates().length, simplifiedGeo.getCoordinates().length);
        System.out.printf("dots %d, %.3fms, simplified: %d, simplified used: %.3fms, rate:%.3f, merge used: %.3fms%n", dots, used * 1.0d / times,
                simplified,
                simplifiedUsed * 1.0d / times,
                used * 1.0d / times / dots,
                unionUsed * 1.d);
    }

    private static int getProcessId() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        return Integer.parseInt(runtimeMXBean.getName().split("@")[0]);
    }

    public static void main(String[] args) throws InterruptedException, IOException {
//        forEachBlock(CoordinatesLoader.loader(), 1000, true, 0.1f, (i, l) -> {
//            log.info(String.format("[%d, %d]", i, i + l - 1));
//        });
        System.out.println(getProcessId());
//
//        System.out.println("waiting for start.....");
//        Thread.sleep(3000);
////        int[] dots = new int[]{
////                30,50,70,90,110,130,150,170,190,210,
////                230,250,270,290,310,330,350
////        };
////
////        Arrays.stream(dots).forEach(dot -> testCase(dot, 15));
//        //预热
////        for(int i = 0; i < 10; i ++){
////            testCase(50,15);
////        }
////        for (int i = 100; i < 600; i += 25) {
////        testCase(1000, 5);
////        }
        ExecutorService executorService = ExecutorsHelper.newFixedThreadPool(8, "build");
        Analyzer analyzer = new Analyzer();
        int count = 40;
        CountDownLatch countDownLatch = new CountDownLatch(count);
        for (int x = 0; x < count; x++) {
            executorService.submit(() -> {
                try {
                    Geometry geometry = analyzer.analyze("total used",
                            () -> {
                                try {
                                    return build(CoordinatesLoader.loader(),
                                            600);
                                } catch (IOException | InterruptedException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                    int pointCount = geometry.getNumPoints();
                    Geometry finalGeometry = geometry;
                    geometry = analyzer.analyze("simplify", () -> DouglasPeuckerSimplifier.simplify(finalGeometry, 1));
                    System.out.printf("%d - %d = %d%n", pointCount, geometry.getNumPoints(), pointCount - geometry.getNumPoints());
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        while (!countDownLatch.await(5, TimeUnit.SECONDS)) {
            System.out.println("remain: " + countDownLatch.getCount());
        }
        analyzer.trace();

//        Geometry geometry = JTSUtil.GEOMETRY_FACTORY.createMultiPolygon(
//                new Polygon[]{
//                        JTSUtil.GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
//                                new Coordinate(0, 0),
//                                new Coordinate(10, 0),
//                                new Coordinate(10, 10),
//                                new Coordinate(0, 10),
//                                new Coordinate(0, 0)
//                        }),
//                        JTSUtil.GEOMETRY_FACTORY.createPolygon(new Coordinate[]{
//                                new Coordinate(1, 1),
//                                new Coordinate(9, 1),
//                                new Coordinate(9, 9),
//                                new Coordinate(1, 9),
//                                new Coordinate(1, 1)
//                        })
//                }
//        );
//
////        geometry.intersects()
//        System.out.println(geometry.getArea());

//        List<Coordinate> coordinateList = new ArrayList<>();
//        for (int i = 0; i < 100; i++) {
//            coordinateList.add(new Coordinate(
//                            Common.random(Integer.MAX_VALUE - 200),
//                            Common.random(Integer.MAX_VALUE - 200)
////                    i, i
//                    )
//            );
//        }
//        Coordinate[] coordinates = coordinateList.toArray(new Coordinate[0]);
//        System.out.println("start");
//        long timestamp = Clock.currentTimeMillis();
//        LineString lineString = JTSUtil.GEOMETRY_FACTORY.createLineString(
//                coordinates
//        );
////        Geometry geometry = buffer(coordinates, 14,3, BufferParameters.CAP_FLAT);
////        Geometry geometry = lineString.buffer(14,3,BufferParameters.CAP_FLAT);
//        long end = Clock.currentTimeMillis();
//        System.out.printf("solution 1: used: %dms\n", end - timestamp);
//
//        timestamp = Clock.currentTimeMillis();
//        Geometry geometry = lineString.buffer(14,3,BufferParameters.CAP_FLAT);
//        end = Clock.currentTimeMillis();
//        System.out.printf("solution 2: used: %dms", end - timestamp);
////        System.out.println(geometry);

    }

    public static Geometry buffer(Coordinate[] coordinates, double distance, int quadrantSegments, int endCapStyle) {
        if (coordinates.length <= 1) return null;
        Geometry geometry = JTSUtil.GEOMETRY_FACTORY.createLineString(new Coordinate[]{
                coordinates[0], coordinates[1]
        }).buffer(distance, quadrantSegments, endCapStyle);
        for (int i = 2, l = coordinates.length; i < l; i++) {
            geometry = geometry.union(
                    JTSUtil.GEOMETRY_FACTORY.createLineString(
                            new Coordinate[]{
                                    coordinates[i - 1], coordinates[i]
                            }
                    ).buffer(distance, quadrantSegments, endCapStyle)
            );
        }
        return geometry;
    }



    static class TestResult {
        Geometry geometry;
        int simplified;
        long simplifiedUsed;
        long used;
    }
}

class Analyzer {
    private static final Singleton<Boolean> ANALYZER_ENABLED = Singleton.with(() -> Config.getValue("org.coodex.analyze.enabled", false));
    private final Map<String, Long> datas = new HashMap<>();
    private final Map<String, Long> times = new HashMap<>();

    public <T> T analyze(String label, Supplier<T> supplier) {
        if (ANALYZER_ENABLED.get()) {
            long start = System.currentTimeMillis();
            try {
                return supplier.get();
            } finally {
                synchronized (this) {
                    Long used = datas.getOrDefault(label, 0L);
                    Long invokeTimes = times.getOrDefault(label, 0L);
                    datas.put(label, System.currentTimeMillis() - start + used);
                    times.put(label, invokeTimes + 1);
                }
            }
        } else {
            return supplier.get();
        }
    }

    public void analyze(String label, Runnable runnable) {
        analyze(label, () -> {
            runnable.run();
            return null;
        });
    }

    public void trace() {
        if (!ANALYZER_ENABLED.get()) return;
        StringJoiner joiner = new StringJoiner("\n");
        datas.forEach((k, v) -> {
            Long used = v;
            Long times = this.times.getOrDefault(k, 1L);
            joiner.add(String.format("%s used %d ms, invoke %d times, avg: %.3fms",
                    k, used, times, used * 1d / times));
        });
        System.out.println(joiner);
    }
}
