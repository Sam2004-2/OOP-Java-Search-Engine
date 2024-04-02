package core;

import java.util.List;
import java.util.Map;

/**
 * Manages search operations using the Indexer, abstracting the search functionality from the UI.
 */
public class Search {
    private Indexer indexer;

    /**
     * Constructs a Search instance with the specified Indexer.
     *
     * @param indexer The indexer used for searching.
     */
    public Search(Indexer indexer) {
        this.indexer = indexer;
    }

    /**
     * Performs a search for the given term and returns the results sorted by relevance.
     *
     * @param term The search term.
     * @return A sorted list of search results, where each entry contains the file path and the occurrence count of the term.
     */
    public List<Map.Entry<String, Integer>> performSearch(String term) {
        return indexer.search(term);
    }

    /**
     * Indexes the directory at the given path.
     *
     * @param directoryPath The path of the directory to index.
     */
    public void indexDirectory(String directoryPath) {
        indexer.indexDirectory(directoryPath);
    }
}
