/*
 * Copyright (c) 2020 coodex.org (jujus.shen@126.com)
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

package org.coodex.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.coodex.util.Common.toAbsolutePath;

/**
 * 资源扫描器，用来替换蹩脚的Common.forEach
 * <p>
 * 参数<code>coodex.resource.path</code>用来指定扩展的资源路径
 */
public class ResourceScanner {
    private static final ThreadLocal<Integer> EXTRA_PATH_INDEX = new ThreadLocal<>();

    private final static Logger log = LoggerFactory.getLogger(ResourceScanner.class);

    public static String KEY_RESOURCE_PATH_EXT = "coodex.resource.path";
    private final BiConsumer<URL, String> processor;
    private final Function<String, Boolean> filter;
    private final boolean extraPath;

    private ResourceScanner(
            BiConsumer<URL, String> processor,
            Function<String, Boolean> filter,
            boolean extraPath) {
        this.processor = processor;
        this.filter = filter == null ? s -> true : filter;
        this.extraPath = extraPath;
    }

    /**
     * @return 扫描过程中processor使用，用来判定当前扫描的包是否是在扩展路径里
     */
    public static boolean isExtraPath() {
        return EXTRA_PATH_INDEX.get() != null;
    }

    public static Integer getExtraPathIndex() {
        return EXTRA_PATH_INDEX.get();
    }

    /**
     * @return 通过<code>coodex.resource.path</code>指定的扩展资源路径绝对路径
     */
    public static List<String> getExtraResourcePath() {
        List<String> resourcePaths = new ArrayList<>();
        String param = System.getProperty(KEY_RESOURCE_PATH_EXT, "").trim();
        if (!Common.isBlank(param)) {
            String[] paths = param.split(Common.PATH_SEPARATOR);
            for (String path : paths) {
                if (!Common.isBlank(path)) {
                    resourcePaths.add(toAbsolutePath(path));
                }
            }
        }
        return resourcePaths;
    }

    public static Builder newBuilder(BiConsumer<URL, String> processor) {
        return new Builder(processor);
    }

    // 按照表达式定义转换为正则表达式
    private static Set<PathPattern> toPathPatterns(String[] paths) {
        Set<PathPattern> pathPatterns = new LinkedHashSet<>();
        if (paths != null && paths.length > 0) {
            for (String path : paths) {
                pathPatterns.add(new PathPattern(Common.trim(path) + "/"));
            }
        }
        return pathPatterns;
    }

    /**
     * @param pathPatterns pathPattern
     * @return 合并正则表达式
     */
    private static Collection<String> merge(Collection<PathPattern> pathPatterns) {
        List<String> list = new ArrayList<>();
        for (PathPattern pathPattern : pathPatterns) {
            list.add(pathPattern.path);
        }
        String[] toMerge = list.toArray(new String[0]);
        list.clear();
        // 排序
        Arrays.sort(toMerge, Comparator.comparingInt(String::length));
        for (String s : toMerge) {
            boolean exits = false;
            for (String x : list) {
                if (s.startsWith(x)) {
                    exits = true;
                    break;
                }
            }
            if (!exits) {
                list.add(s);
            }
        }
        return list;
    }

    public void scan(String... paths) {
        try {
            // 所有的资源根patterns，用来进行初步匹配，减少匹配次数
            final Set<PathPattern> pathPatterns = toPathPatterns(paths);

            Function<String, Boolean> resourceFilter = resourceName -> {
                boolean pathOk = false;
                for (PathPattern pathPattern : pathPatterns) {
                    if (pathPattern.pattern.matcher(resourceName).matches()) {
                        pathOk = true;
                        break;
                    }
                }
                return pathOk && filter.apply(resourceName);
            };
            // 所有paths Pattern的共性根组，例如a/**/x、a/b/c/d会合并成a
            Collection<String> merged = merge(pathPatterns);

            // 扩展资源路径中检索资源
            if (extraPath) {
                scanInExtraPath(resourceFilter, merged);
            }

            // 在共性根里找资源
            for (String path : merged) {
                path = Common.trim(path.replace('\\', '/'), '/');
                // 获取所有的当前根resource，并根据根resource所在位置进行遍历
                Enumeration<URL> resourceRoots = getClass().getClassLoader().getResources(path);

                while (resourceRoots.hasMoreElements()) {
                    URL url = resourceRoots.nextElement();
                    String urlStr = url.toString();// 共性根资源的绝对路径
                    URL baseUrl = new URL( // 把path去掉
                            urlStr.substring(0, urlStr.length() - path.length())
                    );

                    log.debug("scan items in {}", url);
                    // 判定是否是文件系统
                    if ("file".equalsIgnoreCase(url.getProtocol())) {
                        scanInDir(baseUrl, resourceFilter, new File(url.toURI()), path);
                    } else {

                        // jar:file:/jarFilePath.jar!/BOOT-INF/classes!/
                        // jar:file:/jarFilePath.jar!/jarEntry.jar!/
                        // 单一的
                        // jar:file:/jarFilePath.jar!/
                        String[] nodes = urlStr.split("!/");
                        if (nodes.length == 2) {//普通jar包模式
                            scanInZip(
                                    baseUrl,
                                    new URL(nodes[0].substring(url.getProtocol().length() + 1)),
                                    "",
                                    resourceFilter);
                        } else {

                            //看倒数第二个是不是.jar或者.zip
                            String forTest = nodes[nodes.length - 2];
                            forTest = forTest.substring(forTest.length() - 4);
                            String pathInJar = "";
                            int lastJarNode;
                            if (!".jar".equalsIgnoreCase(forTest) && !".zip".equalsIgnoreCase(forTest)) {
                                pathInJar = nodes[nodes.length - 2] + "/";
                                lastJarNode = nodes.length - 3;
                            } else {
                                lastJarNode = nodes.length - 2;
                            }

                            // 构建url
                            String zipItemUrl;
                            if (lastJarNode == 0) {
                                zipItemUrl = nodes[0].substring(url.getProtocol().length() + 1);
                            } else {
                                StringJoiner joiner = new StringJoiner("!/");
                                for (int i = 0; i <= lastJarNode; i++) {
                                    joiner.add(nodes[i]);
                                }
                                zipItemUrl = joiner.toString();
                            }
                            scanInZip(baseUrl, new URL(zipItemUrl), pathInJar, resourceFilter);

                        }
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            log.warn("resource search failed. {}.", e.getLocalizedMessage(), e);
        }
    }

    private void scanInZip(
            URL baseUrl,
            URL zip,
            String context,
            Function<String, Boolean> resourceFilter) throws IOException {

        try (ZipInputStream zipInputStream = new ZipInputStream(zip.openStream())) {
            ZipEntry zipEntry;
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                String entryName = zipEntry.getName();
                if (zipEntry.isDirectory() || !entryName.startsWith(context)) continue;
                String resourceName = entryName.substring(context.length());

                if (resourceFilter.apply(resourceName))
                    try {
                        processor.accept(new URL(baseUrl, resourceName), resourceName);
                    } catch (Throwable th) {
                        log.warn("resource process failed: {}, {}", baseUrl, resourceName, th);
                    }
            }
        }
    }

    private void scanInDir(
            URL baseURL,
            Function<String, Boolean> resourceFilter,
            File searchDir,
            String contextPath) {
        File[] list = searchDir.listFiles();
        if (list == null || list.length == 0) return;
        for (File f : list) {
            if (f.isDirectory()) {
                scanInDir(baseURL, resourceFilter, f, contextPath == null ? f.getName() : (contextPath + "/" + f.getName()));
            } else {
                String resourceName = contextPath == null ? f.getName() : (contextPath + "/" + f.getName());
                if (resourceFilter.apply(resourceName))
                    try {
                        processor.accept(new URL(baseURL, resourceName), resourceName);
                    } catch (Throwable th) {
                        log.warn("resource process failed: {}, {},{}", baseURL, contextPath, resourceName, th);
                    }
            }
        }
    }

    private void scanInExtraPath(Function<String, Boolean> resourceFilter,
                                 Collection<String> merged) {
        AtomicInteger atomicInteger = new AtomicInteger(0);
        getExtraResourcePath().forEach(root -> {
            EXTRA_PATH_INDEX.set(atomicInteger.getAndIncrement());
            try {
                merged.forEach(path -> {
                    try {
                        path = Common.trim(path.replace('\\', '/'), '/');
                        URL resourceRoot = new File(root).toURI().toURL();
                        log.debug("Scan items in coodex.resource.path: [{}]", resourceRoot.toString());
                        scanInDir(
                                resourceRoot,
                                resourceFilter,
                                new File((Common.isBlank(path) ? resourceRoot : new URL(resourceRoot, path)).toURI()),
                                path
                        );
                    } catch (Throwable th) {
                        log.warn("load from {} failed: {}", root, th.getLocalizedMessage(), th);
                    }
                });
            } finally {
                EXTRA_PATH_INDEX.remove();
            }
        });

    }

//    private void scanInZip(String root,
//                           String path,
//                           Function<String, Boolean> filter,
//                           URL zipFile,
//                           String entryPath) throws IOException {
//        log.debug("Scan items in [{}]: {{}}", zipFile.toString(), path);
//        try (ZipInputStream zip = new ZipInputStream(zipFile.openStream())) {
//            ZipEntry entry;
//            String entryContext = Common.isBlank(entryPath) ? "" : entryPath.substring(1);
//
//            while ((entry = zip.getNextEntry()) != null) {
//                if (entry.isDirectory()) continue;
//                String entryName = entry.getName();
//                if (!Common.isBlank(entryContext) && !entryName.startsWith(entryContext)) continue;
//                // 此包中的检索
//                String resourceName = Common.isBlank(entryContext) ? entryName : entryName.substring(entryContext.length() + 1);
//                if (resourceName.startsWith(path) && filter.apply(resourceName)) {
//                    processor.accept(new URL(root + "/" + entryName), resourceName);
//                }
//            }
//        }
//    }

//    private void scanInDir(String root,
//                           String path,
//                           Function<String, Boolean> filter,
//                           File dir,
//                           boolean header) throws MalformedURLException {
//        if (header)
//            log.debug("Scan items in dir[{}]:[{}]", dir.getAbsolutePath(), path);
//        if (dir.isDirectory() && dir.exists()) {
//            for (File f : Optional.ofNullable(dir.listFiles()).orElse(new File[0])) {
//                String resourceName = path + '/' + f.getName();
//                if (f.isDirectory()) {
//                    scanInDir(root, resourceName, filter, f, false);
//                } else {
//                    if (filter.apply(resourceName)) {
//                        processor.accept(new URL(root + '/' + resourceName), resourceName);
//                    }
//                }
//            }
//        }
//    }

    private static class PathPattern {
        private final Pattern pattern;
        private final String path;
        private final String originalPath;

        public PathPattern(String path) {
            this.originalPath = path;
            this.pattern = Pattern.compile(
                    "^" + Common.trim(path)
                            .replaceAll("\\.", "\\\\.")
                            .replaceAll("/\\*{2,}/", "(/|/.+/)")
                            .replaceAll("\\*{2,}", ".+")// 两个以上*匹配任意字符
                            .replaceAll("\\*", "[^/]+")
                            + ".*"
            );
            this.path = pathRoot(path);
        }

        private String pathRoot(String pattern) {
            StringBuilder builder = new StringBuilder();
            StringBuilder node = new StringBuilder();
            for (char ch : pattern.toCharArray()) {
                if (ch == '*') break;
                if (ch == '/') {
                    builder.append(node).append(ch);
                    node = new StringBuilder();
                } else
                    node.append(ch);

            }
            if (node.length() > 0)
                builder.append(node);
            return Common.trim(builder.toString());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof PathPattern)) return false;

            PathPattern that = (PathPattern) o;

            return originalPath.equals(that.originalPath);
        }

        @Override
        public int hashCode() {
            return originalPath.hashCode();
        }
    }

    public static class Builder {
        private final BiConsumer<URL, String> processor;
        private Function<String, Boolean> filter;
        private boolean extraPath = false;

        private Builder(BiConsumer<URL, String> processor) {
            this.processor = Objects.requireNonNull(processor);
        }

        public Builder extraPath(boolean extraPath) {
            this.extraPath = extraPath;
            return this;
        }

        public Builder filter(Function<String, Boolean> filter) {
            this.filter = filter;
            return this;
        }

        public ResourceScanner build() {
            return new ResourceScanner(processor, filter, extraPath);
        }
    }

//    public static void main(String[] args) throws IOException {
//
//        Enumeration<URL> urlEnumeration = ResourceScanner.class.getClassLoader().getResources(null);
//        while(urlEnumeration.hasMoreElements()){
//            System.out.println(urlEnumeration.nextElement());
//        }
//    }

}
