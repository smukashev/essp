package kz.bsbnb.usci.manager.task;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public abstract class Task {
	protected WrapProcess wrapProcess;
	protected String name;

	protected String directory;
	protected String command;

	public WrapProcess getWrapProcess() {
		return wrapProcess;
	}

	public void setWrapProcess(WrapProcess wrapProcess) {
		this.wrapProcess = wrapProcess;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}
}
