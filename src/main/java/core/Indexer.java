package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;
import core.SpellChecker;

/**
 * The Indexer class provides functionality to index words in text files within a directory,
 * allowing for a search operation that identifies files containing specific terms.
 */
public class Indexer {
    private Map<String, Map<String, Integer>> index = new HashMap<>();
    private SpellChecker spellChecker;
    /**
     * Indexes all regular files within the specified directory path.
     * This method recursively walks through the directory and indexes each file found.
     * @param directoryPath The path of the directory to index.
     */
    public void indexDirectory(String directoryPath) {
        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try {
                            indexFile(filePath.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Indexes a single file, extracting and counting each word within the file.
     * Each word is indexed along with its occurrence count in the provided file path.
     *
     * @param filePath The path of the file to index.
     * @throws IOException If an I/O error occurs reading from the file.
     */
    public void indexFile(String filePath) throws IOException {
        Pattern pattern = Pattern.compile("\\w+");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String word = matcher.group().toLowerCase();
                    Map<String, Integer> fileCounts = index.getOrDefault(word, new HashMap<>());
                    fileCounts.put(filePath, fileCounts.getOrDefault(filePath, 0) + 1);
                    index.put(word, fileCounts);
                }
            }
        }
    }

    /**
     * Searches the indexed data for files containing the specified term.
     * Returns a list of file paths and their associated occurrence count of the term, sorted by count in descending order.
     *
     * @param term The search term to find within the indexed files.
     * @return A list of Map entries, where each entry represents a file path and the count of the term's occurrences in that file, sorted by the count in descending order.
     */
    public List<Map.Entry<String, Integer>> search(String term) {
        term = term.toLowerCase(); 
        Map<String, Integer> cumulativeResults = new HashMap<>();
        for (String indexedWord : index.keySet()) {
            if (indexedWord.toLowerCase().equals(term)) {
                Map<String, Integer> fileCounts = index.get(indexedWord);

                fileCounts.forEach((filePath, count) -> cumulativeResults.merge(filePath, count, Integer::sum));
            }
        }
        return cumulativeResults.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
    }


    /**
     * Searches the indexed data for files containing any of the comma-separated words.
     * @param terms Comma-separated search terms.
     * @return Sorted list of search results.
     */
    public List<Map.Entry<String, Integer>> searchCommaSeparatedWords(String terms) {
        Set<String> words = Arrays.stream(terms.split(","))
                                  .map(String::trim)
                                  .map(String::toLowerCase)
                                  .collect(Collectors.toSet());
        Map<String, Integer> cumulativeResults = new HashMap<>();

        words.forEach(word -> {
            List<Map.Entry<String, Integer>> results = search(word);
            results.forEach(entry -> cumulativeResults.merge(entry.getKey(), entry.getValue(), Integer::sum));
        });

        return cumulativeResults.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
    }

    /**
     * Searches the indexed data for files matching the wildcard pattern.
     * @param wildcardPattern The wildcard search pattern.
     * @return Sorted list of search results.
     */
    public List<Map.Entry<String, Integer>> searchWithWildcards(String wildcardPattern) {
    	
        String regex = wildcardPattern.replace("*", ".*").toLowerCase();
        Pattern pattern = Pattern.compile(regex);
        Map<String, Integer> cumulativeResults = new HashMap<>();
        index.keySet().forEach(word -> {
            // Convert the word from the index to lower case before matching
            if (pattern.matcher(word.toLowerCase()).matches()) {
                List<Map.Entry<String, Integer>> results = search(word);
                results.forEach(entry -> cumulativeResults.merge(entry.getKey(), entry.getValue(), Integer::sum));

            }
        });
        
        return cumulativeResults.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toList());
    }
    


    }

    

