package kz.bsbnb.usci.showcase.dao;

import kz.bsbnb.usci.eav.showcase.ShowCase;
import org.springframework.jdbc.core.PreparedStatementCreator;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class CommonDao {
    protected enum HistoryState {
        ACTUAL,
        HISTORY
    }

    /* Prefix for showcase table names */
    protected final static String TABLES_PREFIX = "R_";

    /* Prefix for showcase column names */
    protected final static String COLUMN_PREFIX = "";

    /* Postfix for showcase history tables */
    protected final static String HISTORY_POSTFIX = "_HIS";

    /* Actual showcases */
    protected List<ShowCase> showCases;

    protected String getActualTableName(ShowCase showCaseMeta) {
        return TABLES_PREFIX + showCaseMeta.getTableName();
    }

    protected String getHistoryTableName(ShowCase showCaseMeta) {
        return TABLES_PREFIX + showCaseMeta.getTableName() + HISTORY_POSTFIX;
    }

    protected class GenericInsertPreparedStatementCreator implements PreparedStatementCreator {
        final String query;
        final Object[] values;
        final String keyName = "id";

        public GenericInsertPreparedStatementCreator(String query, Object[] values) {
            this.query = query;
            this.values = values.clone();
        }

        public PreparedStatement createPreparedStatement(Connection con) throws SQLException {
            PreparedStatement ps = con.prepareStatement(
                    query, new String[]{keyName});

            int i = 1;
            for (Object obj : values) {
                ps.setObject(i++, obj);
            }

            return ps;
        }
    }
}
