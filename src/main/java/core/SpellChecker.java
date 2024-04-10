package core;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpellChecker {
    private List<String> dictionary;

    public SpellChecker(String dictionaryPath) {
        dictionary = loadDictionary(dictionaryPath);
    }

    private List<String> loadDictionary(String dictionaryPath) {
        List<String> dictionary = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("resources/words.txt"))) {
            String word;
            while ((word = reader.readLine()) != null) {
                dictionary.add(word.toLowerCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dictionary;
    }

    public List<String> suggestCorrections(String term) {
        List<String> suggestions = new ArrayList<>();
        int minDistance = Integer.MAX_VALUE;

        for (String word : dictionary) {
            int distance = calculateLevenshteinDistance(term, word);
            if (distance < minDistance) {
                suggestions.clear();
                suggestions.add(word);
                minDistance = distance;
            } else if (distance == minDistance) {
                suggestions.add(word);
            }
        }

        return suggestions;
    }

    private int calculateLevenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = min(dp[i - 1][j - 1]
                            + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                            dp[i - 1][j] + 1,
                            dp[i][j - 1] + 1);
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private int min(int x, int y, int z) {
        return Math.min(Math.min(x, y), z);
    }
}
