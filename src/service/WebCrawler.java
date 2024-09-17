package service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static service.PageFetcher.extractLinksFromPage;
import static utils.LinkUtils.readInputStartUrl;

public class WebCrawler {
    private static final Logger logger = Logger.getLogger(WebCrawler.class.getName());

    private final LinkManager linkManager = new LinkManager();
    private final ExecutorService executor;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final List<Future<?>> futures = Collections.synchronizedList(new ArrayList<>());

    public WebCrawler(int threadCount) {
        executor = Executors.newFixedThreadPool(threadCount);
    }

    public void crawl() {
        startCrawl(readInputStartUrl());

        shutdownAndPrintResults();
    }

    private void startCrawl(String startUrl) {
        queue.add(startUrl);

        while (!queue.isEmpty() || hasActiveTasks()) {
            String link = queue.poll();

            if (link != null && linkManager.addVisitedLink(link)) {
                Future<?> future = executor.submit(() -> processPage(link));
                futures.add(future);
            }
        }
    }

    private void processPage(String link) {
        System.out.println("Crawling: " + link);

        try {
            List<String> linksOnPage = extractLinksFromPage(link);
            for (String newLink : linksOnPage) {
                if (linkManager.shouldVisit(newLink)) {
                    queue.add(newLink);
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error processing page: " + link, e);
        }
    }

    private boolean hasActiveTasks() {
        futures.removeIf(Future::isDone);

        return futures.stream().anyMatch(future -> !future.isDone());
    }

    private void shutdownAndPrintResults() {
        try {
            executor.shutdown();
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.log(Level.SEVERE, "Error waiting for task completion: " + e.getMessage(), e);
                }
            }

            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                List<Runnable> interruptedTasks = executor.shutdownNow();
                logger.log(Level.SEVERE, "Stopped " + interruptedTasks.size() + " tasks due to awaitTermination() timeout");
            }
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, "Error while shutting down executor: " + e.getMessage());
        }

        linkManager.printSortedLinks();
    }
}
