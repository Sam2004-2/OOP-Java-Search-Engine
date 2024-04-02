package core;

import java.util.List;
import java.util.Map;

public class Search {
    private Indexer indexer;

    public Search(Indexer indexer) {
        this.indexer = indexer;
    }

    public List<Map.Entry<String, Integer>> performSearch(String term) {
        return indexer.search(term);
    }

    public void indexDirectory(String directoryPath) {
        indexer.indexDirectory(directoryPath);
    }

    // New method for comma-separated words search
    public List<Map.Entry<String, Integer>> performCommaSeparatedSearch(String terms) {
        return indexer.searchCommaSeparatedWords(terms);
    }

    // New method for wildcard search
    public List<Map.Entry<String, Integer>> performWildcardSearch(String pattern) {
        return indexer.searchWithWildcards(pattern);
    }
}
