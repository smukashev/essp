package kz.bsbnb.usci.manager;

import kz.bsbnb.usci.manager.db.TaskDAO;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class App {
	public static void main(String[] args) {
		TimerTask task = new TaskDAO();

		Timer timer = new Timer();
		timer.schedule(task, 1000, 60000);
	}
}
