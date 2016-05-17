package kz.bsbnb.usci.manager.task;

import java.util.Properties;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class MavenTask extends Task {
	public MavenTask(Properties mainProperties) {
		this.directory = mainProperties.getProperty("maven.dir");
		this.command = mainProperties.getProperty("maven.command");
	}
}
