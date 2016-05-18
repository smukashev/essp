package kz.bsbnb.usci.manager.task;

import java.util.Properties;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class GitTask extends CommandTask {
	public GitTask(Properties mainProperties) {
		this.directory = mainProperties.getProperty("git.dir");
		this.command = mainProperties.getProperty("git.command");
	}
}
