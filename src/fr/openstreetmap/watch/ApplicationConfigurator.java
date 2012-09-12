package fr.openstreetmap.watch;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class ApplicationConfigurator implements ApplicationContextAware {
    @Override
    public void setApplicationContext(ApplicationContext arg0) throws BeansException {
        System.out.println("******* Starting up ********");
        Logger.getRootLogger().removeAllAppenders();
        BasicConfigurator.configure();
    }

}
