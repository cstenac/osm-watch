package fr.openstreetmap.watch;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

@Service
public class LastAugmentedDownloader {
    private Engine engine;
    @Autowired
    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    
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
        String url = getURLToDownload();
        
        if (alreadyDownloaded.contains(url)) {
            return;
        }
        alreadyDownloaded.add(url);

        logger.info("Downloading " + url);
        URL u = new URL(url);
        HttpURLConnection huc = (HttpURLConnection)u.openConnection();
        String xml = IOUtils.toString(new GZIPInputStream(huc.getInputStream()));

        logger.info("Handling " + url);

        engine.handleAugmentedDiff(xml);
    }
    
    public  String getURLToDownload() throws Exception {
        final String base = "http://overpass-api.de/augmented_diffs/";
        String maxMillion = getHighestLink(base);
        String maxThousand = getHighestLink(base + "/" + maxMillion);
        String maxUnit = getHighestLink(base + "/" + maxMillion +"/" + maxThousand);
        return  base + "/" + maxMillion +"/" + maxThousand + "/" + maxUnit;
    }
    
    public static void main(String[] args) throws Exception {
        new ApplicationConfigurator().setApplicationContext(null);
        
        DatabaseManager dm = new DatabaseManager();
        dm.init();
        Engine e = new Engine();
        e.setDatabaseManager(dm);
        e.init();
        
        LastAugmentedDownloader lad = new LastAugmentedDownloader();
        lad.setEngine(e);
        lad.run();
    }
    
    private static Logger logger = Logger.getLogger("osm.watch.download");
}
