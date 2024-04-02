package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Indexer {
    private Map<String, Set<String>> index = new HashMap<>();
    private Map<String, Integer> totalWords = new HashMap<>();

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

    private void indexFile(String filePath) throws IOException {
        Pattern pattern = Pattern.compile("\\w+");
        int totalWordCount = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);
                while (matcher.find()) {
                    String word = matcher.group().toLowerCase();
                    index.computeIfAbsent(word, k -> new HashSet<>()).add(filePath);
                    totalWordCount++;
                }
            }
        }
        totalWords.put(filePath, totalWordCount);
    }

    public void indexFiles(Set<String> filePaths) {
        for (String filePath : filePaths) {
            try {
                indexFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public Map<String, Integer> search(String term) {
        Set<String> files = index.getOrDefault(term.toLowerCase(), Collections.emptySet());
        Map<String, Integer> results = new HashMap<>();
        for (String file : files) {
            int count = 0;
            try {
                count = countOccurrences(file, term);
            } catch (IOException e) {
                e.printStackTrace();
            }
            results.put(file, count);
        }
        return results;
    }

    private int countOccurrences(String filePath, String term) throws IOException {
        Pattern pattern = Pattern.compile("\\b" + term.toLowerCase() + "\\b");
        int count = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line.toLowerCase());
                while (matcher.find()) {
                    count++;
                }
            }
        }
        return count;
    }
}
