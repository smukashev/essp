package com.bsbnb.creditregistry.portlets.report.dm;

import java.io.Serializable;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import static com.bsbnb.creditregistry.portlets.report.ReportApplication.log;
import com.bsbnb.creditregistry.portlets.report.ui.CustomDataSource;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;

/**
 *
 * @author Aidar.Myrzahanov
 */
public class DatabaseConnect implements Serializable {

    private User user;

    public DatabaseConnect(User user) {
        this.user = user;
    }

    public long getUserId() {
        return user.getUserId();
    }

    private boolean hasAccessToPersonalData() {
        try {
            return ExpandoValueLocalServiceUtil.getData(user.getCompanyId(), User.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, "hasAccessToPersonalData", user.getPrimaryKey(), false);
        } catch (PortalException pe) {
            log.log(Level.WARNING, "", pe);
        } catch (SystemException se) {
            log.log(Level.WARNING, "", se);
        }
        return false;
    }

    public boolean isUserNationalBankEmployee() {
        /*try {
            return ExpandoValueLocalServiceUtil.getData(user.getCompanyId(), User.class.getName(),
                    ExpandoTableConstants.DEFAULT_TABLE_NAME, "isNb", user.getPrimaryKey(), false);
        } catch (PortalException pe) {
            log.log(Level.WARNING, "", pe);
        } catch (SystemException se) {
            log.log(Level.WARNING, "", se);
        }
        return false;*/

        return true;
    }
    
    public List<User> getCoworkers() {
        List<User> users = new ArrayList<User>();
        users.add(user);
        try {
            if(!isUserNationalBankEmployee()) {
                List<Organization> organizations = user.getOrganizations();
                if(organizations.size()==1) {
                    Organization organization = organizations.get(0);
                    String organizationName = organization.getName().toLowerCase();
                    if(!(organizationName.contains("национальный банк")||organizationName.contains("бсб"))) {
                    users = UserLocalServiceUtil.getOrganizationUsers(organization.getOrganizationId());
                    }
                }
            }
        } catch(SystemException se) {
            log.log(Level.SEVERE, "", se);
        } catch(PortalException se) {
            log.log(Level.SEVERE, "", se);
        }
        return users;
    }

    private Connection getConnection() {
        try {
            Locale previousLocale = Locale.getDefault();
            log.log(Level.INFO, "Current locale: {0}-{1}", new Object[]{previousLocale.getLanguage(), previousLocale.getCountry()});
            Locale.setDefault(new Locale("ru", "RU"));
            Context context = new InitialContext();
            DataSource d = (DataSource) context.lookup("jdbc/RepPool");
            return d.getConnection();
        } catch (SQLException ex) {
            log.log(Level.SEVERE, null, ex);
        } catch (NamingException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void closeResources(ResultSet resultSet, Statement ocs, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException sqle) {
                log.log(Level.INFO, null, sqle);
            }
        }
        if (ocs != null) {
            try {
                ocs.close();
            } catch (SQLException sqle) {
                log.log(Level.INFO, null, sqle);
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException sqle) {
                log.log(Level.INFO, null, sqle);
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException sqle) {
                log.log(Level.WARNING, null, sqle);
            }
        }
    }

    public List<ValuePair> getValueListFromStoredProcedure(String procedureName) {
        List<ValuePair> result = new ArrayList<ValuePair>();
        result.add(new ValuePair("value", "displayName"));
        return result;
    }

    public CustomDataSource getDataSourceFromStoredProcedure(String procedureName, List<Object> parameterList) throws SQLException {
        return new CustomDataSource();
    }

    /*public ResultSet getResultSetFromStoredProcedure(String procedureName, List<Object> parameterList) throws SQLException {
        Connection connection = getConnection();
        CallableStatement statement = null;
        OracleCallableStatement ocs = null;
        try {
            StringBuilder procedureCallBuilder = new StringBuilder("{ call ");
            procedureCallBuilder.append(procedureName);
            procedureCallBuilder.append("(");
            int parametersCount = parameterList.size();
            for (int parameterIndex = 0; parameterIndex < parametersCount + 3; parameterIndex++) {
                procedureCallBuilder.append("?");
                procedureCallBuilder.append(",");
            }
            procedureCallBuilder.deleteCharAt(procedureCallBuilder.length() - 1);
            procedureCallBuilder.append(")}");
            String procedureCallString = procedureCallBuilder.toString();
            log.log(Level.INFO, "Procedure call string: {0}", procedureCallString);
            statement = connection.prepareCall(procedureCallBuilder.toString());
            ocs = statement.unwrap(OracleCallableStatement.class);
            statement.registerOutParameter(1, OracleTypes.CURSOR);
            log.log(Level.INFO, "User id : {0}", user.getUserId());
            statement.setLong(2, user.getUserId());
            boolean hasAccess = hasAccessToPersonalData();
            log.log(Level.INFO, "Has access: {0}", hasAccess);
            statement.setLong(3, hasAccess ? 1 : 0);
            for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
                Object parameter = parameterList.get(parameterIndex);
                log.log(Level.INFO, "Parameter value: {0}", parameter.toString());
                String parameterValueString = "";
                if (parameter instanceof Date) {
                    Date dateParameter = (Date) parameter;
                    statement.setTimestamp(parameterIndex + 4, new Timestamp(dateParameter.getTime()));
                } else {
                    parameterValueString = parameter.toString();
                    statement.setString(parameterIndex + 4, parameterValueString);
                }

                log.log(Level.INFO, "String parameter: {0}", parameterValueString);
            }
            statement.execute();
            return ocs.getCursor(1);
        } finally {
            closeResources(null, null, null, connection);
        }
    }*/

    public boolean approveReport(int reportID, int statusId) {
        /*Connection connection = null;
        CallableStatement statement = null;
        try {
            connection = getConnection();
            String sqlString = "update core.report r set r.status_id=?, r.username=? where r.id=?";
            statement = connection.prepareCall(sqlString);
            statement.setInt(1, statusId);
            statement.setString(2, user.getFullName());
            statement.setInt(3, reportID);
            int result = statement.executeUpdate();
            return result == 1;
        } catch (SQLException sqle) {
            log.log(Level.WARNING, null, sqle);
        } finally {
            closeResources(null, statement, null, connection);
        }
        return false;*/

        return true;
    }

    public ResultSet runQuery(String sqlQuery) {

        Connection conn = getConnection();
        CallableStatement statement = null;
        try {
            log.log(Level.INFO, "SQL string: {0}", sqlQuery);
            statement = conn.prepareCall(sqlQuery);

            statement.setQueryTimeout(300);
            return statement.executeQuery(sqlQuery);
        } catch (SQLException sqle) {
            log.log(Level.SEVERE, "Sql exception", sqle);
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
}
