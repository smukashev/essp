package kz.bsbnb.usci.manager.task;

import java.util.Properties;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class ActiveMQStopTask extends CommandTask {
	public ActiveMQStopTask(Properties mainProperties) {
		this.directory = mainProperties.getProperty("activemq.dir");
		this.command = mainProperties.getProperty("activemq.command.stop");
	}
}
