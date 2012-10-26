package fr.openstreetmap.watch.parsers;

import java.io.EOFException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.persistence.Query;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import fr.openstreetmap.watch.ApplicationConfigurator;
import fr.openstreetmap.watch.DatabaseManager;
import fr.openstreetmap.watch.Engine;
import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.ApplicationState;
import fr.openstreetmap.watch.util.XMLUtils;

@Service
public class LastAugmentedDownloader {
    @Autowired private Engine engine;
    @Autowired private DatabaseManager dbManager;

    private String getHighestLink(String baseUrl) throws Exception {
        URL u = new URL(baseUrl);
        HttpURLConnection huc = (HttpURLConnection)u.openConnection();

        String xml = IOUtils.toString(huc.getInputStream());
        String[] lines = StringUtils.split(xml, "\n");
        /* Horrible :( */
        xml = StringUtils.join(Arrays.copyOfRange(lines, 1, lines.length), "\n");
        xml = xml.replace("]\">", "]\"/>");
        xml = xml.replace("<hr>", "<hr/>");
        xml = xml.replace("&nbsp;", " ");

        Document doc = XMLUtils.parse(xml);
        long max = -1;
        String maxHref = null;
        for (Node n : XMLUtils.xpath(doc, "/html/body/table/tr/td/a")) {
            Element e  = (Element)n;
            String href = e.getAttribute("href");
            String numHref = href.replace(".osc.gz", "").replace("/", "");
            if (numHref.length() > 0 && StringUtils.isNumeric(numHref)) {
                long longHref = Long.parseLong(numHref);
                if (longHref > max) {
                    max = longHref;
                    maxHref = href;
                }
            } else {
            }
        }
        return maxHref;
    }

    Set<String> alreadyDownloaded = new HashSet<String>();
    public  void run() throws Exception {
        long currentId = getCurrentId();

        dbManager.begin();
        List<ApplicationState> asl = dbManager.getEM().createQuery("SELECT x FROM ApplicationState x").getResultList();
        ApplicationState as  = null;
        if (asl.size() == 0) {
            logger.info("Initializing application state");
            as = new ApplicationState();
            as.setId(0);
            as.setLastDownloadedId(currentId - 1);
            dbManager.getEM().persist(as);
            dbManager.commit();
        } else {
            as = asl.get(0);
            dbManager.rollback();
        }

        for (long id = as.getLastDownloadedId() + 1; id <= currentId; id++) {

            String url = getURLToDownload(id);

            logger.info("Downloading " + url);
            URL u = new URL(url);

            String xml = null;
            for (int i = 0; i < 8; i++) {
                try {
                    HttpURLConnection huc = (HttpURLConnection)u.openConnection();
                    xml = IOUtils.toString(new GZIPInputStream(huc.getInputStream()));
                } catch (EOFException e) {
                    logger.info("  File not yet ready, waiting ...");
                    Thread.sleep(1000);
                }
            }
            if (xml == null) {
                logger.error("Failed to download " + url);
                throw new Exception("Failed to download " + url);
            }
            logger.info("Handling " + url);
            engine.handleAugmentedDiff(xml, 2);

            dbManager.begin();
            ApplicationState newAS = (ApplicationState) dbManager.getEM().createQuery("SELECT x FROM ApplicationState x").getResultList().get(0);
            newAS.setLastDownloadedId(id);
            dbManager.getEM().persist(newAS);
            dbManager.commit();
        }

    }
    final String base = "http://overpass-api.de/augmented_diffs/";

    public  long getCurrentId() throws Exception {
        String maxMillion = getHighestLink(base).replaceAll("/$", "");
        System.out.println(maxMillion);
        String maxThousand = getHighestLink(base + "/" + maxMillion).replaceAll("/$", "");
        String maxUnit = getHighestLink(base + "/" + maxMillion +"/" + maxThousand).replaceAll(".osc.gz", "");
        return Integer.parseInt(maxMillion) * 1000000 + Integer.parseInt(maxThousand) * 1000 + Integer.parseInt(maxUnit);
    }

    public  String getURLToDownload(long id) throws Exception {
        return base + "/" + String.format("%03d", id/1000000) + "/" + 
                String.format("%03d", (id % 1000000)/1000) + "/" + 
                String.format("%03d", id % 1000) + ".osc.gz";
    }

    public static void main(String[] args) throws Exception {
        new ApplicationConfigurator().setApplicationContext(null);

        DatabaseManager dm = new DatabaseManager();
        dm.init();
        Engine e = new Engine();
        e.setDatabaseManager(dm);
        e.init();

        LastAugmentedDownloader lad = new LastAugmentedDownloader();
        lad.engine = e;
        lad.dbManager = dm;
        lad.run();
    }

    private static Logger logger = Logger.getLogger("osm.watch.download");
}
