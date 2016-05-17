package kz.bsbnb.usci.manager;

import kz.bsbnb.usci.manager.db.TaskDAO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class App {
	final static Logger logger = LoggerFactory.getLogger(App.class);
	public static void main(String[] args) {
		TimerTask task = new TaskDAO();

		Timer timer = new Timer();
		timer.schedule(task, 1000, 60000);
		logger.info("Manager started...");
	}
}
