package app;

import javax.swing.SwingUtilities;
import core.Indexer;
import core.Search;  // Make sure this import is correct
import core.SpellChecker;
import ui.SearchUI;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            String dictionaryPath = "resources/words.txt";
            Indexer indexer = new Indexer();
            Search search = new Search(indexer);
            SpellChecker spellChecker = new SpellChecker(dictionaryPath); // Create instance of SpellChecker
            SearchUI searchUI = new SearchUI(search, spellChecker);
            searchUI.setVisible(true);
        });
    }
}
 