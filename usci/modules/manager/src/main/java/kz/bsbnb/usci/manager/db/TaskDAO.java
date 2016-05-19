package kz.bsbnb.usci.manager.db;

import kz.bsbnb.usci.manager.CommandLauncher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TimerTask;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class TaskDAO extends TimerTask {
	final Logger logger = LoggerFactory.getLogger(TaskDAO.class);

	Connection dbConnection = null;
	PreparedStatement selectStatement = null;
	PreparedStatement updateStatement = null;
	ResultSet rs = null;
	String selectSQL = "SELECT ID, CODE, VALUE FROM EAV_GLOBAL WHERE TYPE = 'MANAGER_TASKS' AND VALUE != ?";
	String updateSQL = "UPDATE EAV_GLOBAL SET VALUE='0' WHERE ID = ?";

	@Override
	public void run() {
		try {
			dbConnection = DBConnectionClass.getCurrentConnection();
			selectStatement = dbConnection.prepareStatement(selectSQL);
			selectStatement.setString(1, "0");

			// execute select SQL stetement
			rs = selectStatement.executeQuery();

			while (rs.next()) {

				Integer id = rs.getInt("ID");
				String taskName = rs.getString("CODE");
				String taskStatus = rs.getString("VALUE");
				logger.info("Reading " + taskName + " status: " + taskStatus + "...");

				if (!"0".equals(taskStatus)) {
					updateTask(dbConnection, id);
					CommandLauncher.run(taskName);
				}

			}
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (selectStatement != null) {
					selectStatement.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateTask(Connection dbConnection, Integer id) throws SQLException {
		logger.info("Update task with id: " + id);
		updateStatement = dbConnection.prepareStatement(updateSQL);
		updateStatement.setInt(1, id);
		updateStatement.executeUpdate();

		if (updateStatement != null) {
			updateStatement.close();
		}

	}

	@Override
	protected void finalize() throws Throwable {
		if (dbConnection != null) {
			dbConnection.close();
		}
	}

}
