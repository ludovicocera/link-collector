import service.LinkManager;
import service.WebCrawler;

import java.util.concurrent.Executors;

import static utils.ConfigUtils.getThreadCount;

public class Application {
    public static void main(String[] args) {
        WebCrawler webCrawler = new WebCrawler(Executors.newFixedThreadPool(getThreadCount()), new LinkManager());
        webCrawler.crawl();
    }
}
