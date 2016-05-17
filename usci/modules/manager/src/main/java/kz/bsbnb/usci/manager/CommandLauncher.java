package kz.bsbnb.usci.manager;

/**
 * Created by askhat.anetbayev@gmail.com
 */

import kz.bsbnb.usci.manager.task.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class CommandLauncher {
	final static Logger logger = LoggerFactory.getLogger(CommandLauncher.class);

	static Properties mainProperties = null;
	static Map<String, Task> taskMap = null;

	static {
		// TODO: Move all configuration to DB.
		// TODO: If core, sync, receiver to run then launch in this order...
		// TODO: glassfish Task
		mainProperties = Utils.readProperties("main.properties");
/*
		taskMap = new HashMap<String, TaskEntity>();
		taskMap.put("RUN_CORE", new TaskEntity("run", "core.classpath", "core.main", "RUN_CORE", coreProcess));
		taskMap.put("STOP_CORE", new TaskEntity("stop", "core.classpath", "core.main", "STOP_CORE", coreProcess));
*/
		taskMap = new HashMap<String, Task>();

		WrapProcess coreWrapProcess = new WrapProcess();
		WrapProcess syncWrapProcess = new WrapProcess();
		WrapProcess receiverWrapProcess = new WrapProcess();
		WrapProcess showcaseWrapProcess = new WrapProcess();

		Task coreTask = new RunTask("core.classpath", "core.main", "RUN_CORE", coreWrapProcess);
		Task syncTask = new RunTask("sync.classpath", "sync.main", "RUN_SYNC", syncWrapProcess);
		Task receiverTask = new RunTask("receiver.classpath", "receiver.main", "RUN_RECEIVER", receiverWrapProcess);
		Task showcaseTask = new RunTask("showcase.classpath", "showcase.main", "RUN_SHOWCASE", showcaseWrapProcess);

		taskMap.put("RUN_CORE", coreTask);
		taskMap.put("STOP_CORE", new StopTask("STOP_CORE", coreTask.getWrapProcess()));
		taskMap.put("RUN_SYNC", syncTask);
		taskMap.put("STOP_SYNC", new StopTask("STOP_SYNC", syncTask.getWrapProcess()));
		taskMap.put("RUN_RECEIVER", receiverTask);
		taskMap.put("STOP_RECEIVER", new StopTask("STOP_RECEIVER", receiverTask.getWrapProcess()));
		taskMap.put("RUN_SHOWCASE", receiverTask);
		taskMap.put("STOP_SHOWCASE", new StopTask("STOP_SHOWCASE", showcaseTask.getWrapProcess()));
		taskMap.put("RUN_MAVEN", new MavenTask(mainProperties));
		taskMap.put("RUN_GIT", new GitTask(mainProperties));
	}

	public static void main(String[] args) throws Exception {
/*
		Properties mainProperties = Utils.readProperties("main.properties");
		final String currentDirectory = System.getProperty("user.dir");
		logger.info("currentDirectory: " + currentDirectory);
		runCore();
		Thread.sleep(10000);
		stopCore();
*/
	}

	private static void stopService(StopTask stopTask) {
		logger.info("stopService");
		if (stopTask.getWrapProcess() != null)
			stopTask.getWrapProcess().getProcess().destroy();
		else
			logger.info("No process to stop!");
	}

	private static void runService(RunTask runTask) {
		if (mainProperties == null) {
			logger.info("Not initialized mainProperties!");
			return;
		}

		logger.info("runService");
		String javaHome = System.getProperty("java.home");
		final String javaLauncher = new File(javaHome, "bin/java").getPath();
		final String classPath = mainProperties.getProperty(runTask.getClasspath());
		final String securityPolicy = mainProperties.getProperty("java.security.policy");
		final String fileEncoding = mainProperties.getProperty("file.encoding");
		final String coreMain = mainProperties.getProperty(runTask.getMain());
		final ArrayList<String> argumentList = new ArrayList<String>() {
			{
				add(javaLauncher);
				add("-cp");
				add(classPath);
				add(securityPolicy);
				add(fileEncoding);
				add(coreMain);
			}
		};

		try {
			Process process = new ProcessBuilder(argumentList).start();
//			printStdoutStderr(process);
			runTask.getWrapProcess().setProcess(process);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void runCommand(Task task) {
		logger.info("runCommand: " + task.getCommand());

		try {
			Process process = Runtime.getRuntime().exec(task.getCommand(), null, new File(task.getDirectory()));
			WrapProcess wrapWrapProcess = new WrapProcess();
			wrapWrapProcess.setProcess(process);

//			printStdoutStderr(process);

			task.setWrapProcess(wrapWrapProcess);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printStdoutStderr(Process process) throws IOException {
		// TODO: infinite cycle...
		// TODO: Write to DB
		BufferedReader stdInput = new BufferedReader(new
				InputStreamReader(process.getInputStream()));

		BufferedReader stdError = new BufferedReader(new
				InputStreamReader(process.getErrorStream()));

		// read the output from the command
		logger.info("Here is the standard output of the command:\n");
		String s = null;
		while ((s = stdInput.readLine()) != null) {
			logger.info(s);
		}

		// read any errors from the attempted command
		logger.info("Here is the standard error of the command (if any):\n");
		while ((s = stdError.readLine()) != null) {
			logger.info(s);
		}
	}

	public static void run(String taskName) {
		Task task = taskMap.get(taskName);
		if (task instanceof RunTask)
			runService((RunTask) task);
		if (task instanceof StopTask)
			stopService((StopTask) task);
		if (task instanceof MavenTask || task instanceof GitTask)
			runCommand(task);
	}
}