package kz.bsbnb.usci.portlets.query;

import com.vaadin.data.util.ObjectProperty;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;

/**
 *
 * @author Aidar.Myrzahanov
 */
class SqlExecutor {

    private static final String FETCH_EXPLAIN_PLAN_QUERY = "select plan_table_output from table(dbms_xplan.display())";

    private static final String LAST_ERROR_POSITION_QUERY = "DECLARE\n"
            + "  c        INTEGER := DBMS_SQL.open_cursor();\n"
            + "  errorpos integer := -1;\n"
            + "BEGIN\n"
            + "  BEGIN\n"
            + "    DBMS_SQL.parse(c, :sqltext, DBMS_SQL.native);\n"
            + "  EXCEPTION\n"
            + "    WHEN OTHERS THEN\n"
            + "      errorpos := DBMS_SQL.LAST_ERROR_POSITION();\n"
            + "  END;\n"
            + "  :errorpos := errorpos;\n"
            + "  DBMS_SQL.close_cursor(c);\n"
            + "END;";

    private final ObjectProperty<String> connectionStringProperty = new ObjectProperty<String>("jdbc:oracle:thin:@10.8.1.200:1521:essp");
    private final ObjectProperty<String> usernameProperty = new ObjectProperty<String>("core");
    private final ObjectProperty<String> passwordProperty = new ObjectProperty<String>("core");
    private final ObjectProperty<Integer> limitProperty = new ObjectProperty<Integer>(1000);
    private final ObjectProperty<Integer> timeoutProperty = new ObjectProperty<Integer>(300);
    private final QuerySettings settings;

    private QueryType queryType;
    private boolean usingConnectionPool;

    private ResultsTable queryResultTable;
    private String textResults;
    private long executionTimeMillis;
    private int affectedRowsCount;
    private int rowsCount;
    private String exceptionMessage;
    private int sqlErrorPosition = -1;
    private boolean isMaintenance;

    public final Logger logger = Logger.getLogger(SqlExecutor.class);

    SqlExecutor(QuerySettings settings) {
        this.settings = settings;
        usingConnectionPool = false;
        Connection connection = null;
        try {
            connection = getConnection();
            if (connection != null && connection.getMetaData() != null) {
                connectionStringProperty.setValue(connection.getMetaData().getURL());
            }
        } catch (SQLException e) {
            logger.error(null, e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException ex) {
                    logger.error(null, ex);
                }
            }
        }
    }

    private Connection getConnection() {
        Connection conn = null;
        try {
            if (usingConnectionPool) {
                Context context = new InitialContext();
                DataSource d = (DataSource) context.lookup(settings.getPoolName());
                conn = d.getConnection();
            } else {
                Class.forName("oracle.jdbc.OracleDriver");
                conn = DriverManager.getConnection(connectionStringProperty.getValue(), usernameProperty.getValue(), passwordProperty.getValue());
            }
        } catch (SQLException ex) {
            saveException(ex);
        } catch (NamingException ex) {
            saveException(ex);
        } catch (ClassNotFoundException ex) {
            saveException(ex);
        }
        return conn;
    }

    private Connection getReporterConnection() {
        Connection conn = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection("jdbc:oracle:thin:@10.8.1.85:1521:showcase", "reporter", "reporter_2015");
            //conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:orcl", "REPORTER", "REPORTER_2015");
        } catch (SQLException ex) {
            saveException(ex);
        } catch (ClassNotFoundException ex) {
            saveException(ex);
        }
        return conn;
    }

    private void saveException(Exception ex) {
        logger.error(null, ex);
        exceptionMessage = ex.getMessage();
    }

    public void runQuery(String query,boolean isMaintenance) {
        this.isMaintenance = isMaintenance;
        runQuery(query);
    }

    public void runQuery(String query) {
        if (!isQuerySafe(query)) {
            return;
        }
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        exceptionMessage = null;
        sqlErrorPosition = -1;
        queryResultTable = null;
        try {
            if(isMaintenance)
                conn = getReporterConnection();
            else
                conn = getConnection();

            if (conn == null) {
                return;
            }
            statement = conn.createStatement();
            statement.setMaxRows(limitProperty.getValue());
            statement.setQueryTimeout(timeoutProperty.getValue());

            long timeBeforeQueryMillis = System.currentTimeMillis();
            if (queryType == QueryType.SELECT) {
                resultSet = statement.executeQuery(query);
                parseResultSet(resultSet);
            } else if (queryType == QueryType.INSERT_OR_UPDATE) {
                affectedRowsCount = statement.executeUpdate(query);
            } else if (queryType == QueryType.EXPLAIN_PLAN) {
                statement.execute("EXPLAIN PLAN FOR " + query);
                resultSet = statement.executeQuery(FETCH_EXPLAIN_PLAN_QUERY);
                parseResultSet(resultSet);
            }
            long timeAfterQueryMillis = System.currentTimeMillis();
            executionTimeMillis = timeAfterQueryMillis - timeBeforeQueryMillis;
        } catch (SQLException ex) {
            sqlErrorPosition = retrieveErrorPosition(conn, query);
            saveException(ex);
        } finally {
            isMaintenance = false;
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (SQLException sqle) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqle) {
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqle) {
                }
            }
        }
    }

    private int retrieveErrorPosition(Connection connection, String query) {
        CallableStatement callStatement = null;
        try {
            callStatement = connection.prepareCall(LAST_ERROR_POSITION_QUERY);
            callStatement.setString(1, query);
            callStatement.registerOutParameter(2, OracleTypes.INTEGER);
            callStatement.execute();
            return callStatement.getInt(2);
        } catch (SQLException ex) {
            logger.error(null, ex);
        } finally {
            if (callStatement != null) {
                try {
                    callStatement.close();
                } catch (SQLException sqle) {
                }
            }
        }
        return -1;
    }

    private boolean isQuerySafe(String query) {
        String queryText = query.toLowerCase();
        if (queryText.contains("where")) {
            return true;
        }
        if (queryText.contains("update")) {
            exceptionMessage = "UPDATE without WHERE";
            return false;
        }
        if (queryText.contains("delete")) {
            exceptionMessage = "DELETE without WHERE";
            return false;
        }
        return true;
    }

    private void parseResultSet(ResultSet resultSet) throws SQLException, UnsupportedOperationException {
        StringBuilder textBuilder = new StringBuilder();
        ResultSetMetaData metaData = resultSet.getMetaData();
        queryResultTable = new ResultsTable(settings.getOutputDateFormat());
        queryResultTable.setWidth("100%");
        int columnCount = metaData.getColumnCount();
        HashMap<String, Integer> duplicatedColumnNames = new HashMap<String, Integer>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            Class columnClass = String.class;
            int columnType = metaData.getColumnType(columnIndex);
            if (columnType == OracleTypes.NUMBER) {
                columnClass = Double.class;
            } else if (columnType == OracleTypes.TIMESTAMP) {
                columnClass = Date.class;
            }
            String columnName = metaData.getColumnName(columnIndex);
            textBuilder.append(columnName).append("\t");
            Integer nameIndex = duplicatedColumnNames.get(columnName);
            String columnPropertyName;
            if (nameIndex == null) {
                nameIndex = 1;
                columnPropertyName = columnName;
            } else {
                columnPropertyName = columnName + nameIndex;
                nameIndex++;
            }
            duplicatedColumnNames.put(columnName, nameIndex);
            queryResultTable.addContainerProperty(columnPropertyName, columnClass, null);
            queryResultTable.setColumnHeader(columnPropertyName, columnName);
        }
        textBuilder.append("\n");
        int rowNumber = 1;
        while (resultSet.next()) {
            Object[] values = new Object[columnCount];
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                if (resultSet.getObject(columnIndex) == null) {
                    values[columnIndex - 1] = null;
                    continue;
                }
                int columnType = metaData.getColumnType(columnIndex);
                if (columnType == OracleTypes.NUMBER) {
                    values[columnIndex - 1] = resultSet.getDouble(columnIndex);
                } else if (columnType == OracleTypes.TIMESTAMP) {
                    values[columnIndex - 1] = resultSet.getTimestamp(columnIndex);
                } else {
                    values[columnIndex - 1] = resultSet.getString(columnIndex);
                }
                textBuilder.append(values[columnIndex - 1]).append("\t");
            }
            queryResultTable.addItem(values, rowNumber + "");
            textBuilder.append("\n");
            rowNumber++;
        }
        rowsCount = rowNumber - 1;
        textResults = textBuilder.toString();
    }

    /**
     * @return the connectionStringProperty
     */
    ObjectProperty<String> getConnectionStringProperty() {
        return connectionStringProperty;
    }

    /**
     * @return the usernameProperty
     */
    ObjectProperty<String> getUsernameProperty() {
        return usernameProperty;
    }

    /**
     * @return the passwordProperty
     */
    ObjectProperty<String> getPasswordProperty() {
        return passwordProperty;
    }

    /**
     * @return the limitProperty
     */
    ObjectProperty<Integer> getLimitProperty() {
        return limitProperty;
    }

    /**
     * @return the timeoutProperty
     */
    ObjectProperty<Integer> getTimeoutProperty() {
        return timeoutProperty;
    }

    /**
     * @param usingConnectionPool the usingConnectionPool to set
     */
    void setUsingConnectionPool(boolean usingConnectionPool) {
        this.usingConnectionPool = usingConnectionPool;
    }

    /**
     * @param queryType the queryType to set
     */
    void setQueryType(QueryType queryType) {
        this.queryType = queryType;
    }

    /**
     * @return the queryResult
     */
    ResultsTable getQueryResultTable() {
        return queryResultTable;
    }

    /**
     * @return the executionTimeMillis
     */
    long getExecutionTimeMillis() {
        return executionTimeMillis;
    }

    /**
     * @return the affectedRowsNumber
     */
    int getAffectedRowsCount() {
        return affectedRowsCount;
    }

    /**
     * @return the rowsNumber
     */
    int getRowsCount() {
        return rowsCount;
    }

    /**
     * @return the exceptionMessage
     */
    String getExceptionMessage() {
        return exceptionMessage;
    }
    
    int getSqlErrorPosition() {
        return sqlErrorPosition;
    }

    /**
     * @return the textResults
     */
    String getTextResults() {
        return textResults;
    }

}
