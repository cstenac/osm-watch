package fr.openstreetmap.watch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class ApplicationConfigurator implements ApplicationContextAware {
	@Override
	public void setApplicationContext(ApplicationContext arg0) throws BeansException {
		System.out.println("******* Starting up ********");
		//        Logger.getRootLogger().removeAllAppenders();
		//        BasicConfigurator.configure();
	}

	static Properties config;

	private static void parseConfig() {
		try {
			if (config == null) {
				config = new Properties();
				config.load(new FileInputStream(new File(System.getProperty("user.home") + "/osm-watch.properties")));
			}
		} catch (IOException e) {
			throw new Error(e);
		}
	}

	public static synchronized String getBaseURL() {
		parseConfig();
		return config.getProperty("baseurl");
	}

}
