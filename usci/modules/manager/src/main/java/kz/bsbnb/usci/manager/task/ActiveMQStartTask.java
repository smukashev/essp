package kz.bsbnb.usci.manager.task;

import java.util.Properties;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class ActiveMQStartTask extends CommandTask {
	public ActiveMQStartTask(Properties mainProperties) {
		this.directory = mainProperties.getProperty("activemq.dir");
		this.command = mainProperties.getProperty("activemq.command.start");
	}
}
