import service.WebCrawler;

import static utils.ConfigUtils.getThreadCount;

public class Application {
    public static void main(String[] args) {
        WebCrawler crawler = new WebCrawler(getThreadCount());
        crawler.crawl();
    }
}
