package com.bsbnb.creditregistry.portlets.approval.data;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import kz.bsbnb.usci.eav.util.DataUtils;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class DatabaseConnect {

    private User user;
    private final Logger logger = Logger.getLogger(DatabaseConnect.class);

    public DatabaseConnect() {

    }
    public DatabaseConnect(User user) {
        this.user = user;
    }

    public long getUserId() {
        return user.getUserId();
    }

    private boolean hasAccessToPersonalData() {
        try {
            return ExpandoValueLocalServiceUtil.getData(user.getCompanyId(), User.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, "hasAccessToPersonalData", user.getUserId(), false);
        } catch (PortalException pe) {
            logger.warn(null, pe);
        } catch (SystemException se) {
            logger.warn(null, se);
        }
        return false;
    }

    public boolean isUserNationalBankEmployee() {
        try {
            return ExpandoValueLocalServiceUtil.getData(user.getCompanyId(), User.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, "isNb", user.getPrimaryKey(), false);
        } catch (PortalException pe) {
            logger.warn(null, pe);
        } catch (SystemException se) {
            logger.warn(null, se);
        }
        return false;
    }

    public List<User> getCoworkers() {
        List<User> users = new ArrayList<User>();
        users.add(user);
        try {
            if (!isUserNationalBankEmployee()) {
                List<Organization> organizations = user.getOrganizations();
                if (organizations.size() == 1) {
                    Organization organization = organizations.get(0);
                    String organizationName = organization.getName().toLowerCase();
                    if (!(organizationName.contains("национальный банк") || organizationName.contains("бсб"))) {
                        users = UserLocalServiceUtil.getOrganizationUsers(organization.getOrganizationId());
                    }
                }
            }
        } catch (SystemException se) {
            logger.error(null, se);
        } catch (PortalException se) {
            logger.error(null, se);
        }
        return users;
    }

    public Connection getConnection() {
        try {
            Locale previousLocale = Locale.getDefault();
            // log.log(Level.INFO, "Current locale: {0}-{1}",
            //                          new Object[]{previousLocale.getLanguage(), previousLocale.getCountry()});
            // Locale.setDefault(new Locale("ru", "RU"));
            Context context = new InitialContext();
            DataSource d = (DataSource) context.lookup("jdbc/RepPool");
            return d.getConnection();
        } catch (SQLException ex) {
            logger.error(null, ex);
        } catch (NamingException ex) {
            logger.error(null, ex);
        }
        return null;
    }

    private void closeResources(ResultSet resultSet, Statement ocs, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException sqle) {
                logger.info(null, sqle);
            }
        }
        if (ocs != null) {
            try {
                ocs.close();
            } catch (SQLException sqle) {
                logger.info(null, sqle);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException sqle) {
                logger.info(null, sqle);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException sqle) {
                logger.warn(null, sqle);
            }
        }
    }


    public ResultSet getResultSetFromStoredProcedure(String procedureName, List<Object> parameterList) throws SQLException {
        Connection connection = getConnection();
        CallableStatement statement = null;
        OracleCallableStatement ocs = null;
        try {
            StringBuilder procedureCallBuilder = new StringBuilder("{ call ");
            procedureCallBuilder.append(procedureName);
            procedureCallBuilder.append("(");
            int parametersCount = parameterList.size();
            for (int parameterIndex = 0; parameterIndex < parametersCount+1; parameterIndex++) {
                procedureCallBuilder.append("?");
                procedureCallBuilder.append(",");
            }
            procedureCallBuilder.deleteCharAt(procedureCallBuilder.length() - 1);
            procedureCallBuilder.append(")}");

            statement = connection.prepareCall(procedureCallBuilder.toString());
            ocs = statement.unwrap(OracleCallableStatement.class);
            statement.registerOutParameter(1, OracleTypes.CURSOR);
            for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
                Object parameter = parameterList.get(parameterIndex);
                String parameterValueString = "";
                if (parameter instanceof Date) {
                    Date dateParameter = (Date) parameter;
                    statement.setDate(parameterIndex + 2, new java.sql.Date(dateParameter.getTime()));//setTimestamp(parameterIndex + 4, new Timestamp(dateParameter.getTime()));
                } else {
                    parameterValueString = parameter.toString();
                    statement.setString(parameterIndex + 2, parameterValueString);
                }

            }
            statement.execute();
            return ocs.getCursor(1);
        } finally {
            closeResources(null, null, null, connection);


        }
    }

    public ResultSet runQuery(String sqlQuery) {

        Connection conn = getConnection();
        CallableStatement statement = null;
        try {
            logger.info("SQL string: "+ sqlQuery);
            statement = conn.prepareCall(sqlQuery);

            statement.setQueryTimeout(300);
            return statement.executeQuery(sqlQuery);
        } catch (SQLException sqle) {
            logger.error("Sql exception", sqle);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException sqle) {
                }
            }
        }

        return null;
    }

    public Long getLastCrossCheckStatus(long creditorId, Date reportDate) {

        long statusId = 0;
        Connection conn = null;
        PreparedStatement stmt = null;

        String query = "select * from cross_check   where creditor_id=?  and report_date = ? order by date_begin desc";
        try {
            conn = getConnection();
            stmt = conn.prepareStatement(query);
            stmt.setLong(1, creditorId);
            stmt.setDate(2, DataUtils.convert(reportDate));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                statusId = rs.getLong("status_id");
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            logger.error(null, se);
        } finally {
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se2) {
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }

        return statusId;
    }

    public static void main(String args[]){
        DatabaseConnect db = new DatabaseConnect();
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");

        try {
            long statusId = db.getLastCrossCheckStatus((long) 4375, format.parse("01.01.2015"));
            System.out.println(statusId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
