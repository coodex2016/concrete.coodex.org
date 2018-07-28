/*
 * Copyright (c) 2018 coodex.org (jujus.shen@126.com)
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

package org.coodex.concrete.apm.mysql;

import com.mysql.jdbc.*;
import org.coodex.concrete.apm.APM;
import org.coodex.concrete.apm.Trace;

import java.sql.SQLException;
import java.util.Properties;

/**
 * 修改自 https://github.com/openzipkin/brave/blob/master/instrumentation/mysql/src/main/java/brave/mysql/TracingStatementInterceptor.java
 */
public class TracingStatementInterceptor implements StatementInterceptorV2 {

    private static ThreadLocal<Trace> traceThreadLocal = new ThreadLocal<Trace>();

    @Override
    public ResultSetInternalMethods preProcess(String sql, Statement interceptedStatement,
                                               Connection connection) {
        Trace trace = APM.build();
        traceThreadLocal.set(trace);
//        // Gets the next span (and places it in scope) so code between here and postProcess can read it
//        Span span = ThreadLocalSpan.CURRENT_TRACER.next();
//        if (span == null || span.isNoop()) return null;
//
//        // When running a prepared statement, sql will be null and we must fetch the sql from the statement itself
        if (interceptedStatement instanceof PreparedStatement) {
            sql = ((PreparedStatement) interceptedStatement).getPreparedSql();
        }
        int spaceIndex = sql.indexOf(' '); // Allow span names of single-word statements like COMMIT
//        span.kind(Span.Kind.CLIENT).name(spaceIndex == -1 ? sql : sql.substring(0, spaceIndex));
//        span.tag("sql.query", sql);
//        parseServerAddress(connection, span);
//        span.start();
        trace.tag("sql.query", sql).start(spaceIndex == -1 ? sql : sql.substring(0, spaceIndex));
        return null;
    }

    @Override
    public ResultSetInternalMethods postProcess(String sql, Statement interceptedStatement,
                                                ResultSetInternalMethods originalResultSet, Connection connection, int warningCount,
                                                boolean noIndexUsed, boolean noGoodIndexUsed, SQLException statementException) {
//        Span span = ThreadLocalSpan.CURRENT_TRACER.remove();
//        if (span == null || span.isNoop()) return null;
//
//        if (statementException != null) {
//            span.tag("error", Integer.toString(statementException.getErrorCode()));
//        }
//        span.finish();
        Trace trace = traceThreadLocal.get();
        traceThreadLocal.remove();
        if (trace != null) {
            if (statementException != null) {
                trace.error(statementException);
            }
            trace.finish();
        }
        return null;
    }

    /**
     * MySQL exposes the host connecting to, but not the port. This attempts to get the port from the
     * JDBC URL. Ex. 5555 from {@code jdbc:mysql://localhost:5555/database}, or 3306 if absent.
     */
//    static void parseServerAddress(Connection connection, Span span) {
//        try {
//            URI url = URI.create(connection.getMetaData().getURL().substring(5)); // strip "jdbc:"
//            int port = url.getPort() == -1 ? 3306 : url.getPort();
//            String remoteServiceName = connection.getProperties().getProperty("zipkinServiceName");
//            if (remoteServiceName == null || "".equals(remoteServiceName)) {
//                String databaseName = connection.getCatalog();
//                if (databaseName != null && !databaseName.isEmpty()) {
//                    remoteServiceName = "mysql-" + databaseName;
//                } else {
//                    remoteServiceName = "mysql";
//                }
//            }
//            Endpoint.Builder builder = Endpoint.newBuilder().serviceName(remoteServiceName).port(port);
//            builder.parseIp(connection.getHost());
//            span.remoteEndpoint(builder.build());
//        } catch (Exception e) {
//            // remote address is optional
//        }
//    }
    @Override
    public boolean executeTopLevelOnly() {
        return true; // True means that we don't get notified about queries that other interceptors issue
    }

    @Override
    public void init(Connection conn, Properties props) {
        // Don't care
    }

    @Override
    public void destroy() {
        // Don't care
    }
}
