/*
 * Copyright (c) 2019 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.apm.mysql8;

import com.mysql.cj.MysqlConnection;
import com.mysql.cj.Query;
import com.mysql.cj.interceptors.QueryInterceptor;
import com.mysql.cj.log.Log;
import com.mysql.cj.protocol.Resultset;
import com.mysql.cj.protocol.ServerSession;
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.apm.Trace;

import java.util.Properties;
import java.util.function.Supplier;

/**
 * 修改自 https://github.com/openzipkin/brave/blob/master/instrumentation/mysql8/src/main/java/brave/mysql8/TracingQueryInterceptor.java
 */
public class TracingQueryInterceptor implements QueryInterceptor {
    static ThreadLocal<Trace> traceThreadLocal = new ThreadLocal<Trace>();

    @Override
    public <T extends Resultset> T preProcess(Supplier<String> sqlSupplier, Query interceptedQuery) {
        Trace trace = APM.build();
        traceThreadLocal.set(trace);
//         Gets the next span (and places it in scope) so code between here and postProcess can read it
//        Span span = ThreadLocalSpan.CURRENT_TRACER.next();
//        if (span == null || span.isNoop()) return null;

        String sql = sqlSupplier.get();
        int spaceIndex = sql.indexOf(' '); // Allow span names of single-word statements like COMMIT
//        trace.kind(Span.Kind.CLIENT).name(spaceIndex == -1 ? sql : sql.substring(0, spaceIndex));
        trace.tag("sql.query", sql).start(spaceIndex == -1 ? sql : sql.substring(0, spaceIndex));
//        parseServerIpAndPort(connection, span);
//        span.start();
        return null;
    }

    private MysqlConnection connection;
    private boolean interceptingExceptions;

    @Override
    public <T extends Resultset> T postProcess(Supplier<String> sql, Query interceptedQuery,
                                               T originalResultSet, ServerSession serverSession) {
//        if (interceptingExceptions && originalResultSet == null) {
//            // Error case, the span will be finished in TracingExceptionInterceptor.
//            return null;
//        }
//        Span span = ThreadLocalSpan.CURRENT_TRACER.remove();
//        if (span == null || span.isNoop()) return null;
//
//        span.finish();
//
//        return null;
        Trace trace = traceThreadLocal.get();
        traceThreadLocal.remove();
        if (trace != null) {
//            if (statementException != null) {
//                trace.error(statementException);
//            }
            trace.finish();
        }
        return null;
    }

//    /**
//     * MySQL exposes the host connecting to, but not the port. This attempts to get the port from the
//     * JDBC URL. Ex. 5555 from {@code jdbc:mysql://localhost:5555/database}, or 3306 if absent.
//     */
//    static void parseServerIpAndPort(MysqlConnection connection, Span span) {
//        try {
//            URI url = URI.create(connection.getURL().substring(5)); // strip "jdbc:"
//            String remoteServiceName = connection.getProperties().getProperty("zipkinServiceName");
//            if (remoteServiceName == null || "".equals(remoteServiceName)) {
//                String databaseName = getDatabaseName(connection);
//                if (databaseName != null && !databaseName.isEmpty()) {
//                    remoteServiceName = "mysql-" + databaseName;
//                } else {
//                    remoteServiceName = "mysql";
//                }
//            }
//            span.remoteServiceName(remoteServiceName);
//            String host = getHost(connection);
//            if (host != null) {
//                span.remoteIpAndPort(host, url.getPort() == -1 ? 3306 : url.getPort());
//            }
//        } catch (Exception e) {
//            // remote address is optional
//        }
//    }

//    private static String getDatabaseName(MysqlConnection connection) throws SQLException {
//        if (connection instanceof JdbcConnection) {
//            return ((JdbcConnection) connection).getCatalog();
//        }
//        return "";
//    }

//    private static String getHost(MysqlConnection connection) {
//        if (!(connection instanceof JdbcConnection)) return null;
//        return ((JdbcConnection) connection).getHost();
//    }

    @Override
    public boolean executeTopLevelOnly() {
        return true;  // True means that we don't get notified about queries that other interceptors issue
    }

    @Override
    public QueryInterceptor init(MysqlConnection mysqlConnection, Properties properties,
                                 Log log) {
        String exceptionInterceptors = properties.getProperty("exceptionInterceptors");
        TracingQueryInterceptor interceptor = new TracingQueryInterceptor();
        interceptor.connection = mysqlConnection;
        interceptor.interceptingExceptions = exceptionInterceptors != null &&
                exceptionInterceptors.contains(TracingExceptionInterceptor.class.getName());
        if (!interceptor.interceptingExceptions) {
            log.logWarn("TracingExceptionInterceptor not enabled. It is highly recommended to "
                    + "enable it for error logging to Zipkin.");
        }
        return interceptor;
    }

    @Override
    public void destroy() {
        // Don't care
    }
}
