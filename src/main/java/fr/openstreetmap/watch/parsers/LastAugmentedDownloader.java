package fr.openstreetmap.watch.parsers;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import javax.persistence.Query;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
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
import fr.openstreetmap.watch.State;
import fr.openstreetmap.watch.model.db.Alert;
import fr.openstreetmap.watch.model.db.ApplicationState;
import fr.openstreetmap.watch.util.XMLUtils;

@Service
public class LastAugmentedDownloader {
    @Autowired private Engine engine;
    @Autowired private DatabaseManager dbManager;

    HttpClient client = new HttpClient();

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
        ApplicationState as  = null;

        dbManager.begin();
        try {
            List<ApplicationState> asl = dbManager.getEM().createQuery("SELECT x FROM ApplicationState x").getResultList();
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
        } catch (Throwable t) {
            dbManager.rollback();
        }

        State.lastDiscoveredId = currentId;
        State.currentDownloadQueue = currentId - as.getLastDownloadedId() + 1;

        for (long id = as.getLastDownloadedId() + 1; id <= currentId; id++) {
            State.currentDownloadQueue--;

            String url = getURLToDownload(id);

            logger.info("Downloading " + url);
            long before = System.currentTimeMillis();
            State.downloading = url;
            URL u = new URL(url);

            String xml = null;

            try {
                GetMethod gm = new GetMethod(u.toString());
                client.executeMethod(gm);
                int code = gm.getStatusCode();
                logger.info("Status code is " + code);
                byte[] data = IOUtils.toByteArray(gm.getResponseBodyAsStream());
                logger.info("Got data size " + data.length);
                State.totalDownloadSize += data.length;
                xml = IOUtils.toString(new GZIPInputStream(new ByteArrayInputStream(data)));
            } catch (EOFException e) {
                logger.info("  File not yet ready, waiting ...");
                Thread.sleep(1000);
            }

            if (xml == null) {
                logger.error("Failed to download " + url);
                throw new Exception("Failed to download " + url);
            }
            State.downloading = null;
            State.downloadedDiffs++;

            State.totalDownloadTime += (System.currentTimeMillis() - before);

            logger.info("Handling " + url);
            State.processing = true;
            before = System.currentTimeMillis();
            try {
                engine.handleAugmentedDiff(xml, 2);
            } finally {
                State.processing = false;
                State.totalProcessingTime += (System.currentTimeMillis() - before);
            }

            dbManager.begin();
            try {
                ApplicationState newAS = (ApplicationState) dbManager.getEM().createQuery("SELECT x FROM ApplicationState x").getResultList().get(0);
                newAS.setLastDownloadedId(id);
                dbManager.getEM().persist(newAS);
                dbManager.commit();
            } catch (Exception t) {
                dbManager.rollback();
                throw t;
            }
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
        e.pc();

        LastAugmentedDownloader lad = new LastAugmentedDownloader();
        lad.engine = e;
        lad.dbManager = dm;
        lad.run();
    }

    private static Logger logger = Logger.getLogger("osm.watch.download");
}
