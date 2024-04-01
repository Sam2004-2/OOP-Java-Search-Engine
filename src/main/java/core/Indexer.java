package core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Indexer {
    private Map<String, Set<String>> index;

    public Indexer() {
        this.index = new HashMap<>();
    }

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
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        for (String line : lines) {
            String[] words = line.split("\\W+");
            for (String word : words) {
                word = word.toLowerCase(); // Normalize case for indexing
                index.computeIfAbsent(word, k -> new HashSet<>()).add(filePath);
            }
        }
    }

    public Set<String> search(String term) {
        return index.getOrDefault(term.toLowerCase(), Collections.emptySet());
    }
}
