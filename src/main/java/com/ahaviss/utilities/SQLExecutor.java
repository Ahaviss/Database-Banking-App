package com.ahaviss.utilities;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.update.Update;
import java.sql.*;
import java.util.*;

public class SQLExecutor implements AutoCloseable{
    private final HikariDataSource dataSource;
    public SQLExecutor(String url, String username, String password) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(url);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        this.dataSource = new HikariDataSource(config);
    }
    public List<List<Map<String, Object>>> executeSQL(String sql, List<List<Object>> parameters)
            throws SQLException, JSQLParserException {
        if (sql == null || sql.isBlank()) return new ArrayList<>();
        List<ParsedQuery> statements = splitStatements(sql);
        if (statements.isEmpty()) return new ArrayList<>();

        try (Connection connection = dataSource.getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);
            try {
                List<List<Map<String, Object>>> results = executeStatements(connection, statements, parameters);
                connection.commit();
                return results;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw new SQLException("Execution failed, changes rolled back: " + e.getMessage(), e);
            }
            finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        }
    }
    @Override
    public void close () {dataSource.close();}
    public List<List<Map<String, Object>>> executeTransactionalSQL(Connection connection, String sql, List<List<Object>> parameters)
            throws SQLException, JSQLParserException {
        if (sql == null || sql.isBlank()) return new ArrayList<>();
        List<ParsedQuery> statements = splitStatements(sql);
        if (statements.isEmpty()) return new ArrayList<>();
        return executeStatements(connection, statements, parameters);
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
    private List<List<Map<String, Object>>> executeStatements(
            Connection connection,
            List<ParsedQuery> statements,
            List<List<Object>> parameters
    ) throws SQLException {
        List<List<Map<String, Object>>> results = new ArrayList<>();
        int parameterPointer = 0;

        for (ParsedQuery statement : statements) {
            try (PreparedStatement ps = connection.prepareStatement(statement.toString())) {
                int placeholderCount = statement.getPlaceholderCount();

                if (placeholderCount > 0) {
                    if (parameters == null || parameterPointer >= parameters.size()) {
                        throw new SQLException(
                                "Statement expects " + placeholderCount + " parameter(s) but none were supplied. " +
                                        "Statement: " + statement
                        );
                    }
                    List<Object> stmtParams = parameters.get(parameterPointer++);
                    bindParams(ps, stmtParams, placeholderCount);
                }
                boolean hasResult = ps.execute();
                while (true) {
                    if (hasResult) {
                        try (ResultSet rs = ps.getResultSet()) {
                            results.add(drainResultSet(rs));
                        }
                    } else {
                        int updateCount = ps.getUpdateCount();
                        if (updateCount == -1) {
                            break;
                        }
                    }
                    hasResult = ps.getMoreResults();
                }

            }
        }

        return results;
    }

    private static List<ParsedQuery> splitStatements(String sql) throws JSQLParserException, SQLException {
        List<ParsedQuery> parsed = new ArrayList<>();
        for (net.sf.jsqlparser.statement.Statement s : CCJSqlParserUtil.parseStatements(sql)) {
            parsed.add(new ParsedQuery(s, countPlaceholders(s.toString())));
        }
        return parsed;
    }

    private static int countPlaceholders(String sql) {
        int count = 0;
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < sql.length(); i++) {
            char c = sql.charAt(i);
            if (c == '\'' && !inDoubleQuote) { inSingleQuote = !inSingleQuote; continue; }
            if (c == '"'  && !inSingleQuote) { inDoubleQuote = !inDoubleQuote; continue; }
            if (c == '?'  && !inSingleQuote && !inDoubleQuote) count++;
        }
        return count;
    }

    private static void bindParams(PreparedStatement ps, List<Object> params, int expected) throws SQLException {
        int provided = (params == null) ? 0 : params.size();
        if (provided != expected) {
            throw new SQLException(
                    "Parameter count mismatch: statement expects " + expected +
                            " but " + provided + " were provided."
            );
        }
        if (params == null) return;
        for (int i = 0; i < params.size(); i++) {ps.setObject(i + 1, params.get(i));}
    }

    private static List<Map<String, Object>> drainResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        String[] columnNames = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = meta.getColumnLabel(i + 1);
        }

        List<Map<String, Object>> rows = new ArrayList<>();
        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>(columnCount * 2);
            for (int i = 0; i < columnCount; i++) {
                row.put(columnNames[i], rs.getObject(i + 1));
            }
            rows.add(row);
        }
        return rows;
    }
    static class ParsedQuery {
        private final net.sf.jsqlparser.statement.Statement statement;
        private final int placeholderCount;
        public ParsedQuery(net.sf.jsqlparser.statement.Statement statement, int placeholderCount) throws SQLException {
            validate(statement);
            this.statement = statement;
            this.placeholderCount = placeholderCount;
        }
        @Override
        public String toString() {return statement.toString();}
        public int getPlaceholderCount() {return placeholderCount;}
    }
    private static void validate(net.sf.jsqlparser.statement.Statement statement) throws SQLException {
        if (statement instanceof Update update) {
            if (update.getWhere() == null) {
                throw new SQLException("UPDATE without WHERE clause is not permitted: " + statement);
            }
        }
        if (statement instanceof Delete delete) {
            if (delete.getWhere() == null) {
                throw new SQLException("DELETE without WHERE clause is not permitted: " + statement);
            }
        }
    }
}

