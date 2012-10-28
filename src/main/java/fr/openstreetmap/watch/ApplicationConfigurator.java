package fr.openstreetmap.watch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
		if (config == null) {
			try {
				config = new Properties();
				config.load(new FileInputStream(new File(System.getProperty("user.home") + "/osm-watch.properties")));
			} catch (IOException e) {
				throw new Error(e);
			}
		}

	}

	public static synchronized Map<String, String> getPersistenceProperties() {
		parseConfig();
		Map<String, String> p = new HashMap<String, String>();
		if (!config.containsKey("dialect")) throw new Error("Missing 'dialect' key in config");
		if (!config.containsKey("driver")) throw new Error("Missing 'driver' key in config");
		if (!config.containsKey("jdbcurl")) throw new Error("Missing 'jdbcurl' key in config");
		p.put("hibernate.dialect", config.getProperty("dialect"));
		p.put("hibernate.connection.driver_class", config.getProperty("driver"));
		p.put("hibernate.connection.url", config.getProperty("jdbcurl"));
		return p;
	}

	public static synchronized String getMandatoryProperty(String key) {
		parseConfig();
		if (!config.containsKey("key")) throw new Error("Missing '" + key + "' key in config");
		return config.getProperty(key);
	}

	public static synchronized String getBaseURL() {
		parseConfig();
		return config.getProperty("baseurl");
	}

}
