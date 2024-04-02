package core;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class Indexer {
    private Map<String, Map<String, Integer>> index = new HashMap<>();

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

    public Map<String, Integer> search(String term) {
        return index.getOrDefault(term.toLowerCase(), Collections.emptyMap());
    }
}