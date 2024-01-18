package models;

import http.RESTAPIHandler;

import java.util.ArrayList;
import java.util.List;

public class Pagination<E> {

    private static int elementsPerPage = 5;
    private final List<E> startingList;
    private int currentPageIndex = 0;
    private final List<List<E>> pagesList = new ArrayList<>();

    public Pagination(final List<E> startingList) {
        this.startingList = startingList;
        createPagesList();
    }

    public Pagination(final List<E> startingList, final String entries) {
        this.startingList = startingList;
        try {
            int nElements = Integer.parseInt(entries);
            if (Integer.signum(nElements) == 1) {
                elementsPerPage = nElements;
            }
        } catch (NumberFormatException e) {
            System.out.println("Wrong argument -name");
        }
        createPagesList();
    }

    public void printCurrentPage() {
        try {
            RESTAPIHandler.printResultList(pagesList.get(currentPageIndex));
            System.out.printf("---PAGE %d OF %d---\n", currentPageIndex + 1, pagesList.size());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No more pages.");
        }
    }

    public void printPrevPage() {
        try {
            RESTAPIHandler.printResultList(pagesList.get(currentPageIndex - 1));
            System.out.printf("---PAGE %d OF %d---\n", --currentPageIndex + 1, pagesList.size());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No more pages.");
        }
    }

    public void printNextPage() {
        try {
            RESTAPIHandler.printResultList(pagesList.get(currentPageIndex + 1));
            System.out.printf("---PAGE %d OF %d---\n", ++currentPageIndex + 1, pagesList.size());
        } catch (IndexOutOfBoundsException e) {
            System.out.println("No more pages.");
        }
    }

    private void createPagesList() {
        int startListIndex = 0;
        while (startListIndex < startingList.size()) {
            int endListIndex = Math.min(startListIndex + elementsPerPage, startingList.size());
            pagesList.add(new ArrayList<>(startingList.subList(startListIndex, endListIndex)));
            startListIndex = endListIndex;
        }
    }
}