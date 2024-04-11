/**
 * The Search class facilitates searching functionality.
 * It interacts with the Indexer to perform searches and index directories.
 */
package core;

import java.util.List;
import java.util.Map;

public class Search {
    private Indexer indexer;

    /**
     * Constructs a Search object with the provided Indexer.
     * @param indexer The Indexer object used for searching.
     */
    public Search(Indexer indexer) {
        this.indexer = indexer;
    }

    /**
     * Performs a search for the specified term.
     * @return A list of map entries containing search results.
     */
    public List<Map.Entry<String, Integer>> performSearch(String term) {
        return indexer.search(term);
    }

    /**
     * Indexes the specified directory.
     */
    public void indexDirectory(String directoryPath) {
        indexer.indexDirectory(directoryPath);
    }

    /**
     * Performs a search for comma-separated words.
     * @return A list of map entries 
    /**g search results.
     */
    public List<Map.Entry<String, Integer>> performCommaSeparatedSearch(String terms) {
        return indexer.searchCommaSeparatedWords(terms);
    }

    /*
    * Performs a wildcard search using the specified pattern.
    * @return A list of map entries containing search results.
    */

    public List<Map.Entry<String, Integer>> performWildcardSearch(String term) {
        return indexer.searchWithWildcards(term);
    }
   
}
