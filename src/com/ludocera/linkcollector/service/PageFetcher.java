package com.ludocera.linkcollector.service;

import com.ludocera.linkcollector.exception.FetchPageException;
import com.ludocera.linkcollector.utils.LinkUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.List;

public class PageFetcher {
    private PageFetcher() {
        throw new IllegalStateException("Utility class, cannot be instantiated");
    }

    public static List<String> extractLinksFromPage(String urlString) {
        try {
            URL url = URI.create(urlString).toURL();
            String pageContent = fetchPageContent(url);
            return LinkUtils.extractLinks(pageContent, url.getHost());
        } catch (Exception e) {
            throw new FetchPageException(e.getMessage());
        }
    }

    private static String fetchPageContent(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        int responseCode = connection.getResponseCode();

        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new FetchPageException("HTTP error code: " + responseCode + " for URL: " + url);
        }

        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder content = new StringBuilder();
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            return content.toString();
        } finally {
            connection.disconnect();
        }
    }
}

