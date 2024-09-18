package com.ludocera.linkcollector.service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LinkManager {
    private final Set<String> visitedLinks = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public boolean shouldVisit(String link) {
        return visitedLinks.add(link);
    }

    public void printSortedLinks() {
        List<String> sortedLinks = getSortedVisitedLinks();
        System.out.println("\nCollected " + sortedLinks.size() + " links:");
        sortedLinks.forEach(System.out::println);
    }

    public List<String> getSortedVisitedLinks() {
        List<String> sortedLinks = new ArrayList<>(visitedLinks);
        sortedLinks.sort(Comparator.naturalOrder());
        return sortedLinks;
    }
}

