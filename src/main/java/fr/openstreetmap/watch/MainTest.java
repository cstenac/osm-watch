package fr.openstreetmap.watch;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Test;

import fr.openstreetmap.watch.model.db.Alert;

public class MainTest {
    @Test
    public void a() {
        DatabaseManager mm = new DatabaseManager();
        mm.init();
        
        Alert ad = new Alert();
//        ad.setUser("foo");
        
        mm.addAlert(ad);
    }
    
    public static void main(String[] args) {
        BasicConfigurator.configure();
        new MainTest().a();
    }
}
