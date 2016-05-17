package kz.bsbnb.usci.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by askhat.anetbayev@gmail.com
 */
public class Utils {
	final static Logger logger = LoggerFactory.getLogger(CommandLauncher.class);

	public static Properties readProperties(String fileName) {
		Properties prop = new Properties();
		InputStream input = null;

		try {

			input = CommandLauncher.class.getClassLoader().getResourceAsStream(fileName);
			if (input == null) {
				logger.info("Sorry, unable to find " + fileName);
				return null;
			}

			//load a properties file from class path, inside static method
			prop.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return prop;
	}
}
