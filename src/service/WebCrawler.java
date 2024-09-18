package service;

import java.util.List;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static service.PageFetcher.extractLinksFromPage;
import static utils.LinkUtils.readInputStartUrl;

public class WebCrawler {
    private static final Logger logger = Logger.getLogger(WebCrawler.class.getName());

    private final LinkManager linkManager;
    private final ExecutorService executor;
    private final BlockingQueue<String> queue = new LinkedBlockingQueue<>();
    private final List<Future<?>> futures = new CopyOnWriteArrayList<>();

    public WebCrawler(ExecutorService executor, LinkManager linkManager) {
        this.executor = executor;
        this.linkManager = linkManager;
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

            cleanUpFutures();
        }
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

    private boolean hasActiveTasks() {
        return futures.stream().anyMatch(future -> !future.isDone());
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
            logger.log(Level.WARNING, "Error processing page: " + link, e);
        }
    }

    private void cleanUpFutures() {
        futures.removeIf(Future::isDone);
    }
}
