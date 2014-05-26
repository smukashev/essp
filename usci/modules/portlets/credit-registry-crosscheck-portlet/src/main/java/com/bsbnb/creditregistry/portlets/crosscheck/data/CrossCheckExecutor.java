package com.bsbnb.creditregistry.portlets.crosscheck.data;

import java.math.BigInteger;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import oracle.jdbc.OracleTypes;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static com.bsbnb.creditregistry.portlets.crosscheck.CrossCheckApplication.log;
import com.bsbnb.creditregistry.portlets.crosscheck.dm.Creditor;

/**
 *
 * @author Marat.Madybayev
 */
public class CrossCheckExecutor {

    private static CrossCheckExecutor instance = new CrossCheckExecutor();

    private CrossCheckExecutor() {
    }

    public static CrossCheckExecutor get() {
        return instance;
    }

    private Connection getConnection() throws SQLException {
        Context context;
        Connection conn = null;
        try {
            context = new InitialContext();
            DataSource d = (DataSource) context.lookup("jdbc/RepPool");
            conn = d.getConnection();
            return conn;
        } catch (SQLException sqle) {
            throw sqle;
        } catch (NamingException ne) {
            throw new SQLException(ne.getMessage(), ne);
        }
    }

   /* public void loadCreditorProc() throws SQLException {
        Connection conn = getConnection();
        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall("{call REPORT_FILL_REFCREDITOR(?, ?)}");
            stmt.setInt(1, 0);
            stmt.registerOutParameter(2, OracleTypes.INTEGER);
            stmt.execute();
            int error = stmt.getInt(2);
            if (error != 0) {
                String errorMessage = "procedure report_fill_refcreditor failed";
                throw new SQLException(errorMessage);
            }
        } finally {
            try {
                if(stmt!=null) {stmt.close();}
            } catch (SQLException sqle) {
                log.log(Level.WARNING, "Failed to cleanup", sqle);
            }
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                log.log(Level.WARNING, "Failed to cleanup", sqle);
            }
        }
      
    }*/
    
    
    public void crossCheck(Long userId, Creditor[] creditors, java.util.Date date) throws SQLException {
        Date sqlDate = new Date(date.getTime());
        if (creditors == null) {
            crossCheckAll(userId, sqlDate);
        } else {
            for(Creditor creditor : creditors) {
                crossCheck(userId, creditor.getId(), sqlDate);
            }
        }
    }

    private void crossCheck(Long userId, BigInteger creditorId, Date date) throws SQLException {
        Connection conn = getConnection();
        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall("{ call DATA_VALIDATION.cross_check(?, ?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setLong(1, creditorId.longValue());
            stmt.setDate(2, date);
            stmt.setLong(3, userId);
            stmt.registerOutParameter(4, OracleTypes.INTEGER);
            stmt.registerOutParameter(5, OracleTypes.VARCHAR);
            stmt.execute();
            int errorCode = stmt.getInt(4);
            if (errorCode != 0) {
                String errorMessage = stmt.getString(5);
                throw new SQLException(errorMessage);
            }
        } finally {
            try {
                if(stmt!=null) {stmt.close();}
            } catch (SQLException sqle) {
                log.log(Level.WARNING, "Failed to cleanup", sqle);
            }
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                log.log(Level.WARNING, "Failed to cleanup", sqle);
            }
        }
    }

    private void crossCheckAll(Long userId, Date date) throws SQLException {
        Connection conn = getConnection();
        CallableStatement stmt = null;
        try {
            stmt = conn.prepareCall("{ call DATA_VALIDATION.cross_check_all(?, ?, ?, ?)}", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            stmt.setDate(1, date);
            stmt.setLong(2, userId);
            stmt.registerOutParameter(3, OracleTypes.INTEGER);
            stmt.registerOutParameter(4, OracleTypes.VARCHAR);
            stmt.execute();
            int errorCode = stmt.getInt(3);
            if (errorCode != 0) {
                String errorMessage = stmt.getString(4);
                throw new SQLException(errorMessage);
            }
        } catch (SQLException ex) {
            throw ex;
        } finally {
            try {
                if(stmt!=null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                log.log(Level.WARNING, "Failed to cleanup", sqle);
            }
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                log.log(Level.WARNING, "Failed to cleanup", sqle);
            }
        }
    }
}
