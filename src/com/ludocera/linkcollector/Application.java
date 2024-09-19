package com.ludocera.linkcollector;

import com.ludocera.linkcollector.service.LinkManager;
import com.ludocera.linkcollector.service.WebCrawler;

public class Application {
    public static void main(String[] args) {
        WebCrawler webCrawler = new WebCrawler(new LinkManager());
        webCrawler.crawl();
    }
}
