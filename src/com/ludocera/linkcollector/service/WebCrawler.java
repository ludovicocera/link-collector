package com.ludocera.linkcollector.service;

import com.ludocera.linkcollector.utils.ConfigUtils;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.ludocera.linkcollector.service.PageFetcher.extractLinksFromPage;
import static com.ludocera.linkcollector.utils.LinkUtils.readInputStartUrl;

public class WebCrawler {
    private static final Logger logger = Logger.getLogger(WebCrawler.class.getName());

    private final LinkManager linkManager;
    private final ExecutorService executor;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final List<Future<?>> futures = new CopyOnWriteArrayList<>();

    public WebCrawler(LinkManager linkManager) {
        this.linkManager = linkManager;
        
        if (ConfigUtils.useVirtualThreads()) {
            logger.info("Using virtual threads for task execution.");
            executor = Executors.newVirtualThreadPerTaskExecutor();
        } else {
            int threadCount = ConfigUtils.getThreadCount();
            logger.info("Using fixed thread pool with " + threadCount + " threads.");
            executor = Executors.newFixedThreadPool(threadCount);
        }
    }

    public void crawl() {
        startCrawl(readInputStartUrl());
        shutdownAndPrintResults();
    }

    private void startCrawl(String startUrl) {
        queue.add(startUrl);

        while (!queue.isEmpty() || hasActiveTasks()) {
            String link = queue.poll();

            if (link != null) {
                Future<?> future = executor.submit(() -> processPage(link));
                futures.add(future);
            }

            cleanUpFutures();
        }
    }

    private boolean hasActiveTasks() {
        return futures.stream().anyMatch(future -> !future.isDone());
    }

    private void processPage(String link) {
        System.out.println("Crawling: " + link);

        int retryCount = 0;
        while (retryCount < 3) {
            try {
                List<String> linksOnPage = extractLinksFromPage(link);
                for (String newLink : linksOnPage) {
                    if (linkManager.shouldVisit(newLink)) {
                        queue.add(newLink);
                    }
                }
                return;
            } catch (Exception e) {
                retryCount++;
                logger.log(Level.WARNING, "Error processing page: " + link, e);
            }
        }

        logger.log(Level.SEVERE, "Failed to process page after " + retryCount + " retries: " + link);
    }

    private void cleanUpFutures() {
        futures.removeIf(Future::isDone);
    }

    private void shutdownAndPrintResults() {
        try {
            executor.shutdown();

            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                List<Runnable> interruptedTasks = executor.shutdownNow();
                logger.log(Level.SEVERE, "Stopped {} tasks due to awaitTermination() timeout", interruptedTasks.size());
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
            logger.log(Level.SEVERE, "Error while shutting down executor: {}", e.getMessage());
        }

        linkManager.printSortedLinks();
    }
}
