package kz.bsbnb.usci.portlet.report.dm;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.Organization;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoTableConstants;
import com.liferay.portlet.expando.service.ExpandoValueLocalServiceUtil;
import kz.bsbnb.usci.portlet.report.ui.CustomDataSource;
import oracle.jdbc.OracleCallableStatement;
import oracle.jdbc.OracleTypes;
import org.apache.log4j.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.*;
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

    public List<ValuePair> getValueListFromStoredProcedure(String procedureName, String showcase_id) {
        Connection connection = getConnection();
        CallableStatement statement = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;
        try {
            String procedureCallString="";
            if(procedureName.equals("INPUT_PARAMETER_SC_FIELDS"))
                procedureCallString = "{ call " + procedureName + "(?,?,?)}";
            else
                procedureCallString = "{ call " + procedureName + "(?,?)}";
            statement = connection.prepareCall(procedureCallString);
            ocs = statement.unwrap(OracleCallableStatement.class);
            statement.registerOutParameter(1, OracleTypes.CURSOR);
            logger.info("Procedure name: "+ procedureName);
            logger.info("User id for procedure : "+ user.getUserId());
            statement.setLong(2, user.getUserId());
            if(procedureName.equals("INPUT_PARAMETER_SC_FIELDS"))
                statement.setString(3, showcase_id);
            statement.execute();
            cursor = ocs.getCursor(1);
            List<ValuePair> result = new ArrayList<ValuePair>();
            int rowNumber = 0;
            while (cursor.next()) {
                ++rowNumber;
                result.add(new ValuePair(cursor.getString(1), cursor.getString(2)));
            }
            return result;
        } catch (SQLException sqle) {
            logger.error("SQL Exception", sqle);
        } finally {
            closeResources(cursor, ocs, statement, connection);
        }
        return null;
    }

    public CustomDataSource getDataSourceFromStoredProcedure(String procedureName, List<Object> parameterList)
            throws SQLException {
        Connection connection = getConnection();
        CallableStatement statement = null;
        OracleCallableStatement ocs = null;
        ResultSet cursor = null;
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
            logger.info("Procedure call string: "+ procedureCallString);
            statement = connection.prepareCall(procedureCallBuilder.toString());
            ocs = statement.unwrap(OracleCallableStatement.class);
            statement.registerOutParameter(1, OracleTypes.CURSOR);
            logger.info("User id : "+ user.getUserId());

            statement.setLong(2, user.getUserId());
            boolean hasAccess = hasAccessToPersonalData();
            logger.info("Has access: "+ hasAccess);
            statement.setLong(3, hasAccess ? 1 : 0);
            for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
                Object parameter = parameterList.get(parameterIndex);
                logger.info("Parameter value: "+ parameter.toString());
                String parameterValueString = "";
                if (parameter instanceof Date) {
                    Date dateParameter = (Date) parameter;
                    statement.setDate(parameterIndex + 4, new java.sql.Date(dateParameter.getTime()));
                } else {
                    parameterValueString = parameter.toString();
                    statement.setString(parameterIndex + 4, parameterValueString);
                }

                logger.info("String parameter: "+ parameterValueString);
            }
            statement.execute();
            cursor = ocs.getCursor(1);
            return new CustomDataSource(cursor);
        } finally {
            closeResources(cursor, ocs, statement, connection);
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
            for (int parameterIndex = 0; parameterIndex < parametersCount + 3; parameterIndex++) {
                procedureCallBuilder.append("?");
                procedureCallBuilder.append(",");
            }
            procedureCallBuilder.deleteCharAt(procedureCallBuilder.length() - 1);
            procedureCallBuilder.append(")}");
            String procedureCallString = procedureCallBuilder.toString();
            logger.info("Procedure call string: "+ procedureCallString);
            statement = connection.prepareCall(procedureCallBuilder.toString());
            ocs = statement.unwrap(OracleCallableStatement.class);
            statement.registerOutParameter(1, OracleTypes.CURSOR);
            logger.info("User id : "+ user.getUserId());
            statement.setLong(2, user.getUserId());
            boolean hasAccess = hasAccessToPersonalData();
            logger.info("Has access: "+ hasAccess);
            statement.setLong(3, hasAccess ? 1 : 0);
            for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
                Object parameter = parameterList.get(parameterIndex);
                logger.info("Parameter value: "+ parameter.toString());
                String parameterValueString = "";
                if (parameter instanceof Date) {
                    Date dateParameter = (Date) parameter;
                    statement.setDate(parameterIndex + 4, new java.sql.Date(dateParameter.getTime()));//setTimestamp(parameterIndex + 4, new Timestamp(dateParameter.getTime()));
                } else {
                    parameterValueString = parameter.toString();
                    statement.setString(parameterIndex + 4, parameterValueString);
                }

                logger.info("String parameter: "+ parameterValueString);
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
}
