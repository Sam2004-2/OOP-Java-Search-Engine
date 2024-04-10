package util;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import core.Indexer;

public class testing {
    public static void main(String[] args) {
        try {
            // Initialize the indexer
            Indexer indexer = new Indexer();

            // Index the test.txt file
            indexer.indexFile("test.txt");

            // Perform a wildcard search
            //List<Map.Entry<String, Integer>> results = indexer.searchWithWildcards("w*w");
            

            List<Entry<String, Integer>> searchHistory = indexer.HistorySearch();
            for (Entry<String, Integer> line : searchHistory) {
                System.out.println(line);
            }

            // Print the results
            //results.forEach(entry -> System.out.println(entry.getKey() + ": " + entry.getValue()));
        } catch (IOException e) {
            System.err.println("An error occurred while indexing the file: " + e.getMessage());
        }
    }
}

