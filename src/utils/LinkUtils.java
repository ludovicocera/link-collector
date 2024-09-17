package utils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LinkUtils {

    private static final Logger logger = Logger.getLogger(LinkUtils.class.getName());

    public static List<String> extractLinks(String htmlContent, String domain) {
        List<String> foundLinks = new ArrayList<>();
        Pattern pattern = Pattern.compile("href=\"(https?://[^\"]+)\"");
        Matcher matcher = pattern.matcher(htmlContent);

        while (matcher.find()) {
            String foundUrl = matcher.group(1);
            try {
                URI uri = new URI(foundUrl);
                if (uri.getHost() != null && uri.getHost().contains(domain)) {
                    foundLinks.add(foundUrl);
                }
            } catch (URISyntaxException e) {
                logger.log(Level.WARNING, "Skipping invalid URL: " + foundUrl, e);
            }
        }
        return foundLinks;
    }

    public static String readInputStartUrl() {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter the starting URL: ");
        String startUrl = scanner.nextLine();

        if (!isValidUrl(startUrl)) {
            logger.log(Level.SEVERE, "Input " + startUrl + " not recognized as a valid URL");
            throw new IllegalArgumentException("Invalid URL provided");
        }

        return startUrl;
    }

    private static boolean isValidUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (URISyntaxException e) {
            return false;
        }
    }
}

