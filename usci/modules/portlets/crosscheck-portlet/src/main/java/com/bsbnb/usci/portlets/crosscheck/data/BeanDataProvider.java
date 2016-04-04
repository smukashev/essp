package com.bsbnb.usci.portlets.crosscheck.data;

import com.bsbnb.usci.portlets.crosscheck.PortletEnvironmentFacade;
import com.bsbnb.usci.portlets.crosscheck.dm.*;
import com.bsbnb.usci.portlets.crosscheck.helper.DbHelper;
import com.bsbnb.usci.portlets.crosscheck.helper.ModelHelper;
import kz.bsbnb.usci.eav.StaticRouter;
import kz.bsbnb.usci.eav.util.Errors;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;


public class BeanDataProvider implements DataProvider {
    private PortletEnvironmentFacade facade;
    private final Logger logger = Logger.getLogger(BeanDataProvider.class);

    private Connection getConnection() {
        Context context;
        Connection conn = null;
        try {
            context = new InitialContext();
            DataSource d = (DataSource) context.lookup("jdbc/RepPool");
            conn = d.getConnection();
        } catch (SQLException sqle) {
            logger.error(null,sqle);
        } catch (NamingException ne) {
            logger.error(null,ne);
        }

        return conn;
    }

    public BeanDataProvider(PortletEnvironmentFacade facade) throws DataException {
        this.facade = facade;
    }

    @Override
    public List<Creditor> getCreditorsList(String CreditorId) {
        List<Creditor> creditorList = new ArrayList<Creditor>();

        Connection conn = getConnection();
        Statement stmt = null;
        String query = "SELECT t0.REF_CREDITOR_ID AS ID, t0.OPEN_DATE AS CHANGE_DATE, t0.CODE, t0.NAME, t0.SHORT_NAME, " +
                "t0.CLOSE_DATE AS SHUTDOWN_DATE, 0 AS MAIN_OFFICE_ID, t0.SUBJECT_TYPE_ID " +
                "FROM " + StaticRouter.getShowcaseSchemaName() + ".R_REF_CREDITOR t0, " + StaticRouter.getCoreSchemaName() +
                ".EAV_A_CREDITOR_USER t2, " + StaticRouter.getCoreSchemaName() + ".EAV_A_USER t1 " +
                "WHERE ((t1.USER_ID = " + BigInteger.valueOf(facade.getUserID()) +") " +
                "AND ((t2.CREDITOR_ID = t0.REF_CREDITOR_ID) AND (t1.USER_ID = t2.USER_ID))) " +
                "AND (t0.REF_CREDITOR_ID = "+CreditorId+" OR "+CreditorId+" is NULL )"+
                "ORDER BY t0.NAME ASC";

        logger.info("getCreditorsList: " + query);

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Creditor c = ModelHelper.convertToCreditor(rs);
                creditorList.add(c);
            }

        } catch (SQLException e ) {
            logger.error(null,e);
        } finally {
            try {
                if(stmt!=null) {stmt.close();}
            } catch (SQLException sqle) {
                logger.error("Failed to cleanup", sqle);
            }
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.error("Failed to cleanup", sqle);
            }
        }

        return creditorList;
    }

    @Override
    public List<CrossCheck> getCrossChecks(Creditor[] creditors, Date date) {
        List<CrossCheck> crosscheckList = new ArrayList<CrossCheck>();

        String dateString = new SimpleDateFormat("dd-MM-yyyy").format(date);

        Connection conn = getConnection();
        Statement stmt = null;
        String query = "SELECT ID, DATE_BEGIN, DATE_END, REPORT_DATE, STATUS_ID, decode(status_id, 43, 'Ошибка', 44, 'Успешно')  AS STATUS_NAME, USER_NAME, CREDITOR_ID " +
                "FROM reporter.CROSS_CHECK " +
                "WHERE ((CREDITOR_ID IN (";

        for (int i = 0; i < creditors.length; i++) {
            if (i == (creditors.length - 1)) {
                query += creditors[i].getId();
            } else {
                query += creditors[i].getId() + ", ";
            }
        }

        query += ")) AND (REPORT_DATE = TO_DATE('" + dateString + "', 'dd-MM-yyyy'))) ORDER BY DATE_BEGIN DESC, ID ASC";

        logger.error("getCrossChecks: " + query);

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                Creditor c = DbHelper.getCreditor(conn, rs.getBigDecimal("CREDITOR_ID").toBigInteger());
                CrossCheck cc = ModelHelper.convertToCrossCheck(rs, c);
                crosscheckList.add(cc);
            }
        } catch (SQLException e) {
            logger.error(null,e);
        } finally {
            try {
                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
        }

        return crosscheckList;
    }

    @Override
    public List<CrossCheckMessageDisplayWrapper> getMessages(CrossCheck crossCheck) {
        List<CrossCheckMessage> cList = new ArrayList<CrossCheckMessage>();

        Connection conn = getConnection();
        Statement stmt = null;
        String query =  "SELECT ID, DESCRIPTION, DIFF, INNER_VALUE, IS_ERROR, OUTER_VALUE, CROSS_CHECK_ID, MESSAGE_ID " +
                        "FROM reporter.CROSS_CHECK_MESSAGE " +
                        "WHERE (CROSS_CHECK_ID = " + crossCheck.getId() + ") " +
                        "ORDER BY ID ASC";

        logger.info("getMessages: " + query);

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);

            while (rs.next()) {
                CrossCheck cc = DbHelper.getCrossCheck(conn, rs.getBigDecimal("CROSS_CHECK_ID").toBigInteger());
                BigDecimal messageId = rs.getBigDecimal("MESSAGE_ID");
                Message m = null;
                if(messageId!=null) {
                    m = DbHelper.getMessage(conn, messageId.toBigInteger()); // TODO fix
                }
                else
                {
                    m = new Message();
                    m.setId(rs.getBigDecimal("DESCRIPTION"));
                    m.setCode(rs.getString("DESCRIPTION"));
                    m.setNameKz(rs.getString("DESCRIPTION"));
                    m.setNameRu(rs.getString("DESCRIPTION"));
                    m.setNote(rs.getString("DESCRIPTION"));

                }
                CrossCheckMessage cm = ModelHelper.convertToCrossCheckMessage(rs, cc, m);
                cList.add(cm);
            }

            ArrayList<CrossCheckMessageDisplayWrapper> result
                    = new ArrayList<CrossCheckMessageDisplayWrapper>(cList.size());

            for (CrossCheckMessage crossCheckMessage : cList)
                result.add(new CrossCheckMessageDisplayWrapper(crossCheckMessage));

            return result;
        } catch (SQLException e ) {
            logger.error(null,e);
        } finally {
            try {
                if(stmt!=null) {stmt.close();}
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
        }

        return null;
    }

    private Date getFirstNotApprovedDate(BigInteger creditorId) {
        Date date = null;

        Connection conn = getConnection();
        Statement stmt = null;
        String query = "SELECT MAX(OPEN_DATE) AS MAX_CHANGE_DATE FROM " + StaticRouter.getShowcaseSchemaName() +
                ".R_REF_CREDITOR";

        logger.info("getFirstNotApprovedDate: " + query);

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            return rs.getDate("MAX_CHANGE_DATE");
        } catch (SQLException e ) {
            logger.error(null,e);
        } finally {
            try {
                if(stmt!=null) {stmt.close();}
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
        }

        return null;
    }

    private Date getLastApprovedDate(BigInteger creditorId) {
        Date date = null;

        Connection conn = getConnection();
        Statement stmt = null;
        String query = "SELECT MAX(OPEN_DATE) AS MAX_CHANGE_DATE FROM " + StaticRouter.getShowcaseSchemaName() +
                ".R_REF_CREDITOR";

        logger.info("getLastApprovedDate: " + query);

        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            rs.next();
            return rs.getDate("MAX_CHANGE_DATE");
        } catch (SQLException e ) {
            logger.error(null,e);
        } finally {
            try {
                if(stmt!=null) {stmt.close();}
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
        }

        return null;
    }

    @Override
    public Date getCreditorsReportDate(Creditor creditor) {
        Connection conn = getConnection();

        try {
            BigInteger creditorId = creditor.getId();
            Date firstNotApprovedDate = getFirstNotApprovedDate(creditorId);
            if(facade.getRepDate()!=null)
                return facade.getRepDate();

            if (firstNotApprovedDate != null)
                return firstNotApprovedDate;

            Date lastApprovedDate = getLastApprovedDate(creditorId);

            if (lastApprovedDate != null) {
                Creditor targetCreditor = DbHelper.getCreditor(conn, creditorId);

                if (targetCreditor == null)
                    throw new RuntimeException(Errors.getMessage(Errors.E236));

                if (targetCreditor.getSubjectType() == null)
                    throw new RuntimeException("Subject type of the creditor with ID " + creditorId + " is null");

                final Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(lastApprovedDate.getTime());
                calendar.add(Calendar.MONTH, targetCreditor.getSubjectType().getReportPeriodDurationMonths());
                return calendar.getTime();
            }

            // TODO Пока что начальная дата как константа, нужно сделать настройку
            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(2014, Calendar.JANUARY, 13);

            return calendar.getTime();

        } finally {
            try {
                if(conn!=null) {
                    conn.close();
                }
            } catch (SQLException sqle) {
                logger.warn("Failed to cleanup", sqle);
            }
        }
    }
}
