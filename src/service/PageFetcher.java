package service;

import utils.LinkUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PageFetcher {
    private static final Logger logger = Logger.getLogger(PageFetcher.class.getName());

    public static List<String> extractLinksFromPage(String urlString) {
        try {
            URL url = URI.create(urlString).toURL();
            String pageContent = fetchPageContent(url);
            return LinkUtils.extractLinks(pageContent, url.getHost());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error fetching page: " + urlString, e);
        }
        return Collections.emptyList();
    }

    private static String fetchPageContent(URL url) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);

        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("HTTP error code: " + responseCode + " for URL: " + url);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        }
    }
}

