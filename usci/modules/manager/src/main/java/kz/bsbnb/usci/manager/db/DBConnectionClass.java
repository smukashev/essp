package kz.bsbnb.usci.manager.db;

import kz.bsbnb.usci.manager.Utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class DBConnectionClass {
	public static Connection connection = null;

	public static Connection getCurrentConnection() throws SQLException, ClassNotFoundException {
		if (connection != null) {
			return connection;
		} else {
			Properties oracleProperties = Utils.readProperties("oracle.properties");
			Class.forName(oracleProperties.getProperty("jdbcPersistance.driver"));
			connection = DriverManager.getConnection(
					oracleProperties.getProperty("jdbcPersistance.url"),
					oracleProperties.getProperty("jdbcPersistance.user"),
					oracleProperties.getProperty("jdbcPersistance.password"));

			return connection;
		}
	}
}