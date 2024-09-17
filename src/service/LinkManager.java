package service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class LinkManager {
    private final Set<String> visitedLinks = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final List<String> collectedLinks = Collections.synchronizedList(new ArrayList<>());

    public boolean addVisitedLink(String link) {
        boolean isNewLink = visitedLinks.add(link);
        if (isNewLink) {
            collectedLinks.add(link);
        }
        return isNewLink;
    }

    public boolean shouldVisit(String link) {
        return !visitedLinks.contains(link);
    }

    public void printSortedLinks() {
        collectedLinks.sort(Comparator.naturalOrder());
        System.out.println("\nCollected " + collectedLinks.size() + " links:");
        collectedLinks.forEach(System.out::println);
    }
}

