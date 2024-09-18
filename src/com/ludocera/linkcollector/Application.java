package com.ludocera.linkcollector;

import com.ludocera.linkcollector.service.LinkManager;
import com.ludocera.linkcollector.service.WebCrawler;

import java.util.concurrent.Executors;

import static com.ludocera.linkcollector.utils.ConfigUtils.getThreadCount;

public class Application {
    public static void main(String[] args) {
        WebCrawler webCrawler = new WebCrawler(Executors.newFixedThreadPool(getThreadCount()), new LinkManager());
        webCrawler.crawl();
    }
}
