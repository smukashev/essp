package kz.bsbnb.usci.manager.task;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class RunTask extends Task {
	private String classpath;
	private String main;

	public RunTask(String classpath, String main, String name, WrapProcess wrapProcess) {
		this.classpath = classpath;
		this.main = main;
		this.name = name;
		this.wrapProcess = wrapProcess;
	}

	public String getClasspath() {
		return classpath;
	}

	public void setClasspath(String classpath) {
		this.classpath = classpath;
	}

	public String getMain() {
		return main;
	}

	public void setMain(String main) {
		this.main = main;
	}

	@Override
	public String toString() {
		return "RunTask{" +
				"classpath='" + classpath + '\'' +
				", main='" + main + '\'' +
				", name='" + name + '\'' +
				", process=" + wrapProcess +
				'}';
	}
}
