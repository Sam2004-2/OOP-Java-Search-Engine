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

        // Update the total words count for the file
        totalWords.put(filePath, totalWordCount);
    }

    public Set<String> search(String term) {
        return index.getOrDefault(term.toLowerCase(), Collections.emptySet());
    }
}
