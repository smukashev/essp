package kz.bsbnb.usci.manager.task;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class StopTask extends Task {
	public StopTask(String name, WrapProcess wrapProcess) {
		this.name = name;
		this.wrapProcess = wrapProcess;
	}
}
